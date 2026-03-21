//
//  LogsViewModel.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-17.
//

import SwiftUI
import Shared

@MainActor
class LogsViewModel: ObservableObject {
    @Published var logContent: String = "Loading..."
    
    private var pollingTask: Task<Void, Never>?
    
    func startPolling() {
        guard pollingTask == nil else { return }
        
        pollingTask = Task { [weak self] in
            while !Task.isCancelled {
                // Load logs on background thread
                let logs = await Task.detached(priority: .background) {
                    LogReader.shared.readLogs()
                }.value
                
                // Update UI on main thread
                await MainActor.run { [weak self] in
                    self?.logContent = logs
                }
                
                // Wait 10 seconds
                try? await Task.sleep(nanoseconds: 10_000_000_000)
            }
        }
    }
    
    func stopPolling() {
        pollingTask?.cancel()
        pollingTask = nil
    }
    
    deinit {
        // Cancel the polling task in deinit without capturing self
        pollingTask?.cancel()
    }
}

