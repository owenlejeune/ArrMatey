//
//  DevSettingsScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-06.
//

import Shared
import SwiftUI

struct DevSettingsScreen: View {
    
    @Environment(\.dismiss) var dismiss
    
    @StateObject var preferences: PreferencesViewModel = PreferencesViewModel()
    @StateObject private var logViewModel: LogsViewModel = LogsViewModel()
    @State private var showOnboarding: Bool = false
    
    var body: some View {
        Form {
            Section {
                ForEach(InstanceType.allCases, id: \.self) { instanceType in
                    Toggle("Show \(instanceType.name) info card", isOn: Binding(
                        get: { preferences.showInfoCardMap[instanceType] ?? true},
                        set: { newValue in
                            preferences.setInfoCardVisibility(type: instanceType, visible: newValue)
                        }
                    ))
                }
                
                Toggle("Enable activity polling", isOn: Binding(
                    get: { preferences.enableAcitivityPolling },
                    set: { _ in preferences.toggleAcitivityPolling() }
                ))
                
                Picker("HTTP Logging Level", selection: Binding(
                    get: { preferences.logLevel },
                    set: { level in preferences.setLoggingLevel(level)}
                )) {
                    ForEach(LoggerLevel.allCases, id: \.self) { level in
                        Text(level.name).tag(level)
                    }
                }
            }
            
            Section("Onboarding") {
                Button(MR.strings().dev_settings_launch_onboarding.localized()) {
                    showOnboarding = true
                }
            }
            
//            Section("Application Logs") {
//                LogsView(logContent: logViewModel.logContent)
//                    .frame(height: 250)
//            }
        }
        .fullScreenCover(isPresented: $showOnboarding) {
            OnboardingView {
                showOnboarding = false
            }
        }
        .onAppear {
            logViewModel.startPolling()
        }
        .onDisappear {
            logViewModel.stopPolling()
        }
    }
}

struct LogsView: View {
    let logContent: String
    
    var body: some View {
        ZStack {
            Color(uiColor: .systemBackground)
                .opacity(0.05)
            
            ScrollView([.horizontal, .vertical]) {
                Text(logContent.isEmpty ? "NO LOGS" : logContent)
                    .font(.system(.caption, design: .monospaced))
                    .textSelection(.enabled)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(16)
            }
            .onChange(of: logContent) { _, newValue in
                // Only scroll to bottom if content actually changed
                if !newValue.isEmpty {
                    // Scroll handling moved to a more stable approach
                }
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}
