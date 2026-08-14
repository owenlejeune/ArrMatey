//
//  SeerrPersonDetailsScreen.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrPersonDetailsScreen: View {
    @StateObject private var viewModel: SeerrMediaDetailsViewModelS
    @EnvironmentObject private var navigationManager: NavigationManager
    
    init(personId: Int64) {
        _viewModel = StateObject(wrappedValue: SeerrMediaDetailsViewModelS(tmdbId: personId, requestType: .person))
    }
    
    var body: some View {
        Group {
            if let state = viewModel.uiState as? SeerrDetailsStateSuccess {
                successContent(state: state)
            } else if let errorState = viewModel.uiState as? SeerrDetailsStateError {
                ErrorView(
                    errorType: errorState.errorType,
                    message: errorState.message ?? MR.strings().unknown.localized(),
                    onOpenSettings: {
                        if let id = viewModel.selectedInstance?.id {
                            navigationManager.goToEditInstance(of: .seerr, id)
                        }
                    },
                    onRetry: { viewModel.refreshDetails() }
                )
            } else {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .refreshable {
            viewModel.refreshDetails()
        }
    }
    
    @ViewBuilder
    private func successContent(state: SeerrDetailsStateSuccess) -> some View {
        let item = state.item
        
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                RequestMediaDetailsHeader(item: item)
                
                VStack(alignment: .leading, spacing: 12) {
                    Text(item.displayTitle)
                        .font(.title)
                        .bold()
                        .padding(.horizontal, 24)

                    if let person = item as? PersonDetails {
                        if !person.alsoKnownAs.isEmpty {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(MR.strings().also_known_as.localized())
                                    .font(.headline)
                                Text(person.alsoKnownAs.joined(separator: ", "))
                                    .font(.subheadline)
                            }
                            .padding(.horizontal, 24)
                        }
                    }
                    
                    if let overview = item.overview, !overview.isEmpty {
                        Text(overview)
                            .font(.body)
                            .padding(.horizontal, 24)
                    }

                    if let person = item as? PersonDetails {
                        personCreditsSection(person)
                    }
                    
                    infoSection(item: item, state: state)
                }
                .padding(.bottom, 24)
                .padding(.horizontal, 12)
            }
        }
        .ignoresSafeArea(edges: .top)
    }

    @ViewBuilder
    private func personCreditsSection(_ person: PersonDetails) -> some View {
        if let credits = viewModel.personCredits {
            if !credits.cast.isEmpty {
                DiscoverSection(
                    title: MR.strings().appearances.localized(),
                    icon: "play.rectangle",
                    data: PagedData(items: credits.cast, isLoading: false, isLoadingMore: false, error: nil),
                    onItemClick: { item in
                        navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
                    },
                    onLoadMore: { },
                    showOverlays: false
                )
            }

            if !credits.crew.isEmpty {
                DiscoverSection(
                    title: MR.strings().crew.localized(),
                    icon: "person.2.badge.gearshape",
                    data: PagedData(items: credits.crew, isLoading: false, isLoadingMore: false, error: nil),
                    onItemClick: { item in
                        navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
                    },
                    onLoadMore: { },
                    showOverlays: false
                )
            }
        }
    }
    
    private func infoSection(item: RequestMediaDetails, state: SeerrDetailsStateSuccess) -> some View {
        let infoItems: [(String, String)] = buildInfoItems(item: item)
        
        return Group {
            if !infoItems.isEmpty {
                VStack(spacing: 8) {
                    ForEach(infoItems, id: \.0) { label, value in
                        HStack(alignment: .top) {
                            Text(label)
                                .font(.subheadline.bold())
                                .frame(width: 120, alignment: .leading)
                            Text(value)
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                            Spacer()
                        }
                    }
                }
                .padding(16)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .padding(.horizontal, 24)
            }
        }
    }
    
    private func buildInfoItems(item: RequestMediaDetails) -> [(String, String)] {
        var items: [(String, String)] = []
        if let person = item as? PersonDetails {
            if let bday = person.birthday {
                items.append((MR.strings().status.localized(), "Born: " + bday.format(pattern: "MMM dd, yyyy")))
            }
            if let dday = person.deathday {
                items.append(("Died", dday.format(pattern: "MMM dd, yyyy")))
            }
            if let pob = person.placeOfBirth {
                items.append(("Place of Birth", pob))
            }
        }
        return items
    }
}
