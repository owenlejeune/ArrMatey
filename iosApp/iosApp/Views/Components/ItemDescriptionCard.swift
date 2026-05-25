//
//  ItemDescriptionCard.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-15.
//

import SwiftUI

struct ItemDescriptionCard: View {
    let overview: String?
     private let decodedOverview: String?
    
    @State private var expanded = false
    
     init(overview: String?) {
         self.overview = overview
         self.decodedOverview = overview?.decodingHTMLEntities()
     }
    
    var body: some View {
        if let decodedOverview {
            VStack {
                Text(decodedOverview)
                    .font(.system(size: 14))
                    .lineLimit(expanded ? nil : 10)
                    .truncationMode(.tail)
                    .transition(.slide)
                    .background()
                    .onTapGesture {
                        withAnimation(.snappy) { expanded = true }
                    }
            }
        }
    }
}
