//
//  SeerrPersonDetailsScreen.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrPersonDetailsScreen: View {
    @StateObject private var viewModel: SeerrMediaDetailsViewModelS
    @EnvironmentObject private var navigationManager: NavigationManager

    private let columns = [
        GridItem(.adaptive(minimum: GridDensity.normal.iosSize), spacing: GridSpacing.medium.iosSpacing)
    ]
    
    init(personId: Int64) {
        _viewModel = StateObject(wrappedValue: SeerrMediaDetailsViewModelS(tmdbId: personId, requestType: .person))
    }
    
    var body: some View {
        ZStack(alignment: .topLeading) {
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
            .refreshable {
                viewModel.refreshDetails()
            }
        }
    }
    
    @ViewBuilder
    private func successContent(state: SeerrDetailsStateSuccess) -> some View {
        let item = state.item as! PersonDetails
        
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                PersonDetailsHeader(item: item, credits: viewModel.personCredits)
                
                VStack(alignment: .leading, spacing: 24) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(item.displayTitle)
                            .font(.title)
                            .bold()
                        
                        let birthday = item.birthday?.format(pattern: "MMMM d, yyyy") ?? MR.strings().unknown.localized()
                        let birthplace = item.placeOfBirth ?? MR.strings().unknown.localized()
                        Text(MR.strings().born_on.formatted(args: [birthday, birthplace]))
                            .font(.body)
                            .foregroundColor(.secondary)
                        
                        if !item.alsoKnownAs.isEmpty {
                            HStack(alignment: .top, spacing: 8) {
                                Text(MR.strings().also_known_as.localized())
                                    .font(.headline)
                                    .bold()
                                Text(item.alsoKnownAs.joined(separator: ", "))
                                    .font(.body)
                            }
                            .padding(.top, 4)
                        }
                    }
                    
                    if let bio = item.biography, !bio.isEmpty {
                        ItemDescriptionCard(overview: bio)
                    }
                }
                .padding(24)
                .padding(.top, 12)
                
                if let credits = viewModel.personCredits {
                    VStack(alignment: .leading, spacing: 24) {
                        creditsSection(title: MR.strings().appearances.localized(), icon: "film", items: credits.cast)
                        creditsSection(title: MR.strings().crew.localized(), icon: "gearshape", items: credits.crew)
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 24)
                }
            }
        }
        .ignoresSafeArea(edges: .top)
    }
    
    @ViewBuilder
    private func creditsSection(title: String, icon: String, items: [DiscoverResult]) -> some View {
        if !items.isEmpty {
            VStack(alignment: .leading, spacing: 12) {
                HStack(spacing: 8) {
                    Image(systemName: icon)
                        .font(.system(size: 20))
                    Text(title)
                        .font(.headline)
                        .bold()
                    Spacer()
                }
                
                LazyVGrid(columns: columns, alignment: .leading, spacing: 12) {
                    ForEach(items, id: \.id) { result in
                        DiscoverPosterItem(
                            item: result,
                            onItemClick: { item in
                                navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
                            },
                            includeCredits: true
                        )
                    }
                }
            }
        }
    }
}
