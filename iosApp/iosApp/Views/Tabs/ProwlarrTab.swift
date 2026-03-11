//
//  ProwlarrTab.swift
//  iosApp
//
//  Created by Bryan Moon on 2026-03-04.
//

import SwiftUI
import Shared

struct ProwlarrTab: View {
    @Environment(\.navigationContext) private var context
    
    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack {
                ProwlarrTabContent()
            }
        case .launcher:
            ProwlarrTabContent()
        }
    }
}

struct ProwlarrTabContent: View {
    @State private var selectedSegment = 0
    @ObservedObject private var viewModel = ProwlarrIndexersViewModelS()
    
    var body: some View {
        VStack(spacing: 0) {
            Picker("", selection: $selectedSegment) {
                Text(MR.strings().indexers.localized()).tag(0)
                Text(MR.strings().search.localized()).tag(1)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            
            if selectedSegment == 0 {
                ProwlarrIndexersView(viewModel: viewModel)
            } else {
                ProwlarrSearchView()
            }
        }
        .navigationTitle(MR.strings().prowlarr.localized())
        .toolbar {
            if selectedSegment == 0 {
                IndexerSortMenu(
                    sortBy: Binding(
                        get: { viewModel.indexerSortState.sortBy },
                        set: { viewModel.updateSortBy($0) }
                    ),
                    sortOrder: Binding(
                        get: { viewModel.indexerSortState.sortOrder },
                        set: { viewModel.updateSortOrder($0) }
                    )
                )
            }
        }
    }
}
