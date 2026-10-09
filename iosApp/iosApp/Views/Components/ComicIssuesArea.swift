//
//  ComicIssuesArea.swift
//  iosApp
//

import SwiftUI
import Shared

struct ComicIssuesArea: View {
    let volume: ComicVolume
    let instanceId: Int64?

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if volume.issues.isEmpty {
                Text(MR.strings().no_issues.localized())
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical, 12)
            } else {
                ForEach(volume.issues, id: \.id) { issue in
                    ComicIssueCard(issue: issue)
                }
            }
        }
    }
}

struct ComicIssueCard: View {
    let issue: ComicIssue

    var body: some View {
        let isDownloaded = !issue.files.isEmpty
        let totalSize = issue.files.reduce(0) { $0 + $1.size }

        HStack(spacing: 12) {
            Image(systemName: issue.monitored ? "bookmark.fill" : "bookmark")
                .foregroundColor(issue.monitored ? .accentColor : .secondary)

            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    Text("#\(issue.issueNumber ?? "\(Int(issue.calculatedIssueNumber?.doubleValue ?? 0))")")
                        .font(.headline)
                        .fontWeight(.bold)

                    if let title = issue.title, !title.isEmpty {
                        Text(title)
                            .font(.headline)
                    }
                }

                let statusText = isDownloaded ? MR.strings().downloaded.localized() : MR.strings().missing.localized()
                let detailsText = [
                    statusText,
                    issue.date
                ].compactMap { $0 }.joined(separator: " • ")

                if !detailsText.isEmpty {
                    Text(detailsText)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }

            Spacer()

            if isDownloaded {
                Image(systemName: "checkmark.circle.fill")
                    .foregroundColor(.green)
            }
        }
        .padding()
        .background(Color(UIColor.secondarySystemGroupedBackground))
        .cornerRadius(12)
    }
}
