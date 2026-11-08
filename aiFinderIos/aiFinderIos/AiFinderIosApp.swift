import SwiftUI
import FirebaseCore
import GoogleSignIn
import AiFinder

@main
struct AiFinderIosApp: App {
    init() {
        FirebaseApp.configure()
        let bridge = FirebaseAuthBridge()
        IosAuthBridgeRegistry.shared.install(bridge: bridge)
        IosPurchaseHistoryBridgeRegistry.shared.install(bridge: bridge)
        IosFirebaseBridgeRegistry.shared.install(bridge: bridge)
        IosDeviceFilesBridgeRegistry.shared.install(bridge: bridge)
    }

    private struct AiFinderView: UIViewControllerRepresentable {
        func makeUIViewController(context: Context) -> UIViewController {
            MainViewControllerKt.MainViewController()
        }

        func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
    }

    var body: some Scene {
        WindowGroup {
            AiFinderView()
                .ignoresSafeArea()
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
