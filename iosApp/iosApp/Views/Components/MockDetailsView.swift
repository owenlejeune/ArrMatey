//
//  MockDetailsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-07.
//

import SwiftUI
import Shared

struct MockDetailsView: View {
    let item: MockMedia
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(item.detailString)
                .font(.system(size: 14))
                .lineSpacing(4)
            
            Text("Status: \(item.statusString)")
                .font(.system(size: 14))
                .lineSpacing(4)
            
            Spacer()
            
            ProgressView(value: item.statusProgress)
                .progressViewStyle(LinearProgressViewStyle(tint: .arrBlue))
                .frame(height: 6)
        }
    }
}
