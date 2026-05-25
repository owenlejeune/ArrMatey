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
//                        .background {
//                            if colorScheme == .light {
//                                RoundedRectangle(cornerRadius: 16)
//                                    .fill(Color.black.opacity(0.4))
//                                    .blur(radius: 5)
//                                    .padding(-4)
//                            }
//                        }
                } else {
                    Color.clear.frame(height: 64)
                }
            }
            .frame(maxWidth: .infinity, alignment: .center)
        } else {
            Text(item.title ?? MR.strings().unknown.localized())
                .font(.system(size: 36, weight: .bold))
                .lineLimit(4)
                .truncationMode(.tail)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity, alignment: .center)
        }
    }
}
