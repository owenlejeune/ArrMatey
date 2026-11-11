import Shared
import SwiftUI

struct PlaybackHistoryCardView: View {
    let item: PlaybackHistoryItem

    private var edgeColor: Color {
        Color.serverColor(type: item.serverType, name: item.serverName)
    }

    private var progressFraction: Double {
        let percent = item.percentComplete?.doubleValue ?? 0.0
        return min(max(percent / 100.0, 0.0), 1.0)
    }

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(edgeColor)
                .frame(width: 4)

            VStack(alignment: .leading, spacing: 10) {
                HStack(alignment: .top, spacing: 12) {
                    ZStack {
                        if let posterUrl = item.posterUrl, let url = URL(string: posterUrl) {
                            ArrAsyncImage(url: url) { image in
                                image.resizable()
                            } placeholder: {
                                Color(UIColor.secondarySystemBackground)
                            }
                            .aspectRatio(contentMode: .fill)
                        } else {
                            Color(UIColor.secondarySystemBackground)
                        }
                    }
                    .frame(width: 60, height: item.mediaType == .track ? 60 : 90)
                    .clipShape(RoundedRectangle(cornerRadius: 8))

                    VStack(alignment: .leading, spacing: 4) {
                        HStack(spacing: 6) {
                            let username = item.userDisplayName.isEmpty ? item.username : item.userDisplayName
                            let avatarUrl = item.userAvatarUrl

                            if let avatarStr = avatarUrl, let url = URL(string: avatarStr) {
                                ArrAsyncImage(url: url) { image in
                                    image.resizable()
                                } placeholder: {
                                    Circle().fill(Color.orange)
                                }
                                .aspectRatio(contentMode: .fill)
                                .frame(width: 18, height: 18)
                                .clipShape(Circle())
                            } else {
                                Circle()
                                    .fill(Color.orange)
                                    .frame(width: 18, height: 18)
                                    .overlay(
                                        Text(String(username.prefix(1)).uppercased())
                                            .font(.caption2.bold())
                                            .foregroundColor(.white)
                                    )
                            }

                            Text(username)
                                .font(.caption.bold())
                                .lineLimit(1)

                            Spacer()

                            PlaybackDeviceIconView(platform: item.platform, device: item.player)

                            if let percent = item.percentComplete?.intValue {
                                Text("\(percent)%")
                                    .font(.caption2.bold())
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(percent >= 90 ? Color.green.opacity(0.2) : Color(UIColor.tertiarySystemBackground))
                                    .foregroundColor(percent >= 90 ? .green : .secondary)
                                    .clipShape(Capsule())
                            }
                        }

                        Text(item.displayTitle)
                            .font(.headline)
                            .bold()
                            .lineLimit(1)

                        if let subtitle = item.subtitle, !subtitle.isEmpty {
                            Text(subtitle)
                                .font(.caption)
                                .foregroundColor(.secondary)
                                .lineLimit(1)
                        }

                        HStack(spacing: 8) {
                            ProgressView(value: progressFraction)
                                .progressViewStyle(.linear)
                                .tint(Color.accentColor)

                            if let dur = item.durationSeconds?.int64Value {
                                Text(DurationUtilsKt.formatDurationSeconds(totalSeconds: dur))
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .padding(.top, 4)
                    }
                }

                Divider()

                HStack {
                    let serverName = item.serverName ?? MR.strings().server.localized()
                    let isTranscoding = item.streamDecision == .transcode
                    let decisionText = isTranscoding ? MR.strings().transcode.localized() : MR.strings().direct_play.localized()

                    Text("\(serverName) · \(decisionText)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)

                    Spacer()

                    let resolution = item.resolution ?? ""
                    let dateText = formatDate(item.dateEpochSeconds)
                    let rightText = !resolution.isEmpty ? "\(dateText) · \(resolution)" : dateText

                    Text(rightText)
                        .font(.caption.bold())
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
            }
            .padding(12)
        }
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }

    private func formatDate(_ epochSeconds: Int64) -> String {
        guard epochSeconds > 0 else { return "" }
        let date = Date(timeIntervalSince1970: TimeInterval(epochSeconds))
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}
