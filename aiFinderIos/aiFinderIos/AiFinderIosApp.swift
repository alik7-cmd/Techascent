import SwiftUI
import AiFinder

@main
struct AiFinderIosApp: App {
    var body: some Scene {
        WindowGroup {
            AiFinderView()
                .ignoresSafeArea()
        }
    }
}

private struct AiFinderView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
