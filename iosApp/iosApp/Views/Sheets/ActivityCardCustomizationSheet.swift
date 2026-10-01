//
//  ActivityCardCustomizationSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct ActivityCardCustomizationSheet: View {
    @ObservedObject var viewModel: MoreScreenViewModelS
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    ActivityQueueItem(
                        item: MockData.shared.mockQueueItem,
                        useFullColorCards: viewModel.useColoredActivityCards,
                        onClick: {}
                    )
                    .padding(.horizontal)

                    Toggle(isOn: Binding(
                        get: { viewModel.useColoredActivityCards },
                        set: { _ in viewModel.toggleUseColoredActivityCards() }
                    )) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(MR.strings().use_colored_activity_cards.localized())
                            Text(MR.strings().use_colored_activity_cards_desc.localized())
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.vertical)
            }
            .navigationTitle(MR.strings().activity_cards.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "checkmark")
                    }
                }
            }
        }
    }
}
