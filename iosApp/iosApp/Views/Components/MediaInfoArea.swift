//
//  MediaInfoArea.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-15.
//

import SwiftUI
import Shared

struct MediaInfoArea: View {
    let arrItems: [InfoItem]
    let seerrItems: [InfoItem]
    let arrInstance: Instance?
    let seerrInstance: Instance?
    
    init(
        arrItems: [InfoItem] = [],
        seerrItems: [InfoItem] = [],
        arrInstance: Instance? = nil,
        seerrInstance: Instance? = nil
    ) {
        self.arrItems = arrItems
        self.seerrItems = seerrItems
        self.arrInstance = arrInstance
        self.seerrInstance = seerrInstance
    }
    
    init(infoItems: [InfoItem]) {
        self.arrItems = infoItems
        self.seerrItems = []
        self.arrInstance = nil
        self.seerrInstance = nil
    }
    
    private var showFooters: Bool {
        !arrItems.isEmpty && !seerrItems.isEmpty
    }
    
    var body: some View {
        if !arrItems.isEmpty || !seerrItems.isEmpty {
            VStack(alignment: .leading, spacing: 12) {
                Text(MR.strings().information.localized())
                    .font(.title3.bold())
                
                if !arrItems.isEmpty {
                    infoCard(items: arrItems, instance: showFooters ? arrInstance : nil)
                }
                
                if !seerrItems.isEmpty {
                    infoCard(items: seerrItems, instance: showFooters ? seerrInstance : nil)
                }
            }
        }
    }
    
    @ViewBuilder
    private func infoCard(items: [InfoItem], instance: Instance?) -> some View {
        VStack(spacing: 0) {
            ForEach(items) { info in
                Button(action: {
                    info.onClick?()
                }) {
                    HStack(alignment: .center) {
                        Text(info.label)
                            .font(.system(size: 14))
                            .foregroundColor(.primary)
                        Spacer(minLength: 2.0)
                        Text(info.value)
                            .font(.system(size: 14))
                            .foregroundColor(.themePrimary)
                            .lineLimit(2)
                            .truncationMode(.tail)
                            .multilineTextAlignment(.trailing)
                            .frame(maxWidth: .infinity, alignment: .trailing)
                    }
                    .padding(.vertical, 12)
                    .contentShape(Rectangle())
                }
                .disabled(info.onClick == nil)
                
                if info != items.last || instance != nil {
                    Divider()
                }
            }
            
            if let instance = instance {
                HStack(spacing: 12) {
                    instance.type.icon.toImage(renderingMode: .original)
                        .frame(width: 8, height: 8)
                    Text(instance.label)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.secondary)
                    Spacer()
                }
                .padding(.top, 12)
            }
        }
        .padding(.horizontal, 18)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(12)
    }
}
