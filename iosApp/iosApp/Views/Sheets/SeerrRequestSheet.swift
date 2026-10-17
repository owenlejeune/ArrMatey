//
//  SeerrRequestSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrRequestSheet: View {
    let details: RequestMediaDetails
    let serviceDetails: ServiceDetails?
    let currentUser: SeerrUser?
    let users: [SeerrUser]
    let is4k: Bool
    let onDismiss: () -> Void
    let onSubmit: (Int64?, String?, Int64?, [KotlinInt]?, Bool, Int64?) -> Void
    
    @State private var selectedProfileId: Int64?
    @State private var selectedRootFolder: String?
    @State private var selectedUserId: Int64?
    @State private var selectedSeasons: Set<Int32> = []
    
    init(
        details: RequestMediaDetails,
        serviceDetails: ServiceDetails?,
        currentUser: SeerrUser?,
        users: [SeerrUser],
        is4k: Bool = false,
        onDismiss: @escaping () -> Void,
        onSubmit: @escaping (Int64?, String?, Int64?, [KotlinInt]?, Bool, Int64?) -> Void
    ) {
        self.details = details
        self.serviceDetails = serviceDetails
        self.currentUser = currentUser
        self.users = users
        self.is4k = is4k
        self.onDismiss = onDismiss
        self.onSubmit = onSubmit
        
        // Initialize state with default values from service details
        _selectedProfileId = State(initialValue: serviceDetails.map { Int64($0.server.activeProfileId) })
        _selectedRootFolder = State(initialValue: serviceDetails?.server.activeDirectory)
        _selectedUserId = State(initialValue: currentUser?.id)
        
        if let tvDetails = details as? TvDetails {
            _selectedSeasons = State(initialValue: Set(tvDetails.seasons.map { $0.seasonNumber }))
        }
    }
    
    private var requestType: RequestType {
        (details is TvDetails) ? .tv : .movie
    }
    
    private var permissions: SeerrRequestPermissions {
        SeerrPermissionPolicy.shared.getRequestPermissions(user: currentUser, type: requestType, is4k: is4k)
    }
    
    private var canConfigureAdvanced: Bool { permissions.canConfigureAdvanced }
    private var canRequestForOtherUsers: Bool { permissions.canRequestForOtherUsers }
    private var canRequestThis: Bool { permissions.canRequestThis }
    private var willAutoApprove: Bool { permissions.willAutoApprove }
    
    var body: some View {
        NavigationView {
            Form {
                Section {
                    HStack(spacing: 16) {
                        GenericPosterItem(posterUrl: details.fullPosterPath)
                            .frame(width: 80)
                        
                        VStack(alignment: .leading, spacing: 4) {
                            Text(details is TvDetails ? MR.strings().type_series.localized().uppercased() : MR.strings().type_movie.localized().uppercased())
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

                if !canRequestThis {
                    Section {
                        Text(MR.strings().request_no_permission.localized())
                            .foregroundColor(.red)
                            .font(.caption)
                    }
                }

                if canConfigureAdvanced || (canRequestForOtherUsers && !users.isEmpty) {
                    Section(header: Text(MR.strings().advanced.localized())) {
                        if canConfigureAdvanced {
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
                        
                        if canRequestForOtherUsers && !users.isEmpty {
                            Picker(MR.strings().request_as.localized(), selection: $selectedUserId) {
                                ForEach(users, id: \.id) { user in
                                    Text(user.displayName).tag(user.id as Int64?)
                                }
                            }
                        }
                    }
                }

                Section {
                    if !canRequestThis {
                        Text(MR.strings().request_no_permission.localized())
                            .font(.caption)
                            .foregroundColor(.red)
                    } else {
                        HStack(spacing: 8) {
                            Image(systemName: willAutoApprove ? "bolt.fill" : "clock")
                                .foregroundColor(willAutoApprove ? .accentColor : .secondary)
                            Text(willAutoApprove ? MR.strings().request_will_auto_approve.localized() : MR.strings().request_will_require_approval.localized())
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
            }
            .navigationTitle(MR.strings().request.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(MR.strings().cancel.localized()) {
                        onDismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(
                        !canRequestThis ? MR.strings().request.localized() : (is4k ? MR.strings().request_in_4k.localized() : MR.strings().submit_request.localized())
                    ) {
                        let seasons = details is TvDetails ? selectedSeasons.map { KotlinInt(value: $0) } : nil
                        let effectiveUserId = (canRequestForOtherUsers && selectedUserId != currentUser?.id) ? selectedUserId : nil
                        onSubmit(selectedProfileId, selectedRootFolder, nil, seasons, is4k, effectiveUserId)
                    }
                    .disabled(!canRequestThis || (details is TvDetails && selectedSeasons.isEmpty))
                }
            }
        }
    }
}
