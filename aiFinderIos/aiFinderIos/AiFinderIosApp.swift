import SwiftUI
import FirebaseCore
import GoogleSignIn
import AiFinder

@main
struct AiFinderIosApp: App {
    init() {
        FirebaseApp.configure()
        IosAuthBridgeRegistry.shared.install(bridge: FirebaseAuthBridge())
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
