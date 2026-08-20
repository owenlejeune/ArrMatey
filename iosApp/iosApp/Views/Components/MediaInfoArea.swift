//
//  MediaInfoArea.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-15.
//

import SwiftUI
import Shared

struct MediaInfoArea: View {
    let infoItems: [InfoItem]
    
    var body: some View {
        Section {
            VStack(spacing: 12) {
                ForEach(Array(infoItems), id: \.self) { info in
                    HStack(alignment: .center) {
                        Text(info.label)
                            .font(.system(size: 14))
                        Spacer(minLength: 2.0)
                        Text(info.value)
                            .font(.system(size: 14))
                            .foregroundColor(.themePrimary)
                            .lineLimit(2)
                            .truncationMode(.tail)
                            .multilineTextAlignment(.trailing)
                            .frame(maxWidth: .infinity, alignment: .trailing)
                    }
                    
                    if info != infoItems.last {
                        Divider()
                    }
                }
            }
        } header: {
            Text(MR.strings().information.localized())
                .font(.system(size: 26, weight: .bold))
        }
    }
}
