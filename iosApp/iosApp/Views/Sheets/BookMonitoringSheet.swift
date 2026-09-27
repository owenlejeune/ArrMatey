//
//  BookMonitoringSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct BookMonitoringSheet: View {
    let onSelectOption: (AuthorMonitorType) -> Void
    @Environment(\.dismiss) private var dismiss

    private let options: [AuthorMonitorType] = [
        .all,
        .future,
        .missing,
        .existing,
        .firstBook,
        .latestBook,
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
            .navigationTitle(MR.strings().book_monitoring.localized())
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
