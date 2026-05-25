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
    @Published var hideInstanceSwitcher: Bool = false
    
    @Published var bottomTabItems: [AnyTabItem] = []
    @Published var drawerTabs: [AnyTabItem] = []
    @Published var removedTabs: [AnyTabItem] = []
    
    init() {
        self.preferenceStore = KoinBridge.shared.getPreferencesStore()
        self.tabManager = KoinBridge.shared.getTabManager()
        observeFlows()
    }
    
    private func observeFlows() {
        preferenceStore.showInfoCards.observeAsync(on: self) { owner, cards in
            owner.showInfoCardMap = cards.mapValues(\.boolValue)
        }
        preferenceStore.enableActivityPolling.observeAsync(on: self) { owner, polling in
            owner.enableAcitivityPolling = polling.boolValue
        }
        preferenceStore.httpLogLevel.observeAsync(on: self, to: \.logLevel)
        preferenceStore.tabPreferences.observeAsync(on: self, to: \.tabPreferences)
        preferenceStore.shouldShowReleaseNotes.observeAsync(on: self) { owner, show in
            owner.shouldShowReleaseNotes = show.boolValue
        }
        preferenceStore.useServiceNavLogos.observeAsync(on: self) { owner, useLogos in
            owner.useServiceNavLogos = useLogos.boolValue
        }
        preferenceStore.hideInstanceSwitcher.observeAsync(on: self) { owner, hide in
            owner.hideInstanceSwitcher = hide.boolValue
        }
        
        tabManager.tabConfiguration.observeAsync(on: self) { owner, config in
            owner.bottomTabItems = config.visibleTabs.map({ AnyTabItem(item: $0) })
            owner.drawerTabs = config.drawerTabs.map({ AnyTabItem(item: $0) })
            owner.removedTabs = config.hiddenTabs.map({ AnyTabItem(item: $0) })
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
    
    func saveNavigationLayout(visible: [TabItem], hidden: [TabItem], removed: [TabItem]) {
        let newPrefs = TabPreferences(
            orderedVisibleKeys: visible.map { $0.key },
            orderedHiddenKeys: hidden.map { $0.key },
            orderedRemovedKeys: removed.map { $0.key }
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
