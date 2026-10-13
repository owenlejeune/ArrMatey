import Shared
import SwiftUI

struct PlaybackStreamCardView: View {
    let session: PlaybackStreamSession
    var onTerminate: (() -> Void)? = nil

    @State private var currentProgressMs: Int64 = 0

    private var isPlaying: Bool {
        session.state?.caseInsensitiveCompare("playing") == .orderedSame
    }

    private var isPaused: Bool {
        session.state?.caseInsensitiveCompare("paused") == .orderedSame
    }

    private var totalMs: Int64 {
        session.totalDurationMs?.int64Value ?? 0
    }

    private var remainingMs: Int64 {
        max(totalMs - currentProgressMs, 0)
    }

    private var progressFraction: Double {
        if totalMs > 0 {
            return min(max(Double(currentProgressMs) / Double(totalMs), 0.0), 1.0)
        }
        return min(max(Double(session.progressPercent), 0.0), 1.0)
    }

    private var progressPercentInt: Int {
        if totalMs > 0 {
            return Int((progressFraction * 100).rounded())
        }
        return Int((Double(session.progressPercent) * 100).rounded())
    }

    private var stateText: String {
        if isPaused { return MR.strings().paused.localized() }
        if isPlaying { return MR.strings().playing.localized() }
        return session.state?.capitalized ?? MR.strings().active.localized()
    }

    private var stateColor: Color {
        if isPaused { return .orange }
        if isPlaying { return .green }
        return .secondary
    }

    private var edgeColor: Color {
        Color.serverColor(type: session.serverType, name: session.serverName)
    }

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(edgeColor)
                .frame(width: 4)

            VStack(alignment: .leading, spacing: 12) {
                HStack(alignment: .top, spacing: 12) {
                    ZStack {
                        if let posterUrl = session.posterUrl, let url = URL(string: posterUrl) {
                            ArrAsyncImage(url: url) { image in
                                image.resizable()
                            } placeholder: {
                                Color(UIColor.secondarySystemBackground)
                            }
                            .aspectRatio(contentMode: .fill)
                        } else {
                            Color(UIColor.secondarySystemBackground)
                        }

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

                    VStack(alignment: .leading, spacing: 6) {
                        HStack(spacing: 6) {
                            let username = session.userDisplayName.isEmpty ? session.username : session.userDisplayName
                            let avatarUrl = session.userAvatarUrl

                            if let avatarStr = avatarUrl, let url = URL(string: avatarStr) {
                                ArrAsyncImage(url: url) { image in
                                    image.resizable()
                                } placeholder: {
                                    Circle().fill(Color.orange)
                                }
                                .aspectRatio(contentMode: .fill)
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

                            let isTranscoding = session.isTranscoding || session.streamDecision == .transcode
                            if isTranscoding {
                                Image(systemName: "bolt.fill")
                                    .font(.system(size: 12))
                                    .foregroundColor(.yellow)
                                    .padding(4)
                                    .background(Color.yellow.opacity(0.2))
                                    .clipShape(Circle())
                            }

                            PlaybackDeviceIconView(platform: session.platform, device: session.device ?? session.player)

                            if let onTerminate = onTerminate {
                                Button(action: onTerminate) {
                                    Image(systemName: "xmark")
                                        .font(.system(size: 12))
                                        .foregroundColor(.secondary)
                                }
                                .buttonStyle(.plain)
                            }
                        }

                        Text(session.displayTitle)
                            .font(.headline)
                            .bold()
                            .lineLimit(1)

                        if let subtitle = session.subtitle ?? session.parentTitle, !subtitle.isEmpty {
                            Text(subtitle)
                                .font(.caption)
                                .foregroundColor(.secondary)
                                .lineLimit(1)
                        }

                        Text(stateText)
                            .font(.caption2.bold())
                            .foregroundColor(stateColor)
                            .padding(.vertical, 2)

                        ProgressView(value: progressFraction)
                            .progressViewStyle(.linear)
                            .tint(Color.accentColor)

                        HStack {
                            Text(formatTimeMs(currentProgressMs))
                                .font(.caption2)
                                .foregroundColor(.secondary)

                            Spacer()

                            if totalMs > 0 {
                                Text("-\(formatTimeMs(remainingMs))")
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            } else {
                                Text("\(progressPercentInt)%")
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                }

                Divider()

                HStack {
                    let serverName = session.serverName ?? MR.strings().server.localized()
                    let deviceLabel = session.device ?? session.player ?? session.ipAddress ?? ""
                    let detail = deviceLabel.isEmpty ? serverName : "\(serverName) · \(deviceLabel)"
                    Text(detail)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)

                    Spacer()

                    let quality = session.videoResolution ?? "\(progressPercentInt)%"
                    Text(quality)
                        .font(.caption.bold())
                        .foregroundColor(.secondary)
                }
            }
            .padding(12)
        }
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
        .task(id: "\(session.sessionId)-\(session.progressMs?.int64Value ?? 0)-\(isPlaying)") {
            currentProgressMs = session.progressMs?.int64Value ?? (totalMs > 0 ? Int64(Double(session.progressPercent) * Double(totalMs)) : 0)
            guard isPlaying else { return }
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                guard !Task.isCancelled else { break }
                if totalMs == 0 || currentProgressMs < totalMs {
                    currentProgressMs += 1000
                }
            }
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

struct PlaybackDeviceIconView: View {
    let platform: String?
    let device: String?

    private var systemImageName: String {
        let combined = "\(platform ?? "") \(device ?? "")".lowercased()
        if combined.contains("iphone") || combined.contains("android phone") || combined.contains("mobile") {
            return "iphone"
        } else if combined.contains("ipad") || combined.contains("tablet") {
            return "ipad"
        } else if combined.contains("tv") || combined.contains("appletv") || combined.contains("roku") || combined.contains("firetv") || combined.contains("chromecast") {
            return "tv"
        } else if combined.contains("mac") || combined.contains("windows") || combined.contains("linux") || combined.contains("desktop") || combined.contains("web") || combined.contains("chrome") || combined.contains("firefox") || combined.contains("safari") {
            return "desktopcomputer"
        } else if combined.contains("playstation") || combined.contains("xbox") {
            return "gamecontroller"
        } else {
            return "display"
        }
    }

    var body: some View {
        Image(systemName: systemImageName)
            .font(.system(size: 12))
            .foregroundColor(.secondary)
            .padding(4)
            .background(Color(UIColor.tertiarySystemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 4))
    }
}
