//
//  EpisodeDetailsViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-19.
//

import Shared
import SwiftUI

@MainActor
class EpisodeDetailsViewModelS: ObservableObject {
    private let viewModel: EpisodeDetailsViewModel
    
    @Published private(set) var episode: Episode
    @Published private(set) var history: HistoryState = HistoryStateInitial()
    @Published private(set) var monitorStatus: OperationStatus = OperationStatusIdle()
    @Published private(set) var deleteStatus: OperationStatus = OperationStatusIdle()
    
    init(seriesId: Int64, episode: Episode) {
        self.episode = episode
        self.viewModel = KoinBridge.shared.getEpisodeDetailsViewModel(seriesId: seriesId, episode: episode)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.episode.observeAsync(on: self, to: \.episode)
        viewModel.history.observeAsync(on: self, to: \.history)
        viewModel.monitorStatus.observeAsync(on: self, to: \.monitorStatus)
        viewModel.deleteStatus.observeAsync(on: self, to: \.deleteStatus)
    }
    
    func toggleMonitor() {
        viewModel.toggleMonitor()
    }
    
    func executeAutomaticSearch() {
        viewModel.executeAutomaticSearch()
    }
    
    func refreshHistory() {
        viewModel.refreshHistory()
    }
    
    func deleteEpisode() {
        viewModel.deleteEpisode()
    }
    
    func resetMonitorStatus() {
        viewModel.resetMonitorStatus()
    }
}
