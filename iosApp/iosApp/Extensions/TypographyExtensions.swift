//
//  TypographyExtensions.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-21.
//

import SwiftUI

extension Font {
    /// Standard rounded style for metrics, counters, and badges
    static func metric(_ style: TextStyle = .title2, weight: Weight = .bold) -> Font {
        .system(style, design: .rounded).weight(weight)
    }

    /// Clean headline font with standard HIG dynamic sizing
    static func sectionHeader(weight: Weight = .bold) -> Font {
        .title3.weight(weight)
    }

    /// Subtitle font with secondary weight
    static func subtitle(weight: Weight = .medium) -> Font {
        .subheadline.weight(weight)
    }

    /// Caption font for chips, badges, and tags
    static func badge(weight: Weight = .semibold) -> Font {
        .caption.weight(weight)
    }
}

extension View {
    /// Applies standard primary title styling
    func screenTitleStyle() -> some View {
        self.font(.largeTitle.weight(.bold))
            .foregroundStyle(.primary)
    }

    /// Applies section title styling
    func sectionHeaderStyle() -> some View {
        self.font(.title3.weight(.bold))
            .foregroundStyle(.primary)
    }

    /// Applies secondary caption styling
    func metadataCaptionStyle() -> some View {
        self.font(.caption)
            .foregroundStyle(.secondary)
    }
}
