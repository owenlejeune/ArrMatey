//
//  ReleaseDownloadButtons.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-22.
//

import SwiftUI
import Shared

struct ReleaseDownloadButtons: View {
    var onInteractiveClicked: () -> Void
    var automaticSearchEnabled: Bool
    var onAutomaticClicked: () -> Void
    var automaticSearchInProgress: Bool = false
    
    var body: some View {
        HStack(spacing: 6) {
            Button(action: onInteractiveClicked) {
                Label(
                    title: { Text(MR.strings().interactive.localized()) },
                    icon: { Image(systemName: "person.fill") }
                )
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .tint(.themePrimary)
            .controlSize(.regular)
            
            Button(action: onAutomaticClicked) {
                Group {
                    if automaticSearchInProgress {
                        ProgressView()
                            .controlSize(.small)
                            .tint(.white)
                    } else {
                        Label(
                            title: { Text(MR.strings().automatic.localized()) },
                            icon: { Image(systemName: "magnifyingglass") }
                        )
                        .foregroundStyle(.white)
                    }
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.regular)
            .disabled(!automaticSearchEnabled || automaticSearchInProgress)
        }
    }
}
