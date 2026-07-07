//
//  InteractiveSearchScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-24.
//

import Shared
import SwiftUI

struct InteractiveSearchScreen: View {
    private let type: InstanceType
    private let releaseParams: ReleaseParams

    @ObservedObject private var viewModel: InteractiveSearchViewModelS
    @ObservedObject private var instancesViewModel: InstancesViewModelS

    @EnvironmentObject private var navigationManager: NavigationManager

    @State private var searchPresented: Bool = false
    @State private var confirmRelease: ArrRelease? = nil

    private var filterBinding: Binding<ReleaseFilterBy>
    private var filterLanguageBinding: Binding<Language?>
    private var filterQualityBinding: Binding<QualityInfo?>
    private var filterCustomFormatBinding: Binding<CustomFormat?>
    private var filterProtocolBinding: Binding<ReleaseProtocol?>
    private var filterIndexerBinding: Binding<String?>

    init(type: InstanceType, releaseParams: ReleaseParams, defaultFilter: ReleaseFilterBy = .any) {
        self.type = type
        self.releaseParams = releaseParams

        let vm = InteractiveSearchViewModelS(type: type, defaultFilter: defaultFilter)
        self.viewModel = vm
        self.instancesViewModel = InstancesViewModelS(type: type)

        self.filterBinding = Binding(
            get: { vm.filterUiState.filterBy },
            set: { vm.setFilterby($0) }
        )
        self.filterLanguageBinding = Binding(
            get: { vm.filterUiState.language },
            set: { vm.setFilterLanguage($0) }
        )
        self.filterQualityBinding = Binding(
            get: { vm.filterUiState.quality },
            set: { vm.setFilterQuality($0) }
        )
        self.filterCustomFormatBinding = Binding(
            get: { vm.filterUiState.customFormat },
            set: { vm.setFilterCustomFormat($0) }
        )
        self.filterProtocolBinding = Binding(
            get: { vm.filterUiState.protocol },
            set: { vm.setFilterProtocol($0) }
        )
        self.filterIndexerBinding = Binding(
            get: { vm.filterUiState.indexer },
            set: { vm.setFilterIndexer($0) }
        )
    }

    var body: some View {
        ZStack {
            contentForState()
        }
        .searchable(text: Binding(get: { viewModel.searchQuery }, set: { viewModel.updateSearchQuery($0) }), isPresented: $searchPresented, prompt: MR.strings().search.localized())
        .onAppear {
            viewModel.getRelease(releaseParams)
        }
        .alert(MR.strings().grab_release_title.localized(), isPresented: Binding(get: { confirmRelease != nil }, set: { if !$0 { confirmRelease = nil }})) {
            Button(MR.strings().grab.localized()) {
                if let release = confirmRelease {
                    viewModel.downloadRelease(release, true)
                }
                confirmRelease = nil
            }
            Button(MR.strings().cancel.localized(), role: .cancel) { confirmRelease = nil }
        } message: {
            if let release = confirmRelease {
                Text(MR.strings().confirm_grab_release.formatted(args: [release.title]))
            }
        }
        .toolbar {
            toolbarContent
        }
    }

    @ViewBuilder
    private func contentForState() -> some View {
        switch viewModel.releaseUiState {
        case is ReleaseLibraryLoading:
            ProgressView()
                .progressViewStyle(.circular)
        case let success as ReleaseLibrarySuccess:
            ScrollView {
                LazyVStack(spacing: 18) {
                    ForEach(success.items, id: \.guid) { item in
                        let isLoading = (viewModel.downloadReleaseState as? DownloadStateLoading)?.guid == item.guid

                        ReleaseItemView(item: item, animate: isLoading, onItemClick: { release in
                            if release.downloadAllowed {
                                viewModel.downloadRelease(release, false)
                            } else {
                                confirmRelease = release
                            }
                        })
                    }
                }
                .padding(.horizontal, 18)
            }
        case let error as ReleaseLibraryError:
            ErrorView(
                errorType: error.type,
                message: error.message,
                onOpenSettings: {
                    navigationManager.maybeEditInstance(of: type, instancesViewModel.instancesState.selectedInstance)
                },
                onRetry: {
                    viewModel.getRelease(releaseParams)
                }
            )
        default:
            EmptyView()
        }
    }

    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .primaryAction) {
            ReleaseSortByPickerMenu(
                sortBy: Binding(
                    get: { viewModel.filterUiState.sortBy },
                    set: { viewModel.setSortBy($0) }
                ),
                sortOrder: Binding(
                    get: { viewModel.filterUiState.sortOrder },
                    set: { viewModel.setSortOrder($0) }
                )
            )
            .menuIndicator(.hidden)
        }

        ToolbarItem(placement: .primaryAction) {
            ReleaseFilterByPickerMenu(
                filterBy: filterBinding,
                filterQuality: filterQualityBinding,
                filterLanguage: filterLanguageBinding,
                filterIndexer: filterIndexerBinding,
                filterProtocol: filterProtocolBinding,
                filterCustomFormat: filterCustomFormatBinding,
                type: type,
                languages: languages,
                indexers: indexers,
                qualities: qualities,
                protocols: protocols,
                customFormats: customFormats,
                customFilters: viewModel.customFilters,
                selectedCustomFilterId: viewModel.filterUiState.customFilterId?.int64Value,
                onCustomFilterChange: { id in
                    viewModel.setCustomFilter(id)
                }
            )
            .menuIndicator(.hidden)
        }
    }

    private var languages: Set<Language> {
        (viewModel.releaseUiState as? ReleaseLibrarySuccess)?.filterLanguages ?? Set()
    }

    private var qualities: Set<QualityInfo> {
        (viewModel.releaseUiState as? ReleaseLibrarySuccess)?.filterQualities ?? Set()
    }

    private var customFormats: Set<CustomFormat> {
        (viewModel.releaseUiState as? ReleaseLibrarySuccess)?.filterCustomFormats ?? Set()
    }

    private var protocols: Set<ReleaseProtocol> {
        (viewModel.releaseUiState as? ReleaseLibrarySuccess)?.filterProtocols ?? Set()
    }

    private var indexers: Set<String> {
        (viewModel.releaseUiState as? ReleaseLibrarySuccess)?.filterIndexers ?? Set()
    }

}
