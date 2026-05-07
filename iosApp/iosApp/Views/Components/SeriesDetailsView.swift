//
//  SeriesDetailsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-10.
//

import SwiftUI
import Shared

struct SeriesDetailsView: View {
    let item: ArrSeries
    let isActive: Bool
    
    private var countString: String {
        let progress = Int(item.statusProgress)
        return "\(item.episodeFileCount)/\(item.episodeCount) (\(progress)%)"
    }
    
    private var seasonString: String {
        return MR.plurals().seasons.localized(item.seasonCount)
    }
    
    private var fileSizeString: String {
        ByteCountFormatter.string(fromByteCount: item.fileSize, countStyle: .file)
    }
    
    private var firstLine: String {
        [item.network, seasonString, fileSizeString]
            .compactMap { $0 }
            .joined(separator: " • ")
    }
    
    private var statusString: String {
        switch item.status {
        case .continuing:
            if let nextAiring = item.nextAiring {
                return formatDate(nextAiring)
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
            
            HStack {
                Text("\(item.episodeFileCount)")
                    .font(.system(size: 12))
                
                Spacer()
                
                Text("/\(item.episodeCount)")
                    .font(.system(size: 12))
            }
            .padding(.bottom, 1)
            .padding(.top, 4)
            
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
