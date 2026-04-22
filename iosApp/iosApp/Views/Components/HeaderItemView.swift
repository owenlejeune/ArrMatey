//
//  HeaderItemView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-15.
//

import Shared
import SwiftUI

struct HeaderItemView: View {
    @Binding private var header: InstanceHeader
    private let availableSsids: [String]
    private let localNetworkConfigured: Bool
    
    init(
        header: Binding<InstanceHeader>,
        availableSsids: [String] = [],
        localNetworkConfigured: Bool = false
    ) {
        self._header = header
        self.availableSsids = availableSsids
        self.localNetworkConfigured = localNetworkConfigured
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 24) {
                Text(MR.strings().header_name.localized()).layoutPriority(2)
                TextField(
                    text: Binding(
                        get: { header.key },
                        set: { header = InstanceHeader(key: $0, value: header.value, restrictionType: header.restrictionType, restrictedSsids: header.restrictedSsids) }
                    ),
                    prompt: Text("X-Custom-Header")
                ) {
                    EmptyView()
                }
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .multilineTextAlignment(.trailing)
            }
            
            HStack(spacing: 12) {
                Text(MR.strings().header_value.localized()).layoutPriority(2)
                TextField(
                    text: Binding(
                        get: { header.value },
                        set: { header = InstanceHeader(key: header.key, value: $0, restrictionType: header.restrictionType, restrictedSsids: header.restrictedSsids) }
                    ),
                    prompt: Text("value")
                ) {
                    EmptyView()
                }
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .multilineTextAlignment(.trailing)
            }
            
            if localNetworkConfigured {
                Picker("Restriction", selection: Binding(
                    get: { header.restrictionType },
                    set: { header = InstanceHeader(key: header.key, value: header.value, restrictionType: $0, restrictedSsids: header.restrictedSsids) }
                )) {
                    Text("Always").tag(HeaderRestrictionType.always)
                    Text("Remote Only").tag(HeaderRestrictionType.remoteOnly)
                    Text("Specific SSIDs").tag(HeaderRestrictionType.specificSsids)
                }
                .pickerStyle(.segmented)
                
                if header.restrictionType == .specificSsids {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack {
                            ForEach(availableSsids, id: \.self) { ssid in
                                let isSelected = header.restrictedSsids.contains(ssid) || availableSsids.count == 1
                                Button(action: {
                                    guard availableSsids.count > 1 else { return }
                                    var newSsids = header.restrictedSsids
                                    if isSelected {
                                        newSsids.removeAll { $0 == ssid }
                                    } else {
                                        newSsids.append(ssid)
                                    }
                                    header = InstanceHeader(key: header.key, value: header.value, restrictionType: header.restrictionType, restrictedSsids: newSsids)
                                }) {
                                    Text(ssid)
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 6)
                                        .background(isSelected ? Color.accentColor : Color.secondary.opacity(0.2))
                                        .foregroundColor(isSelected ? .white : .primary)
                                        .cornerRadius(16)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }
                }
            }
        }
        .padding(.vertical, 4)
    }
}
