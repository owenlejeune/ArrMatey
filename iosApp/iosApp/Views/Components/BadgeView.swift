//
//  BadgeView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-09.
//

import SwiftUI

struct BadgeView: View {
    let text: String
    let color: Color
    var systemImage: String? = nil
    
    var body: some View {
        HStack(spacing: 3) {
            if let systemImage = systemImage {
                Image(systemName: systemImage)
                    .font(.caption2.weight(.semibold))
            }
            Text(text)
                .font(.caption2.weight(.semibold))
        }
        .foregroundStyle(.white)
        .padding(.horizontal, 6)
        .padding(.vertical, 3)
        .background(
            RoundedRectangle(cornerRadius: 6, style: .continuous)
                .fill(color)
        )
        .accessibilityElement(children: .combine)
    }
}

