//
//  AboutCard.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-16.
//

import SwiftUI
import Shared
import Flow

struct AboutCard: View {
    let onFeatureRequestClick: () -> Void
    let onBugReportClick: () -> Void
    let onGitHubClick: () -> Void
    let onDonateClick: () -> Void
    let onLibrariesClick: () -> Void
    
    var body: some View {
        VStack(alignment: .center, spacing: 12) {
            if let uiImage = Bundle.main.icon {
                Image(uiImage: uiImage)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 60, height: 60)
                    .cornerRadius(12)
            }
            
            Text(MR.strings().app_name.desc().localized())
                .font(.headline)
            
            Text(MR.strings().version_label.formatted(args: [Bundle.main.releaseVersionNumber ?? "1.0"]))
                .font(.subheadline)
                .foregroundColor(.secondary)
            
            HFlow(spacing: 10) {
                Chip(title: MR.strings().bug_report.localized(), icon: "ladybug.fill", action: onBugReportClick)
                Chip(title: MR.strings().feature_request.localized(), icon: "sparkle.text.clipboard", action: onFeatureRequestClick)
                Chip(title: MR.strings().donate.localized(), icon: "heart", action: onDonateClick)
                Chip(title: MR.strings().github.localized(), icon: "terminal", action: onGitHubClick)
                Chip(title: MR.strings().libraries.localized(), icon: "book.closed", action: onLibrariesClick)
            }
            .padding(.top, 8)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .cornerRadius(16)
    }
}
