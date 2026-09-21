//
//  CalendarViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-09.
//

import Shared
import SwiftUI

@MainActor
class CalendarViewModelS: ObservableObject {
    private let viewModel: CalendarViewModel
    private let preferencesStore: PreferencesStore
    
    @Published private(set) var calendarState: CalendarState = CalendarState()
    @Published private(set) var instances: [Instance] = []
    @Published private(set) var useColoredCards: Bool = false
    
    init() {
        self.viewModel = KoinBridge.shared.getCalendarViewModel()
        self.preferencesStore = KoinBridge.shared.getPreferencesStore()
        startObserving()
    }
    
    private func startObserving() {
        viewModel.calendarState.observeAsync(on: self, to: \.calendarState)
        viewModel.instances.observeAsync(on: self, to: \.instances)
        preferencesStore.useColoredCalendarCards.observeAsync(on: self) { owner, val in
            owner.useColoredCards = val.boolValue
        }
    }
    
    func load() {
        viewModel.load()
    }
    
    func loadMore() {
        viewModel.loadMore()
    }
    
    func reset() {
        viewModel.reset()
    }
    
    func toggleViewMode() {
        viewModel.toggleViewMode()
    }
    
    func setContentFilter(_ contentFilter: ContentFilter) {
        viewModel.setContentFilter(contentFilter: contentFilter)
    }
    
    func toggleShowMonitoredOnly() {
        viewModel.toggleShowMonitoredOnly()
    }
    
    func toggleShowPremiersOnly() {
        viewModel.toggleShowPremiersOnly()
    }
    
    func toggleShowFinalesOnly() {
        viewModel.toggleShowFinalesOnly()
    }
}
