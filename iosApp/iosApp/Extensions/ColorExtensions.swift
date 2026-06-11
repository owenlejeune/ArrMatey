//
//  ColorExtensions.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-24.
//

import SwiftUI
import Shared

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (1, 1, 1, 0)
        }

        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
    
    init(hex: UInt64) {
        if hex > 0xFFFFFF {
            let r = Double((hex >> 24) & 0xFF) / 255.0
            let g = Double((hex >> 16) & 0xFF) / 255.0
            let b = Double((hex >> 8) & 0xFF) / 255.0
            let a = Double(hex & 0xFF) / 255.0
            self.init(.sRGB, red: r, green: g, blue: b, opacity: a)
        } else {
            let r = Double((hex >> 16) & 0xFF) / 255.0
            let g = Double((hex >> 8) & 0xFF) / 255.0
            let b = Double(hex & 0xFF) / 255.0
            self.init(.sRGB, red: r, green: g, blue: b, opacity: 1.0)
        }
    }
    
    init(argb: UInt64) {
        let a = Double((argb >> 56) & 0xFF) / 255.0
        let r = Double((argb >> 48) & 0xFF) / 255.0
        let g = Double((argb >> 40) & 0xFF) / 255.0
        let b = Double((argb >> 32) & 0xFF) / 255.0
        
        self.init(.sRGB, red: r, green: g, blue: b, opacity: a)
    }
    
    func midpoint(with other: Color) -> Color {
        // Use SwiftUI's native UIColor/NSColor bridge to extract color components safely
#if os(iOS) || os(tvOS) || os(watchOS) || os(visionOS)
        let uiColor1 = UIColor(self)
        let uiColor2 = UIColor(other)
#elseif os(macOS)
        let uiColor1 = NSColor(self)
        let uiColor2 = NSColor(other)
#endif
        
        var r1: CGFloat = 0, g1: CGFloat = 0, b1: CGFloat = 0, a1: CGFloat = 0
        var r2: CGFloat = 0, g2: CGFloat = 0, b2: CGFloat = 0, a2: CGFloat = 0
        
        uiColor1.getRed(&r1, green: &g1, blue: &b1, alpha: &a1)
        uiColor2.getRed(&r2, green: &g2, blue: &b2, alpha: &a2)
        
        // Calculate the mathematical midpoint for each channel
        let midRed = (r1 + r2) / 2.0
        let midGreen = (g1 + g2) / 2.0
        let midBlue = (b1 + b2) / 2.0
        let midAlpha = (a1 + a2) / 2.0
        
        return Color(.sRGB, red: midRed, green: midGreen, blue: midBlue, opacity: midAlpha)
    }
}
