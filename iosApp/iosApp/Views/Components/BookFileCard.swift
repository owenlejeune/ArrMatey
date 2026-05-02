//
//  BookFileCard.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-02.
//

import Shared
import SwiftUI

struct BookFileCard: View {
    let file: BookFile
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(file.path ?? "")
                .font(.system(size: 18, weight: .medium))
            
            Text(fileInfoLine(file: file))
                .font(.system(size: 14))
            
            if let dateAdded = file.dateAdded?.format(pattern: "MMM d, yyyy") {
                Text(MR.strings().added_on.formatted(args: [dateAdded]))
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
    
    private func fileInfoLine(file: BookFile) -> String {
        let sizeString = file.size?.int64Value.bytesAsFileSizeString() ?? ""
        let qualityName = file.quality?.qualityLabel ?? ""
        return [sizeString, qualityName]
            .filter { !$0.isEmpty }
            .joined(separator: " • ")
    }
}
