//
//  SeerrIssueDetailsSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrIssueDetailsSheet: View {
    let issuePackage: MediaIssuePackage
    let onDismiss: () -> Void
    
    @ObservedObject private var viewModel: IssueDetailsViewModelS
    @State private var newComment = ""
    @State private var showCloseConfirmation = false
    
    init(issuePackage: MediaIssuePackage, onDismiss: @escaping () -> Void) {
        self.issuePackage = issuePackage
        self.onDismiss = onDismiss
        self.viewModel = IssueDetailsViewModelS(issuePackage: issuePackage)
    }
    
    private var commentsList: [Comment] {
        viewModel.uiState.issuePackage.issue.comments
    }
    
    private var descriptionComment: Comment? {
        commentsList.min(by: { $0.id < $1.id })
    }
    
    private var additionalComments: [Comment] {
        guard commentsList.count > 1 else { return [] }
        return Array(commentsList.dropFirst())
    }
    
    private var isSubmitting: Bool {
        viewModel.uiState.commentSubmissionStatus is OperationStatusInProgress
    }
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                ScrollView {
                    VStack(alignment: .leading, spacing: 24) {
                        if let description = descriptionComment {
                            descriptionSection(description)
                        }
                        
                        if !additionalComments.isEmpty {
                            commentsSection
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(24)
                }
                
                Divider()
                commentInputBar
            }
            .navigationTitle(viewModel.uiState.issuePackage.details?.displayTitle ?? MR.strings().issues.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(action: onDismiss) {
                        Image(systemName: "xmark")
                    }
                }
            }
            .alert(MR.strings().confirm_close_issue.localized(), isPresented: $showCloseConfirmation) {
                Button(MR.strings().yes.localized(), role: .destructive) {
                    viewModel.closeIssue(viewModel.uiState.issuePackage.issue.id)
                }
                Button(MR.strings().no.localized(), role: .cancel) {}
            }
            .onChange(of: isSubmitting) { _, submitting in
                if !submitting && viewModel.uiState.commentSubmissionStatus is OperationStatusSuccess {
                    newComment = ""
                }
            }
        }
    }
    
    // MARK: - Comment Input Bar
    
    private var commentInputBar: some View {
        HStack(spacing: 12) {
            Button(action: { showCloseConfirmation = true }) {
                Image(systemName: "checkmark.circle")
                    .font(.title2)
                    .foregroundColor(.white)
                    .frame(width: 44, height: 44)
                    .background(Color.red)
                    .clipShape(Circle())
            }
            
            HStack {
                TextField(MR.strings().comment.localized(), text: $newComment)
                    .textFieldStyle(.plain)
                    .disabled(isSubmitting)
                
                Button(action: {
                    viewModel.submitIssueComment(newComment)
                }) {
                    if isSubmitting {
                        ProgressView()
                            .frame(width: 24, height: 24)
                    } else {
                        Image(systemName: "paperplane.fill")
                            .foregroundColor(newComment.isEmpty ? .secondary : .accentColor)
                    }
                }
                .disabled(isSubmitting || newComment.isEmpty)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(Color(.systemGray6))
            .clipShape(Capsule())
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(.bar)
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
        .padding(.vertical, 12)
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
