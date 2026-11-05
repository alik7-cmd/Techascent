import AiFinder
import FirebaseAuth
import FirebaseCore
import FirebaseDatabase
import GoogleSignIn
import UIKit

final class FirebaseAuthBridge: NSObject, IosAuthBridge {
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
