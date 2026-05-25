//
//  ReleaseItemView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-24.
//

import Shared
import SwiftUI

struct ReleaseItemView: View {
    let item: ArrRelease
    let animate: Bool
    var onItemClick: ((ArrRelease) -> Void)? = nil
    
    private var thirdLine: String {
        [
            LabelUtilsKt.singleLanguageLabel(item.languages),
            item.indexerLabel,
            item.ageMinutes.formatAgeMinutes()
        ]
        .filter { !$0.isEmpty }
        .joined(separator: " • ")
    }
    
    private var secondLine: String {
        [
            item.quality?.qualityLabel,
            item.size.bytesAsFileSizeString()
        ]
            .compactMap { $0 }
            .joined(separator: " • ")
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(item.title)
                .font(.headline)
            
            HStack(spacing: 0) {
                Text(item.typeLabel)
                    .foregroundColor(Color(hex: item.peerColorHex))
                
                
                Text(" • \(secondLine)")
            }
            .font(.subheadline)
            
            Text(thirdLine)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .opacity(animate ? 0.6 : 1.0)
        .onTapGesture {
            onItemClick?(item)
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 10, style: .continuous)
                .fill(Color(.systemGroupedBackground))
        )
    }
}
