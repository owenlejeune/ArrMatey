//
//  NoInstanceView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-04.
//

import SwiftUI
import Shared

struct NoInstanceView: View {
    let type: InstanceType
    
    @EnvironmentObject private var navigation: NavigationManager
    
    var body: some View {
        VStack(alignment: .center, spacing: 12) {
            Image(systemName: "externaldrive.fill.trianglebadge.exclamationmark")
                .font(.system(size: 64))
                .imageScale(.large)
            
            VStack(spacing: 4) {
                Text(MR.strings().no_type_instances.formatted(args: [type.name]))
                    .font(.system(size: 20, weight: .bold))
                    .multilineTextAlignment(.center)
            
                Text(MR.strings().no_type_instances_message.formatted(args: [type.name]))
                    .multilineTextAlignment(.center)
            }
            
            Button(action: {
                navigation.goToNewInstance(of: type)
            }) {
                HStack {
                    Image(systemName: "plus.circle.fill")
                        .foregroundColor(.primary)
                    Text(MR.strings().add_instance.localized())
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(.primary)
                }
                .padding(.horizontal, 32)
                .padding(.vertical, 12)
                .background(.primary.opacity(0.1))
                .clipShape(RoundedRectangle(cornerRadius: 10))
            }
        }
        .padding(.horizontal, 24)
    }
}
