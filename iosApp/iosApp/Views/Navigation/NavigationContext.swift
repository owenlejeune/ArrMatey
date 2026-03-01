//
//  NavigationContext.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-01.
//

import SwiftUI

enum NavigationContext {
    case mainTab
    case launcher
}

struct NavigationContextKey: EnvironmentKey {
    static let defaultValue: NavigationContext = .mainTab
}

extension EnvironmentValues {
    var navigationContext: NavigationContext {
        get { self[NavigationContextKey.self] }
        set { self[NavigationContextKey.self] = newValue }
    }
}
