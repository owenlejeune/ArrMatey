//
//  MovieFilesScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-23.
//

import SwiftUI
import Shared

struct MovieFilesScreen: View {
    private let movie: ArrMovie
    
    @ObservedObject private var viewModel: MovieFilesViewModelS
    
    @Environment(\.dismiss) var dismiss
    
    init(json: String) {
        self.movie = ArrMediaCompanion().fromJson(value: json) as! ArrMovie
        self.viewModel = MovieFilesViewModelS(movieId: self.movie.id?.int64Value ?? 0)
    }
    
    private var uiState: MovieFilesState {
        viewModel.uiState
    }
    
    var body: some View {
        ZStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text(MR.strings().files.localized())
                        .font(.system(size: 22, weight: .medium))
                    
                    if let file = movie.movieFile {
                        MediaFileCard(file: file)
                    }
                    
                    MovieExtraFilesView(extraFiles: uiState.extraFiles)
                    
                    Text(MR.strings().history.localized())
                        .font(.system(size: 22, weight: .medium))
                    
                    if uiState.history.isEmpty {
                        Text(MR.strings().no_history.localized())
                            .foregroundColor(.secondary)
                    } else {
                        ForEach(uiState.history, id: \.id) { item in
                            HistoryItemView(item: item)
                        }
                    }
                }
                .padding(.horizontal, 24)
            }
            .refreshable {
                viewModel.refreshHistory()
            }
        }
    }
}
