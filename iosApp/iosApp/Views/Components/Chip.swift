//
//  Chip.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-16.
//

import SwiftUI

struct Chip: View {
    let title: String
    let icon: String
    var tintColor: Color? = nil
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(tintColor ?? .secondary)
                Text(title)
                    .font(.caption.weight(.medium))
                    .foregroundStyle(tintColor ?? .primary)
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(
                Capsule(style: .continuous)
                    .fill(tintColor?.opacity(0.12) ?? Color(uiColor: .tertiarySystemFill))
            )
            .overlay(
                Capsule(style: .continuous)
                    .stroke(Color.primary.opacity(0.05), lineWidth: 0.5)
            )
        }
        .buttonStyle(.plain)
    }
}

