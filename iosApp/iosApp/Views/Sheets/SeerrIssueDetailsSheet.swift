//
//  SeerrIssueDetailsSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrIssueDetailsSheet: View {
    let issuePackage: MediaIssuePackage
    let onDismiss: () -> Void
    
    @State private var newComment = ""
    @State private var showCloseConfirmation = false
    
    private var comments: [Comment] { issuePackage.issue.comments }
    private var firstComment: Comment? { comments.min(by: { $0.id < $1.id }) }
    private var additionalComments: [Comment] {
        guard let first = firstComment else { return [] }
        return comments.filter { $0.id != first.id }.sorted(by: { $0.id < $1.id })
    }
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    if let description = firstComment {
                        descriptionSection(description)
                    }
                    
                    if !additionalComments.isEmpty {
                        commentsSection
                    }
                }
                .padding(24)
            }
            .navigationTitle(issuePackage.details?.displayTitle ?? MR.strings().issues.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(action: onDismiss) {
                        Image(systemName: "xmark")
                    }
                }
            }
        }
    }
    
    // MARK: - Description
    
    private func descriptionSection(_ comment: Comment) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(MR.strings().description_.localized())
                .font(.title3.bold())
            
            Text(comment.message)
                .font(.body)
            
            if let user = comment.user {
                HStack(spacing: 4) {
                    Text(MR.strings().opened_by.localized())
                        .font(.caption)
                        .foregroundColor(.secondary)
                    Text(user.displayName)
                        .font(.caption.bold())
                    
                    if let url = URL(string: user.avatar) {
                        AsyncImage(url: url) { image in
                            image.resizable().aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Color(.systemGray4)
                        }
                        .frame(width: 16, height: 16)
                        .clipShape(Circle())
                    }
                }
            }
            
            if let createdAt = comment.createdAt {
                Text(createdAt.format(pattern: "MMM d, yyyy"))
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
    }
    
    // MARK: - Comments
    
    private var commentsSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(MR.strings().comments.localized())
                .font(.title3.bold())
            
            ForEach(additionalComments, id: \.id) { comment in
                HStack(alignment: .top, spacing: 12) {
                    if let avatar = comment.user?.avatar, let url = URL(string: avatar) {
                        AsyncImage(url: url) { image in
                            image.resizable().aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Color(.systemGray4)
                        }
                        .frame(width: 36, height: 36)
                        .clipShape(Circle())
                    } else {
                        Circle()
                            .fill(Color(.systemGray4))
                            .frame(width: 36, height: 36)
                    }
                    
                    VStack(alignment: .leading, spacing: 4) {
                        if let user = comment.user {
                            Text(user.displayName)
                                .font(.subheadline.bold())
                        }
                        Text(comment.message)
                            .font(.body)
                        if let createdAt = comment.createdAt {
                            Text(createdAt.format(pattern: "MMM d, yyyy"))
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
            }
        }
    }
}
