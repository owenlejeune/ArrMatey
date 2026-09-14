//
//  TracearrMediaAnalyticsComponents.swift
//  iosApp
//

import SwiftUI
import Shared

func formatMsAsDurationSwift(_ ms: Int64) -> String {
    let totalSeconds = ms / 1000
    let hours = totalSeconds / 3600
    let minutes = (totalSeconds % 3600) / 60
    if hours > 0 {
        return "\(hours)h \(minutes)m"
    } else {
        return "\(minutes)m"
    }
}

struct StatChip: View {
    let title: String
    let icon: String
    var color: Color? = nil

    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: icon)
                .font(.system(size: 14))
            Text(title)
                .font(.caption)
                .fontWeight(.medium)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(
            Capsule()
                .fill(color?.opacity(0.15) ?? Color.secondary.opacity(0.1))
        )
        .foregroundColor(color ?? .primary)
    }
}

struct TracearrSummaryChipRowView: View {
    let uiState: TracearrMediaUiState

    var body: some View {
        if uiState.isTracearrConfigured, let stats = uiState.stats, let allTime = stats.windows?.allTime?.combined {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    StatChip(title: MR.plurals().plays_count.localized(allTime.plays), icon: "play.fill", color: .accentColor)
                    StatChip(title: formatMsAsDurationSwift(allTime.watchTimeMs), icon: "clock.fill")
                    if allTime.uniqueUsers > 0 {
                        StatChip(title: MR.plurals().viewers_count.localized(Int32(allTime.uniqueUsers)), icon: "person.2.fill")
                    }
                    if let perServer = stats.windows?.allTime?.perServer, !perServer.isEmpty {
                        StatChip(title: MR.plurals().servers_count.localized(Int32(perServer.count)), icon: "server.rack")
                    }
                }
            }
        }
    }
}

struct TracearrAnalyticsSectionView: View {
    let uiState: TracearrMediaUiState
    let onWindowSelected: (TracearrStatsWindowType) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Picker("Time Window", selection: Binding(
                get: { uiState.selectedStatsWindow },
                set: { onWindowSelected($0) }
            )) {
                Text(MR.strings().all_time.localized()).tag(TracearrStatsWindowType.allTime)
                Text(MR.strings().last_30_days.localized()).tag(TracearrStatsWindowType.last30)
                Text(MR.strings().last_7_days.localized()).tag(TracearrStatsWindowType.last7)
            }
            .pickerStyle(.segmented)

            HStack(spacing: 12) {
                MetricCard(title: MR.strings().total_plays.localized(), value: "\(uiState.totalPlays)", icon: "play.fill")
                MetricCard(title: MR.strings().watch_time.localized(), value: formatMsAsDurationSwift(uiState.totalWatchTimeMs), icon: "clock.fill")
                MetricCard(title: MR.strings().viewers.localized(), value: "\(uiState.uniqueUsers)", icon: "person.2.fill")
            }

            if !uiState.perServerStats.isEmpty {
                VStack(alignment: .leading, spacing: 10) {
                    Text(MR.strings().per_server_breakdown.localized())
                        .font(.headline)

                    ForEach(uiState.perServerStats, id: \.self) { serverStat in
                        HStack {
                            Label(serverStat.serverName ?? serverStat.serverId ?? MR.strings().server.localized(), systemImage: "server.rack")
                                .font(.subheadline)
                                .fontWeight(.medium)
                            Spacer()
                            Text("\(MR.plurals().plays_count.localized(serverStat.plays)) • \(formatMsAsDurationSwift(serverStat.watchTimeMs))")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
                .padding()
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(12)
            }

            if let watchers = uiState.watchers?.watchers, !watchers.isEmpty {
                VStack(alignment: .leading, spacing: 10) {
                    Text(MR.strings().top_watchers.localized())
                        .font(.headline)

                    ForEach(watchers, id: \.self) { watcher in
                        WatcherRowView(watcher: watcher)
                    }
                }
            }
        }
    }
}

struct MetricCard: View {
    let title: String
    let value: String
    let icon: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Image(systemName: icon)
                .foregroundColor(.accentColor)
            Text(value)
                .font(.title3)
                .fontWeight(.bold)
                .lineLimit(1)
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }
}

struct WatcherRowView: View {
    let watcher: TracearrMediaWatcher

    var body: some View {
        HStack(spacing: 12) {
            Circle()
                .fill(Color.accentColor.opacity(0.2))
                .frame(width: 40, height: 40)
                .overlay(
                    Image(systemName: "person.fill")
                        .foregroundColor(.accentColor)
                )

            VStack(alignment: .leading, spacing: 2) {
                Text(watcher.user?.identityName ?? watcher.user?.username ?? MR.strings().users.localized())
                    .font(.subheadline)
                    .fontWeight(.semibold)
                Text("\(MR.plurals().plays_count.localized(watcher.plays)) • \(formatMsAsDurationSwift(watcher.watchTimeMs))")
                    .font(.caption)
                    .foregroundColor(.secondary)
                if let day = watcher.lastWatchedDay {
                    Text(MR.strings().last_watched.formatted(args: [day]))
                        .font(.caption2)
                        .foregroundColor(Color(UIColor.tertiaryLabel))
                }
            }

            Spacer()

            if let pct = watcher.completionPct {
                Text("\(pct.intValue)%")
                    .font(.caption)
                    .fontWeight(.bold)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.accentColor.opacity(0.15))
                    .foregroundColor(.accentColor)
                    .cornerRadius(8)
            }
        }
        .padding()
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }
}

struct TracearrHistorySectionView: View {
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    let uiState: TracearrMediaUiState
    let onLoadMore: () -> Void
    var onClickItem: ((TracearrHistoryItem) -> Void)? = nil
    var isLargeScreen: Bool? = nil

    private var effectiveIsLargeScreen: Bool {
        isLargeScreen ?? (horizontalSizeClass == .regular)
    }

    var body: some View {
        VStack(spacing: 12) {
            if uiState.historyItems.isEmpty && !uiState.isLoading {
                VStack(spacing: 8) {
                    Text(MR.strings().no_stream_history.localized())
                        .foregroundColor(.secondary)
                }
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(12)
            } else {
                if effectiveIsLargeScreen {
                    TracearrHistoryTableView(
                        items: uiState.historyItems,
                        hasMore: uiState.nextHistoryCursor != nil,
                        isLoadingMore: uiState.isLoadingHistoryMore,
                        onLoadMore: onLoadMore,
                        onClickItem: { item in
                            onClickItem?(item)
                        }
                    )
                } else {
                    ForEach(uiState.historyItems, id: \.id) { item in
                        TracearrHistoryCardView(item: item)
                            .contentShape(Rectangle())
                            .onTapGesture {
                                onClickItem?(item)
                            }
                    }

                    if uiState.nextHistoryCursor != nil {
                        HStack {
                            Spacer()
                            if uiState.isLoadingHistoryMore {
                                ProgressView()
                            } else {
                                Button(MR.strings().load_more_history.localized()) {
                                    onLoadMore()
                                }
                                .buttonStyle(.bordered)
                            }
                            Spacer()
                        }
                        .padding(.vertical, 8)
                    }
                }
            }
        }
    }
}
