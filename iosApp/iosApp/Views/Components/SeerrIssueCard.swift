//
//  SeerrIssueCard.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrIssueCard: View {
    let issuePackage: MediaIssuePackage
    let onClick: () -> Void
    
    private var issue: Issue { issuePackage.issue }
    private var details: RequestMediaDetails? { issuePackage.details }
    
    var body: some View {
        Button(action: onClick) {
            VStack(alignment: .leading, spacing: 12) {
                headerRow
                
                if issue.media?.mediaType == .tv {
                    seasonEpisodeRow
                }
                
                if let firstComment = issue.comments.min(by: { $0.id < $1.id }) {
                    Text(firstComment.message)
                        .font(.subheadline)
                        .foregroundColor(.white.opacity(0.9))
                        .lineLimit(3)
                }
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
        if let posterUrl = details?.fullPosterPath, let url = URL(string: posterUrl) {
            AsyncImage(url: url) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .blur(radius: 4)
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
            
            VStack(alignment: .leading, spacing: 4) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 2) {
                        HStack(spacing: 8) {
                            if let year = details?.displayDate?.year {
                                Text(String(year))
                                    .font(.caption)
                                    .foregroundColor(.white.opacity(0.8))
                            }
                            if let mediaType = issue.media?.mediaType {
                                RequestTypeChip(type: mediaType)
                            }
                        }
                        Text(details?.displayTitle ?? "")
                            .font(.headline)
                            .foregroundColor(.white)
                            .lineLimit(2)
                    }
                    Spacer()
                    SeerrIssueStatusChip(issue: issue)
                }
                
                issueTypeLabel
                
                if let createdBy = issue.createdBy {
                    VStack(alignment: .leading, spacing: 2) {
                        UserInfoLabel(
                            label: MR.strings().opened_by.localized(),
                            displayName: createdBy.displayName,
                            avatarUrl: createdBy.avatar
                        )
                        if let createdAt = issue.createdAt {
                            Text(createdAt.format(pattern: "HH:mm, MMM d, yyyy"))
                                .font(.caption2)
                                .foregroundColor(.white.opacity(0.7))
                        }
                    }
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
    
    // MARK: - Issue Type
    
    private var issueTypeLabel: some View {
        let typeLabel: String = {
            switch issue.issueType {
            case 1: return MR.strings().video.localized()
            case 2: return MR.strings().audio.localized()
            case 3: return MR.strings().subtitle.localized()
            default: return MR.strings().other.localized()
            }
        }()
        
        return HStack(spacing: 4) {
            Text(MR.strings().type.localized())
                .font(.caption)
                .fontWeight(.medium)
                .foregroundColor(.white.opacity(0.7))
            Text(typeLabel)
                .font(.caption)
                .foregroundColor(.white)
        }
    }
    
    // MARK: - Season / Episode
    
    private var seasonEpisodeRow: some View {
        HStack(spacing: 8) {
            Text(MR.strings().season.localized())
                .font(.caption)
                .foregroundColor(.white.opacity(0.7))
            
            Text(issue.problemSeason == 0
                 ? MR.strings().all.localized()
                 : "\(issue.problemSeason)")
                .font(.caption2.bold())
                .padding(.horizontal, 8)
                .padding(.vertical, 2)
                .background(Color.accentColor)
                .foregroundColor(.white)
                .clipShape(Capsule())
            
            Text(MR.strings().episode.localized())
                .font(.caption)
                .foregroundColor(.white.opacity(0.7))
            
            Text(issue.problemEpisode == 0
                 ? MR.strings().all.localized()
                 : "\(issue.problemEpisode)")
                .font(.caption2.bold())
                .padding(.horizontal, 8)
                .padding(.vertical, 2)
                .background(Color.accentColor)
                .foregroundColor(.white)
                .clipShape(Capsule())
        }
    }
}

// MARK: - Issue Status Chip

struct SeerrIssueStatusChip: View {
    let issue: Issue
    
    var body: some View {
        let isOpen = issue.status == 1
        let label = isOpen ? MR.strings().open.localized() : MR.strings().unknown.localized()
        let (bg, fg): (Color, Color) = isOpen
            ? (.blue.opacity(0.2), .blue)
            : (.orange.opacity(0.2), .orange)
        
        Text(label)
            .font(.caption2.bold())
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(bg)
            .foregroundColor(fg)
            .clipShape(Capsule())
    }
}
