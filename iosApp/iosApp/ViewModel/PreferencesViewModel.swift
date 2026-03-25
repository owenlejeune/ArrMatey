//
//  PreferencesViewModel.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-05.
//

import SwiftUI
import Shared

@MainActor
class PreferencesViewModel: ObservableObject {
    private let preferenceStore: PreferencesStore
    private let tabManager: TabManager

    @Published var showInfoCardMap: [InstanceType:Bool] = [:]
    @Published var enableAcitivityPolling: Bool = true
    @Published var logLevel: LoggerLevel = .headers
    @Published var tabPreferences: TabPreferences = TabPreferences()
    @Published var shouldShowReleaseNotes: Bool = false
    @Published var useServiceNavLogos: Bool = false
    
    @Published var bottomTabItems: [AnyTabItem] = []
    @Published var hiddenTabs: [AnyTabItem] = []
    
    init() {
        self.preferenceStore = KoinBridge.shared.getPreferencesStore()
        self.tabManager = KoinBridge.shared.getTabManager()
        observeFlows()
    }
    
    private func observeFlows() {
        preferenceStore.showInfoCards.observeAsync {
            self.showInfoCardMap = $0.mapValues(\.boolValue)
        }
        preferenceStore.enableActivityPolling.observeAsync {
            self.enableAcitivityPolling = $0.boolValue
        }
        preferenceStore.httpLogLevel.observeAsync { self.logLevel = $0 }
        preferenceStore.tabPreferences.observeAsync { self.tabPreferences = $0 }
        preferenceStore.shouldShowReleaseNotes.observeAsync { self.shouldShowReleaseNotes = $0.boolValue }
        preferenceStore.useServiceNavLogos.observeAsync { self.useServiceNavLogos = $0.boolValue }
        
        tabManager.tabConfiguration.observeAsync { [weak self] config in
            self?.bottomTabItems = config.visibleTabs.map({ AnyTabItem(item: $0) })
            self?.hiddenTabs = config.drawerTabs.map({ AnyTabItem(item: $0) })
        }
    }
    
    func setInfoCardVisibility(type: InstanceType, visible: Bool) {
        preferenceStore.setInfoCardVisibility(type: type, value: visible)
    }

    func toggleAcitivityPolling() {
        preferenceStore.toggleActivityPolling()
    }
    
    func setLoggingLevel(_ level: LoggerLevel) {
        preferenceStore.setLogLevel(level: level)
    }
    
    func resetTabPreferences() {
        preferenceStore.resetTabPreferences()
    }
    
    func saveTabPreferences(_ preferences: TabPreferences) {
        preferenceStore.saveTabPreferences(tabPreferences: preferences)
    }
    
    func updateTabPreferences(_ preferences: TabPreferences) {
        preferenceStore.updateTabPreferences(tabPreferences: preferences)
    }
    
    func saveNavigationLayout(visible: [TabItem], hidden: [TabItem]) {
        let newPrefs = TabPreferences(
            orderedVisibleKeys: visible.map { $0.key },
            orderedHiddenKeys: hidden.map { $0.key }
        )
        preferenceStore.updateTabPreferences(tabPreferences: newPrefs)
    }
    
    func markReleaseNotesAsSeen() {
        preferenceStore.markReleaseNotesAsSeen()
    }
    
    func toggleUseServiceNavLogos() {
        preferenceStore.toggleUseServiceNavLogos()
    }
}
