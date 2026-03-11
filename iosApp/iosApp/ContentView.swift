import SwiftUI
import Shared

struct ContentView: View {
    @EnvironmentObject var navigationManager: NavigationManager
    @ObservedObject private var queueViewModel = ActivityQueueViewModelS()
    @ObservedObject private var preferences = PreferencesViewModel()
    
    init() {
        let appearance = UITabBarAppearance()
        appearance.configureWithDefaultBackground()
        
        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }

    var body: some View {
        TabView(selection: $navigationManager.selectedTab) {
            ForEach(preferences.tabPreferences.bottomTabItems, id: \.self) { tabItem in
                NavigationStack(path: $navigationManager.settingsPath) {
                    TabItemContent(tabItem: tabItem)
                        .toolbar { toolbarItem }
                }
                .id(tabItem.name)
                .tabItem {
                    if preferences.useServiceNavLogos, let logo = tabItem.associatedType?.tabIcon {
                        Label(
                            title: { Text(tabItem.resource.localized()) },
                            icon: { logo.toImage(renderingMode: .template) }
                        )
                    } else {
                        Label(tabItem.resource.localized(), systemImage: tabItem.iosIcon)
                    }
                }
                .tag(tabItem)
                .badge(badgeValue(for: tabItem))
                .toolbar(preferences.tabPreferences.bottomTabItems.count <= 1 ? .hidden : .visible, for: .tabBar)
            }
        }
        .tabViewStyle(.sidebarAdaptable)
        .fullScreenCover(isPresented: $navigationManager.showLauncher) {
            AppLauncherGrid()
                .environmentObject(navigationManager)
        }
    }

    private func badgeValue(for tabItem: TabItem) -> Int {
        tabItem == .activity ? Int(queueViewModel.tasksWithIssues) : 0
    }
    
    private var toolbarItem: some ToolbarContent {
        ToolbarItem(placement: .topBarLeading) {
            Button {
                navigationManager.showLauncher = true
            } label: {
                Image(systemName: "line.3.horizontal")
            }
        }
    }
}

struct AppLauncherGrid: View {
    @ObservedObject private var preferences = PreferencesViewModel()
    @EnvironmentObject private var navigationManager: NavigationManager

    private let columns = [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())]

    var body: some View {
        NavigationStack(path: $navigationManager.launcherPath) {
            ScrollView {
                launcherContent
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button(action: {
                        navigationManager.showLauncher = false
                        navigationManager.launcherPath = NavigationPath()
                    }) {
                        Image(systemName: "xmark")
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        navigationManager.launcherPath.append(TabItem.settings)
                    } label: {
                        Image(systemName: "gearshape.fill")
                    }
                }
            }
            .navigationDestination(for: TabItem.self) { item in
                LauncherTabView(tabItem: item)
            }
            .navigationDestination(for: SettingsRoute.self) { route in
                SettingsRouteView(route: route)
            }
            .navigationDestination(for: MediaRoute.self) { route in
                MediaRouteDestination(route: route)
            }
        }
    }
    
    private var launcherContent: some View {
        LazyVGrid(columns: columns, spacing: 25) {
            ForEach(preferences.tabPreferences.hiddenTabs, id: \.self) { item in
                Button {
                    print("🔘 Tapped item: \(item.name)")
                    navigationManager.launcherPath.append(item)
                    print("📊 Launcher path count: \(navigationManager.launcherPath.count)")
                } label: {
                    VStack(spacing: 12) {
                        launcherIcon(for: item)
                        
                        Text(item.resource.localized())
                            .font(.caption)
                            .foregroundColor(.themeOnPrimaryContainer)
                    }
                    .frame(width: 80, height: 80)
                    .background(.themePrimary.opacity(0.1))
                    .cornerRadius(16)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(25)
    }

    @ViewBuilder
    private func launcherIcon(for item: TabItem) -> some View {
        if preferences.useServiceNavLogos, let logo = item.associatedType?.tabIcon {
            logo.toImage(renderingMode: .template)
                .foregroundColor(.themeOnPrimaryContainer)
        } else {
            Image(systemName: item.iosIcon)
                .font(.system(size: 30))
                .foregroundColor(.themeOnPrimaryContainer)
        }
    }
}

struct LauncherTabView: View {
    let tabItem: TabItem
    @EnvironmentObject var navigationManager: NavigationManager
    
    var body: some View {
        Group {
            switch tabItem {
            case .shows:
                SeriesTab()
                    .environment(\.navigationContext, .launcher)
            case .movies:
                MoviesTab()
                    .environment(\.navigationContext, .launcher)
            case .music:
                MusicTab()
                    .environment(\.navigationContext, .launcher)
            case .activity:
                ActivityTab()
                    .environment(\.navigationContext, .launcher)
            case .calendar:
                CalendarTab()
                    .environment(\.navigationContext, .launcher)
            case .downloads:
                DownloadsTab()
                    .environment(\.navigationContext, .launcher)
            case .requests:
                EmptyView()
            case .prowlarr:
                ProwlarrTab()
                    .environment(\.navigationContext, .launcher)
            case .settings:
                SettingsScreen()
            }
        }
        .navigationTitle(LocalizedStringKey(tabItem.resource.localized()))
    }
}
