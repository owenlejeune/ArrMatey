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
                case .library: LibraryTab()
                case .shows: SeriesTab()
                case .movies: MoviesTab()
                case .music: MusicTab()
                case .books: BooksTab()
                case .audiobooks: AudiobooksTab()
                case .activity: ActivityTab()
                case .calendar: CalendarTab()
                case .downloads: DownloadsTab()
                case .requests: RequestsTab()
                case .discover: DiscoverTab()
                case .prowlarr: ProwlarrTab()
                case .bazarr: BazarrTab()
                case .dashboard: DashboardTab()
                case .tracearr: TracearrTab()
                }
            } else if let custom = tabItem as? TabItemCustomWebpage {
                CustomWebpageViewerScreen(webpageId: custom.id)
            } else if let _ = tabItem as? TabItemSettings {
                SettingsScreen()
            }
        }
    }
}
