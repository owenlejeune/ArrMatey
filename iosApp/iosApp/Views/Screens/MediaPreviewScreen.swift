//
//  MediaPreviewScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-29.
//

import SwiftUI
import Shared

struct MediaPreviewScreen: View {
    private let media: ArrMedia
    private let type: InstanceType
    
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var navigation: NavigationManager

    @ObservedObject private var viewModel: MediaPreviewViewModelS

    @State private var sheetPresented: Bool = false
    
    init(json: String, type: InstanceType) {
        self.type = type
        self.media = ArrMediaCompanion().fromJson(value: json)
        self.viewModel = MediaPreviewViewModelS(type: type)
    }
    
    private var lastAddedItemId: Int64? {
        viewModel.lastAddedItemId
    }
    
    private var addItemStatus: OperationStatus {
        viewModel.addItemStatus
    }
    
    private var qualityProfiles: [QualityProfile] {
        viewModel.qualityProfiles
    }
    
    private var rootFolders: [RootFolder] {
        viewModel.rootFolders
    }
    
    private var tags: [Tag] {
        viewModel.tags
    }
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                MediaDetailsHeader(item: media, type: type)
                    .frame(height: 400)

                VStack(alignment: .leading, spacing: 12) {
                    if let airingString = makeAiringString(for: media) {
                        Text(airingString)
                            .font(.system(size: 20, weight: .medium))
                            .foregroundColor(.themePrimary)
                    }

                    ItemDescriptionCard(overview: media.overview)
                }
                .padding(.horizontal, 24)
            }
            .frame(alignment: .top)
        }
        .ignoresSafeArea(edges: .top)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button(MR.strings().add.localized(), systemImage: "plus") {
                    sheetPresented = true
                }
            }
        }
        .onChange(of: lastAddedItemId) { _, newValue in
            if let id = newValue {
                sheetPresented = false
                navigation.replaceCurrent(with: .details(id: id, type: type), for: type)
            }
        }
        .sheet(isPresented: $sheetPresented) {
            addMediaSheet()
        }
    }
    
    @ViewBuilder
    private func addMediaSheet() -> some View {
        switch media {
        case let series as ArrSeries:
            AddSeriesForm(
                series: series,
                addItemStatus: addItemStatus,
                qualityProfiles: qualityProfiles,
                rootFolders: rootFolders,
                tags: tags,
                onAddItem: { item, searchOnAdd in
                    viewModel.addItem(item, searchOnAdd)
                },
                onDismiss: { sheetPresented = false }
            )
                .presentationDetents([.medium])
                .presentationBackground(.ultraThinMaterial)
        case let movie as ArrMovie:
            AddMovieForm(
                movie: movie,
                addItemStatus: addItemStatus,
                qualityProfiles: qualityProfiles,
                rootFolders: rootFolders,
                tags: tags,
                onAddItem: { item, searchOnAdd in
                    viewModel.addItem(item, searchOnAdd)
                },
                onDismiss: { sheetPresented = false }
            )
                .presentationDetents([.medium])
                .presentationBackground(.ultraThinMaterial)
        case let artist as Arrtist:
            AddArtistForm(
                artist: artist,
                addItemStatus: addItemStatus,
                qualityProfiles: qualityProfiles,
                rootFolders: rootFolders,
                tags: tags,
                onAddItem: { item, searchOnAdd in
                    viewModel.addItem(item, searchOnAdd)
                },
                onDismiss: { sheetPresented = false }
            )
            .presentationDetents([.medium])
            .presentationBackground(.ultraThinMaterial)
        default: EmptyView()
        }
    }

    private func makeAiringString(for item: ArrMedia) -> String? {
        switch item {
        case let series as ArrSeries:
            if series.status == .continuing {
                if let airing = series.nextAiring?.format(pattern: "HH:mm MMMM d, yyyy") {
                    return "\(MR.strings().airing_next.localized()) \(airing)"
                } else {
                    return MR.strings().continuing_unknown.localized()
                }
            } else { return nil }
        case let movie as ArrMovie:
            if let inCinemas = movie.inCinemas?.format(pattern: "HH:mm MMMM d, yyyy"), movie.digitalRelease == nil, movie.physicalRelease == nil {
                return "\(MR.strings().in_cinemas.localized()) \(inCinemas)"
            } else {
                return nil
            }
        default: return nil
        }
    }
    
}
