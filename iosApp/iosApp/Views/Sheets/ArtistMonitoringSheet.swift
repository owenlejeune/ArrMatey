//
//  ArtistMonitoringSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct ArtistMonitoringSheet: View {
    let onSelectOption: (ArtistMonitorType) -> Void
    @Environment(\.dismiss) private var dismiss

    private let options: [ArtistMonitorType] = [
        .all,
        .future,
        .missing,
        .existing,
        .firstAlbum,
        .latestAlbum,
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
            .navigationTitle(MR.strings().artist_monitoring.localized())
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
