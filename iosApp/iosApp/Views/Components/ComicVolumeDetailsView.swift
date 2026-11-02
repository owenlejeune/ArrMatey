//
//  ComicVolumeDetailsView.swift
//  iosApp
//

import Shared
import SwiftUI

struct ComicVolumeDetailsView: View {
    let item: ComicVolume
    let isActive: Bool

    private var firstLine: String {
        [item.publisher, item.year.map { String($0.intValue) }]
            .compactMap { $0 }
            .joined(separator: " • ")
    }

    private var progressColor: Color {
        isActive ? .arrPurple : Color(argb: item.statusColor)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            if !firstLine.isEmpty {
                Text(firstLine)
                    .font(.system(size: 14))
                    .lineSpacing(4)
            }

            if item.id != nil {
                Text("\(item.issuesDownloaded)/\(item.issueCount)")
                    .font(.system(size: 12))
                    .padding(.bottom, 1)
                    .padding(.top, 4)

                ProgressView(value: item.statusProgress)
                    .progressViewStyle(LinearProgressViewStyle(tint: progressColor))
                    .frame(height: 6)
            }
        }
    }
}
