//
//  AudiobookFileCard.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-19.
//

import Shared
import SwiftUI

struct AudiobookFileCard: View {
    let file: AudiobookFile
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(file.path.flatMap { URL(filePath: $0) }?.lastPathComponent ?? MR.strings().unknown.localized())
                .font(.system(size: 18, weight: .medium))
            
            Text(fileInfoLine)
                .font(.system(size: 14))
            
            if let created = file.createdAt?.format(pattern: "MMM d, yyyy") {
                Text(MR.strings().added_on.formatted(args: [created]))
                    .font(.system(size: 14))
            }
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 10, style: .continuous)
                .fill(Color(.systemGroupedBackground))
        )
    }
    
    private var fileInfoLine: String {
        let sizeString = file.size?.int64Value.bytesAsFileSizeString()
        return [file.format, sizeString]
            .compactMap { $0 }
            .joined(separator: " • ")
    }
}
