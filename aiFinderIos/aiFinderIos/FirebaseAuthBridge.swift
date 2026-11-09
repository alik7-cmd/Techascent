import AiFinder
import FirebaseAuth
import FirebaseCore
import FirebaseDatabase
import FirebaseFunctions
import FirebaseStorage
import GoogleSignIn
import UIKit
import CoreImage.CIFilterBuiltins

extension FirebaseAuthBridge {
    private func reference(_ path: String) throws -> DatabaseReference {
        try authorize(path)
        guard let url = FirebaseApp.app()?.options.databaseURL, !url.isEmpty else {
            throw bridgeError("Missing Firebase database URL.")
        }
        return Database.database(url: url).reference().child(path)
    }

    private func authorize(_ path: String) throws {
        guard let uid = Auth.auth().currentUser?.uid,
              path.hasPrefix("aifinder/users/\(uid)/"),
              !path.contains("..") else {
            throw bridgeError("Sign in to access your inventory.")
        }
    }

    private func bridgeError(_ message: String) -> NSError {
        NSError(domain: "AiFinder.Firebase", code: 1, userInfo: [NSLocalizedDescriptionKey: message])
    }

    private func jsonText(_ value: Any) throws -> String {
        let data = try JSONSerialization.data(withJSONObject: value, options: [.fragmentsAllowed])
        guard let text = String(data: data, encoding: .utf8) else { throw bridgeError("Invalid database response.") }
        return text
    }

    func read(path: String, completion: @escaping (String?, String?) -> Void) {
        do {
            try reference(path).getData { error, snapshot in
                if let error { completion(nil, error.localizedDescription); return }
                do { completion(try self.jsonText(snapshot?.value ?? NSNull()), nil) }
                catch { completion(nil, error.localizedDescription) }
            }
        } catch { completion(nil, error.localizedDescription) }
    }

    func write(path: String, json: String, completion: @escaping (String?) -> Void) {
        do {
            let value = try JSONSerialization.jsonObject(with: Data(json.utf8), options: [.fragmentsAllowed])
            if path.hasPrefix("aifinder/appReviews/") {
                let parts = path.split(separator: "/", omittingEmptySubsequences: false)
                guard let uid = Auth.auth().currentUser?.uid,
                      parts.count == 4, String(parts[2]) == uid,
                      UUID(uuidString: String(parts[3])) != nil,
                      let record = value as? [String: Any],
                      record["userId"] as? String == uid,
                      let review = record["review"] as? String,
                      !review.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty,
                      review.utf16.count <= 2000,
                      let url = FirebaseApp.app()?.options.databaseURL, !url.isEmpty else {
                    throw bridgeError("Unable to submit feedback. Check your account and message.")
                }
                Database.database(url: url).reference().child(path).setValue(value) { error, _ in
                    completion(error?.localizedDescription)
                }
                return
            }
            try reference(path).setValue(value) { error, _ in completion(error?.localizedDescription) }
        } catch { completion(error.localizedDescription) }
    }

    func update(path: String, json: String, completion: @escaping (String?) -> Void) {
        do {
            guard let value = try JSONSerialization.jsonObject(with: Data(json.utf8)) as? [String: Any] else {
                throw bridgeError("Invalid inventory update.")
            }
            try reference(path).updateChildValues(value) { error, _ in completion(error?.localizedDescription) }
        } catch { completion(error.localizedDescription) }
    }

    func upload(path: String, base64: String, completion: @escaping (String?) -> Void) {
        do {
            try authorize(path)
            guard let data = Data(base64Encoded: base64) else { throw bridgeError("Invalid photo data.") }
            let metadata = StorageMetadata()
            metadata.contentType = "image/jpeg"
            Storage.storage().reference().child(path).putData(data, metadata: metadata) { _, error in
                completion(error?.localizedDescription)
            }
        } catch { completion(error.localizedDescription) }
    }

    func download(path: String, completion: @escaping (String?, String?) -> Void) {
        do {
            try authorize(path)
            Storage.storage().reference().child(path).getData(maxSize: 20 * 1024 * 1024) { data, error in
                if let error {
                    if (error as NSError).code == StorageErrorCode.objectNotFound.rawValue { completion("", nil) }
                    else { completion(nil, error.localizedDescription) }
                } else if let data { completion(data.base64EncodedString(), nil) }
                else { completion(nil, "Missing photo response.") }
            }
        } catch { completion(nil, error.localizedDescription) }
    }

    func deletePhoto(path: String, completion: @escaping (String?) -> Void) {
        do {
            try authorize(path)
            Storage.storage().reference().child(path).delete { error in
                if let error, (error as NSError).code != StorageErrorCode.objectNotFound.rawValue {
                    completion(error.localizedDescription)
                } else { completion(nil) }
            }
        } catch { completion(error.localizedDescription) }
    }

    func wallet(userId: String, operation: String, json: String, completion: @escaping (String?, String?) -> Void) {
        do {
            let root = try reference("aifinder/users/\(userId)/scanWallet")
            guard let payload = try JSONSerialization.jsonObject(with: Data(json.utf8)) as? [String: Any],
                  ["load", "consume", "purchase"].contains(operation) else {
                throw bridgeError("Invalid wallet operation.")
            }
            if operation == "load" {
                Functions.functions(region: "us-central1").httpsCallable("ensureScanWallet").call { result, error in
                    if let error { completion(nil, error.localizedDescription); return }
                    guard Auth.auth().currentUser?.uid == userId else {
                        completion(nil, "The signed-in user changed. Please try again.")
                        return
                    }
                    do {
                        guard let value = result?.data as? [String: Any] else {
                            throw self.bridgeError("Invalid scan balance returned by the server.")
                        }
                        completion(try self.jsonText(value), nil)
                    } catch { completion(nil, error.localizedDescription) }
                }
                return
            }
            let scanId = payload["scanId"] as? String ?? ""
            if operation == "consume" && (scanId.isEmpty || scanId.contains(where: { ".#$[]/".contains($0) })) {
                throw bridgeError("Invalid scan ID.")
            }
            let purchaseId = payload["purchaseId"] as? String ?? ""
            let key = String(purchaseId.map { ".#$[]/".contains($0) ? "_" : $0 })
            let scans = (payload["scansGranted"] as? NSNumber)?.int64Value ?? 0
            if operation == "purchase" && (key.isEmpty || scans <= 0) { throw bridgeError("Invalid purchase.") }

            let transact = {
                root.getData { error, _ in
                    if let error { completion(nil, error.localizedDescription); return }
                    root.runTransactionBlock({ data in
                        let raw = data.childData(byAppendingPath: "remainingScans").value
                        let count: Int64
                        if raw == nil || raw is NSNull { count = 0 }
                        else if let number = raw as? NSNumber, number.int64Value >= 0,
                                number.doubleValue == Double(number.int64Value) { count = number.int64Value }
                        else { return TransactionResult.abort() }
                        var remaining = count
                        if operation == "consume" {
                            let used = data.childData(byAppendingPath: "usedScans/\(scanId)")
                            if used.value as? Bool != true {
                                guard remaining > 0 else { return TransactionResult.abort() }
                                remaining -= 1
                                used.value = true
                            }
                        } else if operation == "purchase" {
                            let credited = data.childData(byAppendingPath: "creditedPurchases/\(key)")
                            if credited.value as? Bool != true {
                                let sum = remaining.addingReportingOverflow(scans)
                                guard !sum.overflow else { return TransactionResult.abort() }
                                remaining = sum.partialValue
                                credited.value = true
                            }
                        }
                        data.childData(byAppendingPath: "remainingScans").value = remaining
                        data.childData(byAppendingPath: "activeSubscription").value = remaining > 0
                        return TransactionResult.success(withValue: data)
                    }, andCompletionBlock: { error, committed, snapshot in
                        if let error { completion(nil, error.localizedDescription); return }
                        guard committed, let value = snapshot?.value else {
                            completion(nil, "Could not update scan credits. Check your balance and try again.")
                            return
                        }
                        do {
                            let response = try self.jsonText(value)
                            if operation == "purchase" {
                                let history = try self.reference("aifinder/users/\(userId)/purchases/\(key)")
                                history.updateChildValues(["status": "credited", "creditedAt": ServerValue.timestamp()]) { error, _ in
                                    completion(error == nil ? response : nil, error?.localizedDescription)
                                }
                            } else { completion(response, nil) }
                        } catch { completion(nil, error.localizedDescription) }
                    }, withLocalEvents: false)
                }
            }
            if operation == "purchase" {
                let history = try reference("aifinder/users/\(userId)/purchases/\(key)")
                history.getData { error, snapshot in
                    if let error { completion(nil, error.localizedDescription); return }
                    if snapshot?.childSnapshot(forPath: "status").value as? String == "credited" { transact(); return }
                    var record = payload
                    record["status"] = "pending"
                    record["recordedAt"] = ServerValue.timestamp()
                    history.setValue(record) { error, _ in
                        if let error { completion(nil, error.localizedDescription) } else { transact() }
                    }
                }
            } else { transact() }
        } catch { completion(nil, error.localizedDescription) }
    }
}

final class FirebaseAuthBridge: NSObject, IosAuthBridge, IosPurchaseHistoryBridge, IosFirebaseBridge, IosDeviceFilesBridge {
    func inventoryPdf(lines: [String], completion: @escaping (String?, String?) -> Void) {
        let font = UIFont.systemFont(ofSize: 12)
        let wrapped = lines.flatMap { line -> [String] in
            var result: [String] = []
            var current = ""
            for char in line.replacingOccurrences(of: "\n", with: " ") {
                let next = current + String(char)
                if !current.isEmpty && (next as NSString).size(withAttributes: [.font: font]).width > 523 {
                    result.append(current)
                    current = String(char)
                } else { current = next }
            }
            result.append(current)
            return result
        }
        let renderer = UIGraphicsPDFRenderer(bounds: CGRect(x: 0, y: 0, width: 595, height: 842))
        let data = renderer.pdfData { context in
            for (index, line) in wrapped.enumerated() {
                if index % 44 == 0 { context.beginPage() }
                (line as NSString).draw(
                    at: CGPoint(x: 36, y: 48 + (index % 44) * 17),
                    withAttributes: [.font: font, .foregroundColor: UIColor.darkGray]
                )
            }
        }
        completion(data.base64EncodedString(), nil)
    }
    func generateQr(value: String, label: String, completion: @escaping (String?, String?) -> Void) {
        DispatchQueue.global(qos: .userInitiated).async {
            let filter = CIFilter.qrCodeGenerator()
            filter.message = Data(value.utf8)
            filter.correctionLevel = "M"
            guard let output = filter.outputImage else { completion(nil, "Couldn't generate QR label."); return }
            let scaled = output.transformed(by: CGAffineTransform(scaleX: 12, y: 12))
            guard let cgImage = CIContext().createCGImage(scaled, from: scaled.extent) else {
                completion(nil, "Couldn't render QR label."); return
            }
            let qr = UIImage(cgImage: cgImage)
            let size = CGSize(width: qr.size.width + 96, height: qr.size.height + 96 + (label.isEmpty ? 0 : 112))
            let renderer = UIGraphicsImageRenderer(size: size)
            let image = renderer.image { context in
                UIColor.white.setFill()
                context.fill(CGRect(origin: .zero, size: size))
                qr.draw(at: CGPoint(x: 48, y: 48))
                if !label.isEmpty {
                    let style = NSMutableParagraphStyle()
                    style.alignment = .center
                    style.lineBreakMode = .byTruncatingTail
                    (label as NSString).draw(
                        in: CGRect(x: 24, y: qr.size.height + 72, width: size.width - 48, height: 96),
                        withAttributes: [.font: UIFont.systemFont(ofSize: 20), .foregroundColor: UIColor.darkGray,
                                         .paragraphStyle: style]
                    )
                }
            }
            guard let data = image.pngData() else { completion(nil, "Couldn't encode QR label."); return }
            completion(data.base64EncodedString(), nil)
        }
    }

    func decodeQr(base64: String, completion: @escaping (String?, String?) -> Void) {
        DispatchQueue.global(qos: .userInitiated).async {
            guard let data = Data(base64Encoded: base64), let image = CIImage(data: data),
                  let detector = CIDetector(ofType: CIDetectorTypeQRCode, context: CIContext(),
                                            options: [CIDetectorAccuracy: CIDetectorAccuracyHigh]) else {
                completion(nil, "Couldn't read the QR photo."); return
            }
            let code = detector.features(in: image).compactMap { ($0 as? CIQRCodeFeature)?.messageString }.first
            completion(code ?? "", nil)
        }
    }

    func share(name: String, base64: String, mimeType: String, completion: @escaping (String?) -> Void) {
        guard name.range(of: "^[A-Za-z0-9._-]+$", options: .regularExpression) != nil,
              let data = Data(base64Encoded: base64), let presenter = Self.presentingViewController() else {
            completion("Couldn't prepare the export."); return
        }
        do {
            let url = FileManager.default.temporaryDirectory.appendingPathComponent(name)
            try data.write(to: url, options: .atomic)
            let controller = UIActivityViewController(activityItems: [url], applicationActivities: nil)
            controller.popoverPresentationController?.sourceView = presenter.view
            controller.popoverPresentationController?.sourceRect = CGRect(
                x: presenter.view.bounds.midX, y: presenter.view.bounds.midY, width: 1, height: 1
            )
            controller.completionWithItemsHandler = { _, _, _, error in
                do { try FileManager.default.removeItem(at: url) }
                catch { NSLog("Couldn't remove temporary export: %@", error.localizedDescription) }
                if let error { NSLog("Sharing failed: %@", error.localizedDescription) }
            }
            presenter.present(controller, animated: true) { completion(nil) }
        } catch { completion(error.localizedDescription) }
    }
    func loadPurchases(userId: String, completion: @escaping ([ScanPurchase]?, String?) -> Void) {
        guard Auth.auth().currentUser?.uid == userId else {
            completion(nil, "Sign in to view your purchases.")
            return
        }
        guard let databaseURL = FirebaseApp.app()?.options.databaseURL, !databaseURL.isEmpty else {
            completion(nil, "Missing Firebase database URL.")
            return
        }
        Database.database(url: databaseURL).reference()
            .child("aifinder").child("users").child(userId).child("purchases")
            .getData { error, snapshot in
                if let error {
                    completion(nil, error.localizedDescription)
                    return
                }
                guard let snapshot else {
                    completion(nil, "Missing purchase history response.")
                    return
                }
                var purchases: [ScanPurchase] = []
                for case let child as DataSnapshot in snapshot.children {
                    guard let row = child.value as? [String: Any],
                          let purchaseId = row["purchaseId"] as? String,
                          let productId = row["productId"] as? String,
                          let scans = row["scansGranted"] as? NSNumber,
                          let date = row["purchasedAt"] as? NSNumber,
                          let store = row["store"] as? String,
                          let status = row["status"] as? String,
                          scans.int64Value >= 0, date.int64Value >= 0,
                          scans.doubleValue == Double(scans.int64Value),
                          date.doubleValue == Double(date.int64Value) else {
                        completion(nil, "Purchase history contains an invalid record.")
                        return
                    }
                    purchases.append(ScanPurchase(
                        purchaseId: purchaseId,
                        productId: productId,
                        scansGranted: scans.int64Value,
                        purchasedAt: date.int64Value,
                        store: store,
                        status: status
                    ))
                }
                completion(purchases, nil)
            }
    }

    var currentUser: AuthUser? {
        Auth.auth().currentUser.map(Self.authUser)
    }

    func signIn(completion: @escaping (AuthUser?, IosAuthFailure?) -> Void) {
        guard let clientID = FirebaseApp.app()?.options.clientID else {
            completion(nil, Self.failure(.unavailable, "Missing Firebase iOS client ID."))
            return
        }
        let callbackScheme = clientID.components(separatedBy: ".").reversed().joined(separator: ".")
        let urlTypes = Bundle.main.object(forInfoDictionaryKey: "CFBundleURLTypes") as? [[String: Any]]
        let schemes = urlTypes?.flatMap { $0["CFBundleURLSchemes"] as? [String] ?? [] } ?? []
        guard schemes.contains(callbackScheme) else {
            completion(nil, Self.failure(.unavailable, "Add REVERSED_CLIENT_ID to the app's URL schemes."))
            return
        }
        guard let presenter = Self.presentingViewController() else {
            completion(nil, Self.failure(.unavailable, "No active view controller for Google sign-in."))
            return
        }
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.signIn(withPresenting: presenter) { result, error in
            if let error {
                completion(nil, Self.authFailure(error))
                return
            }
            guard let user = result?.user, let idToken = user.idToken?.tokenString else {
                completion(nil, Self.failure(.unknown, "Google Sign-In returned no ID token."))
                return
            }
            let credential = GoogleAuthProvider.credential(
                withIDToken: idToken,
                accessToken: user.accessToken.tokenString
            )
            Auth.auth().signIn(with: credential) { result, error in
                if let error {
                    completion(nil, Self.authFailure(error))
                } else if let user = result?.user {
                    completion(Self.authUser(user), nil)
                } else {
                    completion(nil, Self.failure(.unknown, "Firebase returned no user."))
                }
            }
        }
    }

    func signOut(completion: @escaping (IosAuthFailure?) -> Void) {
        do {
            try Auth.auth().signOut()
            GIDSignIn.sharedInstance.signOut()
            completion(nil)
        } catch {
            completion(Self.authFailure(error))
        }
    }

    func saveSignInMetadata(
        userId: String,
        data: [String: String],
        completion: @escaping (IosAuthFailure?) -> Void
    ) {
        guard Auth.auth().currentUser?.uid == userId else {
            completion(Self.failure(.unknown, "The signed-in user does not match the metadata owner."))
            return
        }
        guard let databaseURL = FirebaseApp.app()?.options.databaseURL, !databaseURL.isEmpty else {
            completion(Self.failure(.unavailable, "Missing DATABASE_URL in GoogleService-Info.plist."))
            return
        }
        Database.database(url: databaseURL).reference()
            .child("aifinder").child("users").child(userId).child("signedIn")
            .updateChildValues(data) { error, _ in
                completion(error.map(Self.authFailure))
            }
    }

    private static func authUser(_ user: FirebaseAuth.User) -> AuthUser {
        AuthUser(
            id: user.uid,
            displayName: user.displayName,
            email: user.email,
            photoUrl: user.photoURL?.absoluteString,
            provider: user.providerData.contains { $0.providerID == "google.com" } ? .google : nil
        )
    }

    private static func presentingViewController() -> UIViewController? {
        let scene = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive }
        var controller = scene?.windows.first { $0.isKeyWindow }?.rootViewController
        while let current = controller {
            if let presented = current.presentedViewController {
                controller = presented
            } else if let navigation = current as? UINavigationController {
                controller = navigation.visibleViewController
            } else if let tabs = current as? UITabBarController {
                controller = tabs.selectedViewController
            } else {
                return current
            }
        }
        return nil
    }

    private static func failure(_ kind: IosAuthFailureKind, _ message: String) -> IosAuthFailure {
        IosAuthFailure(kind: kind, message: message)
    }

    private static func authFailure(_ error: Error) -> IosAuthFailure {
        let error = error as NSError
        if error.domain == kGIDSignInErrorDomain && error.code == GIDSignInError.canceled.rawValue {
            return failure(.cancelled, error.localizedDescription)
        }
        if error.domain == NSURLErrorDomain ||
            (error.domain == AuthErrorDomain && error.code == AuthErrorCode.networkError.rawValue) {
            return failure(.network, error.localizedDescription)
        }
        return failure(.unknown, error.localizedDescription)
    }
}
