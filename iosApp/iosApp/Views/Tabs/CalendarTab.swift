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
                    .navigationDestination(for: MediaRoute.self) { value in
                        MediaRouteDestination(route: value)
                    }
            }
        case .launcher:
            CalendarTabContent()
        }
    }
}

struct CalendarTabContent: View {
    
    @StateObject private var viewModel = CalendarViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager
    
    @State private var pendingDestinations: [ResolvedMediaDestination] = []
    @State private var showInstancePicker = false
    
    private var viewModeIcon: String {
        viewModel.calendarState.filterState.viewMode == .list ? "calendar" : "list.bullet"
    }
    
    var body: some View {
        Group {
            ZStack {
                if viewModel.calendarState.filterState.viewMode == .list {
                    CalendarListView(
                        state: viewModel.calendarState,
                        onLoadMore: { viewModel.loadMore() },
                        onItemClick: handleItemClick
                    )
                } else {
                    CalendarMonthView(
                        state: viewModel.calendarState,
                        onLoadMore: { viewModel.loadMore() },
                        onItemClick: handleItemClick
                    )
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
        .confirmationDialog(
            MR.strings().select_instance.localized(),
            isPresented: $showInstancePicker,
            titleVisibility: .visible
        ) {
            ForEach(pendingDestinations, id: \.instance.id) { dest in
                Button(dest.instance.label) {
                    Task {
                        await navigateTo(destination: dest)
                    }
                }
            }
            Button(MR.strings().cancel.localized(), role: .cancel) {
                pendingDestinations = []
            }
        }
    }
    
    private func handleItemClick(_ item: CalendarItem) {
        Task {
            let destinations = await viewModel.resolveDestination(item: item)
            if destinations.count > 1 {
                await MainActor.run {
                    pendingDestinations = destinations
                    showInstancePicker = true
                }
            } else if let dest = destinations.first {
                await navigateTo(destination: dest)
            }
        }
    }
    
    private func navigateTo(destination: ResolvedMediaDestination) async {
        await viewModel.selectInstance(instance: destination.instance)
        
        await MainActor.run {
            switch destination {
            case let movieDest as ResolvedMediaDestinationMovie:
                let route = MediaRoute.details(
                    arrId: movieDest.movieId?.int64Value,
                    tmdbId: movieDest.tmdbId?.int64Value,
                    tvdbId: nil,
                    instanceType: .radarr,
                    requestType: nil,
                    instanceId: destination.instance.id
                )
                navigationManager.go(to: route, of: .radarr)
                
            case let seriesDest as ResolvedMediaDestinationSeries:
                let route = MediaRoute.details(
                    arrId: seriesDest.seriesId?.int64Value,
                    tmdbId: seriesDest.tmdbId?.int64Value,
                    tvdbId: seriesDest.tvdbId?.int64Value,
                    instanceType: .sonarr,
                    requestType: nil,
                    instanceId: destination.instance.id
                )
                navigationManager.go(to: route, of: .sonarr)
                
            case let episodeDest as ResolvedMediaDestinationEpisodeDetails:
                let seriesJson = episodeDest.series.toJson()
                let episodeJson = episodeDest.episode.toJson()
                navigationManager.go(to: .episodeDetails(seriesJson, episodeJson), of: .sonarr)
                
            case let artistDest as ResolvedMediaDestinationArtist:
                let route = MediaRoute.details(
                    arrId: artistDest.artistId?.int64Value,
                    tmdbId: nil,
                    tvdbId: nil,
                    instanceType: .lidarr,
                    requestType: nil,
                    instanceId: destination.instance.id
                )
                navigationManager.go(to: route, of: .lidarr)
                
            case let bookDest as ResolvedMediaDestinationBookDetails:
                let bookJson = bookDest.book.toJson()
                let authorJson = bookDest.author.toJson()
                navigationManager.go(to: .bookDetails(bookJson: bookJson, authorJson: authorJson), of: .booksehelf)
                
            case let audiobookDest as ResolvedMediaDestinationAudiobookDetails:
                let route = MediaRoute.details(
                    arrId: audiobookDest.audiobookId?.int64Value,
                    tmdbId: nil,
                    tvdbId: nil,
                    instanceType: .listenarr,
                    requestType: nil,
                    instanceId: destination.instance.id
                )
                navigationManager.go(to: route, of: .listenarr)
                
            default:
                break
            }
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
