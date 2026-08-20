//
//  SeerrViewRequestSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrViewRequestSheet: View {
    let details: RequestMediaDetails
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    let onDismissRequest: () -> Void
    
    @State private var selectedProfileId: Int64? = nil
    @State private var selectedRootFolder: String? = nil
    @State private var selectedSeasons: Set<Int32> = []
    
    private var request: MediaRequest? {
        return details.mediaInfo?.requests.first { $0.status == 1 }
    }
    
    init(details: RequestMediaDetails, viewModel: UnifiedMediaDetailsViewModelS, onDismissRequest: @escaping () -> Void) {
        self.details = details
        self.viewModel = viewModel
        self.onDismissRequest = onDismissRequest
        
        if let request = details.mediaInfo?.requests.first(where: { $0.status == 1 }) {
            let defaultProfileId = viewModel.serviceDetails.map { Int64($0.server.activeProfileId) }
            _selectedProfileId = State(initialValue: request.profileId?.int64Value ?? defaultProfileId)
            _selectedRootFolder = State(initialValue: request.rootFolder ?? viewModel.serviceDetails?.server.activeDirectory)
            _selectedSeasons = State(initialValue: Set(request.seasons.map { $0.seasonNumber }))
        }
    }
    
    var body: some View {
        if let request = request {
            NavigationStack {
                Form {
                    Section {
                        HStack(spacing: 16) {
                            GenericPosterItem(posterUrl: details.fullPosterPath)
                                .frame(width: 80)
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text(MR.strings().pending_request.localized().uppercased())
                                    .font(.caption.bold())
                                    .foregroundColor(.accentColor)
                                
                                Text(details.displayTitle)
                                    .font(.headline)
                                    .lineLimit(2)
                            }
                        }
                        .padding(.vertical, 8)
                    }
                    
                    if let tvDetails = details as? TvDetails {
                        Section(header: Text(MR.strings().seasons_header.localized())) {
                            Toggle(MR.strings().all_seasons.localized(), isOn: Binding(
                                get: { selectedSeasons.count == tvDetails.seasons.count },
                                set: { isOn in
                                    if isOn {
                                        selectedSeasons = Set(tvDetails.seasons.map { $0.seasonNumber })
                                    } else {
                                        selectedSeasons = []
                                    }
                                }
                            ))
                            
                            ForEach(tvDetails.seasons, id: \.seasonNumber) { season in
                                Toggle(isOn: Binding(
                                    get: { selectedSeasons.contains(season.seasonNumber) },
                                    set: { isOn in
                                        if isOn {
                                            selectedSeasons.insert(season.seasonNumber)
                                        } else {
                                            selectedSeasons.remove(season.seasonNumber)
                                        }
                                    }
                                )) {
                                    VStack(alignment: .leading) {
                                        Text(season.seasonNumber == 0 ? MR.strings().specials.localized() : MR.strings().season_label.formatted(args: [season.seasonNumber]))
                                        Text(MR.plurals().episodes.localized(season.episodeCount))
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                    }
                                }
                            }
                        }
                    }
                    
                    Section(header: Text(MR.strings().requested_by.localized())) {
                        HStack(spacing: 12) {
                            AsyncImage(url: URL(string: request.requestedBy.avatar)) { image in
                                image.resizable()
                            } placeholder: {
                                Image(systemName: "person.circle.fill")
                                    .foregroundColor(.secondary)
                            }
                            .frame(width: 40, height: 40)
                            .clipShape(Circle())
                            
                            VStack(alignment: .leading) {
                                Text(request.requestedBy.displayName)
                                    .font(.body.bold())
                                Text(request.requestedBy.email)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                    
                    Section(header: Text(MR.strings().advanced.localized())) {
                        Picker(MR.strings().quality_profile.localized(), selection: $selectedProfileId) {
                            Text(MR.strings().unknown.localized()).tag(nil as Int64?)
                            ForEach(viewModel.serviceDetails?.profiles ?? [], id: \.id) { profile in
                                Text(profile.name).tag(profile.id as Int64?)
                            }
                        }
                        
                        Picker(MR.strings().root_folder.localized(), selection: $selectedRootFolder) {
                            Text(MR.strings().unknown.localized()).tag(nil as String?)
                            ForEach(viewModel.serviceDetails?.rootFolders ?? [], id: \.path) { folder in
                                Text(folder.path).tag(folder.path as String?)
                            }
                        }
                    }
                    
                    Section {
                        Button(action: {
                            let seasons = details is TvDetails ? selectedSeasons.map { KotlinInt(value: $0) } : nil
                            viewModel.approveRequest(
                                requestId: request.id,
                                profileId: selectedProfileId,
                                rootFolder: selectedRootFolder,
                                languageProfileId: request.languageProfileId?.int64Value,
                                seasons: seasons
                            )
                            onDismissRequest()
                        }) {
                            Text(MR.strings().approve_request.localized())
                                .frame(maxWidth: .infinity)
                                .foregroundColor(.white)
                        }
                        .listRowBackground(Color.green)
                        
                        Button(role: .destructive, action: {
                            viewModel.declineRequest(requestId: request.id)
                            onDismissRequest()
                        }) {
                            Text(MR.strings().decline_request.localized())
                                .frame(maxWidth: .infinity)
                        }
                    }
                }
                .navigationTitle(MR.strings().pending_request.localized())
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .navigationBarLeading) {
                        Button(MR.strings().cancel.localized()) {
                            onDismissRequest()
                        }
                    }
                }
            }
        } else {
            EmptyView()
        }
    }
}
