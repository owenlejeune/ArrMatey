//
//  TracearrStreamDetailsSheet.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-05.
//

import Shared
import SwiftUI

struct TracearrStreamDetailsSheet: View {
    let session: TracearrStreamSession
    var onNavigateToDetails: ((TracearrMediaType?, Int64?) -> Void)? = nil
    @Environment(\.dismiss) private var dismiss

    @State private var currentProgressMs: Int64 = 0
    @State private var currentWatchTimeMs: Int64 = 0
    @State private var currentPausedMs: Int64 = 0

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

    private var progressFraction: Double {
        if totalMs > 0 {
            return min(max(Double(currentProgressMs) / Double(totalMs), 0.0), 1.0)
        }
        return 0.0
    }

    private var progressPercent: Int {
        Int((progressFraction * 100).rounded())
    }

    private var serverColor: Color {
        let serverType = session.server?.type ?? session.serverType
        if serverType == .plex {
            return Color(hex: 0xE5A00D)
        } else if serverType == .jellyfin {
            return Color(hex: 0xAA5CC3)
        } else if serverType == .emby {
            return Color(hex: 0x52B54B)
        } else {
            let name = (session.server?.name ?? session.serverName ?? "").lowercased()
            if name.contains("plex") {
                return Color(hex: 0xE5A00D)
            } else if name.contains("jellyfin") {
                return Color(hex: 0xAA5CC3)
            } else if name.contains("emby") {
                return Color(hex: 0x52B54B)
            }
            return .accentColor
        }
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 12) {
                    // Header Bar
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

                    // Media Header Card
                    mediaHeaderCard

                    // User Card
                    userCard

                    // Server Card
                    serverCard

                    // Playback Card
                    playbackCard

                    // Location Card
                    locationCard

                    // Device Card
                    deviceCard

                    // Stream Details Card
                    streamDetailsCard

                    // Video Card
                    videoCard

                    // Audio Card
                    audioCard

                    // Subtitles Card
                    if let subtitleInfo = session.subtitleInfo {
                        subtitlesCard(subtitleInfo)
                    }
                }
                .padding(16)
            }
            .navigationBarHidden(true)
        }
        .task(id: "\(session.id)-\(session.progressMs?.int64Value ?? 0)-\(isPlaying)-\(isPaused)") {
            currentProgressMs = session.progressMs?.int64Value ?? 0

            let basePaused = session.pausedDurationMs?.int64Value ?? 0
            var activePauseMs: Int64 = 0
            if isPaused, let lastPausedDate = instantToDate(session.lastPausedAt) {
                activePauseMs = max(Int64(Date().timeIntervalSince(lastPausedDate) * 1000), 0)
            }
            currentPausedMs = basePaused + activePauseMs

            if let startedDate = instantToDate(session.startedAt) {
                let totalElapsedMs = max(Int64(Date().timeIntervalSince(startedDate) * 1000), 0)
                currentWatchTimeMs = max(totalElapsedMs - currentPausedMs, 0)
            } else {
                currentWatchTimeMs = session.progressMs?.int64Value ?? 0
            }

            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                guard !Task.isCancelled else { break }
                if isPlaying {
                    if currentProgressMs < totalMs {
                        currentProgressMs += 1000
                    }
                    currentWatchTimeMs += 1000
                } else if isPaused {
                    currentPausedMs += 1000
                }
            }
        }
    }

    // MARK: - Media Header Card
    private var mediaHeaderCard: some View {
        HStack(alignment: .top, spacing: 12) {
            let imageUrl = session.posterUrl ?? session.thumbPath
            TracearrImage(urlString: imageUrl) {
                Color(UIColor.tertiarySystemBackground)
            }
            .aspectRatio(contentMode: .fill)
            .frame(width: 70, height: 105)
            .clipShape(RoundedRectangle(cornerRadius: 8))

            VStack(alignment: .leading, spacing: 4) {
                let typeLabel: String = {
                    if session.mediaType == .episode { return MR.strings().episode.localized() }
                    if session.mediaType == .movie { return MR.strings().movie_singular.localized() }
                    if session.mediaType == .track { return MR.strings().track.localized() }
                    return session.mediaType?.name ?? MR.strings().media.localized()
                }()
                let yearText = session.year != nil ? " · \(session.year!.stringValue)" : ""

                HStack(spacing: 4) {
                    Image(systemName: session.mediaType == .movie ? "film" : "tv")
                        .font(.system(size: 11))
                        .foregroundColor(.secondary)
                    Text("\(typeLabel)\(yearText)")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }

                let displayTitle = session.grandparentTitle ?? session.showTitle ?? session.mediaTitle ?? MR.strings().unknown.localized()
                Text(displayTitle)
                    .font(.subheadline.bold())
                    .lineLimit(2)

                let subtitle = subtitleText
                if !subtitle.isEmpty {
                    Text(subtitle)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }

                Spacer()

                HStack(spacing: 8) {
                    ProgressView(value: progressFraction)
                        .progressViewStyle(.linear)
                        .tint(.accentColor)

                    Text("\(progressPercent)%")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
        .contentShape(Rectangle())
        .onTapGesture {
            if let tmdbId = session.mediaDetails?.tmdbId?.int64Value {
                dismiss()
                onNavigateToDetails?(session.mediaType, tmdbId)
            }
        }
    }

    // MARK: - User Card
    private var userCard: some View {
        HStack(spacing: 12) {
            let username = session.effectiveUsername.isEmpty ? MR.strings().user.localized() : session.effectiveUsername
            let avatarUrl = session.effectiveUserAvatar

            if let avatarStr = avatarUrl, !avatarStr.isEmpty {
                TracearrImage(urlString: avatarStr) {
                    Circle().fill(Color.orange)
                }
                .aspectRatio(contentMode: .fill)
                .frame(width: 36, height: 36)
                .clipShape(Circle())
            } else {
                Circle()
                    .fill(Color.orange)
                    .frame(width: 36, height: 36)
                    .overlay(
                        Text(String(username.prefix(1)).uppercased())
                            .font(.caption.bold())
                            .foregroundColor(.white)
                    )
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(username)
                    .font(.subheadline.bold())

                Text(MR.strings().view_profile.localized())
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }

            Spacer()

            Image(systemName: "arrow.up.right.square")
                .font(.system(size: 14))
                .foregroundColor(.secondary)
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }

    // MARK: - Server Card
    private var serverCard: some View {
        cardSection(icon: "server.rack", title: MR.strings().server.localized()) {
            HStack {
                Text(MR.strings().server.localized())
                    .font(.subheadline)
                    .foregroundColor(.secondary)

                Spacer()

                let serverTypeName = session.server?.type?.name ?? session.serverType?.name ?? session.serverName ?? "Server"
                Text(serverTypeName)
                    .font(.subheadline.bold())
                    .foregroundColor(serverColor)

                let sName = session.effectiveServerName
                if !sName.isEmpty {
                    Text("· \(sName)")
                        .font(.subheadline.bold())
                }
            }
        }
    }

    // MARK: - Playback Card
    private var playbackCard: some View {
        cardSection(icon: "clock", title: MR.strings().playback.localized()) {
            VStack(spacing: 8) {
                let started = formatStartedAt(session.startedAt)
                if !started.isEmpty {
                    detailRow(label: MR.strings().started.localized(), value: started)
                }
                detailRow(label: MR.strings().watch_time.localized(), value: formatDetailedDuration(currentWatchTimeMs))
                if currentPausedMs > 0 || isPaused {
                    detailRow(label: MR.strings().paused.localized(), value: formatDetailedDuration(currentPausedMs))
                }
                detailRow(label: MR.strings().media_length.localized(), value: formatDetailedDuration(totalMs, hideSecondsIfHours: true))
            }
        }
    }

    // MARK: - Location Card
    private var locationCard: some View {
        cardSection(icon: "mappin.and.ellipse", title: MR.strings().location.localized()) {
            VStack(spacing: 8) {
                detailRow(label: MR.strings().ip_address.localized(), value: session.ipAddress ?? MR.strings().unknown.localized())

                let locationParts = [session.geoCity, session.geoRegion, session.geoCountry].compactMap { $0 }.filter { !$0.isEmpty }
                let locationStr = locationParts.isEmpty ? MR.strings().local_network.localized() : locationParts.joined(separator: ", ")

                HStack(spacing: 6) {
                    Image(systemName: "network")
                        .font(.system(size: 13))
                        .foregroundColor(.secondary)
                    Text(locationStr)
                        .font(.subheadline.weight(.medium))
                    Spacer()
                }
            }
        }
    }

    // MARK: - Device Card
    private var deviceCard: some View {
        let platformEnum = TracearrDevicePlatform.companion.fromSession(
            platform: session.platform,
            product: session.product,
            device: session.device
        )
        let deviceIcon: String = {
            switch platformEnum {
            case .phone: return "iphone"
            case .tablet: return "ipad"
            case .tv: return "tv"
            case .desktop: return "desktopcomputer"
            case .console: return "gamecontroller"
            default: return "display"
            }
        }()

        return cardSection(icon: deviceIcon, title: MR.strings().device.localized()) {
            VStack(spacing: 8) {
                if let platform = session.platform, !platform.isEmpty {
                    detailRow(label: MR.strings().platform.localized(), value: platform)
                }
                if let product = session.product, !product.isEmpty {
                    detailRow(label: MR.strings().product.localized(), value: product)
                }
                if let device = session.device, !device.isEmpty {
                    detailRow(label: MR.strings().device.localized(), value: device)
                }
                let player = session.player ?? session.playerName
                if let player = player, !player.isEmpty {
                    detailRow(label: MR.strings().player.localized(), value: player)
                }
                if let deviceId = session.deviceId, !deviceId.isEmpty {
                    detailRow(label: MR.strings().device_id.localized(), value: deviceId, ellipsize: true)
                }
            }
        }
    }

    // MARK: - Stream Details Card
    private var streamDetailsCard: some View {
        let isTranscode = session.isTranscode?.boolValue == true ||
            session.videoDecision == .transcode ||
            session.audioDecision == .transcode
        let badgeText = isTranscode ? MR.strings().transcode.localized() : MR.strings().direct_play.localized()
        let badgeColor: Color = isTranscode ? .orange : .green

        return cardSection(
            icon: "gauge.with.dots.needle.bottom.50percent",
            title: MR.strings().stream_details.localized(),
            badge: (badgeText, badgeColor)
        ) {
            VStack(spacing: 8) {
                let srcContainer = session.transcodeInfo?.sourceContainer ?? "MKV"
                let dstContainer = session.transcodeInfo?.streamContainer ?? srcContainer
                detailComparisonRow(label: MR.strings().container.localized(), source: srcContainer, stream: dstContainer)

                let bitrateVal = session.bitrate?.doubleValue ?? session.sourceVideoDetails?.bitrate?.doubleValue
                if let bitrate = bitrateVal {
                    detailRow(label: MR.strings().bitrate.localized(), value: formatBitrate(bitrate))
                }
            }
        }
    }

    // MARK: - Video Card
    private var videoCard: some View {
        let isTranscode = session.videoDecision == .transcode
        let badgeText = isTranscode ? MR.strings().transcode.localized() : MR.strings().direct_play.localized()
        let badgeColor: Color = isTranscode ? .orange : .green

        return cardSection(
            icon: "video",
            title: MR.strings().video.localized(),
            badge: (badgeText, badgeColor)
        ) {
            VStack(spacing: 8) {
                detailHeaderRow

                Divider()

                let srcCodec = session.sourceVideoCodecDisplay ?? session.sourceVideoCodec ?? "H.264"
                let dstCodec = session.streamVideoCodecDisplay ?? session.streamVideoCodec ?? srcCodec
                detailComparisonRow(label: MR.strings().codec.localized(), source: srcCodec, stream: dstCodec)

                let srcRes: String = {
                    if let w = session.sourceVideoWidth, let h = session.sourceVideoHeight {
                        return "\(w)×\(h) (\(session.resolution ?? "1080p"))"
                    }
                    return session.resolution ?? "1080p"
                }()
                let dstRes: String = {
                    if let w = session.streamVideoDetails?.width, let h = session.streamVideoDetails?.height {
                        return "\(Int(w.doubleValue))×\(Int(h.doubleValue)) (\(session.resolution ?? "1080p"))"
                    }
                    return srcRes
                }()
                detailComparisonRow(label: MR.strings().resolution.localized(), source: srcRes, stream: dstRes)

                let srcBitrate = session.sourceVideoDetails?.bitrate != nil ? formatBitrate(session.sourceVideoDetails!.bitrate!.doubleValue) : ""
                let dstBitrate = session.streamVideoDetails?.bitrate != nil ? formatBitrate(session.streamVideoDetails!.bitrate!.doubleValue) : srcBitrate
                if !srcBitrate.isEmpty {
                    detailComparisonRow(label: MR.strings().bitrate.localized(), source: srcBitrate, stream: dstBitrate)
                }

                let srcFramerate = session.sourceVideoDetails?.framerate ?? ""
                let dstFramerate = session.streamVideoDetails?.framerate ?? srcFramerate
                if !srcFramerate.isEmpty {
                    detailComparisonRow(label: MR.strings().framerate.localized(), source: srcFramerate, stream: dstFramerate)
                }

                let srcHdr = session.sourceVideoDetails?.dynamicRange ?? "SDR"
                let dstHdr = session.streamVideoDetails?.dynamicRange ?? srcHdr
                detailComparisonRow(label: MR.strings().hdr.localized(), source: srcHdr, stream: dstHdr)

                if let profile = session.sourceVideoDetails?.profile, !profile.isEmpty {
                    detailRow(label: MR.strings().profile.localized(), value: profile)
                }

                let colorParts = [
                    session.sourceVideoDetails?.colorSpace,
                    session.sourceVideoDetails?.colorDepth != nil ? "\(session.sourceVideoDetails!.colorDepth!.intValue)bit" : nil
                ].compactMap { $0 }
                if !colorParts.isEmpty {
                    detailRow(label: MR.strings().color.localized(), value: colorParts.joined(separator: " "))
                }
            }
        }
    }

    // MARK: - Audio Card
    private var audioCard: some View {
        let isTranscode = session.audioDecision == .transcode
        let badgeText = isTranscode ? MR.strings().transcode.localized() : MR.strings().direct_play.localized()
        let badgeColor: Color = isTranscode ? .orange : .green

        return cardSection(
            icon: "waveform",
            title: MR.strings().audio.localized(),
            badge: (badgeText, badgeColor)
        ) {
            VStack(spacing: 8) {
                detailHeaderRow

                Divider()

                let srcAudioCodec = session.sourceAudioCodecDisplay ?? session.sourceAudioCodec ?? "EAC3"
                let dstAudioCodec = session.streamAudioCodecDisplay ?? session.streamAudioCodec ?? srcAudioCodec
                detailComparisonRow(label: MR.strings().codec.localized(), source: srcAudioCodec, stream: dstAudioCodec)

                let srcChannels = session.audioChannelsDisplay ?? (session.sourceAudioChannels?.intValue == 2 ? "Stereo" : (session.sourceAudioChannels != nil ? "\(session.sourceAudioChannels!.intValue) Channels" : "Stereo"))
                let dstChannels: String = {
                    if let ch = session.streamAudioDetails?.channels?.doubleValue {
                        return Int(ch) == 2 ? "Stereo" : "\(Int(ch)) Channels"
                    }
                    return srcChannels
                }()
                detailComparisonRow(label: MR.strings().channels.localized(), source: srcChannels, stream: dstChannels)

                let srcAudioBitrate = session.sourceAudioDetails?.bitrate != nil ? formatBitrate(session.sourceAudioDetails!.bitrate!.doubleValue) : ""
                let dstAudioBitrate = session.streamAudioDetails?.bitrate != nil ? formatBitrate(session.streamAudioDetails!.bitrate!.doubleValue) : srcAudioBitrate
                if !srcAudioBitrate.isEmpty {
                    detailComparisonRow(label: MR.strings().bitrate.localized(), source: srcAudioBitrate, stream: dstAudioBitrate)
                }

                let srcLang = session.sourceAudioDetails?.language ?? ""
                let dstLang = session.streamAudioDetails?.language ?? srcLang
                if !srcLang.isEmpty {
                    detailComparisonRow(label: MR.strings().language.localized(), source: srcLang, stream: dstLang)
                }

                if let sampleRate = session.sourceAudioDetails?.sampleRate?.doubleValue {
                    detailRow(label: MR.strings().sample_rate.localized(), value: "\(Int(sampleRate / 1000)) kHz")
                }
            }
        }
    }

    // MARK: - Subtitles Card
    private func subtitlesCard(_ subtitleInfo: TracearrSubtitleInfo) -> some View {
        cardSection(icon: "captions.bubble", title: MR.strings().subtitle.localized()) {
            let formatStr = [subtitleInfo.codec, subtitleInfo.language].compactMap { $0 }.joined(separator: " · ")
            detailRow(label: MR.strings().subtitle_format.localized(), value: formatStr)
        }
    }

    // MARK: - Helper Views
    @ViewBuilder
    private func cardSection<Content: View>(
        icon: String,
        title: String,
        badge: (String, Color)? = nil,
        @ViewBuilder content: () -> Content
    ) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: 14))
                    .foregroundColor(.accentColor)

                Text(title)
                    .font(.subheadline.bold())

                Spacer()

                if let (text, color) = badge {
                    Text(text)
                        .font(.caption2.bold())
                        .foregroundColor(color)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 2)
                        .background(color.opacity(0.15))
                        .clipShape(Capsule())
                }
            }

            content()
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }

    private var detailHeaderRow: some View {
        HStack(spacing: 0) {
            Text("")
                .frame(maxWidth: .infinity, alignment: .leading)

            Text(MR.strings().source_header.localized())
                .font(.caption2.bold())
                .foregroundColor(.secondary)
                .frame(maxWidth: .infinity, alignment: .center)

            Color.clear
                .frame(width: 16, height: 1)

            Text(MR.strings().stream_header.localized())
                .font(.caption2.bold())
                .foregroundColor(.secondary)
                .frame(maxWidth: .infinity, alignment: .center)
        }
    }

    private func detailRow(label: String, value: String, ellipsize: Bool = false) -> some View {
        HStack {
            Text(label)
                .font(.subheadline)
                .foregroundColor(.secondary)

            Spacer()

            Text(value)
                .font(.subheadline.bold())
                .lineLimit(ellipsize ? 1 : nil)
                .truncationMode(.middle)
        }
    }

    private func detailComparisonRow(label: String, source: String, stream: String) -> some View {
        HStack(spacing: 0) {
            Text(label)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .frame(maxWidth: .infinity, alignment: .leading)

            Text(source)
                .font(.subheadline.bold())
                .frame(maxWidth: .infinity, alignment: .center)

            Image(systemName: "arrow.right")
                .font(.caption2)
                .foregroundColor(.secondary)
                .frame(width: 16)

            Text(stream)
                .font(.subheadline.bold())
                .frame(maxWidth: .infinity, alignment: .center)
        }
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

    private func instantToDate(_ instant: KotlinInstant?) -> Date? {
        guard let instant = instant else { return nil }
        return Date(timeIntervalSince1970: TimeInterval(instant.toEpochMilliseconds()) / 1000.0)
    }

    private func formatStartedAt(_ instant: KotlinInstant?) -> String {
        guard let instant = instant, let date = instantToDate(instant) else { return "" }

        let displayFormatter = DateFormatter()
        displayFormatter.dateFormat = "MMM d, h:mm a"
        let dateStr = displayFormatter.string(from: date)

        let diffMinutes = Int(Date().timeIntervalSince(date) / 60)
        let relativeStr: String
        if diffMinutes < 1 {
            relativeStr = "just now"
        } else if diffMinutes == 1 {
            relativeStr = "1 minute ago"
        } else if diffMinutes < 60 {
            relativeStr = "\(diffMinutes) minutes ago"
        } else if diffMinutes < 120 {
            relativeStr = "1 hour ago"
        } else {
            relativeStr = "\(diffMinutes / 60) hours ago"
        }
        return "\(dateStr) (\(relativeStr))"
    }

    private func formatDetailedDuration(_ ms: Int64, hideSecondsIfHours: Bool = false) -> String {
        if ms <= 0 { return "0s" }
        let totalSeconds = ms / 1000
        let seconds = totalSeconds % 60
        let minutes = (totalSeconds / 60) % 60
        let hours = totalSeconds / 3600
        if hours > 0 {
            return hideSecondsIfHours ? "\(hours)h \(minutes)m" : "\(hours)h \(minutes)m \(seconds)s"
        } else if minutes > 0 {
            return "\(minutes)m \(seconds)s"
        } else {
            return "\(seconds)s"
        }
    }

    private func formatBitrate(_ kbps: Double) -> String {
        if kbps <= 0 { return "" }
        if kbps >= 1000 {
            let mbps = kbps / 1000.0
            return String(format: "%.1f Mbps", mbps)
        } else {
            return "\(Int(kbps)) kbps"
        }
    }
}
