//
//  IndexerStatusSheet.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-04.
//

import Shared
import SwiftUI

struct IndexerStatusSheet: View {
    let indexerStatus: IndexerStatus
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    if let disableUntil = indexerStatus.disabledTill {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(MR.strings().disabled_until.localized())
                                .font(.headline)
                            Text(disableUntil.format(pattern: "HH:mm MMM d, yyyy"))
                        }
                    }
                    
                    if let mostRecentFailure = indexerStatus.mostRecentFailure {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(MR.strings().most_recent_failure.localized())
                                .font(.headline)
                            Text(mostRecentFailure.format(pattern: "HH:mm MMM d, yyyy"))
                        }
                    }
                    
                    if let initialFailure = indexerStatus.initialFailure {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(MR.strings().initial_failure.localized())
                                .font(.headline)
                            Text(initialFailure.format(pattern: "HH:mm MMM d, yyyy"))
                        }
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 24)
                .padding(.vertical, 36)
            }
        }
    }
}
