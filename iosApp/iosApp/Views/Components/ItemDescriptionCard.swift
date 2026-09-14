//
//  ItemDescriptionCard.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-15.
//

import SwiftUI

struct ItemDescriptionCard: View {
    let overview: String?

    @State private var expanded = false
    @State private var decodedOverview: String?

    init(overview: String?) {
        self.overview = overview
    }

    var body: some View {
        if let text = decodedOverview ?? overview {
            VStack {
                Text(text)
                    .font(.system(size: 14))
                    .lineLimit(expanded ? nil : 10)
                    .truncationMode(.tail)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.vertical, 12)
            .padding(.horizontal, 18)
            .background(Color(.secondarySystemBackground))
            .cornerRadius(12)
            .transition(.slide)
            .onTapGesture {
                if !expanded {
                    withAnimation(.snappy) { expanded = true }
                }
            }
            .onAppear {
                decode()
            }
            .onChange(of: overview) { _, _ in
                decode()
            }
        }
    }

    private func decode() {
        if let overview = overview {
            decodedOverview = overview.decodingHTMLEntities()
        }
    }
}
