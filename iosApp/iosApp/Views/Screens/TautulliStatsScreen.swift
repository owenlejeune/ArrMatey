import Shared
import SwiftUI

struct TautulliStatsScreen: View {
    @StateObject private var viewModel = TautulliStatsViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        Group {
            if viewModel.state is TautulliStatsStateLoading || viewModel.state is TautulliStatsStateInitial {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = viewModel.state as? TautulliStatsStateError {
                VStack(spacing: 12) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 48))
                        .foregroundColor(.red)
                    Text(error.message)
                        .foregroundColor(.secondary)
                    Button(MR.strings().retry.localized()) {
                        viewModel.refresh()
                    }
                    .buttonStyle(.borderedProminent)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let success = viewModel.state as? TautulliStatsStateSuccess {
                let activeSections = success.sections.filter { !$0.rows.isEmpty }
                if activeSections.isEmpty {
                    VStack(spacing: 8) {
                        Text(MR.strings().no_stats_available.localized())
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        VStack(spacing: 16) {
                            filterControls

                            LazyVStack(spacing: 16) {
                                ForEach(activeSections, id: \.statTitle) { section in
                                    HomeStatSectionCardView(section: section, instance: success.instance)
                                }
                            }
                        }
                        .padding(16)
                    }
                    .refreshable {
                        viewModel.refresh()
                    }
                }
            }
        }
        .navigationTitle(MR.strings().statistics.localized())
        .navigationBarTitleDisplayMode(.inline)
    }

    private var filterControls: some View {
        VStack(spacing: 10) {
            Picker(MR.strings().statistics.localized(), selection: Binding(
                get: { viewModel.timeRange },
                set: { viewModel.setTimeRange($0) }
            )) {
                Text(MR.strings().last_7_days.localized()).tag(Int32(7))
                Text(MR.strings().last_30_days.localized()).tag(Int32(30))
                Text(MR.strings().all_time.localized()).tag(Int32(0))
            }
            .pickerStyle(.segmented)

            Picker(MR.strings().statistics.localized(), selection: Binding(
                get: { viewModel.statsType },
                set: { viewModel.setStatsType($0) }
            )) {
                Text(MR.strings().plays.localized()).tag(TautulliStatsType.plays)
                Text(MR.strings().watch_time.localized()).tag(TautulliStatsType.duration)
            }
            .pickerStyle(.segmented)
        }
    }
}

struct HomeStatSectionCardView: View {
    let section: TautulliHomeStatSection
    let instance: Instance

    private var sectionTitle: String {
        switch section.statId {
        case .topMovies: return MR.strings().top_movies.localized()
        case .popularMovies: return MR.strings().popular_movies.localized()
        case .topTv: return MR.strings().top_tv.localized()
        case .popularTv: return MR.strings().popular_tv.localized()
        case .topMusic: return MR.strings().top_music.localized()
        case .popularMusic: return MR.strings().popular_music.localized()
        case .topPlatforms: return MR.strings().top_platforms.localized()
        default: return section.statTitle ?? section.statId?.value ?? ""
        }
    }

    private var sectionIcon: String {
        switch section.statId {
        case .topMovies, .popularMovies: return "film"
        case .topTv, .popularTv: return "tv"
        case .topMusic, .popularMusic: return "music.note"
        case .topPlatforms: return "desktopcomputer"
        default: return "chart.bar.xaxis"
        }
    }

    private var maxCount: Int64 {
        section.rows.compactMap { $0.totalPlays?.int64Value ?? $0.totalDuration?.int64Value }.max() ?? 1
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(spacing: 8) {
                Image(systemName: sectionIcon)
                    .font(.headline)
                    .foregroundColor(Color(hex: 0x2e7d32))

                Text(sectionTitle)
                    .font(.headline.bold())
                    .foregroundColor(Color(hex: 0x2e7d32))

                Spacer()
            }

            VStack(spacing: 12) {
                ForEach(section.rows.indices, id: \.self) { index in
                    let row = section.rows[index]
                    HStack(alignment: .center, spacing: 12) {
                        Text("\(index + 1)")
                            .font(.caption2.bold())
                            .foregroundColor(Color(hex: 0x1b5e20))
                            .frame(width: 24, height: 24)
                            .background(Color(hex: 0xc8e6c9))
                            .clipShape(Circle())

                        let thumbUrl = TautulliRepositoryKt.buildPmsImageUrl(instance: instance, imagePath: row.thumb) ?? row.userThumb
                        if let thumbStr = thumbUrl, let url = URL(string: thumbStr) {
                            ArrAsyncImage(url: url) { image in
                                image.resizable()
                            } placeholder: {
                                Color(UIColor.tertiarySystemBackground)
                            }
                            .frame(width: 42, height: 60)
                            .clipShape(RoundedRectangle(cornerRadius: 6))
                        } else {
                            Color(UIColor.tertiarySystemBackground)
                                .frame(width: 42, height: 60)
                                .clipShape(RoundedRectangle(cornerRadius: 6))
                        }

                        VStack(alignment: .leading, spacing: 4) {
                            let titleText = row.title ?? row.friendlyName ?? row.user ?? row.platform ?? ""
                            Text(titleText)
                                .font(.subheadline.bold())
                                .lineLimit(1)

                            if let sub = row.grandparentTitle ?? row.parentTitle, !sub.isEmpty {
                                Text(sub)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                    .lineLimit(1)
                            }

                            let rowCount = row.totalPlays?.int64Value ?? row.totalDuration?.int64Value ?? 0
                            let fraction = maxCount > 0 ? Double(rowCount) / Double(maxCount) : 0.0

                            ProgressView(value: min(max(fraction, 0.0), 1.0))
                                .progressViewStyle(.linear)
                                .tint(Color(hex: 0x2e7d32))
                        }

                        Spacer()

                        if let plays = row.totalPlays?.int32Value {
                            Text(MR.plurals().plays_count.localized(plays))
                                .font(.caption.bold())
                                .foregroundColor(.secondary)
                        } else if let duration = row.totalDuration?.int64Value {
                            Text(formatDuration(duration))
                                .font(.caption.bold())
                                .foregroundColor(.secondary)
                        }
                    }
                }
            }
        }
        .padding(16)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(16)
    }

    private func formatDuration(_ seconds: Int64) -> String {
        if seconds <= 0 { return "0h" }
        let hours = seconds / 3600
        let days = hours / 24
        if days > 0 {
            return "\(days)d \(hours % 24)h"
        }
        return "\(hours)h"
    }
}
