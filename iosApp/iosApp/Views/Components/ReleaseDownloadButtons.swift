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
    var deleteInProgress: Bool = false
    var onDelete: (() -> Void)? = nil
    
    var body: some View {
        HStack(spacing: 8) {
            if let onDelete = onDelete {
                Button(action: onDelete) {
                    if deleteInProgress {
                        ProgressView()
                            .controlSize(.small)
                            .tint(.white)
                            .frame(width: 20, height: 20)
                    } else {
                        Image(systemName: "trash")
                            .font(.system(size: 15))
                            .frame(width: 20, height: 20)
                    }
                }
                .tint(.red)
                .buttonStyle(.borderedProminent)
                .clipShape(RoundedRectangle(cornerRadius: 10))
                .disabled(deleteInProgress)
            }
            
            Button(action: onInteractiveClicked) {
                HStack(spacing: 6) {
                    Image(systemName: "person.fill")
                        .font(.system(size: 14))
                    Text(MR.strings().interactive.localized())
                        .font(.system(size: 14, weight: .medium))
                        .lineLimit(1)
                        .minimumScaleFactor(0.7)
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .tint(.themePrimary)
            .clipShape(Capsule())
            
            Button(action: onAutomaticClicked) {
                HStack(spacing: 6) {
                    if automaticSearchInProgress {
                        ProgressView()
                            .controlSize(.small)
                            .tint(.white)
                    } else {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 14))
                        Text(MR.strings().automatic.localized())
                            .font(.system(size: 14, weight: .medium))
                            .lineLimit(1)
                            .minimumScaleFactor(0.7)
                    }
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .tint(.themePrimary)
            .clipShape(Capsule())
            .disabled(!automaticSearchEnabled || automaticSearchInProgress)
        }
    }
}
