//
//  IosCrashManager.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-24.
//

import Foundation
import Shared
import UIKit

class IOSCrashManager: CrashManager {    
    static let shared = IOSCrashManager()
    private init() {}
    
    func initialize() {
        // Use a trailing closure or a direct reference to the static method
        NSSetUncaughtExceptionHandler { exception in
            IOSCrashManager.handleException(exception)
        }
    }
    
    private static func handleException(_ exception: NSException) {
        let logReader = LogReader.shared
        let path = logReader.getLogFilePath()
        
        let report = """
        Name: \(exception.name.rawValue)
        Reason: \(exception.reason ?? "Unknown")
        Call Stack:
        \(exception.callStackSymbols.joined(separator: "\n"))
        """
        
        do {
            try report.write(toFile: path, atomically: true, encoding: .utf8)
        } catch {
            print("Failed to write crash report: \(error)")
        }
    }
}
