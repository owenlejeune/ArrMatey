//
//  MovieDetailsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-10.
//

import SwiftUI
import Shared

struct MovieDetailsView: View {
    let item: ArrMovie
    let isActive: Bool
    
    private var releaseDateString: String? {
        guard let releaseDate = item.releaseDate else { return nil }
        let timeInterval = TimeInterval(releaseDate.epochSeconds)
        let date = Date(timeIntervalSince1970: timeInterval)
        
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        return formatter.string(from: date)
    }
    
    private var runtimeString: String? {
        let runtime = item.runtime.intValue
        guard runtime > 0 else { return nil }
        let hours = runtime / 60
        let minutes = runtime % 60
        
        if hours > 0 {
            return "\(hours)h \(minutes)m"
        } else {
            return "\(minutes)m"
        }
    }
    
    private var firstLine: String {
        [runtimeString, item.studio]
            .compactMap { $0 }
            .joined(separator: " • ")
    }
    
    private var fileSizeString: String? {
        guard item.fileSize > 0 else { return nil }
        return ByteCountFormatter.string(fromByteCount: item.fileSize, countStyle: .file)
    }
    
    private var statusLabel: String? {
        guard item.fileSize == 0 else { return nil }
        return item.status.name
    }
    
    private var qualityString: String? {
        item.movieFile?.quality?.quality.name
    }
    
    private var secondLine: String {
        [statusLabel, fileSizeString, qualityString]
            .compactMap { $0 }
            .joined(separator: " • ")
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
        case .released:
            return .green
        case .inCinemas:
            return .blue
        case .announced:
            return .orange
        default:
            return .gray
        }
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            if let releaseDateString = releaseDateString {
                Text(releaseDateString)
                    .font(.system(size: 14))
                    .lineSpacing(4)
            }
            
            Text(firstLine)
                .font(.system(size: 14))
                .lineSpacing(4)
            
            Text(secondLine)
                .font(.system(size: 14))
                .lineSpacing(4)
            
            Spacer()
            
            ProgressView(value: item.statusProgress)
                .progressViewStyle(LinearProgressViewStyle(tint: progressColor))
                .frame(height: 6)
        }
    }
}
