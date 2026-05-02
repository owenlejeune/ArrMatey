//
//  AuthorFilesScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-02.
//

import SwiftUI
import Shared

struct AuthorFilesScreen: View {
    let author: Author
    
    @ObservedObject private var viewModel: AuthorFilesViewModelS
    @Environment(\.dismiss) private var dismiss
    
    init(authorJson: String) {
        let author = Author.companion.fromJson(value: authorJson)
        self.author = author
        self.viewModel = AuthorFilesViewModelS(authorId: author.id?.int64Value ?? 0)
    }
    
    var body: some View {
        List {
            Section(header: Text(MR.strings().files.localized())) {
                if viewModel.uiState.files.isEmpty {
                    Text(MR.strings().no_files.localized())
                        .foregroundColor(.secondary)
                } else {
                    ForEach(viewModel.uiState.files, id: \.id) { file in
                        BookFileCard(file: file)
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
        .navigationTitle(author.title ?? "")
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
