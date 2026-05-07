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
    
    private var albumCountString: String {
        return MR.plurals().albums.localized(item.albumCount)
    }
    
    private var fileSizeString: String {
        ByteCountFormatter.string(fromByteCount: item.fileSize, countStyle: .file)
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
            Text(firstLine)
                .font(.system(size: 14))
                .lineSpacing(4)
            
            Text(statusString)
                .font(.system(size: 14))
                .lineSpacing(4)
            
            Spacer()
            
            HStack {
                Text("\(item.trackFileCount)")
                    .font(.system(size: 12))
                
                Spacer()
                
                Text("/\(item.trackCount)")
                    .font(.system(size: 12))
            }
            .padding(.bottom, 1)
            
            ProgressView(value: item.statusProgress)
                .progressViewStyle(LinearProgressViewStyle(tint: progressColor))
                .frame(height: 6)
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
