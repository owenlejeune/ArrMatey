//
//  TabItemContent.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-13.
//

import SwiftUI
import Shared

struct TabItemContent: View {
    let tabItem: TabItem
    @EnvironmentObject var navigationManager: NavigationManager

    var body: some View {
        Group {
            switch tabItem {
            case .shows: SeriesTab()
            case .movies: MoviesTab()
            case .music: MusicTab()
            case .activity: ActivityTab()
            case .downloads: DownloadsTab()
            case .calendar: CalendarTab()
            case .requests: EmptyView()
            case .prowlarr: ProwlarrTab()
            case .settings: SettingsScreen()
            }
        }
        .navigationTitle(LocalizedStringKey(tabItem.resource.localized()))
        // Add navigationDestination for SettingsRoute when in main tabs
        .navigationDestination(for: SettingsRoute.self) { route in
            SettingsRouteView(route: route)
        }
    }
}
