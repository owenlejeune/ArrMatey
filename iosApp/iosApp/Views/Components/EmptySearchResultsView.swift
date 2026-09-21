//
//  EmptySearchResultsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-30.
//

import Shared
import SwiftUI

struct EmptySearchResultsView: View {
    let type: InstanceType
    let query: String
    let onShouldSearch: () -> Void
    
    private var mediaType: String {
        type == .sonarr ? "series" : "movie"
    }
    
    var body: some View {
        ContentUnavailableView {
            Label(
                MR.strings().no_query_results.formatted(args: [query]),
                systemImage: "magnifyingglass"
            )
        } description: {
            Text(MR.strings().no_query_results_label.localized())
        } actions: {
            Button(action: onShouldSearch) {
                Label(
                    MR.strings().no_query_results_link.formatted(args: [mediaType]),
                    systemImage: "globe"
                )
            }
            .buttonStyle(.borderedProminent)
            .padding(.top, 4)
        }
    }
}

