//
//  SeriesMonitoringSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeriesMonitoringSheet: View {
    let onSelectOption: (SeriesMonitorType) -> Void
    @Environment(\.dismiss) private var dismiss

    private let options: [SeriesMonitorType] = [
        .all,
        .future,
        .missing,
        .existing,
        .recent,
        .pilot,
        .firstSeason,
        .lastSeason,
        .monitorSpecials,
        .unmonitorSpecials,
        .none
    ]

    var body: some View {
        NavigationStack {
            List {
                ForEach(options, id: \.self) { option in
                    Button(action: {
                        onSelectOption(option)
                        dismiss()
                    }) {
                        Text(option.resource.localized())
                            .foregroundStyle(Color.primary)
                    }
                }
            }
            .navigationTitle(MR.strings().series_monitoring.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(action: { dismiss() }) {
                        Label(MR.strings().cancel.localized(), systemImage: "xmark")
                    }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}
