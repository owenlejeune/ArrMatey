//
//  ClearLogoView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-12.
//

import SwiftUI
import Shared

struct ClearLogoView: View {
    let item: ArrMedia
    
    @Environment(\.colorScheme) private var colorScheme
    
    var body: some View {
        if let clearLogo = item.getClearLogo()?.remoteUrl {
            AsyncImage(url: URL(string: clearLogo)) { phase in
                if let image = phase.image {
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(minHeight: 64)
                        .shadow(color: Color.black.opacity(0.6), radius: 3, x: 0, y: 1.5)
                }
            }
            .frame(maxWidth: .infinity, alignment: .center)
        }
    }
}
