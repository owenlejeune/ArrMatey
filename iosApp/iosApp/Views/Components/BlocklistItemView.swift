//
//  BlocklistItemView.swift
//  iosApp
//
//  Created by Assistant on 2026-03-30.
//

import Shared
import SwiftUI

struct BlocklistItemView: View {
    let item: BlocklistItem
    let onDelete: () -> Void

    private var associatedColor: Color {
        item.instanceType?.associatedSwiftColor ?? .accentColor
    }

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(associatedColor)
                .frame(width: 5)

            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 12) {
                    Text(item.date.format(pattern: "MMM d, yyyy"))
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                    Spacer()
                    if let instanceName = item.instanceName, !instanceName.isEmpty {
                        Text(instanceName)
                            .font(.system(size: 11, weight: .medium))
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color(.secondarySystemBackground))
                            .cornerRadius(4)
                            .foregroundColor(.secondary)
                    }
                    Button(action: onDelete) {
                        Image(systemName: "trash")
                            .font(.system(size: 14))
                            .foregroundColor(.red)
                    }
                    .buttonStyle(.plain)
                }

                Text(item.displayTitle?.breakable() ?? "---")
                    .fontWeight(.semibold)

                if let sourceTitle = item.sourceTitle, !sourceTitle.isEmpty {
                    Text(sourceTitle.breakable())
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                }

                Text(subLabel)
                    .font(.system(size: 12))
                    .foregroundColor(.secondary)
            }
            .padding(.vertical, 12)
            .padding(.horizontal, 16)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .background(Color(uiColor: .secondarySystemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(Color.primary.opacity(0.06), lineWidth: 0.5)
        )
    }

    private var subLabel: String {
        var components: [String] = []

        if let qualityLabel = item.quality?.qualityLabel {
            components.append(qualityLabel)
        }
        components.append(LabelUtilsKt.singleLanguageLabel(item.languages))

        if let indexer = item.indexerLabel {
            components.append(indexer)
        }

        if let message = item.message, !message.isEmpty {
            components.append(message)
        }

        return components.joined(separator: " • ")
    }
}
