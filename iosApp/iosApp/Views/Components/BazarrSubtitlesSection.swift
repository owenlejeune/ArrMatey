//
//  BazarrSubtitlesSection.swift
//  iosApp
//

import SwiftUI
import Shared

/// "Subtitles" section embedded in the Sonarr episode and Radarr movie detail screens,
/// backed by the selected Bazarr instance. Renders nothing when Bazarr isn't configured
/// or isn't tracking the item, so it is safe to drop into any detail screen.
struct BazarrSubtitlesSection: View {
    let target: BazarrMediaTarget
    @StateObject private var viewModel: BazarrMediaSubtitlesViewModelS
    @State private var showSearch = false

    init(target: BazarrMediaTarget) {
        self.target = target
        _viewModel = StateObject(wrappedValue: BazarrMediaSubtitlesViewModelS(target: target))
    }

    var body: some View {
        let state = viewModel.state
        if state is BazarrSubtitlesUiStateNoInstance || state is BazarrSubtitlesUiStateNotTracked {
            EmptyView()
        } else {
            VStack(alignment: .leading, spacing: 8) {
                Text(MR.strings().bazarr_subtitles.localized()).font(.title3).bold()
                content(state)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .sheet(isPresented: $showSearch, onDismiss: { viewModel.load() }) {
                BazarrSubtitleSearchSheet(target: target)
            }
        }
    }

    @ViewBuilder private func content(_ state: BazarrSubtitlesUiState) -> some View {
        if state is BazarrSubtitlesUiStateLoading {
            ProgressView()
        } else if let error = state as? BazarrSubtitlesUiStateError {
            Text(error.message).foregroundStyle(.red)
        } else if let success = state as? BazarrSubtitlesUiStateSuccess {
            if success.present.isEmpty {
                Text(MR.strings().bazarr_no_subtitles.localized()).foregroundStyle(.secondary)
            } else {
                ForEach(Array(success.present.enumerated()), id: \.offset) { _, subtitle in
                    HStack {
                        SubtitleChip(label: subtitleLabel(subtitle))
                        Text(subtitle.name).font(.subheadline)
                        Spacer()
                        if subtitle.isExternal {
                            Button { viewModel.delete(subtitle) } label: {
                                Image(systemName: "trash").foregroundStyle(.red)
                            }
                            .buttonStyle(.borderless)
                        }
                    }
                }
            }

            if !success.missing.isEmpty {
                Text(MR.strings().bazarr_missing_subtitles.localized()).font(.subheadline)
                ForEach(Array(success.missing.enumerated()), id: \.offset) { _, language in
                    HStack {
                        SubtitleChip(label: chipLabel(language))
                        Button(MR.strings().bazarr_auto_search.localized()) {
                            viewModel.autoSearch(language)
                        }
                        .buttonStyle(.borderless)
                        Spacer()
                    }
                }
            }

            Button {
                showSearch = true
            } label: {
                Label(MR.strings().bazarr_search_subtitles.localized(), systemImage: "magnifyingglass")
            }
            .buttonStyle(.bordered)

            if viewModel.operationState is OperationStatusInProgress {
                ProgressView()
            }
        }
    }
}

private func subtitleLabel(_ subtitle: BazarrSubtitle) -> String {
    var label = subtitle.code2.isEmpty ? subtitle.name : subtitle.code2.uppercased()
    if subtitle.forced { label += " · Forced" }
    if subtitle.hi { label += " · HI" }
    return label
}
