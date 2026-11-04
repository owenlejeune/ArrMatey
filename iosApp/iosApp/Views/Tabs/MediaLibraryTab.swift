//
//  MediaLibraryTab.swift
//  iosApp
//

import SwiftUI
import Shared

struct MediaLibraryTab: View {
    @StateObject private var viewModel = MediaServerLibraryViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager
    @State private var searchQuery: String = ""

    private let columns = [
        GridItem(.adaptive(minimum: 105, maximum: 140), spacing: 12)
    ]

    var body: some View {
        Group {
            if viewModel.configuredServers.isEmpty && !viewModel.isLoading {
                ContentUnavailableView {
                    Label(MR.strings().no_media_servers_configured.localized(), systemImage: "tv.slash")
                } description: {
                    Text(MR.strings().media_servers.localized())
                } actions: {
                    Button(MR.strings().settings.localized()) {
                        navigationManager.openSettings()
                    }
                    .buttonStyle(.borderedProminent)
                }
            } else {
                VStack(spacing: 0) {
                    // Server Picker / Libraries Row
                    if viewModel.configuredServers.count > 1 || !viewModel.libraries.isEmpty {
                        headerFilterRow
                    }

                    if viewModel.isLoading && viewModel.filteredItems.isEmpty {
                        ProgressView()
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else if viewModel.filteredItems.isEmpty {
                        ContentUnavailableView.search(text: searchQuery)
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else {
                        ScrollView {
                            LazyVGrid(columns: columns, spacing: 16) {
                                ForEach(viewModel.filteredItems, id: \.id) { item in
                                    mediaItemCell(for: item)
                                }
                            }
                            .padding(16)
                        }
                        .refreshable {
                            viewModel.refresh()
                        }
                    }
                }
            }
        }
        .navigationTitle(MR.strings().media_library.localized())
        .navigationBarTitleDisplayMode(.inline)
        .searchable(text: $searchQuery, placement: .navigationBarDrawer(displayMode: .automatic))
        .onChange(of: searchQuery) { _, query in
            viewModel.updateSearchQuery(query)
        }
    }

    @ViewBuilder
    private var headerFilterRow: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                if viewModel.configuredServers.count > 1 {
                    ForEach(viewModel.configuredServers, id: \.id) { server in
                        let isSelected = viewModel.selectedServer?.id == server.id
                        Button {
                            viewModel.selectServer(server)
                        } label: {
                            HStack(spacing: 6) {
                                server.serverType.icon.toImage(renderingMode: .template)
                                    .resizable()
                                    .aspectRatio(contentMode: .fit)
                                    .frame(width: 14, height: 14)
                                    .foregroundColor(server.serverType.brandColor.toSwiftUI())
                                Text(server.label)
                                    .font(.system(size: 13, weight: isSelected ? .bold : .medium))
                            }
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(
                                RoundedRectangle(cornerRadius: 16, style: .continuous)
                                    .fill(isSelected ? server.serverType.brandColor.toSwiftUI().opacity(0.15) : Color.primary.opacity(0.05))
                            )
                            .overlay(
                                RoundedRectangle(cornerRadius: 16, style: .continuous)
                                    .stroke(isSelected ? server.serverType.brandColor.toSwiftUI().opacity(0.4) : Color.clear, lineWidth: 1)
                            )
                        }
                        .buttonStyle(.plain)
                    }

                    Divider()
                        .frame(height: 20)
                }

                if !viewModel.libraries.isEmpty {
                    // All libraries pill
                    let isAllSelected = viewModel.selectedLibrary == nil
                    Button {
                        viewModel.selectLibrary(nil)
                    } label: {
                        Text(MR.strings().all.localized())
                            .font(.system(size: 13, weight: isAllSelected ? .bold : .medium))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(
                                RoundedRectangle(cornerRadius: 16, style: .continuous)
                                    .fill(isAllSelected ? Color.themePrimary.opacity(0.15) : Color.primary.opacity(0.05))
                            )
                            .overlay(
                                RoundedRectangle(cornerRadius: 16, style: .continuous)
                                    .stroke(isAllSelected ? Color.themePrimary.opacity(0.4) : Color.clear, lineWidth: 1)
                            )
                    }
                    .buttonStyle(.plain)

                    ForEach(viewModel.libraries, id: \.id) { lib in
                        let isSelected = viewModel.selectedLibrary?.id == lib.id
                        Button {
                            viewModel.selectLibrary(lib)
                        } label: {
                            Text(lib.title)
                                .font(.system(size: 13, weight: isSelected ? .bold : .medium))
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(
                                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                                        .fill(isSelected ? Color.themePrimary.opacity(0.15) : Color.primary.opacity(0.05))
                                )
                                .overlay(
                                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                                        .stroke(isSelected ? Color.themePrimary.opacity(0.4) : Color.clear, lineWidth: 1)
                                )
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
        }
    }

    @ViewBuilder
    private func mediaItemCell(for item: UnifiedMediaItem) -> some View {
        Button {
            navigationManager.navigateToMediaServerItem(item: item)
        } label: {
            VStack(alignment: .leading, spacing: 4) {
                GenericPosterItem(
                    posterUrl: item.posterUrl,
                    aspectRatio: .poster,
                    posterHeight: nil,
                    additionalContent: {
                        ZStack(alignment: .topTrailing) {
                            Color.clear
                            MediaServerBadgeOverlay(
                                serverType: item.serverType,
                                iconSize: 12,
                                innerPadding: 3,
                                borderWidth: 1
                            )
                            .padding(4)
                        }
                        if item.played {
                            ZStack(alignment: .topLeading) {
                                Color.clear
                                Image(systemName: "checkmark.circle.fill")
                                    .font(.system(size: 13))
                                    .foregroundColor(Color(red: 0x50 / 255.0, green: 0xD2 / 255.0, blue: 0x7D / 255.0))
                                    .padding(4)
                            }
                        }
                    }
                )

                Text(item.title)
                    .font(.system(size: 12, weight: .semibold))
                    .lineLimit(1)
                    .foregroundColor(.primary)

                if let year = item.year?.stringValue {
                    Text(year)
                        .font(.system(size: 11))
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
            }
        }
        .buttonStyle(.plain)
    }
}

