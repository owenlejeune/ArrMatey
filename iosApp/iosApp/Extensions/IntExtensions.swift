//
//  IntExtensions.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-15.
//

extension Int {
    func formatAsRuntime() -> String {
        let hours = self / 60
        let minutes = self % 60

        var components: [String] = []

        if hours > 0 {
            components.append("\(hours)h")
        }

        if minutes > 0 {
            components.append("\(minutes)m")
        }

        if components.isEmpty {
            return "0m"
        }

        return components.joined(separator: " ")
    }
}

import Shared

extension Int64 {
    func formatWatchTime() -> String {
        return TimeUtilsKt.formatWatchTimeMs(ms: self)
    }
}

func formatWatchTime(ms: Int64) -> String {
    return TimeUtilsKt.formatWatchTimeMs(ms: ms)
}
