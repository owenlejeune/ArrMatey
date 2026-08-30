import SwiftUI
import MarkdownView
import Shared
import UserNotifications

@main
struct iOSApp: App {

    @ObservedObject private var preferences: PreferencesViewModel
    @StateObject private var navigationManager = NavigationManager()

    init() {
        KoinHelperKt.doInitKoin()
        LoggingHelperKt.doInitLogging()
        NetworkUtilsKt.initializeNetworkUtils()
        IOSCrashManager.shared.initialize()
        preferences = PreferencesViewModel()
    }

    private var latestUpdate: FeatureUpdate {
        ReleaseNotes.shared.latestUpdate
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(navigationManager)
                .onAppear {
                    UNUserNotificationCenter.current().delegate = navigationManager
                }
                .sheet(isPresented: Binding<Bool>(
                    get: { preferences.shouldShowReleaseNotes },
                    set: { _ in preferences.markReleaseNotesAsSeen() }
                )) {
                    ReleaseNotesSheet()
                }
        }
    }
}

extension View {
    func apply<V: View>(@ViewBuilder _ block: (Self) -> V) -> V { block(self) }

    @ViewBuilder
    func glassCompatibleButtonStyle() -> some View {
        if #available(iOS 26, *) {
            self.buttonStyle(.glassProminent)
        } else {
            self
        }
    }
}
