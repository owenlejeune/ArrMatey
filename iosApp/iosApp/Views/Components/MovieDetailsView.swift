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
        formatter.dateFormat = "MMMM d, yyyy"
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
        guard item.id != nil, let size = item.fileSize?.int64Value, size > 0 else { return nil }
        return ByteCountFormatter.string(fromByteCount: size, countStyle: .file)
    }
    
    private var qualityString: String? {
        item.movieFile?.quality?.quality.name
    }
    
    private var secondLine: String {
        [qualityString, fileSizeString]
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
            
            if !firstLine.isEmpty {
                Text(firstLine)
                    .font(.system(size: 14))
                    .lineSpacing(4)
            }
            
            if !secondLine.isEmpty {
                Text(secondLine)
                    .font(.system(size: 14))
                    .lineSpacing(4)
            }
            
            if item.id != nil {
                ProgressView(value: item.statusProgress)
                    .progressViewStyle(LinearProgressViewStyle(tint: progressColor))
                    .frame(height: 6)
                    .padding(.top, 4)
            }
        }
    }
}
