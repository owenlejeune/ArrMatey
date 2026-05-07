//
//  AuthorDetailsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-04.
//

import SwiftUI
import Shared

struct AuthorDetailsView: View {
    let item: Author
    let isActive: Bool
    
    private var booksCountString: String {
        return MR.plurals().books_count.localized(item.totalBookCount)
    }
    
    private var fileSizeString: String {
        ByteCountFormatter.string(fromByteCount: item.fileSize, countStyle: .file)
    }
    
    private var firstLine: String {
        [booksCountString, fileSizeString]
            .compactMap { $0 }
            .joined(separator: " • ")
    }
    
    private var statusString: String {
        switch item.status {
        case .continuing:
            if let nextRelease = item.nextBook?.releaseDate {
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
                Text("\(item.bookFileCount)")
                    .font(.system(size: 12))
                
                Spacer()
                
                Text("/\(item.bookCount)")
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
