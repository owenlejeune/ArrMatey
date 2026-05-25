//
//  AudiobookDetailsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-24.
//

import SwiftUI
import Shared

struct AudiobookDetailsView: View {
    let item: Audiobook
    let isActive: Bool
    
    private var statusColor: Color {
        if item.statusStr == "quality-match" {
            return .green
        } else if item.monitored {
            return .red
        } else {
            return .gray
        }
    }
    
    private var progressColor: Color {
        isActive ? .arrPurple : statusColor
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(item.authors.joined(separator: ", "))
                .font(.system(size: 14))
            
            let seriesString: String? = {
                if let series = item.series {
                    if let number = item.seriesNumber {
                        return "\(series) (#\(number))"
                    } else {
                        return series
                    }
                }
                return nil
            }()
            
            let fileSizeString = item.fileSize > 0 ? item.fileSize.bytesAsFileSizeString() : nil
            
            let secondLine = [seriesString, fileSizeString, item.publisher]
                .compactMap { $0 }
                .joined(separator: " • ")
            
            if !secondLine.isEmpty {
                Text(secondLine)
                    .font(.system(size: 14))
            }
            
            Text(item.status.resource.localized())
                .font(.system(size: 14))
            
            if item.id != nil {
                ProgressView(value: isActive ? 1.0 : Double(item.statusProgress))
                    .progressViewStyle(LinearProgressViewStyle(tint: progressColor))
                    .frame(height: 6)
                    .padding(.top, 8)
            }
        }
    }
}

