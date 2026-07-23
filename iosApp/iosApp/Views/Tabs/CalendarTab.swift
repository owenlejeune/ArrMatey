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
    
    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack {
                CalendarTabContent()
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
                    CalendarListView(state: viewModel.calendarState, onLoadMore: { viewModel.loadMore() })
                } else {
                    CalendarMonthView(state: viewModel.calendarState, onLoadMore: { viewModel.loadMore() })
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
                instanceId: Binding(
                    get: { viewModel.calendarState.filterState.instanceId?.int64Value },
                    set: { viewModel.setFilterInstanceId($0) }
                ),
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
                ),
                instances: viewModel.instances
            )
            .menuIndicator(.hidden)
        }
    }
}
