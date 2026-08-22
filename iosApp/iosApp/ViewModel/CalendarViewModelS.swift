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
    
    @Published private(set) var calendarState: CalendarState = CalendarState()
    
    init() {
        self.viewModel = KoinBridge.shared.getCalendarViewModel()
        startObserving()
    }
    
    private func startObserving() {
        viewModel.calendarState.observeAsync(on: self, to: \.calendarState)
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
    
    func resolveDestination(item: CalendarItem) async -> [ResolvedMediaDestination] {
        await withCheckedContinuation { continuation in
            Task { @MainActor in
                let result = try? await viewModel.resolveDestination(item: item)
                continuation.resume(returning: result ?? [])
            }
        }
    }
    
    func selectInstance(instance: Instance) async {
        try? await viewModel.selectInstance(instance: instance)
    }
}
