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
            VStack(alignment: .leading, spacing: 12) {
                HStack(spacing: 4) {
                    Text(MR.strings().bazarr_subtitles.localized())
                        .font(.title3)
                        .bold()
                    
                    if let icon = InstanceType.bazarr.tabIcon {
                        Image(uiImage: icon.toUIImage()!)
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 24, height: 24)
                    }
                    
                    Spacer()
                    
                    Button {
                        showSearch = true
                    } label: {
                        HStack(spacing: 4) {
                            Image(systemName: "magnifyingglass")
                            Text(MR.strings().bazarr_search_subtitles.localized())
                        }
                        .font(.subheadline)
                    }
                    .buttonStyle(.bordered)
                }
                .frame(maxWidth: .infinity)
                
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
            HStack {
                Spacer()
                ProgressView()
                Spacer()
            }
            .padding(16)
        } else if let error = state as? BazarrSubtitlesUiStateError {
            Text(error.message).foregroundStyle(.red)
        } else if let success = state as? BazarrSubtitlesUiStateSuccess {
            if !success.embedded.isEmpty {
                EmbeddedSubtitlesCard(embedded: success.embedded)
            }
            
            ForEach(success.present, id: \.self) { subtitle in
                PresentSubtitleRow(subtitle: subtitle, onDelete: { viewModel.delete(subtitle) })
            }

            ForEach(success.missing, id: \.self) { language in
                MissingSubtitleRow(language: language, onAutoSearch: { viewModel.autoSearch(language) })
            }

            if viewModel.operationState is OperationStatusInProgress {
                HStack(spacing: 8) {
                    ProgressView()
                    Text(MR.strings().bazarr_auto_search.localized())
                        .font(.caption)
                }
                .padding(.top, 8)
            }
        }
    }
}

private struct EmbeddedSubtitlesCard: View {
    let embedded: [BazarrSubtitle]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(MR.strings().bazarr_embedded_count.formatted(args: [embedded.count]))
                .font(.subheadline)
                .fontWeight(.semibold)
            
            FlowLayout(spacing: 4) {
                ForEach(embedded, id: \.self) { subtitle in
                    SubtitleChip(label: subtitleLabel(subtitle))
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.vertical, 12)
        .padding(.horizontal, 18)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(12)
    }
}

private struct PresentSubtitleRow: View {
    let subtitle: BazarrSubtitle
    let onDelete: () -> Void
    
    var body: some View {
        HStack(spacing: 8) {
            VStack(alignment: .leading, spacing: 2) {
                Text(subtitle.name)
                    .font(.subheadline)
                if let path = subtitle.path {
                    Text(path)
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                        .truncationMode(.middle)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            
            SubtitleChip(label: subtitleLabel(subtitle))
            
            if subtitle.isExternal {
                Button(action: onDelete) {
                    Image(systemName: "trash")
                        .foregroundStyle(.red)
                }
                .buttonStyle(.borderless)
            }
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 18)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(12)
    }
}

private struct MissingSubtitleRow: View {
    let language: BazarrSubtitleLanguage
    let onAutoSearch: () -> Void
    
    var body: some View {
        Button(action: onAutoSearch) {
            HStack(spacing: 8) {
                SubtitleChip(label: chipLabel(language))
                Text(MR.strings().missing.localized())
                    .foregroundStyle(.red)
                    .font(.subheadline)
                
                Spacer()
                
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(.secondary)
            }
        }
        .buttonStyle(.plain)
        .padding(.vertical, 12)
        .padding(.horizontal, 18)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(12)
    }
}

private func subtitleLabel(_ subtitle: BazarrSubtitle) -> String {
    var label = subtitle.code2.isEmpty ? subtitle.name : subtitle.code2.uppercased()
    if subtitle.forced { label += " · Forced" }
    if subtitle.hi { label += " · HI" }
    return label
}
