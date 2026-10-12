// STAGE 1 SPIKE — throwaway.
//
// Answers the CMP-9551 question directly: walk the iOS accessibility tree
// that Compose Multiplatform's bridge produces, and report which testTags
// surfaced as accessibilityIdentifier.
//
// No XCTest target needed — CMP populates UIView.accessibilityElements
// regardless of whether VoiceOver is active, so we can read the tree from
// inside the app process itself.
//
// Two hard-won notes:
//  * Apps built with the iOS 27 SDK are REFUSED at launch without the
//    UIScene lifecycle ("UIScene life cycle is required for apps built with
//    this SDK"), so a scene delegate is mandatory, not optional.
//  * The delegate class is registered PROGRAMMATICALLY rather than by the
//    Info.plist UISceneDelegateClassName string, which fails to resolve for a
//    hand-built bundle.

import UIKit
import ComposeApp

func spikeWrite(_ name: String, _ text: String) {
    guard let docs = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first
    else { return }
    try? text.write(to: docs.appendingPathComponent(name), atomically: true, encoding: .utf8)
}

func spikeLog(_ text: String) {
    NSLog("SPIKE| %@", text)
}

final class SpikeSceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        spikeLog("scene willConnect")
        guard let windowScene = scene as? UIWindowScene else { return }
        let window = UIWindow(windowScene: windowScene)
        window.rootViewController = MainViewControllerKt.MainViewController()
        window.makeKeyAndVisible()
        self.window = window
        spikeWrite("stage.txt", "scene-connected")

        // Give CMP time to compose and run its accessibility sync.
        DispatchQueue.main.asyncAfter(deadline: .now() + 6) {
            SpikeReporter.run()
        }
    }
}

final class SpikeAppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions options: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        spikeLog("didFinishLaunching")
        spikeWrite("stage.txt", "did-finish-launching")
        return true
    }

    // Register the scene delegate in code. Avoids relying on the Info.plist
    // UISceneDelegateClassName string resolving for a hand-built bundle.
    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        spikeLog("configurationForConnecting")
        let config = UISceneConfiguration(name: "Default", sessionRole: connectingSceneSession.role)
        config.delegateClass = SpikeSceneDelegate.self
        return config
    }
}

enum SpikeReporter {

    // Walk any object that vends accessibility children, whether it is a
    // UIView or CMP's AccessibilityRoot wrapper (which is NOT a UIView).
    // Reflection keeps this working for both without hardcoding CMP internals.
    static func kvc(_ obj: Any, _ key: String) -> Any? {
        (obj as? NSObject)?.value(forKey: key)
    }

    static func identifier(_ obj: Any) -> String {
        if let v = obj as? UIView { return v.accessibilityIdentifier ?? "<nil>" }
        if let s = kvc(obj, "accessibilityIdentifier") as? String { return s }
        return "<nil>"
    }

    static func collect(_ obj: Any, depth: Int, into out: inout [String]) {
        let indent = String(repeating: "  ", count: depth)
        out.append("\(indent)\(type(of: obj)) id=\(identifier(obj))")

        // UIView path
        if let view = obj as? UIView {
            if let synthetic = view.accessibilityElements, !synthetic.isEmpty {
                out.append("\(indent)  SYNTHETIC count=\(synthetic.count)")
                for child in synthetic { collect(child, depth: depth + 1, into: &out) }
            }
            for sub in view.subviews { collect(sub, depth: depth + 1, into: &out) }
            return
        }

        // Non-UIView accessibility object path (CMP's AccessibilityRoot)
        if let children = kvc(obj, "accessibilityElements") as? [Any], !children.isEmpty {
            out.append("\(indent)  CHILDREN count=\(children.count)")
            for child in children { collect(child, depth: depth + 1, into: &out) }
        }
    }

    static func run() {
        spikeLog("reporter running")
        var lines: [String] = []

        let scenes = UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }
        let windows = scenes.flatMap { $0.windows }
        lines.append("=== window/scene inventory ===")
        lines.append("scenes=\(scenes.count) windows=\(windows.count)")

        // Walk EVERY window, not just the first. A ModalBottomSheet is presented
        // in its own window; reading windows[0] alone reported every tag as
        // MISSING while the sheet was open.
        var walked = 0
        for (i, w) in windows.enumerated() {
            guard let v = w.rootViewController?.view else { continue }
            lines.append("=== window[\(i)] hidden=\(w.isHidden) ===")
            collect(v, depth: 0, into: &lines)
            walked += 1
        }
        if walked == 0 {
            spikeWrite("spike-report.txt", "NO_ROOT_VIEW\n")
            return
        }

        let joined = lines.joined(separator: "\n")

        // Real Saku tags from TestTags.kt, as rendered by SakuDaftarScreen.
        let expected: [(String, String)] = [
            ("daftar_screen",                              "screen root container"),
            ("daftar_list",                                "transaction lazy list"),
            ("component_nav_bottom",                       "floating bottom nav"),
            ("component_card_transaction_inc_salary",      "TransactionCard: Gaji"),
            ("component_card_transaction_edit_inc_salary", "row Uh action"),
            ("component_card_transaction_delete_inc_salary", "row Hapus action"),
            ("component_card_transaction_exp_daily_food", "TransactionCard: Makanan"),
            ("component_card_transaction_inc_freelance",  "TransactionCard: Freelance"),
            ("avatar_inc_salary",                          "CategoryAvatar")
        ]

        var verdict = "=== VERDICT ===\n"
        for (tag, desc) in expected {
            verdict += "SPIKE_TAG \(joined.contains(tag) ? "FOUND" : "MISSING") \(tag) - \(desc)\n"
        }

        spikeWrite("spike-report.txt", joined + "\n\n" + verdict)
        spikeLog("report written")
    }
}

// Entry point.
UIApplicationMain(CommandLine.argc, CommandLine.unsafeArgv, nil, NSStringFromClass(SpikeAppDelegate.self))