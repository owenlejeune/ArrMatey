//
//  SeerrViewRequestSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrViewRequestSheet: View {
    let details: RequestMediaDetails
    let requestOverride: MediaRequest?
    let serviceDetails: ServiceDetails?
    let onDismissRequest: () -> Void
    let onApproveRequest: (Int64, Int64?, String?, Int64?, [Int32]?) -> Void
    let onDeclineRequest: (Int64) -> Void

    @State private var selectedProfileId: Int64? = nil
    @State private var selectedRootFolder: String? = nil
    @State private var selectedSeasons: Set<Int32> = []

    private var request: MediaRequest? {
        return requestOverride ?? details.mediaInfo?.requests.first { $0.status == 1 }
    }

    init(
        details: RequestMediaDetails,
        request: MediaRequest? = nil,
        serviceDetails: ServiceDetails? = nil,
        onDismissRequest: @escaping () -> Void,
        onApproveRequest: @escaping (Int64, Int64?, String?, Int64?, [Int32]?) -> Void,
        onDeclineRequest: @escaping (Int64) -> Void
    ) {
        self.details = details
        self.requestOverride = request
        self.serviceDetails = serviceDetails
        self.onDismissRequest = onDismissRequest
        self.onApproveRequest = onApproveRequest
        self.onDeclineRequest = onDeclineRequest

        let targetRequest = request ?? details.mediaInfo?.requests.first(where: { $0.status == 1 })
        if let targetRequest = targetRequest {
            let defaultProfileId = serviceDetails.map { Int64($0.server.activeProfileId) }
            _selectedProfileId = State(initialValue: targetRequest.profileId?.int64Value ?? defaultProfileId)
            _selectedRootFolder = State(initialValue: targetRequest.rootFolder ?? serviceDetails?.server.activeDirectory)
            _selectedSeasons = State(initialValue: Set(targetRequest.seasons.map { $0.seasonNumber }))
        }
    }

    init(details: RequestMediaDetails, viewModel: UnifiedMediaDetailsViewModelS, onDismissRequest: @escaping () -> Void) {
        self.init(
            details: details,
            request: nil,
            serviceDetails: viewModel.serviceDetails,
            onDismissRequest: onDismissRequest,
            onApproveRequest: { requestId, profileId, rootFolder, languageProfileId, seasons in
                let seasonsKotlin = seasons?.map { KotlinInt(value: $0) }
                viewModel.approveRequest(
                    requestId: requestId,
                    profileId: profileId,
                    rootFolder: rootFolder,
                    languageProfileId: languageProfileId,
                    seasons: seasonsKotlin
                )
            },
            onDeclineRequest: { requestId in
                viewModel.declineRequest(requestId: requestId)
            }
        )
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
                            ForEach(serviceDetails?.profiles ?? [], id: \.id) { profile in
                                Text(profile.name).tag(profile.id as Int64?)
                            }
                        }

                        Picker(MR.strings().root_folder.localized(), selection: $selectedRootFolder) {
                            Text(MR.strings().unknown.localized()).tag(nil as String?)
                            ForEach(serviceDetails?.rootFolders ?? [], id: \.path) { folder in
                                Text(folder.path).tag(folder.path as String?)
                            }
                        }
                    }

                    Section {
                        Button(action: {
                            let seasonsArray = details is TvDetails ? Array(selectedSeasons) : nil
                            onApproveRequest(
                                request.id,
                                selectedProfileId,
                                selectedRootFolder,
                                request.languageProfileId?.int64Value,
                                seasonsArray
                            )
                            onDismissRequest()
                        }) {
                            Text(MR.strings().approve_request.localized())
                                .frame(maxWidth: .infinity)
                                .foregroundColor(.white)
                        }
                        .listRowBackground(Color.green)

                        Button(role: .destructive, action: {
                            onDeclineRequest(request.id)
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
