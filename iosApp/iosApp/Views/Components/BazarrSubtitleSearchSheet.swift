//
//  BazarrSubtitleSearchSheet.swift
//  iosApp
//

import SwiftUI
import Shared

/// Modal listing manual provider subtitle results for an episode or movie, with a
/// per-row download action. Presented from the Wanted lists and the detail subtitle section.
struct BazarrSubtitleSearchSheet: View {
    let target: BazarrMediaTarget
    @StateObject private var viewModel: BazarrSubtitleSearchViewModelS
    @Environment(\.dismiss) private var dismiss

    init(target: BazarrMediaTarget) {
        self.target = target
        _viewModel = StateObject(wrappedValue: BazarrSubtitleSearchViewModelS(target: target))
    }

    var body: some View {
        NavigationStack {
            content
                .navigationTitle(MR.strings().bazarr_search_subtitles.localized())
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button { dismiss() } label: { Image(systemName: "xmark") }
                    }
                }
        }
    }

    @ViewBuilder private var content: some View {
        let state = viewModel.searchState
        if state is SubtitleSearchStateLoading || state is SubtitleSearchStateIdle {
            ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if let error = state as? SubtitleSearchStateError {
            Text(error.message).foregroundStyle(.red).padding()
        } else if let success = state as? SubtitleSearchStateSuccess {
            if success.results.isEmpty {
                Text(MR.strings().bazarr_no_results.localized())
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                List(Array(success.results.enumerated()), id: \.offset) { _, result in
                    BazarrSearchResultRow(
                        result: result,
                        status: viewModel.downloadStatus(for: result),
                        onDownload: { viewModel.download(result) }
                    )
                }
                .listStyle(.plain)
            }
        }
    }
}

private struct BazarrSearchResultRow: View {
    let result: ProviderSubtitle
    let status: OperationStatus?
    let onDownload: () -> Void

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 6) {
                    SubtitleChip(label: languageLabel(result))
                    Text(result.provider).font(.headline)
                    Text("· \(result.score)").font(.caption).foregroundStyle(.secondary)
                }
                if let info = result.releaseInfo.first {
                    Text(info).font(.caption).foregroundStyle(.secondary).lineLimit(2)
                }
            }
            Spacer()
            downloadControl
        }
    }

    @ViewBuilder private var downloadControl: some View {
        if status is OperationStatusInProgress {
            ProgressView()
        } else if status is OperationStatusSuccess {
            Image(systemName: "checkmark.circle.fill").foregroundStyle(.green)
        } else {
            Button(action: onDownload) {
                Image(systemName: "arrow.down.circle")
            }
            .buttonStyle(.bordered)
        }
    }
}

private func languageLabel(_ result: ProviderSubtitle) -> String {
    var label = result.language.uppercased()
    if result.isForced { label += " · Forced" }
    if result.isHearingImpaired { label += " · HI" }
    return label
}
