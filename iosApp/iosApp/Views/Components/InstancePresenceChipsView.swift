//
//  InstancePresenceChipsView.swift
//  iosApp
//

import SwiftUI
import Shared

struct InstancePresenceChipsView: View {
    let presences: [InstanceMediaPresence]
    let selectedInstanceId: Int64?
    let onSelectInstance: (Int64) -> Void
    let onAddInstance: (Instance) -> Void

    var body: some View {
        if presences.count > 1 {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(presences, id: \.instance.id) { presence in
                        let isSelected = presence.instance.id == selectedInstanceId
                        let isPresent = presence.isPresent
                        let arrMedia = presence.arrMedia
                        
                        let (statusText, statusColor) = statusInfo(isPresent: isPresent, arrMedia: arrMedia, isSelected: isSelected)
                        
                        Button(action: {
                            if isPresent {
                                onSelectInstance(presence.instance.id)
                            } else {
                                onAddInstance(presence.instance)
                            }
                        }) {
                            HStack(spacing: 6) {
                                if !isPresent {
                                    Image(systemName: "plus")
                                        .font(.system(size: 11, weight: .bold))
                                        .foregroundStyle(isSelected ? Color.primary : Color.themePrimary)
                                } else {
                                    Circle()
                                        .fill(statusColor)
                                        .frame(width: 8, height: 8)
                                }

                                Text(presence.instance.label)
                                    .font(.subheadline.weight(.medium))
                                    .foregroundStyle(isSelected ? Color.primary : Color.secondary)

                                Text("• \(statusText)")
                                    .font(.caption)
                                    .foregroundStyle(isSelected ? Color.primary.opacity(0.8) : Color.secondary.opacity(0.8))
                            }
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(isSelected ? Color.themePrimary.opacity(0.15) : Color(UIColor.secondarySystemBackground))
                            )
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(
                                        isSelected ? Color.themePrimary : (isPresent ? Color.clear : Color.secondary.opacity(0.3)),
                                        lineWidth: isSelected ? 1.5 : 1
                                    )
                            )
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.vertical, 2)
            }
        }
    }

    private func statusInfo(isPresent: Bool, arrMedia: ArrMedia?, isSelected: Bool) -> (String, Color) {
        if !isPresent {
            return (MR.strings().not_added.localized(), isSelected ? Color.secondary : Color.gray)
        }
        if let media = arrMedia {
            if media.isDownloaded {
                return (MR.strings().downloaded.localized(), isSelected ? Color(red: 0.29, green: 0.87, blue: 0.5) : Color.green)
            }
            if let movie = media as? ArrMovie, movie.hasFile {
                return (MR.strings().downloaded.localized(), isSelected ? Color(red: 0.29, green: 0.87, blue: 0.5) : Color.green)
            }
            if let series = media as? ArrSeries, let stats = series.statistics, stats.episodeFileCount > 0 {
                return (MR.strings().downloaded.localized(), isSelected ? Color(red: 0.29, green: 0.87, blue: 0.5) : Color.green)
            }
            if media.monitored {
                return (MR.strings().monitored.localized(), isSelected ? Color.yellow : Color.orange)
            }
        }
        return (MR.strings().unmonitored.localized(), isSelected ? Color.secondary.opacity(0.8) : Color.gray)
    }
}
