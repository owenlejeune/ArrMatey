//
//  CalendarCardCustomizationSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct CalendarCardCustomizationSheet: View {
    @ObservedObject var viewModel: MoreScreenViewModelS
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    MovieCalendarItem(
                        movie: MockData.shared.mockMovie,
                        date: LocalDate(year: 2026, month: 1, day: 1),
                        instances: [],
                        useFullColorCards: viewModel.useColoredCalendarCards,
                        posterImage: InstanceType.radarr.mockCover,
                        onNavigate: { _ in }
                    )
                    .padding(.horizontal)

                    Toggle(isOn: Binding(
                        get: { viewModel.useColoredCalendarCards },
                        set: { _ in viewModel.toggleUseColoredCalendarCards() }
                    )) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(MR.strings().use_colored_calendar_cards.localized())
                            Text(MR.strings().use_colored_calendar_cards_desc.localized())
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.vertical)
            }
            .navigationTitle(MR.strings().calendar_cards.localized())
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
