//
//  SensoryFeedbackExtensions.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-21.
//

import SwiftUI
import UIKit

/// Utility for trigger-based and imperative haptic sensory feedback
enum HapticFeedback {
    static func selection() {
        UISelectionFeedbackGenerator().selectionChanged()
    }

    static func impact(style: UIImpactFeedbackGenerator.FeedbackStyle = .medium) {
        UIImpactFeedbackGenerator(style: style).impactOccurred()
    }

    static func notification(type: UINotificationFeedbackGenerator.FeedbackType) {
        UINotificationFeedbackGenerator().notificationOccurred(type)
    }

    static func success() {
        notification(type: .success)
    }

    static func warning() {
        notification(type: .warning)
    }

    static func error() {
        notification(type: .error)
    }
}
