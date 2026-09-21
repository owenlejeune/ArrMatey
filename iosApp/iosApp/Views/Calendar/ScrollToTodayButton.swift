//
//  ScrollToTodayButton.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-09.
//

import Shared
import SwiftUI

struct ScrollToTodayButton: View {
    let action: () -> Void
    
    var body: some View {
        Button(action: {
            HapticFeedback.impact(style: .light)
            action()
        }) {
            HStack(spacing: 6) {
                Image(systemName: "calendar")
                    .font(.subheadline.weight(.semibold))
                Text(MR.strings().today.localized())
                    .font(.subheadline.weight(.semibold))
            }
            .foregroundStyle(.white)
            .padding(.horizontal, 18)
            .padding(.vertical, 10)
            .background(
                Capsule(style: .continuous)
                    .fill(Color.accentColor)
            )
            .overlay(
                Capsule(style: .continuous)
                    .stroke(Color.white.opacity(0.15), lineWidth: 0.5)
            )
            .shadow(color: Color.black.opacity(0.15), radius: 10, x: 0, y: 4)
        }
        .padding(.bottom, 16)
    }
}

