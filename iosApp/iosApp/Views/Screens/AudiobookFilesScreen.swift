//
//  AudiobookFilesScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-19.
//

import SwiftUI
import Shared

struct AudiobookFilesScreen: View {
    let audiobook: Audiobook
    
    @ObservedObject private var viewModel: AudiobookFilesViewModelS
    @Environment(\.dismiss) private var dismiss
    
    init(audiobookJson: String) {
        let audiobook = Audiobook.companion.fromJson(value: audiobookJson)
        self.audiobook = audiobook
        self.viewModel = AudiobookFilesViewModelS(audiobookId: audiobook.id?.int64Value ?? 0)
    }
    
    var body: some View {
        List {
            Section(header: Text(MR.strings().files.localized())) {
                if viewModel.uiState.files.isEmpty {
                    Text(MR.strings().no_files.localized())
                        .foregroundColor(.secondary)
                } else {
                    ForEach(viewModel.uiState.files, id: \.id) { file in
                        AudiobookFileCard(file: file)
                    }
                }
            }
            
            Section(header: Text(MR.strings().history.localized())) {
                if viewModel.uiState.history.isEmpty {
                    Text(MR.strings().no_history.localized())
                        .foregroundColor(.secondary)
                } else {
                    ForEach(viewModel.uiState.history, id: \.id) { historyItem in
                        HistoryItemView(item: historyItem)
                    }
                }
            }
        }
        .navigationTitle(audiobook.title ?? "")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    viewModel.refreshHistory()
                }) {
                    if viewModel.uiState.isRefreshing {
                        ProgressView()
                    } else {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
        }
    }
}
