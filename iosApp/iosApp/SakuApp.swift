// xcode: set sdk=iOS

import UIKit
import ComposeApp

// The Swift host for the Compose Multiplatform UI.
//
// Deliberately minimal: every screen lives in :composeApp commonMain, so this
// file only has to hand a UIViewController to UIKit. All the interesting
// surface is `MainViewController()` in
// composeApp/src/iosMain/kotlin/id/almezi/simplemoneytracker_kt/MainViewController.kt,
// which mirrors MainActivity on Android.

/// Scene delegate. Required because the iOS 27 SDK mandates a UIScene lifecycle;
/// without it the app launches to a blank window.
class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }
        let window = UIWindow(windowScene: windowScene)
        window.rootViewController = MainViewControllerKt.MainViewController()
        window.makeKeyAndVisible()
        self.window = window
    }
}

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        configurationForConnecting session: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        UISceneConfiguration(name: "Default Configuration", sessionRole: session.role)
    }
}
