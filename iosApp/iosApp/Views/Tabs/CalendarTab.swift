//
//  CalendarTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-09.
//

import Shared
import SwiftUI

struct CalendarTab: View {
    @Environment(\.navigationContext) private var context
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack(path: $navigationManager.calendarPath) {
                CalendarTabContent()
                    .navigationDestination(for: MediaRoute.self) { route in
                        MediaRouteDestination(route: route)
                    }
            }
        case .launcher:
            CalendarTabContent()
        }
    }
}

struct CalendarTabContent: View {
    
    @ObservedObject private var viewModel = CalendarViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager
    
    private var viewModeIcon: String {
        viewModel.calendarState.filterState.viewMode == .list ? "calendar" : "list.bullet"
    }
    
    var body: some View {
        Group {
            ZStack {
                if viewModel.calendarState.filterState.viewMode == .list {
                    CalendarListView(state: viewModel.calendarState, instances: viewModel.instances, onItemClick: { item, instanceId in
                        handleItemClick(item, instanceId: instanceId)
                    }, onLoadMore: { viewModel.loadMore() })
                } else {
                    CalendarMonthView(state: viewModel.calendarState, instances: viewModel.instances, onItemClick: { item, instanceId in
                        handleItemClick(item, instanceId: instanceId)
                    }, onLoadMore: { viewModel.loadMore() })
                }
            }
        }
        .navigationTitle(MR.strings().schedule.localized())
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            toolbarContent
        }
        .refreshable {
            viewModel.load()
        }
        .onAppear {
            viewModel.load()
        }
    }

    private func handleItemClick(_ item: CalendarItem, instanceId: Int64?) {
        let instanceType = (item as? InstanceTypeIdentifiable)?.instanceType ?? .sonarr
        switch item {
        case let movie as ArrMovie:
            navigationManager.go(to: .details(arrId: movie.id?.int64Value, tmdbId: movie.tmdbId, instanceType: instanceType, instanceId: instanceId), of: instanceType)
        case let epGroup as EpisodeGroup:
            navigationManager.go(to: .details(arrId: epGroup.first.seriesId, instanceType: instanceType, instanceId: instanceId), of: instanceType)
        case let episode as Episode:
            if let series = episode.series {
                navigationManager.go(to: .details(arrId: series.id?.int64Value, tmdbId: series.tmdbId?.int64Value, instanceType: instanceType, instanceId: instanceId), of: instanceType)
                navigationManager.go(to: .episodeDetails(series.toJson(), episode.toJson()), of: instanceType)
            }
        case let album as ArrAlbum:
            navigationManager.go(to: .details(arrId: album.id, instanceType: instanceType, instanceId: instanceId), of: instanceType)
        case let book as Book:
            if let author = book.author {
                navigationManager.go(to: .details(arrId: author.id?.int64Value, instanceType: instanceType, instanceId: instanceId), of: instanceType)
                navigationManager.go(to: .bookDetails(bookJson: book.toJson(), authorJson: author.toJson()), of: instanceType)
            }
        case let audiobook as Audiobook:
            navigationManager.go(to: .details(arrId: audiobook.id?.int64Value, instanceType: instanceType, instanceId: instanceId), of: instanceType)
        default: break
        }
    }
    
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .topBarLeading) {
            Button {
                navigationManager.showLauncher = true
            } label: {
                Image(systemName: "line.3.horizontal")
            }
        }

        ToolbarItemGroup(placement: .topBarTrailing) {
            Button(action: {
                viewModel.toggleViewMode()
            }) {
                Image(systemName: viewModeIcon)
            }
        
            CalendarFilterMenu(
                contentFilter: Binding(
                    get: { viewModel.calendarState.filterState.contentFilter },
                    set: { viewModel.setContentFilter($0) }
                ),
                onlyMonitored: Binding(
                    get: { viewModel.calendarState.filterState.showMonitoredOnly },
                    set: { _ in viewModel.toggleShowMonitoredOnly() }
                ),
                onlyPremiers: Binding(
                    get: { viewModel.calendarState.filterState.showPremiersOnly },
                    set: { _ in viewModel.toggleShowPremiersOnly() }
                ),
                onlyFinales: Binding(
                    get: { viewModel.calendarState.filterState.showFinalesOnly },
                    set: { _ in viewModel.toggleShowFinalesOnly() }
                )
            )
            .menuIndicator(.hidden)
        }
    }
}
