//
//  NavigationManager.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-11.
//

import SwiftUI
import Shared
import UserNotifications

class NavigationManager: NSObject, ObservableObject, UNUserNotificationCenterDelegate {
    private let tabManager: TabManager = KoinBridge.shared.getTabManager()

    @Published var paths: [String: NavigationPath] = [:]

    @Published var selectedTab: AnyTabItem = AnyTabItem(item: TabItemStandard.dashboard as TabItem)
    @Published var selectedDrawerTab: AnyTabItem? = nil

    @Published var showLauncher: Bool = false
    @Published var showMoreDrawer: Bool = false

    private var pendingSettingsRoute: SettingsRoute? = nil

    // MARK: - Scoped Path Accessors

    func pathBinding(for tabKey: String) -> Binding<NavigationPath> {
        Binding(
            get: { self.paths[tabKey] ?? NavigationPath() },
            set: { self.paths[tabKey] = $0 }
        )
    }

    func path(for tabKey: String) -> NavigationPath {
        paths[tabKey] ?? NavigationPath()
    }

    func setPath(_ path: NavigationPath, for tabKey: String) {
        paths[tabKey] = path
    }

    // MARK: - Compatibility Path Properties

    var settingsPath: NavigationPath {
        get { paths[TabItemSettings.shared.key] ?? NavigationPath() }
        set { paths[TabItemSettings.shared.key] = newValue }
    }
    var seriesPath: NavigationPath {
        get { paths[TabItemStandard.shows.key] ?? NavigationPath() }
        set { paths[TabItemStandard.shows.key] = newValue }
    }
    var moviePath: NavigationPath {
        get { paths[TabItemStandard.movies.key] ?? NavigationPath() }
        set { paths[TabItemStandard.movies.key] = newValue }
    }
    var musicPath: NavigationPath {
        get { paths[TabItemStandard.music.key] ?? NavigationPath() }
        set { paths[TabItemStandard.music.key] = newValue }
    }
    var bookPath: NavigationPath {
        get { paths[TabItemStandard.books.key] ?? NavigationPath() }
        set { paths[TabItemStandard.books.key] = newValue }
    }
    var audiobookPath: NavigationPath {
        get { paths[TabItemStandard.audiobooks.key] ?? NavigationPath() }
        set { paths[TabItemStandard.audiobooks.key] = newValue }
    }
    var seerrPath: NavigationPath {
        get { paths[TabItemStandard.requests.key] ?? NavigationPath() }
        set { paths[TabItemStandard.requests.key] = newValue }
    }
    var launcherPath: NavigationPath {
        get { paths["launcher"] ?? NavigationPath() }
        set { paths["launcher"] = newValue }
    }
    var dashboardPath: NavigationPath {
        get { paths[TabItemStandard.dashboard.key] ?? NavigationPath() }
        set { paths[TabItemStandard.dashboard.key] = newValue }
    }
    var bazarrPath: NavigationPath {
        get { paths[TabItemStandard.bazarr.key] ?? NavigationPath() }
        set { paths[TabItemStandard.bazarr.key] = newValue }
    }
    var tracearrPath: NavigationPath {
        get { paths[TabItemStandard.tracearr.key] ?? NavigationPath() }
        set { paths[TabItemStandard.tracearr.key] = newValue }
    }
    var libraryPath: NavigationPath {
        get { paths[TabItemStandard.library.key] ?? NavigationPath() }
        set { paths[TabItemStandard.library.key] = newValue }
    }
    var calendarPath: NavigationPath {
        get { paths[TabItemStandard.calendar.key] ?? NavigationPath() }
        set { paths[TabItemStandard.calendar.key] = newValue }
    }

    // MARK: - Modern Router Operations

    func push(_ route: AppRoute) {
        let key = activeContextKey()
        var currentPath = paths[key] ?? NavigationPath()
        currentPath.append(route)
        paths[key] = currentPath
    }

    func push<T: Hashable>(_ route: T) {
        let key = activeContextKey()
        var currentPath = paths[key] ?? NavigationPath()
        currentPath.append(route)
        paths[key] = currentPath
    }

    func navigate(to tabKey: String, pushing route: AppRoute? = nil) {
        let allTabs = tabManager.tabConfiguration.value.visibleTabs + tabManager.tabConfiguration.value.drawerTabs
        if let match = allTabs.first(where: { $0.key == tabKey }) {
            navigateToTab(match)
        }
        if let route = route {
            push(route)
        }
    }

    func pop() {
        let key = activeContextKey()
        guard var currentPath = paths[key], !currentPath.isEmpty else { return }
        currentPath.removeLast()
        paths[key] = currentPath
    }

    func popToRoot() {
        let key = activeContextKey()
        paths[key] = NavigationPath()
    }

    private func activeContextKey() -> String {
        if showLauncher {
            return "launcher"
        }
        return selectedTab.key
    }

    // MARK: - Navigation Intents

    func go(to route: MediaRoute, of type: InstanceType) {
        if showLauncher {
            push(route)
            return
        }

        if selectedTab.key == TabItemStandard.library.key {
            var libPath = paths[TabItemStandard.library.key] ?? NavigationPath()
            libPath.append(route)
            paths[TabItemStandard.library.key] = libPath
            return
        }

        navigateToTab(tabFor(type))

        let targetKey = tabKey(for: type)
        var tabPath = paths[targetKey] ?? NavigationPath()
        tabPath.append(route)
        paths[targetKey] = tabPath
    }

    func replaceCurrent(with route: MediaRoute, for type: InstanceType) {
        if showLauncher {
            var lPath = paths["launcher"] ?? NavigationPath()
            if !lPath.isEmpty { lPath.removeLast() }
            lPath.append(route)
            paths["launcher"] = lPath
            return
        }

        if selectedTab.key == TabItemStandard.library.key {
            var libPath = paths[TabItemStandard.library.key] ?? NavigationPath()
            if !libPath.isEmpty { libPath.removeLast() }
            libPath.append(route)
            paths[TabItemStandard.library.key] = libPath
            return
        }

        navigateToTab(tabFor(type))

        let targetKey = tabKey(for: type)
        var tabPath = paths[targetKey] ?? NavigationPath()
        if !tabPath.isEmpty { tabPath.removeLast() }
        tabPath.append(route)
        paths[targetKey] = tabPath
    }

    func go(to route: SettingsRoute) {
        if showLauncher {
            push(route)
        } else {
            var sPath = paths[TabItemSettings.shared.key] ?? NavigationPath()
            sPath.append(route)
            paths[TabItemSettings.shared.key] = sPath
        }
    }

    func go(to route: BazarrRoute) {
        if showLauncher {
            push(route)
        } else {
            var bPath = paths[TabItemStandard.bazarr.key] ?? NavigationPath()
            bPath.append(route)
            paths[TabItemStandard.bazarr.key] = bPath
        }
    }

    func go(to route: TracearrRoute) {
        if showLauncher {
            push(route)
        } else if selectedTab.key == TabItemStandard.dashboard.key {
            var dPath = paths[TabItemStandard.dashboard.key] ?? NavigationPath()
            dPath.append(route)
            paths[TabItemStandard.dashboard.key] = dPath
        } else {
            var tPath = paths[TabItemStandard.tracearr.key] ?? NavigationPath()
            tPath.append(route)
            paths[TabItemStandard.tracearr.key] = tPath
        }
    }

    func setSelectedDrawerTab(_ tab: AnyTabItem?) {
        selectedDrawerTab = tab
    }

    func setSelectedDrawerTab(_ tab: TabItem) {
        selectedDrawerTab = AnyTabItem(item: tab)
    }

    func goToNewInstance(of type: InstanceType) {
        clearAllPaths()

        if showLauncher {
            let settingsTab = AnyTabItem(item: TabItemSettings.shared as TabItem)
            var lPath = paths["launcher"] ?? NavigationPath()
            lPath.append(settingsTab)
            lPath.append(SettingsRoute.services)
            lPath.append(SettingsRoute.newInstance(type))
            paths["launcher"] = lPath
        } else {
            pendingSettingsRoute = .newInstance(type)
            showLauncher = true
        }
    }

    func goToEditInstance(of type: InstanceType, _ id: Int64) {
        clearAllPaths()

        if showLauncher {
            let settingsTab = AnyTabItem(item: TabItemSettings.shared as TabItem)
            var lPath = paths["launcher"] ?? NavigationPath()
            lPath.append(settingsTab)
            lPath.append(SettingsRoute.services)
            lPath.append(SettingsRoute.editInstance(id))
            paths["launcher"] = lPath
        } else {
            pendingSettingsRoute = .editInstance(id)
            showLauncher = true
        }
    }

    func clearAllPaths() {
        paths.removeAll()
    }

    func maybeEditInstance(of type: InstanceType, _ instance: Instance?) {
        if let i = instance {
            goToEditInstance(of: type, i.id)
        }
    }

    func applyPendingRoute() {
        if let route = pendingSettingsRoute {
            var lPath = paths["launcher"] ?? NavigationPath()
            lPath.append(SettingsRoute.services)
            lPath.append(route)
            paths["launcher"] = lPath
            pendingSettingsRoute = nil
        }
    }

    func completeSetupAndDismiss() {
        self.showLauncher = false
        self.paths.removeAll()
    }

    func goInLauncher(to route: SettingsRoute) {
        var lPath = paths["launcher"] ?? NavigationPath()
        lPath.append(route)
        paths["launcher"] = lPath
    }

    func popLauncherPath() {
        var lPath = paths["launcher"] ?? NavigationPath()
        if !lPath.isEmpty {
            lPath.removeLast()
            paths["launcher"] = lPath
        }
    }

    func clearLauncherPath() {
        paths["launcher"] = NavigationPath()
    }

    func openSettings() {
        openOverlay(TabItemSettings.shared)
    }

    func popToSettings() {
        if showLauncher {
            clearLauncherPath()
            var lPath = NavigationPath()
            lPath.append(AnyTabItem(item: TabItemSettings.shared))
            paths["launcher"] = lPath
        } else {
            paths[TabItemSettings.shared.key] = NavigationPath()
        }
    }

    func goToSeerrDetails(tmdbId: Int64, requestType: RequestType) {
        let route = SeerrRoute.details(tmdbId: tmdbId, requestType: requestType)
        if showLauncher {
            push(route)
        } else {
            var sPath = paths[TabItemStandard.requests.key] ?? NavigationPath()
            sPath.append(route)
            paths[TabItemStandard.requests.key] = sPath
        }
    }

    func goToDiscoverCategory(category: DiscoverCategory) {
        let route = SeerrRoute.category(category: category)
        if showLauncher {
            push(route)
        } else {
            var sPath = paths[TabItemStandard.requests.key] ?? NavigationPath()
            sPath.append(route)
            paths[TabItemStandard.requests.key] = sPath
        }
    }

    func goToPersonDetails(id: Int64) {
        let route = SeerrRoute.personDetails(id: id)
        if showLauncher {
            push(route)
        } else {
            var sPath = paths[TabItemStandard.requests.key] ?? NavigationPath()
            sPath.append(route)
            paths[TabItemStandard.requests.key] = sPath
        }
    }

    func goToDetails(
        arrId: Int64? = nil,
        tmdbId: Int64? = nil,
        tvdbId: Int64? = nil,
        instanceType: InstanceType? = nil,
        requestType: RequestType? = nil,
        instanceId: Int64? = nil,
        episodeId: Int64? = nil
    ) {
        let route = MediaRoute.details(
            arrId: arrId,
            tmdbId: tmdbId,
            tvdbId: tvdbId,
            instanceType: instanceType,
            requestType: requestType,
            instanceId: instanceId,
            episodeId: episodeId
        )
        if showLauncher {
            push(route)
        } else if let type = instanceType {
            go(to: route, of: type)
        } else if selectedTab.key == TabItemStandard.tracearr.key {
            var tPath = paths[TabItemStandard.tracearr.key] ?? NavigationPath()
            tPath.append(route)
            paths[TabItemStandard.tracearr.key] = tPath
        } else {
            var sPath = paths[TabItemStandard.requests.key] ?? NavigationPath()
            sPath.append(route)
            paths[TabItemStandard.requests.key] = sPath
        }
    }

    func goToPreview(_ json: String, type: InstanceType) {
        let route = MediaRoute.preview(json, type: type)
        if showLauncher {
            push(route)
        } else {
            go(to: route, of: type)
        }
    }

    func goToArrDetailsOrPreview(item: ArrMedia, type: InstanceType? = nil, instanceId: Int64? = nil) {
        if item.id == nil {
            if item is ArrMovie || item is ArrSeries {
                let tmdbId = (item as? ArrMovie)?.tmdbId ?? (item as? ArrSeries)?.tmdbId?.int64Value
                let tvdbId = (item as? ArrSeries)?.tvdbId
                goToDetails(arrId: nil, tmdbId: tmdbId, tvdbId: tvdbId, instanceType: type, instanceId: instanceId)
            } else if let type = type {
                goToPreview(item.toJson(), type: type)
            }
        } else {
            goToDetails(arrId: item.id?.int64Value, instanceType: type, instanceId: instanceId)
        }
    }

    func goToDetailsOnDashboard(
        arrId: Int64? = nil,
        tmdbId: Int64? = nil,
        tvdbId: Int64? = nil,
        instanceType: InstanceType? = nil,
        requestType: RequestType? = nil,
        instanceId: Int64? = nil,
        episodeId: Int64? = nil
    ) {
        let route = MediaRoute.details(
            arrId: arrId,
            tmdbId: tmdbId,
            tvdbId: tvdbId,
            instanceType: instanceType,
            requestType: requestType,
            instanceId: instanceId,
            episodeId: episodeId
        )
        if showLauncher {
            push(route)
        } else {
            var dPath = paths[TabItemStandard.dashboard.key] ?? NavigationPath()
            dPath.append(route)
            paths[TabItemStandard.dashboard.key] = dPath
        }
    }

    func goToSeerrDetailsOnDashboard(tmdbId: Int64, requestType: RequestType) {
        if requestType == .person {
            goToPersonDetailsOnDashboard(id: tmdbId)
        } else {
            let route = SeerrRoute.details(tmdbId: tmdbId, requestType: requestType)
            if showLauncher {
                push(route)
            } else {
                var dPath = paths[TabItemStandard.dashboard.key] ?? NavigationPath()
                dPath.append(route)
                paths[TabItemStandard.dashboard.key] = dPath
            }
        }
    }

    func goToPersonDetailsOnDashboard(id: Int64) {
        let route = SeerrRoute.personDetails(id: id)
        if showLauncher {
            push(route)
        } else {
            var dPath = paths[TabItemStandard.dashboard.key] ?? NavigationPath()
            dPath.append(route)
            paths[TabItemStandard.dashboard.key] = dPath
        }
    }

    func goToArrDetailsOrPreviewOnDashboard(item: ArrMedia, type: InstanceType? = nil, instanceId: Int64? = nil) {
        if item.id == nil {
            if item is ArrMovie || item is ArrSeries {
                let tmdbId = (item as? ArrMovie)?.tmdbId ?? (item as? ArrSeries)?.tmdbId?.int64Value
                let tvdbId = (item as? ArrSeries)?.tvdbId
                goToDetailsOnDashboard(arrId: nil, tmdbId: tmdbId, tvdbId: tvdbId, instanceType: type, instanceId: instanceId)
            } else if let type = type {
                let route = MediaRoute.preview(item.toJson(), type: type)
                if showLauncher {
                    push(route)
                } else {
                    var dPath = paths[TabItemStandard.dashboard.key] ?? NavigationPath()
                    dPath.append(route)
                    paths[TabItemStandard.dashboard.key] = dPath
                }
            }
        } else {
            goToDetailsOnDashboard(arrId: item.id?.int64Value, instanceType: type, instanceId: instanceId)
        }
    }

    func navigateToTab(_ tab: TabItem) {
        let visibleTabs = tabManager.tabConfiguration.value.visibleTabs
        let visibleKeys = visibleTabs.map { $0.key }

        DispatchQueue.main.async {
            if visibleKeys.contains(tab.key) {
                self.closeOverlay()
                self.selectedTab = AnyTabItem(item: tab)
            } else {
                self.openOverlay(tab)
            }
        }
    }

    func openOverlay(_ tab: TabItem) {
        clearLauncherPath()
        var lPath = NavigationPath()
        lPath.append(AnyTabItem(item: tab))
        paths["launcher"] = lPath
        showLauncher = true
    }

    func closeOverlay() {
        showLauncher = false
        clearLauncherPath()
    }

    func openRequestsTab() {
        navigateToTab(TabItemStandard.requests as TabItem)
    }

    func openDiscoverTab() {
        navigateToTab(TabItemStandard.discover as TabItem)
    }

    func openProwlarrTab() {
        navigateToTab(TabItemStandard.prowlarr as TabItem)
    }

    func openBazarrTab() {
        navigateToTab(TabItemStandard.bazarr as TabItem)
    }

    func openTracearrTab() {
        navigateToTab(TabItemStandard.tracearr as TabItem)
    }

    func openDownloadsTab() {
        navigateToTab(TabItemStandard.downloads as TabItem)
    }

    func openActivityTab() {
        navigateToTab(TabItemStandard.activity as TabItem)
    }

    func openScheduleTab() {
        navigateToTab(TabItemStandard.calendar as TabItem)
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        if let action = userInfo["action"] as? String {
            handleAction(action, userInfo: userInfo)
        }
        completionHandler()
    }

    func handleAction(_ action: String, userInfo: [AnyHashable: Any] = [:]) {
        switch action {
        case NotificationConstants.shared.ACTION_OPEN_SCHEDULE:
            openScheduleTab()

            if let itemIdStr = userInfo[NotificationConstants.shared.EXTRA_ITEM_ID] as? String,
               let itemId = Int64(itemIdStr),
               let typeName = userInfo[NotificationConstants.shared.EXTRA_INSTANCE_TYPE] as? String,
               let type = InstanceType.allCases.first(where: { $0.name == typeName }) {

                let tmdbId = (userInfo[NotificationConstants.shared.EXTRA_TMDB_ID] as? String).flatMap { Int64($0) }
                let instanceId = (userInfo[NotificationConstants.shared.EXTRA_INSTANCE_ID] as? String).flatMap { Int64($0) }
                let episodeId = (userInfo[NotificationConstants.shared.EXTRA_EPISODE_ID] as? String).flatMap { Int64($0) }

                var calPath = NavigationPath()
                calPath.append(MediaRoute.details(
                    arrId: itemId,
                    tmdbId: tmdbId,
                    tvdbId: nil,
                    instanceType: type,
                    requestType: nil,
                    instanceId: instanceId,
                    episodeId: episodeId
                ))
                paths[TabItemStandard.calendar.key] = calPath
            }
        case NotificationConstants.shared.ACTION_OPEN_DOWNLOADS:
            openDownloadsTab()
        case NotificationConstants.shared.ACTION_OPEN_ACTIVITY:
            openActivityTab()
        case NotificationConstants.shared.ACTION_OPEN_REQUESTS:
            openRequestsTab()
        case NotificationConstants.shared.ACTION_OPEN_DASHBOARD:
            closeOverlay()
        default:
            break
        }
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.banner, .list, .sound])
    }

    func openArrDashboard(id: Int64) {
        let route = SettingsRoute.arrDashboard(id)
        if showLauncher {
            push(route)
        } else {
            var dPath = paths[TabItemStandard.dashboard.key] ?? NavigationPath()
            dPath.append(route)
            paths[TabItemStandard.dashboard.key] = dPath
        }
    }

    func tabFor(_ type: InstanceType) -> TabItem {
        switch type {
        case .sonarr: return TabItemStandard.shows as TabItem
        case .radarr: return TabItemStandard.movies as TabItem
        case .lidarr: return TabItemStandard.music as TabItem
        case .bookshelf: return TabItemStandard.books as TabItem
        case .listenarr: return TabItemStandard.audiobooks as TabItem
        case .seerr: return TabItemStandard.requests as TabItem
        case .prowlarr: return TabItemStandard.prowlarr as TabItem
        case .bazarr: return TabItemStandard.bazarr as TabItem
        case .tracearr: return TabItemStandard.tracearr as TabItem
        }
    }

    func tabKey(for type: InstanceType) -> String {
        tabFor(type).key
    }

    func shouldShowDrawerButton(for tabKey: String) -> Bool {
        guard UIDevice.current.userInterfaceIdiom == .phone else { return false }
        guard !showLauncher else { return false }
        let visibleTabs = tabManager.tabConfiguration.value.visibleTabs
        guard visibleTabs.contains(where: { $0.key == tabKey }) else { return false }
        guard selectedTab.key == tabKey else { return false }
        guard (paths[tabKey]?.isEmpty ?? true) else { return false }
        return true
    }
}

// MARK: - App Navigation Routes

enum AppRoute: Hashable {
    case media(MediaRoute)
    case seerr(SeerrRoute)
    case settings(SettingsRoute)
    case bazarr(BazarrRoute)
    case tracearr(TracearrRoute)
    case customWebpage(id: Int64)
}

enum MediaRoute: Hashable {
    case details(
        arrId: Int64? = nil,
        tmdbId: Int64? = nil,
        tvdbId: Int64? = nil,
        instanceType: InstanceType? = nil,
        requestType: RequestType? = nil,
        instanceId: Int64? = nil,
        episodeId: Int64? = nil
    )
    case search(query: String, type: InstanceType, instanceId: Int64? = nil)
    case globalSearch(query: String = "")
    case preview(_ json: String, type: InstanceType)
    case movieRelease(movieId: Int64, instanceId: Int64? = nil)
    case movieFiles(String)
    case seriesReleases(
        seriesId: Int64? = nil,
        seasonNumber: Int32? = nil,
        episodeId: Int64? = nil,
        instanceId: Int64? = nil
    )
    case albumReleases(
        albumId: Int64,
        artistId: Int64? = nil,
        instanceId: Int64? = nil
    )
    case bookReleases(bookId: Int64, instanceId: Int64? = nil)
    case audiobookReleases(id: Int64?, query: String, instanceId: Int64? = nil)
    case authorFiles(authorJson: String)
    case audiobookFiles(audiobookJson: String)
    case bookDetails(bookJson: String, authorJson: String, instanceId: Int64? = nil)
    case episodeDetails(String, String, instanceId: Int64? = nil)
}

enum SeerrRoute: Hashable {
    case details(tmdbId: Int64, requestType: RequestType)
    case category(category: DiscoverCategory)
    case personDetails(id: Int64)
}

enum SettingsRoute: Hashable {
    case services
    case userInterface
    case integrations
    case backupRestore
    case newInstance(_ : InstanceType = .sonarr)
    case dev
    case editInstance(Int64)
    case navigationConfig
    case arrDashboard(Int64)
    case newDownloadClient
    case editDownloadClient(Int64)
    case newCustomWebpage
    case editCustomWebpage(Int64)
}

enum BazarrRoute: Hashable {
    case library
    case details(Int64, BazarrMediaType)
}

enum TracearrRoute: Hashable {
    case history
    case user(String)
    case users
    case violations
    case activity
}

extension MediaRoute {
    static func details(id: Int64, type: InstanceType) -> MediaRoute {
        return .details(arrId: id, tmdbId: nil, tvdbId: nil, instanceType: type, requestType: nil)
    }

    static func details(tmdbId: Int64, requestType: RequestType) -> MediaRoute {
        return .details(arrId: nil, tmdbId: tmdbId, tvdbId: nil, instanceType: nil, requestType: requestType)
    }
}
