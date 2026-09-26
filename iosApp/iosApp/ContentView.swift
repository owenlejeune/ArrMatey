import SwiftUI
import Shared

struct ContentView: View {
    @EnvironmentObject var navigationManager: NavigationManager
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @StateObject private var queueViewModel = ActivityQueueViewModelS()
    @StateObject private var preferences = PreferencesViewModel()

    init() {
        let appearance = UITabBarAppearance()
        appearance.configureWithDefaultBackground()

        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }

    var body: some View {
        Group {
            if preferences.isFirstLaunch {
                OnboardingView {
                    preferences.markFirstLaunchComplete()
                }
            } else if preferences.bottomTabItems.isEmpty {
                ProgressView()
            } else if horizontalSizeClass == .regular {
                SidebarSplitViewContainer(
                    navigationManager: navigationManager,
                    preferences: preferences
                )
            } else {
                CompactTabContainer(
                    navigationManager: navigationManager,
                    preferences: preferences
                )
            }
        }
        .onAppear {
            validateSelection(items: preferences.bottomTabItems)
        }
        .onChange(of: preferences.bottomTabItems.map { $0.key }) { _, _ in
            validateSelection(items: preferences.bottomTabItems)
        }
        .fullScreenCover(isPresented: $navigationManager.showLauncher) {
            AppLauncherGrid()
                .environmentObject(navigationManager)
        }
    }

    private func validateSelection(items: [AnyTabItem]) {
        guard !items.isEmpty else { return }

        let allTabs = items + preferences.drawerTabs
        if !allTabs.contains(where: { $0.key == navigationManager.selectedTab.key }) {
            navigationManager.selectedTab = items.first!
        }
    }
}

// MARK: - iPhone Compact Tab Container

struct CompactTabContainer: View {
    @ObservedObject var navigationManager: NavigationManager
    @ObservedObject var preferences: PreferencesViewModel

    var visibleTabs: [AnyTabItem] { preferences.bottomTabItems }
    var drawerTabs: [AnyTabItem] { preferences.drawerTabs }

    var body: some View {
        TabView(selection: Binding(
            get: { navigationManager.selectedTab.key },
            set: { newKey in
                if newKey == "more_drawer" {
                    navigationManager.selectedTab = AnyTabItem(item: TabItemSettings.shared as TabItem)
                } else if let match = (visibleTabs + drawerTabs).first(where: { $0.key == newKey }) {
                    navigationManager.selectedTab = match
                }
            }
        )) {
            ForEach(visibleTabs, id: \.key) { tabItem in
                NavigationStack(path: navigationManager.pathBinding(for: tabItem.key)) {
                    TabItemContent(tabItem: tabItem.item)
                        .withAppDestinations()
                }
                .tabItem {
                    TabLabel(item: tabItem.item, useServiceLogos: preferences.useServiceNavLogos)
                }
                .tag(tabItem.key)
            }

            if !drawerTabs.isEmpty {
                NavigationStack(path: navigationManager.pathBinding(for: "more_drawer")) {
                    MoreDrawerView(navigationManager: navigationManager, preferences: preferences)
                        .withAppDestinations()
                }
                .tabItem {
                    Label(MR.strings().navigation_items_drawer.localized(), systemImage: "ellipsis.circle.fill")
                }
                .tag("more_drawer")
            }
        }
        .toolbar(visibleTabs.count <= 1 && drawerTabs.isEmpty ? .hidden : .automatic, for: .tabBar)
    }
}

// MARK: - iPad Sidebar / Split View Container

struct SidebarSplitViewContainer: View {
    @ObservedObject var navigationManager: NavigationManager
    @ObservedObject var preferences: PreferencesViewModel
    @State private var columnVisibility: NavigationSplitViewVisibility = .all

    var body: some View {
        NavigationSplitView(columnVisibility: $columnVisibility) {
            SidebarContentView(
                navigationManager: navigationManager,
                preferences: preferences
            )
            .navigationTitle("ArrMatey")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        navigationManager.push(SettingsRoute.navigationConfig)
                    } label: {
                        Image(systemName: "slider.horizontal.3")
                    }
                }
            }
        } detail: {
            NavigationStack(path: navigationManager.pathBinding(for: navigationManager.selectedTab.key)) {
                TabItemContent(tabItem: navigationManager.selectedTab.item)
                    .withAppDestinations()
            }
        }
        .navigationSplitViewStyle(.balanced)
    }
}

struct SidebarContentView: View {
    @ObservedObject var navigationManager: NavigationManager
    @ObservedObject var preferences: PreferencesViewModel

    private var allActiveTabs: [AnyTabItem] {
        preferences.bottomTabItems + preferences.drawerTabs
    }

    private var coreTabs: [AnyTabItem] {
        allActiveTabs.filter { tab in
            guard let standard = tab.item as? TabItemStandard else { return false }
            return standard == .dashboard || standard == .library
        }
    }

    private var mediaTabs: [AnyTabItem] {
        allActiveTabs.filter { tab in
            guard let standard = tab.item as? TabItemStandard else { return false }
            return standard == .shows || standard == .movies || standard == .music || standard == .books || standard == .audiobooks
        }
    }

    private var activityTabs: [AnyTabItem] {
        allActiveTabs.filter { tab in
            guard let standard = tab.item as? TabItemStandard else { return false }
            return standard == .activity || standard == .calendar || standard == .downloads || standard == .requests || standard == .discover
        }
    }

    private var managementTabs: [AnyTabItem] {
        allActiveTabs.filter { tab in
            guard let standard = tab.item as? TabItemStandard else { return false }
            return standard == .prowlarr || standard == .bazarr || standard == .tracearr
        }
    }

    private var customWebpages: [AnyTabItem] {
        allActiveTabs.filter { $0.item is TabItemCustomWebpage }
    }

    var body: some View {
        List(selection: Binding(
            get: { navigationManager.selectedTab.key },
            set: { newKey in
                if let match = allActiveTabs.first(where: { $0.key == newKey }) {
                    navigationManager.selectedTab = match
                }
            }
        )) {
            if !coreTabs.isEmpty {
                Section {
                    ForEach(coreTabs, id: \.key) { tab in
                        sidebarRow(for: tab)
                    }
                }
            }

            if !mediaTabs.isEmpty {
                Section(header: Text("Media")) {
                    ForEach(mediaTabs, id: \.key) { tab in
                        sidebarRow(for: tab)
                    }
                }
            }

            if !activityTabs.isEmpty {
                Section(header: Text("Activity")) {
                    ForEach(activityTabs, id: \.key) { tab in
                        sidebarRow(for: tab)
                    }
                }
            }

            if !managementTabs.isEmpty {
                Section(header: Text("Management")) {
                    ForEach(managementTabs, id: \.key) { tab in
                        sidebarRow(for: tab)
                    }
                }
            }

            if !customWebpages.isEmpty {
                Section(header: Text("Web Pages")) {
                    ForEach(customWebpages, id: \.key) { tab in
                        sidebarRow(for: tab)
                    }
                }
            }

            Section {
                Button {
                    navigationManager.push(SettingsRoute.services)
                } label: {
                    Label(MR.strings().settings.localized(), systemImage: "gearshape")
                        .foregroundStyle(.primary)
                }
            }
        }
        .listStyle(.sidebar)
    }

    @ViewBuilder
    private func sidebarRow(for tab: AnyTabItem) -> some View {
        HStack {
            TabLabel(item: tab.item, useServiceLogos: preferences.useServiceNavLogos)
            Spacer()
        }
        .tag(tab.key)
    }
}

// MARK: - More / Drawer View

struct MoreDrawerView: View {
    @ObservedObject var navigationManager: NavigationManager
    @ObservedObject var preferences: PreferencesViewModel

    private let columns = [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())]

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Drawer Grid
                LazyVGrid(columns: columns, spacing: 20) {
                    ForEach(preferences.drawerTabs, id: \.key) { item in
                        NavigationLink(value: item) {
                            VStack(spacing: 8) {
                                launcherIcon(for: item.item)

                                Text(tabName(for: item.item))
                                    .font(.caption.weight(.medium))
                                    .lineLimit(1)
                                    .foregroundStyle(.primary)
                            }
                            .frame(width: 88, height: 88)
                            .background(Color(.secondarySystemGroupedBackground))
                            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                            .overlay(
                                RoundedRectangle(cornerRadius: 18, style: .continuous)
                                    .stroke(Color.primary.opacity(0.06), lineWidth: 0.5)
                            )
                            .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 16)

                // Quick Navigation / Settings Section
                VStack(spacing: 0) {
                    NavigationLink(value: SettingsRoute.navigationConfig) {
                        HStack {
                            Label(MR.strings().customize_navigation.localized(), systemImage: "slider.horizontal.3")
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.caption.bold())
                                .foregroundColor(.secondary)
                        }
                        .padding()
                    }

                    Divider()

                    NavigationLink(value: SettingsRoute.services) {
                        HStack {
                            Label(MR.strings().settings.localized(), systemImage: "gearshape.fill")
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.caption.bold())
                                .foregroundColor(.secondary)
                        }
                        .padding()
                    }
                }
                .background(Color(.secondarySystemGroupedBackground))
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                .padding(.horizontal, 20)
            }
        }
        .navigationTitle(MR.strings().navigation_items_drawer.localized())
        .navigationBarTitleDisplayMode(.large)
    }

    @ViewBuilder
    private func launcherIcon(for item: TabItem) -> some View {
        if preferences.useServiceNavLogos, let logo = item.associatedType?.tabIcon {
            logo.toImage(renderingMode: .template)
                .foregroundStyle(Color.accentColor)
        } else {
            Image(systemName: item.iosIcon)
                .font(.title2)
                .foregroundStyle(Color.accentColor)
        }
    }

    private func tabName(for item: TabItem) -> String {
        if let custom = item as? TabItemCustomWebpage {
            return custom.name
        }
        return item.resource.localized()
    }
}

// MARK: - App Launcher Grid Modal (Compatibility & Overlay Support)

struct AppLauncherGrid: View {
    @StateObject private var preferences = PreferencesViewModel()
    @EnvironmentObject private var navigationManager: NavigationManager

    private let columns = [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())]

    var body: some View {
        NavigationStack(path: navigationManager.pathBinding(for: "launcher")) {
            ScrollView {
                launcherContent
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button(action: {
                        navigationManager.showLauncher = false
                        navigationManager.clearLauncherPath()
                    }) {
                        Image(systemName: "xmark")
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        navigationManager.openSettings()
                    } label: {
                        Image(systemName: "gearshape.fill")
                    }
                }
            }
            .withAppDestinations()
        }
    }

    private var launcherContent: some View {
        LazyVGrid(columns: columns, spacing: 20) {
            ForEach(preferences.drawerTabs, id: \.key) { item in
                Button {
                    HapticFeedback.selection()
                    var lPath = navigationManager.path(for: "launcher")
                    lPath.append(item)
                    navigationManager.setPath(lPath, for: "launcher")
                } label: {
                    VStack(spacing: 8) {
                        launcherIcon(for: item.item)

                        Text(tabName(for: item.item))
                            .font(.caption.weight(.medium))
                            .lineLimit(1)
                            .foregroundStyle(.primary)
                    }
                    .frame(width: 88, height: 88)
                    .background(Color(.secondarySystemGroupedBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                    .overlay(
                        RoundedRectangle(cornerRadius: 18, style: .continuous)
                            .stroke(Color.primary.opacity(0.06), lineWidth: 0.5)
                    )
                    .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(20)
    }

    @ViewBuilder
    private func launcherIcon(for item: TabItem) -> some View {
        if preferences.useServiceNavLogos, let logo = item.associatedType?.tabIcon {
            logo.toImage(renderingMode: .template)
                .foregroundStyle(Color.accentColor)
        } else {
            Image(systemName: item.iosIcon)
                .font(.title2)
                .foregroundStyle(Color.accentColor)
        }
    }

    private func tabName(for item: TabItem) -> String {
        if let custom = item as? TabItemCustomWebpage {
            return custom.name
        }
        return item.resource.localized()
    }
}

// MARK: - Tab Label Component

struct TabLabel: View {
    let item: TabItem
    let useServiceLogos: Bool

    var body: some View {
        if let standard = item as? TabItemStandard {
            if useServiceLogos, let logo = standard.associatedType?.tabIcon {
                Label(
                    title: { Text(standard.resource.localized()) },
                    icon: { logo.toImage(renderingMode: .template) }
                )
            } else {
                Label(standard.resource.localized(), systemImage: standard.iosIcon)
            }
        } else if let custom = item as? TabItemCustomWebpage {
            Label(custom.name, systemImage: custom.iosIcon)
        }
    }
}
