//
//  MediaServerLibraryViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class MediaServerLibraryViewModelS: ObservableObject {
    private let viewModel: MediaServerLibraryViewModel

    @Published private(set) var configuredServers: [MediaServerIntegration] = []
    @Published private(set) var selectedServer: MediaServerIntegration? = nil
    @Published private(set) var libraries: [UnifiedMediaLibrary] = []
    @Published private(set) var selectedLibrary: UnifiedMediaLibrary? = nil
    @Published private(set) var filteredItems: [UnifiedMediaItem] = []
    @Published private(set) var isLoading: Bool = false
    @Published private(set) var isRefreshing: Bool = false
    @Published private(set) var error: String? = nil
    @Published var searchQuery: String = ""

    init() {
        self.viewModel = KoinBridge.shared.getMediaServerLibraryViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.configuredServers.observeAsync(on: self) { owner, servers in
            owner.configuredServers = servers
        }
        viewModel.selectedServer.observeAsync(on: self) { owner, server in
            owner.selectedServer = server
        }
        viewModel.libraries.observeAsync(on: self) { owner, libs in
            owner.libraries = libs
        }
        viewModel.selectedLibrary.observeAsync(on: self) { owner, lib in
            owner.selectedLibrary = lib
        }
        viewModel.filteredItems.observeAsync(on: self) { owner, items in
            owner.filteredItems = items
        }
        viewModel.isLoading.observeAsync(on: self) { owner, loading in
            owner.isLoading = loading.boolValue
        }
        viewModel.isRefreshing.observeAsync(on: self) { owner, refreshing in
            owner.isRefreshing = refreshing.boolValue
        }
        viewModel.error.observeAsync(on: self) { owner, err in
            owner.error = err
        }
    }

    func selectServer(_ server: MediaServerIntegration) {
        viewModel.selectServer(server: server)
    }

    func selectLibrary(_ library: UnifiedMediaLibrary?) {
        viewModel.selectLibrary(library: library)
    }

    func updateSearchQuery(_ query: String) {
        self.searchQuery = query
        viewModel.setSearchQuery(query: query)
    }

    func toggleWatched(_ item: UnifiedMediaItem) {
        viewModel.toggleWatched(item: item)
    }

    func refresh() {
        viewModel.refresh()
    }
}

