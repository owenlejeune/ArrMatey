//
//  ArtistDetailsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-04.
//

import SwiftUI
import Shared

struct ArtistDetailsView: View {
    let item: Arrtist
    let isActive: Bool
    
    private var albumCountString: String? {
        // Lookup results have no statistics, so counts would always read 0
        guard item.id != nil else { return nil }
        return MR.plurals().albums.localized(item.albumCount)
    }
    
    private var fileSizeString: String? {
        guard let size = item.fileSize?.int64Value, size > 0 else { return nil }
        return ByteCountFormatter.string(fromByteCount: size, countStyle: .file)
    }
    
    private var firstLine: String {
        [albumCountString, fileSizeString]
            .compactMap { $0 }
            .joined(separator: " • ")
    }
    
    private var statusString: String {
        switch item.status {
        case .continuing:
            if let nextRelease = item.nextAlbum?.releaseDate {
                return formatDate(nextRelease)
            } else if item.id == nil {
                return item.status.name
            } else {
                return "\(item.status.name) - \(MR.strings().unknown.localized())"
            }
        default:
            return item.status.name
        }
    }
    
    private var progressColor: Color {
        if isActive {
            return .arrPurple
        } else {
            return statusColor
        }
    }
    
    private var statusColor: Color {
        switch item.status {
        case .continuing:
            return .green
        case .ended:
            return .red
        default:
            return .gray
        }
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            if !firstLine.isEmpty {
                Text(firstLine)
                    .font(.system(size: 14))
                    .lineSpacing(4)
            }
            
            Text(statusString)
                .font(.system(size: 14))
                .lineSpacing(4)
            
            Spacer()
            
            if item.id != nil {
                Text("\(item.trackFileCount)/\(item.trackCount)")
                    .font(.system(size: 12))
                    .padding(.bottom, 1)

                ProgressView(value: item.statusProgress)
                    .progressViewStyle(LinearProgressViewStyle(tint: progressColor))
                    .frame(height: 6)
            }
        }
    }
    
    private func formatDate(_ instant: KotlinInstant) -> String {
        let timeInterval = TimeInterval(instant.epochSeconds)
        let date = Date(timeIntervalSince1970: timeInterval)
        
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}
