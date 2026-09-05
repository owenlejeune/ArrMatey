import Shared
import SwiftUI

struct TracearrTab: View {
    @Environment(\.navigationContext) private var context
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack(path: $navigationManager.tracearrPath) {
                TracearrTabContent()
            }
        case .launcher:
            TracearrTabContent()
        }
    }
}

struct TracearrTabContent: View {
    @StateObject private var viewModel = TracearrViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        Group {
            if viewModel.state is TracearrStreamsStateNoInstance {
                NoInstanceView(type: .tracearr)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let success = viewModel.state as? TracearrStreamsStateSuccess {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        nowPlayingHeader(count: success.streams.count)

                        if success.streams.isEmpty {
                            VStack(spacing: 12) {
                                Image(systemName: "tv")
                                    .font(.system(size: 48))
                                    .foregroundColor(.secondary)
                                Text(MR.strings().no_active_streams.localized())
                                    .foregroundColor(.secondary)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.top, 40)
                        } else {
                            LazyVStack(spacing: 16) {
                                ForEach(success.streams, id: \.id) { session in
                                    TracearrStreamCardView(session: session)
                                }
                            }
                        }
                    }
                    .padding(16)
                }
                .refreshable {
                    viewModel.refresh()
                }
            } else if viewModel.state is TracearrStreamsStateLoading || viewModel.state is TracearrStreamsStateInitial {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = viewModel.state as? TracearrStreamsStateError {
                VStack(spacing: 12) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 48))
                        .foregroundColor(.red)
                    Text(error.message)
                        .foregroundColor(.secondary)
                    Button(MR.strings().retry.localized()) {
                        viewModel.loadStreams()
                    }
                    .buttonStyle(.borderedProminent)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .navigationTitle(MR.strings().tracearr.localized())
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button {
                    navigationManager.showLauncher = true
                } label: {
                    Image(systemName: "line.3.horizontal")
                }
            }
        }
    }

    @ViewBuilder
    private func nowPlayingHeader(count: Int) -> some View {
        HStack(spacing: 8) {
            Image(systemName: "tv")
                .font(.title3)
                .foregroundColor(.accentColor)
            Text(MR.strings().now_playing.localized())
                .font(.title3.bold())
            Text(MR.plurals().streams.localized(Int32(count)))
                .font(.caption.bold())
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(Color(UIColor.secondarySystemBackground))
                .clipShape(Capsule())
            Spacer()
        }
    }
}

struct TracearrStreamCardView: View {
    let session: TracearrStreamSession

    private var isPaused: Bool {
        session.state?.caseInsensitiveCompare("paused") == .orderedSame
    }

    private var isPlaying: Bool {
        session.state?.caseInsensitiveCompare("playing") == .orderedSame
    }

    private var stateColor: Color {
        if isPaused { return .orange }
        if isPlaying { return .green }
        return .blue
    }

    private var stateText: String {
        if isPaused { return MR.strings().paused.localized() }
        if isPlaying { return MR.strings().playing.localized() }
        return session.state?.capitalized ?? MR.strings().active.localized()
    }

    private var totalMs: Int64 {
        session.totalDurationMs?.int64Value ?? session.durationMs?.int64Value ?? 0
    }

    private var progressMs: Int64 {
        session.progressMs?.int64Value ?? 0
    }

    private var progressFraction: Double {
        if totalMs > 0 {
            return min(max(Double(progressMs) / Double(totalMs), 0.0), 1.0)
        }
        return 0.0
    }

    var body: some View {
        HStack(spacing: 0) {
            // Left Accent Bar
            Rectangle()
                .fill(stateColor)
                .frame(width: 4)

            VStack(alignment: .leading, spacing: 12) {
                HStack(alignment: .top, spacing: 12) {
                    // Poster / Thumbnail
                    ZStack {
                        let imageUrl = session.posterUrl ?? session.thumbPath
                        if let urlStr = imageUrl, urlStr.hasPrefix("http://") || urlStr.hasPrefix("https://"), let url = URL(string: urlStr) {
                            AsyncImage(url: url) { image in
                                image.resizable().aspectRatio(contentMode: .fill)
                            } placeholder: {
                                Color(UIColor.secondarySystemBackground)
                            }
                        } else {
                            Color(UIColor.secondarySystemBackground)
                        }

                        // Play/Pause Overlay Icon
                        Circle()
                            .fill(Color.black.opacity(0.6))
                            .frame(width: 36, height: 36)
                            .overlay(
                                Image(systemName: isPaused ? "pause.fill" : "play.fill")
                                    .foregroundColor(.white)
                                    .font(.system(size: 16))
                            )
                    }
                    .frame(width: 80, height: 120)
                    .clipShape(RoundedRectangle(cornerRadius: 8))

                    // Content Column
                    VStack(alignment: .leading, spacing: 6) {
                        // User & Device Row
                        HStack(spacing: 6) {
                            let username = session.effectiveUsername.isEmpty ? MR.strings().user.localized() : session.effectiveUsername
                            let avatarUrl = session.effectiveUserAvatar

                            if let avatarStr = avatarUrl, let url = URL(string: avatarStr) {
                                AsyncImage(url: url) { image in
                                    image.resizable().aspectRatio(contentMode: .fill)
                                } placeholder: {
                                    Circle().fill(Color.orange)
                                }
                                .frame(width: 22, height: 22)
                                .clipShape(Circle())
                            } else {
                                Circle()
                                    .fill(Color.orange)
                                    .frame(width: 22, height: 22)
                                    .overlay(
                                        Text(String(username.prefix(1)).uppercased())
                                            .font(.caption2.bold())
                                            .foregroundColor(.white)
                                    )
                            }

                            Text(username)
                                .font(.subheadline.bold())
                                .lineLimit(1)

                            Spacer()

                            let isTranscoding = session.isTranscode?.boolValue == true ||
                                session.videoDecision == .transcode ||
                                session.audioDecision == .transcode

                            if isTranscoding {
                                Image(systemName: "bolt.fill")
                                    .font(.system(size: 12))
                                    .foregroundColor(.yellow)
                                    .padding(4)
                                    .background(Color.yellow.opacity(0.2))
                                    .clipShape(Circle())
                            }

                            Image(systemName: isTvDevice ? "tv" : "iphone")
                                .font(.system(size: 12))
                                .foregroundColor(.secondary)
                                .padding(4)
                                .background(Color(UIColor.tertiarySystemBackground))
                                .clipShape(RoundedRectangle(cornerRadius: 4))

                            if session.canTerminate?.boolValue == true {
                                Image(systemName: "xmark")
                                    .font(.system(size: 12))
                                    .foregroundColor(.secondary)
                            }
                        }

                        // Title
                        let displayTitle = session.grandparentTitle ?? session.showTitle ?? session.mediaTitle ?? MR.strings().unknown.localized()
                        Text(displayTitle)
                            .font(.headline)
                            .bold()
                            .lineLimit(1)

                        // Subtitle
                        Text(subtitleText)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .lineLimit(1)

                        Spacer()

                        // Progress Bar
                        ProgressView(value: progressFraction)
                            .progressViewStyle(.linear)
                            .tint(Color.accentColor)

                        // Time & State Row
                        HStack {
                            Text(formatTimeMs(progressMs))
                                .font(.caption2)
                                .foregroundColor(.secondary)

                            Spacer()

                            Text(stateText)
                                .font(.caption2.bold())
                                .foregroundColor(stateColor)
                        }
                    }
                }

                Divider()

                // Footer Row
                HStack {
                    let serverName = session.effectiveServerName.isEmpty ? MR.strings().server.localized() : session.effectiveServerName
                    let location = session.geoCountry ?? session.ipAddress ?? MR.strings().local_network.localized()
                    Text("\(serverName) · \(location)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)

                    Spacer()

                    let quality = session.quality ?? session.resolution ?? "1080p"
                    Text(quality)
                        .font(.caption.bold())
                        .foregroundColor(.secondary)
                }
            }
            .padding(12)
        }
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }

    private var isTvDevice: Bool {
        if let platform = session.platform, platform.localizedCaseInsensitiveContains("TV") { return true }
        if let device = session.device, device.localizedCaseInsensitiveContains("TV") { return true }
        return false
    }

    private var subtitleText: String {
        if session.mediaType == .episode || (session.seasonNumber != nil && session.episodeNumber != nil) {
            let s = session.seasonNumber?.intValue ?? 0
            let e = session.episodeNumber?.intValue ?? 0
            let sStr = String(format: "%02d", s)
            let eStr = String(format: "%02d", e)
            let epTitle = session.mediaTitle ?? ""
            return "S\(sStr) E\(eStr) · \(epTitle)"
        } else if session.mediaType == .movie {
            let yearStr = session.year?.stringValue ?? ""
            let movieTitle = session.mediaTitle ?? ""
            return "\(yearStr) · \(movieTitle)"
        } else {
            let parts = [session.artistName, session.albumName, session.mediaTitle].compactMap { $0 }
            return parts.joined(separator: " · ")
        }
    }

    private func formatTimeMs(_ ms: Int64) -> String {
        if ms <= 0 { return "0:00" }
        let totalSeconds = ms / 1000
        let seconds = totalSeconds % 60
        let minutes = (totalSeconds / 60) % 60
        let hours = totalSeconds / 3600
        let secondsStr = String(format: "%02d", seconds)
        if hours > 0 {
            let minutesStr = String(format: "%02d", minutes)
            return "\(hours):\(minutesStr):\(secondsStr)"
        } else {
            return "\(minutes):\(secondsStr)"
        }
    }
}
