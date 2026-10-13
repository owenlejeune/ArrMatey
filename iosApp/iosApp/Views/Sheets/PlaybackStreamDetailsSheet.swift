import Shared
import SwiftUI

struct PlaybackStreamDetailsSheet: View {
    let session: PlaybackStreamSession
    var onTerminateStream: ((String?) -> Void)? = nil
    var onNavigateToDetails: ((PlaybackMediaType?, Int64?) -> Void)? = nil
    var onNavigateToUser: ((String) -> Void)? = nil

    @Environment(\.dismiss) private var dismiss

    @State private var showTerminateAlert: Bool = false
    @State private var terminateMessage: String = ""
    @State private var currentProgressMs: Int64 = 0

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
        session.totalDurationMs?.int64Value ?? 0
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

    private var serverColor: Color {
        Color.serverColor(type: session.serverType, name: session.serverName)
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 12) {
                    HStack(spacing: 8) {
                        Image(systemName: isPlaying ? "play.fill" : "pause.fill")
                            .foregroundColor(stateColor)
                            .font(.system(size: 16))

                        Text(MR.strings().session_details.localized())
                            .font(.headline.bold())

                        Spacer()

                        Text(stateText)
                            .font(.caption.bold())
                            .foregroundColor(stateColor)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(stateColor.opacity(0.15))
                            .clipShape(Capsule())

                        Button {
                            dismiss()
                        } label: {
                            Image(systemName: "xmark")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(.secondary)
                                .padding(6)
                                .background(Color(UIColor.tertiarySystemBackground))
                                .clipShape(Circle())
                        }
                    }
                    .padding(.top, 4)

                    mediaHeaderCard

                    userCard

                    streamInfoCard

                    if onTerminateStream != nil {
                        Button(role: .destructive) {
                            showTerminateAlert = true
                        } label: {
                            HStack {
                                Image(systemName: "stop.circle.fill")
                                Text(MR.strings().terminate_stream.localized())
                            }
                            .frame(maxWidth: .infinity)
                            .padding(14)
                            .background(Color.red.opacity(0.15))
                            .foregroundColor(.red)
                            .cornerRadius(12)
                        }
                        .padding(.top, 8)
                    }
                }
                .padding(16)
            }
            .background(Color(UIColor.systemGroupedBackground))
            .alert(MR.strings().terminate_stream_dialog_title.localized(), isPresented: $showTerminateAlert) {
                TextField(MR.strings().terminate_stream_dialog_message.localized(), text: $terminateMessage)
                Button(MR.strings().cancel.localized(), role: .cancel) {}
                Button(MR.strings().terminate_stream_confirm.localized(), role: .destructive) {
                    let msg = terminateMessage.trimmingCharacters(in: .whitespacesAndNewlines)
                    onTerminateStream?(msg.isEmpty ? nil : msg)
                    dismiss()
                }
            } message: {
                Text(MR.strings().terminate_stream_dialog_message.localized())
            }
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
    }

    private var mediaHeaderCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .top, spacing: 12) {
                if let posterUrl = session.posterUrl, let url = URL(string: posterUrl) {
                    ArrAsyncImage(url: url) { image in
                        image.resizable()
                    } placeholder: {
                        Color(UIColor.secondarySystemBackground)
                    }
                    .frame(width: 75, height: 110)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text(session.displayTitle)
                        .font(.title3.bold())
                        .lineLimit(2)

                    if let subtitle = session.subtitle ?? session.parentTitle, !subtitle.isEmpty {
                        Text(subtitle)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .lineLimit(2)
                    }

                    if let yearStr = session.year, !yearStr.isEmpty {
                        Text(yearStr)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }

                    Spacer()

                    let serverTitle = session.serverName ?? MR.strings().server.localized()
                    Text(serverTitle)
                        .font(.subheadline.bold())
                        .foregroundColor(serverColor)
                }
                Spacer()
            }

            VStack(spacing: 4) {
                ProgressView(value: progressFraction)
                    .progressViewStyle(.linear)
                    .tint(Color.accentColor)

                HStack {
                    Text(formatTimeMs(currentProgressMs))
                        .font(.caption2)
                        .foregroundColor(.secondary)

                    Spacer()

                    if totalMs > 0 {
                        Text("-\(formatTimeMs(max(totalMs - currentProgressMs, 0)))")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    } else {
                        Text("\(progressPercentInt)%")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }
            }

            if let tmdbId = session.tmdbId?.int64Value, tmdbId > 0 {
                Button {
                    onNavigateToDetails?(session.mediaType, tmdbId)
                    dismiss()
                } label: {
                    HStack {
                        Image(systemName: "arrow.up.right.square")
                        Text(MR.strings().details.localized())
                    }
                    .font(.subheadline.bold())
                    .frame(maxWidth: .infinity)
                    .padding(8)
                    .background(Color.accentColor.opacity(0.12))
                    .foregroundColor(.accentColor)
                    .cornerRadius(8)
                }
            }
        }
        .padding(14)
        .background(Color(UIColor.secondarySystemGroupedBackground))
        .cornerRadius(14)
    }

    private var userCard: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 10) {
                let username = session.userDisplayName.isEmpty ? session.username : session.userDisplayName

                if let avatarStr = session.userAvatarUrl, let url = URL(string: avatarStr) {
                    ArrAsyncImage(url: url) { image in
                        image.resizable()
                    } placeholder: {
                        Circle().fill(Color.orange)
                    }
                    .frame(width: 38, height: 38)
                    .clipShape(Circle())
                } else {
                    Circle()
                        .fill(Color.orange)
                        .frame(width: 38, height: 38)
                        .overlay(
                            Text(String(username.prefix(1)).uppercased())
                                .font(.headline.bold())
                                .foregroundColor(.white)
                        )
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(username)
                        .font(.headline)
                        .lineLimit(1)

                    if let email = session.username as String?, email != username && email.contains("@") {
                        Text(email)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }

                Spacer()

                PlaybackDeviceIconView(platform: session.platform, device: session.device ?? session.player)
            }

            Divider()

            let device = session.device ?? session.player ?? MR.strings().unknown.localized()
            let platform = session.platform ?? ""
            let devLabel = platform.isEmpty ? device : "\(device) (\(platform))"
            detailRow(title: MR.strings().device.localized(), value: devLabel)

            if let ip = session.ipAddress, !ip.isEmpty {
                detailRow(title: MR.strings().ip_address.localized(), value: ip)
            }
        }
        .padding(14)
        .background(Color(UIColor.secondarySystemGroupedBackground))
        .cornerRadius(14)
    }

    private var streamInfoCard: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text(MR.strings().stream_details.localized())
                    .font(.headline)

                Spacer()

                let isTranscoding = session.isTranscoding || session.streamDecision == .transcode
                let decisionText = isTranscoding ? MR.strings().transcode.localized() : (session.streamDecision == .directStream ? MR.strings().direct_stream.localized() : MR.strings().direct_play.localized())
                let decisionColor: Color = isTranscoding ? .orange : .green

                Text(decisionText)
                    .font(.caption.bold())
                    .foregroundColor(decisionColor)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(decisionColor.opacity(0.15))
                    .clipShape(Capsule())
            }

            Divider()

            if let res = session.videoResolution ?? session.qualityProfile, !res.isEmpty {
                detailRow(title: MR.strings().resolution.localized(), value: res)
            }

            if let transcodeDecision = session.videoDecision?.name, session.isTranscoding {
                detailRow(title: MR.strings().transcode.localized(), value: transcodeDecision)
            }

            if let bandwidth = session.bandwidthKbps?.int64Value, bandwidth > 0 {
                detailRow(title: MR.strings().bandwidth.localized(), value: "\(bandwidth) kbps")
            }

            if let container = session.container, !container.isEmpty {
                detailRow(title: MR.strings().container.localized(), value: container.uppercased())
            }
        }
        .padding(14)
        .background(Color(UIColor.secondarySystemGroupedBackground))
        .cornerRadius(14)
    }

    @ViewBuilder
    private func detailRow(title: String, value: String) -> some View {
        HStack {
            Text(title)
                .font(.subheadline)
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
                .font(.subheadline.bold())
                .foregroundColor(.primary)
                .multilineTextAlignment(.trailing)
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
