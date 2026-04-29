//
//  SeerrRequestCard.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrRequestCard: View {
    let mediaPackage: MediaRequestPackage
    let user: SeerrUser?
    let operationsState: RequestOperationsState
    let onApprove: () -> Void
    let onDecline: () -> Void
    let onEdit: () -> Void
    let onDelete: () -> Void
    let onRemoveFromService: () -> Void
    let onClick: () -> Void
    
    private var request: MediaRequest { mediaPackage.request }
    private var details: RequestMediaDetails? { mediaPackage.details }
    
    var body: some View {
        Button(action: onClick) {
            VStack(alignment: .leading, spacing: 4) {
                headerRow
                
                if request.type == .tv && !request.seasons.isEmpty {
                    seasonInfo
                }
                
                Spacer().frame(height: 8)
                
                RequestActionButtons(
                    isAdmin: user?.hasPermission(permission: .admin) ?? false,
                    request: request,
                    operationsState: operationsState,
                    onApprove: onApprove,
                    onDecline: onDecline,
                    onEdit: onEdit,
                    onDelete: onDelete,
                    onRemoveFromService: onRemoveFromService
                )
            }
            .padding(18)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background {
                ZStack {
                    backdropLayer
                    Color.black.opacity(0.6)
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .contentShape(RoundedRectangle(cornerRadius: 16))
            .shadow(radius: 6)
        }
        .buttonStyle(.plain)
    }
    
    // MARK: - Backdrop
    
    @ViewBuilder
    private var backdropLayer: some View {
        if let backdropUrl = details?.fullBackdropPath, let url = URL(string: backdropUrl) {
            AsyncImage(url: url) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Color(.systemGray5)
            }
        } else {
            Color(.systemGray5)
        }
    }
    
    // MARK: - Header
    
    private var headerRow: some View {
        HStack(alignment: .top, spacing: 8) {
            posterImage
            
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 8) {
                    if let year = details?.displayDate?.year {
                        Text(String(year))
                            .font(.caption)
                            .foregroundColor(.white.opacity(0.8))
                    }
                    RequestTypeChip(type: request.type)
                }
                
                Text(details?.displayTitle ?? "")
                    .font(.headline)
                    .foregroundColor(.white)
                    .lineLimit(2)
                
                HStack(alignment: .top, spacing: 12) {
                    SeerrStatusChip(request: request)
                    requestMetadata
                }
            }
        }
    }
    
    // MARK: - Poster
    
    @ViewBuilder
    private var posterImage: some View {
        if let posterUrl = details?.fullPosterPath, let url = URL(string: posterUrl) {
            AsyncImage(url: url) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Color(.systemGray4)
            }
            .frame(width: 67, height: 100)
            .clipShape(RoundedRectangle(cornerRadius: 8))
        } else {
            RoundedRectangle(cornerRadius: 8)
                .fill(Color(.systemGray4))
                .frame(width: 67, height: 100)
                .overlay {
                    Image(systemName: "photo")
                        .foregroundColor(.gray)
                }
        }
    }
    
    // MARK: - Metadata
    
    private var requestMetadata: some View {
        VStack(alignment: .leading, spacing: 2) {
            UserInfoLabel(
                label: MR.strings().requested_by.localized(),
                displayName: request.requestedBy.displayName,
                avatarUrl: request.requestedBy.avatar
            )
            Text(request.createdAt.format(pattern: "HH:mm, MMM d, yyyy"))
                .font(.caption2)
                .foregroundColor(.white.opacity(0.7))
            
            if let modifiedBy = request.modifiedBy {
                Spacer().frame(height: 4)
                UserInfoLabel(
                    label: MR.strings().modified_by.localized(),
                    displayName: modifiedBy.displayName,
                    avatarUrl: modifiedBy.avatar
                )
                Text(request.updatedAt.format(pattern: "HH:mm, MMM d, yyyy"))
                    .font(.caption2)
                    .foregroundColor(.white.opacity(0.7))
            }
        }
    }
    
    // MARK: - Season Info
    
    private var seasonInfo: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(MR.strings().seasons_header.localized())
                .font(.caption2)
                .foregroundColor(.white.opacity(0.7))
            
            HStack(spacing: 4) {
                ForEach(request.seasons, id: \.seasonNumber) { season in
                    Text("\(season.seasonNumber)")
                        .font(.caption2.bold())
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.white.opacity(0.3))
                        .clipShape(Capsule())
                        .foregroundColor(.white)
                }
            }
        }
    }
}

// MARK: - Status Chip

struct SeerrStatusChip: View {
    let request: MediaRequest
    
    var body: some View {
        let (label, bg, fg) = statusAttributes
        Text(label)
            .font(.caption2.bold())
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(bg)
            .foregroundColor(fg)
            .clipShape(Capsule())
    }
    
    private var statusAttributes: (String, Color, Color) {
        let mediaStatusVal = request.media.status
        let requestStatusVal = request.status
        
        // Media statuses: 1=Unknown, 2=Pending, 3=Processing, 4=PartiallyAvailable, 5=Available, 7=Deleted
        switch mediaStatusVal {
        case 7:
            return (MR.strings().deleted.localized(), .red.opacity(0.2), .red)
        case 5:
            return (MR.strings().available.localized(), .green.opacity(0.2), .green)
        case 4:
            return (MR.strings().partially_available.localized(), .orange.opacity(0.2), .orange)
        case 3:
            return (MR.strings().processing.localized(), .blue.opacity(0.2), .blue)
        default:
            break
        }
        
        // Request statuses: 1=Pending, 2=Approved, 3=Declined, 5=Available
        switch requestStatusVal {
        case 3:
            return (MR.strings().declined.localized(), .red.opacity(0.2), .red)
        case 2:
            return (MR.strings().approved.localized(), .blue.opacity(0.2), .blue)
        default:
            return (MR.strings().pending.localized(), .orange.opacity(0.2), .orange)
        }
    }
}

// MARK: - Request Type Chip

struct RequestTypeChip: View {
    let type: RequestType
    
    var body: some View {
        Text(type.name)
            .font(.caption2.bold())
            .padding(.horizontal, 6)
            .padding(.vertical, 2)
            .background(type == .movie ? Color.blue.opacity(0.2) : Color.purple.opacity(0.2))
            .foregroundColor(type == .movie ? .blue : .purple)
            .clipShape(Capsule())
    }
}

// MARK: - User Info

struct UserInfoLabel: View {
    let label: String
    let displayName: String
    let avatarUrl: String?
    
    var body: some View {
        HStack(spacing: 4) {
            Text(label)
                .font(.caption2)
                .foregroundColor(.white.opacity(0.7))
            + Text(" ")
            + Text(displayName)
                .font(.caption2.bold())
                .foregroundColor(.white)
            
            if let avatarUrl, let url = URL(string: avatarUrl) {
                AsyncImage(url: url) { image in
                    image.resizable().aspectRatio(contentMode: .fill)
                } placeholder: {
                    Color(.systemGray4)
                }
                .frame(width: 14, height: 14)
                .clipShape(Circle())
            }
        }
    }
}

// MARK: - Action Buttons

struct RequestActionButtons: View {
    let isAdmin: Bool
    let request: MediaRequest
    let operationsState: RequestOperationsState
    let onApprove: () -> Void
    let onDecline: () -> Void
    let onEdit: () -> Void
    let onDelete: () -> Void
    let onRemoveFromService: () -> Void
    
    @State private var showDeclineConfirm = false
    @State private var showDeleteConfirm = false
    @State private var showRemoveConfirm = false
    
    private var isPendingApproval: Bool { request.status == 1 }
    private var isApproved: Bool { request.status == 2 || request.status == 5 || request.media.status >= 4 }
    private var isDeclined: Bool { request.status == 3 }
    
    var body: some View {
        VStack(spacing: 6) {
            if isPendingApproval && !isApproved {
                pendingButtons
            }
            
            if isAdmin && (isApproved || isDeclined) && !isPendingApproval {
                adminButtons
            }
        }
    }
    
    private var pendingButtons: some View {
        VStack(spacing: 6) {
            HStack(spacing: 6) {
                if isAdmin {
                    Button(action: onApprove) {
                        HStack(spacing: 4) {
                            if operationsState.approvalStates[request.id.asKotlinLong] != nil {
                                ProgressView().tint(.white)
                            } else {
                                Image(systemName: "checkmark")
                                Text(MR.strings().approve.localized())
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                        .background(Color.green)
                        .foregroundColor(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .font(.subheadline.bold())
                    }
                    .disabled(operationsState.approvalStates[request.id.asKotlinLong] != nil)
                }
                
                Button(action: {
                    if showDeclineConfirm {
                        onDecline()
                        showDeclineConfirm = false
                    } else {
                        showDeclineConfirm = true
                        resetConfirmAfterDelay { showDeclineConfirm = false }
                    }
                }) {
                    HStack(spacing: 4) {
                        if operationsState.cancelStates[request.id.asKotlinLong] != nil {
                            ProgressView().tint(.white)
                        } else {
                            Image(systemName: "xmark")
                            Text(showDeclineConfirm
                                 ? MR.strings().confirm.localized()
                                 : (isAdmin ? MR.strings().decline.localized() : MR.strings().cancel_request.localized()))
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                    .background(Color.red)
                    .foregroundColor(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .font(.subheadline.bold())
                }
                .disabled(operationsState.cancelStates[request.id.asKotlinLong] != nil)
            }
            
            if isDebug() {
                if isAdmin || request.type == .tv {
                    Button(action: onEdit) {
                        HStack(spacing: 4) {
                            Image(systemName: "pencil")
                            Text(MR.strings().edit.localized())
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                        .background(Color.orange)
                        .foregroundColor(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .font(.subheadline.bold())
                    }
                }
            }
        }
    }
    
    private var adminButtons: some View {
        VStack(spacing: 6) {
            Button(action: {
                if showDeleteConfirm {
                    onDelete()
                    showDeleteConfirm = false
                } else {
                    showDeleteConfirm = true
                    resetConfirmAfterDelay { showDeleteConfirm = false }
                }
            }) {
                HStack(spacing: 4) {
                    Image(systemName: "trash")
                    Text(showDeleteConfirm
                         ? MR.strings().confirm.localized()
                         : MR.strings().delete_request.localized())
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
                .background(Color.red)
                .foregroundColor(.white)
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .font(.subheadline.bold())
            }
            
            if isApproved && request.media.status != 6 {
                let serviceName = request.type == .movie ? InstanceType.radarr.name : InstanceType.sonarr.name
                Button(action: {
                    if showRemoveConfirm {
                        onRemoveFromService()
                        showRemoveConfirm = false
                    } else {
                        showRemoveConfirm = true
                        resetConfirmAfterDelay { showRemoveConfirm = false }
                    }
                }) {
                    HStack(spacing: 4) {
                        Image(systemName: "trash")
                        Text(showRemoveConfirm
                             ? MR.strings().confirm.localized()
                             : MR.strings().remove_from_service.formatted(args: [serviceName]))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                    .background(Color.red)
                    .foregroundColor(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .font(.subheadline.bold())
                }
            }
        }
    }
    
    private func resetConfirmAfterDelay(_ reset: @escaping () -> Void) {
        Task {
            try? await Task.sleep(for: .seconds(3))
            await MainActor.run { reset() }
        }
    }
}
