//
//  MediaServerBadgeOverlay.swift
//  iosApp
//

import SwiftUI
import Shared

struct MediaServerBadgeOverlay: View {
    let serverType: MediaServerType
    var iconSize: CGFloat = 16
    var innerPadding: CGFloat = 4
    var borderWidth: CGFloat = 1.5

    var body: some View {
        serverType.icon.toImage(renderingMode: .template)
            .resizable()
            .aspectRatio(contentMode: .fit)
            .foregroundColor(serverType.brandColor.toSwiftUI())
            .frame(width: iconSize, height: iconSize)
            .padding(innerPadding)
            .background(Circle().fill(Color(.systemBackground).opacity(0.95)))
            .overlay(
                Circle().stroke(serverType.brandColor.toSwiftUI(), lineWidth: borderWidth)
            )
            .shadow(color: .black.opacity(0.2), radius: 2, x: 0, y: 1)
    }
}

struct InLibraryBadgeOverlay: View {
    var iconSize: CGFloat = 16
    var innerPadding: CGFloat = 4
    var borderWidth: CGFloat = 1

    var body: some View {
        Image(systemName: "checkmark.circle.fill")
            .font(.system(size: iconSize))
            .foregroundColor(Color(red: 0x50 / 255.0, green: 0xD2 / 255.0, blue: 0x7D / 255.0))
            .padding(innerPadding)
            .background(Circle().fill(Color(.systemBackground).opacity(0.9)))
            .overlay(
                Circle().stroke(Color.secondary.opacity(0.3), lineWidth: borderWidth)
            )
            .shadow(color: .black.opacity(0.2), radius: 2, x: 0, y: 1)
    }
}

