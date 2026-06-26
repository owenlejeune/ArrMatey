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
            if let standard = tabItem as? TabItemStandard {
                switch standard {
                case .shows: SeriesTab().environment(\.navigationContext, .mainTab)
                case .movies: MoviesTab().environment(\.navigationContext, .mainTab)
                case .music: MusicTab().environment(\.navigationContext, .mainTab)
                case .books: BooksTab().environment(\.navigationContext, .mainTab)
                case .audiobooks: AudiobooksTab().environment(\.navigationContext, .mainTab)
                case .activity: ActivityTab().environment(\.navigationContext, .mainTab)
                case .calendar: CalendarTab().environment(\.navigationContext, .mainTab)
                case .downloads: DownloadsTab().environment(\.navigationContext, .mainTab)
                case .requests: RequestsTab().environment(\.navigationContext, .mainTab)
                case .prowlarr: ProwlarrTab().environment(\.navigationContext, .mainTab)
                case .bazarr: BazarrTab().environment(\.navigationContext, .mainTab)
                case .dashboard: DashboardTab().environment(\.navigationContext, .mainTab)
                }
            } else if let custom = tabItem as? TabItemCustomWebpage {
                CustomWebpageViewerScreen(webpageId: custom.id)
            } else if let _ = tabItem as? TabItemSettings {
                SettingsScreen()
            }
        }
        .navigationDestination(for: SettingsRoute.self) { route in
            SettingsRouteView(route: route)
        }
    }
}
