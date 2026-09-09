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
                    .navigationDestination(for: MediaRoute.self) { route in
                        MediaRouteDestination(route: route)
                    }
                    .navigationDestination(for: TracearrRoute.self) { route in
                        TracearrRouteDestination(route: route)
                    }
            }
        case .launcher:
            TracearrTabContent()
                .navigationDestination(for: MediaRoute.self) { route in
                    MediaRouteDestination(route: route)
                }
                .navigationDestination(for: TracearrRoute.self) { route in
                    TracearrRouteDestination(route: route)
                }
        }
    }
}

struct TracearrTabContent: View {
    @StateObject private var viewModel = TracearrViewModelS()
    @StateObject private var instancesViewModel = InstancesViewModelS(type: .tracearr)
    @ObservedObject private var globalPreferences = PreferencesViewModel()
    @EnvironmentObject private var navigationManager: NavigationManager
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass

    private var isLargeScreen: Bool { horizontalSizeClass == .regular }

    var body: some View {
        Group {
            if viewModel.state is TracearrStateNoInstance {
                NoInstanceView(type: .tracearr)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let success = viewModel.state as? TracearrStateSuccess {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        if let stats = success.stats {
                            TracearrDashboardStatsView(
                                stats: stats,
                                isExpanded: isLargeScreen,
                                onNavigateToHistory: {
                                    navigationManager.go(to: TracearrRoute.history)
                                },
                                onNavigateToAllUsers: {
                                    navigationManager.go(to: TracearrRoute.users)
                                },
                                onNavigateToViolations: {
                                    navigationManager.go(to: TracearrRoute.violations)
                                }
                            )
                        }

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
                                        .contentShape(Rectangle())
                                        .onTapGesture {
                                            viewModel.setSelectedStream(session)
                                        }
                                }
                            }
                        }

                        // History Section Header
                        historyHeader

                        let historyItems = Array(success.history.prefix(5))
                        if historyItems.isEmpty {
                            VStack(spacing: 8) {
                                Text(MR.strings().no_history.localized())
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 20)
                        } else if isLargeScreen {
                            TracearrHistoryTableView(
                                items: historyItems,
                                onClickItem: { historyItem in
                                    viewModel.setSelectedHistoryStream(historyItem)
                                }
                            )
                        } else {
                            LazyVStack(spacing: 16) {
                                ForEach(historyItems, id: \.id) { historyItem in
                                    TracearrHistoryCardView(item: historyItem)
                                        .contentShape(Rectangle())
                                        .onTapGesture {
                                            viewModel.setSelectedHistoryStream(historyItem)
                                        }
                                }
                            }
                        }
                    }
                    .padding(16)
                }
                .refreshable {
                    viewModel.refresh()
                }
            } else if viewModel.state is TracearrStateLoading || viewModel.state is TracearrStateInitial {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = viewModel.state as? TracearrStateError {
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
        .sheet(item: Binding(
            get: { viewModel.selectedSession },
            set: { if $0 == nil { viewModel.clearSelected() } }
        )) { session in
            TracearrStreamDetailsSheet(
                session: session,
                onNavigateToDetails: { mediaType, tmdbId in
                    if let tmdbId = tmdbId {
                        navigationManager.goToDetails(
                            tmdbId: tmdbId,
                            requestType: mediaType?.requestType
                        )
                    }
                },
                onNavigateToUser: { userRef in
                    navigationManager.go(to: TracearrRoute.user(userRef))
                }
            )
        }
        .navigationTitle(MR.strings().tracearr.localized())
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if !globalPreferences.hideInstanceSwitcher || instancesViewModel.instancesState.instances.count > 1 {
                ToolbarItem(placement: .topBarLeading) {
                    InstancePickerMenu(
                        instances: instancesViewModel.instancesState.instances,
                        onChangeInstance: { instancesViewModel.setInstanceActive($0) },
                        onAddNewInstance: { navigationManager.goToNewInstance(of: .tracearr) }
                    )
                    .menuIndicator(.hidden)
                }
            } else {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        navigationManager.showLauncher = true
                    } label: {
                        Image(systemName: "line.3.horizontal")
                    }
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

    @ViewBuilder
    private var historyHeader: some View {
        HStack(spacing: 8) {
            Image(systemName: "clock.arrow.circlepath")
                .font(.title3)
                .foregroundColor(.accentColor)
            Text(MR.strings().history.localized())
                .font(.title3.bold())
            Spacer()
            Button(MR.strings().all.localized()) {
                navigationManager.go(to: TracearrRoute.history)
            }
            .font(.subheadline)
        }
    }
}

struct TracearrStreamCardView: View {
    let session: TracearrStreamSession

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
        session.totalDurationMs?.int64Value ?? session.durationMs?.int64Value ?? 0
    }

    private var remainingMs: Int64 {
        max(totalMs - currentProgressMs, 0)
    }

    private var progressFraction: Double {
        if totalMs > 0 {
            return min(max(Double(currentProgressMs) / Double(totalMs), 0.0), 1.0)
        }
        return 0.0
    }

    private var edgeColor: Color {
        Color.serverColor(type: session.server?.type ?? session.serverType, name: session.server?.name ?? session.serverName)
    }

    var body: some View {
        HStack(spacing: 0) {
            // Left Accent Bar
            Rectangle()
                .fill(edgeColor)
                .frame(width: 4)

            VStack(alignment: .leading, spacing: 12) {
                HStack(alignment: .top, spacing: 12) {
                    // Poster / Thumbnail
                    ZStack {
                        let imageUrl = session.posterUrl ?? session.thumbPath
                        TracearrImage(urlString: imageUrl) {
                            Color(UIColor.secondarySystemBackground)
                        }
                        .aspectRatio(contentMode: .fill)

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

                            if let avatarStr = avatarUrl, !avatarStr.isEmpty {
                                TracearrImage(urlString: avatarStr) {
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

                            TracearrDeviceIconView(session: session)

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

                        Text(stateText)
                            .font(.caption2.bold())
                            .foregroundColor(stateColor)
                            .padding(.vertical, 2)

                        // Progress Bar
                        ProgressView(value: progressFraction)
                            .progressViewStyle(.linear)
                            .tint(Color.accentColor)

                        // Progress Time Row
                        HStack {
                            Text(formatTimeMs(currentProgressMs))
                                .font(.caption2)
                                .foregroundColor(.secondary)

                            Spacer()

                            Text("-\(formatTimeMs(remainingMs))")
                                .font(.caption2)
                                .foregroundColor(.secondary)
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
        .task(id: "\(session.id)-\(session.progressMs?.int64Value ?? 0)-\(isPlaying)") {
            currentProgressMs = session.progressMs?.int64Value ?? 0
            guard isPlaying else { return }
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                guard !Task.isCancelled else { break }
                if currentProgressMs < totalMs {
                    currentProgressMs += 1000
                }
            }
        }
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

struct TracearrDeviceIconView: View {
    let session: TracearrStreamSession

    private var platformEnum: TracearrDevicePlatform {
        TracearrDevicePlatform.companion.fromSession(
            platform: session.platform,
            product: session.product,
            device: session.device
        )
    }

    private var systemImageName: String {
        switch platformEnum {
        case .phone: return "iphone"
        case .tablet: return "ipad"
        case .tv: return "tv"
        case .desktop: return "desktopcomputer"
        case .console: return "gamecontroller"
        default: return "display"
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

struct TracearrImage<Placeholder: View>: View {
    let urlString: String?
    let placeholder: () -> Placeholder

    @State private var image: UIImage? = nil
    @State private var isLoading: Bool = false

    init(urlString: String?, @ViewBuilder placeholder: @escaping () -> Placeholder) {
        self.urlString = urlString
        self.placeholder = placeholder
    }

    var body: some View {
        ZStack {
            if let uiImage = image {
                Image(uiImage: uiImage)
                    .resizable()
            } else {
                placeholder()
            }
        }
        .task(id: urlString) {
            await loadImage()
        }
    }

    private func loadImage() async {
        guard let urlStr = urlString,
              !urlStr.isEmpty,
              (urlStr.hasPrefix("http://") || urlStr.hasPrefix("https://")),
              let url = URL(string: urlStr) else {
            return
        }

        var request = URLRequest(url: url)
        request.setValue("image/*", forHTTPHeaderField: "Accept")

        let instances = KoinBridge.shared.getInstanceRepository().allInstancesFlow.value
        if let instance = instances.first(where: {
            (urlStr.hasPrefix($0.getEffectiveBaseUrl()) || urlStr.hasPrefix($0.url)) && $0.type == .tracearr
        }) {
            request.setValue("Bearer \(instance.apiKey)", forHTTPHeaderField: "Authorization")
        }

        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            if let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode),
               let downloadedImage = UIImage(data: data) {
                await MainActor.run {
                    self.image = downloadedImage
                }
            }
        } catch {
            // image remains nil -> placeholder shown
        }
    }
}

struct CompactStatCard: View {
    let iconName: String
    let label: String
    let value: String
    var containerColor: Color = Color(UIColor.secondarySystemBackground)
    var contentColor: Color = .primary
    var onClick: (() -> Void)? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 4) {
                Image(systemName: iconName)
                    .font(.system(size: 20))
                Text(value)
                    .font(.title3.bold())
            }
            .foregroundColor(contentColor)

            Text(label)
                .font(.caption)
                .foregroundColor(contentColor.opacity(0.8))
                .lineLimit(1)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(containerColor)
        .cornerRadius(16)
        .contentShape(Rectangle())
        .onTapGesture {
            onClick?()
        }
    }
}

struct CountStatItem: View {
    let iconName: String
    let label: String
    let count: Int
    var containerColor: Color = Color(UIColor.secondarySystemBackground)
    var contentColor: Color = .primary
    var onClick: (() -> Void)? = nil

    var body: some View {
        CompactStatCard(
            iconName: iconName,
            label: label,
            value: "\(count)",
            containerColor: containerColor,
            contentColor: contentColor,
            onClick: onClick
        )
    }
}

struct SplitStatCard: View {
    let iconName: String
    let firstLabel: String
    let firstValue: String
    let secondLabel: String
    let secondValue: String
    var containerColor: Color
    var contentColor: Color = .primary
    var onClick: (() -> Void)? = nil

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: iconName)
                .font(.system(size: 28))
                .foregroundColor(contentColor)

            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 4) {
                    Text(firstValue)
                        .font(.title3.bold())
                    Text(firstLabel)
                        .font(.caption)
                }
                .foregroundColor(contentColor)

                HStack(spacing: 4) {
                    Text(secondValue)
                        .font(.title3.bold())
                    Text(secondLabel)
                        .font(.caption)
                }
                .foregroundColor(contentColor)
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(containerColor)
        .cornerRadius(16)
        .contentShape(Rectangle())
        .onTapGesture {
            onClick?()
        }
    }
}

struct TracearrDashboardStatsView: View {
    let stats: TracearrTodayStats
    var isExpanded: Bool = false
    var onNavigateToHistory: (() -> Void)? = nil
    var onNavigateToAllUsers: (() -> Void)? = nil
    var onNavigateToViolations: (() -> Void)? = nil

    private static let tracearrBlue = Color(hex: 0x00b4d8)
    private static let tracearrDarkBlue = Color(hex: 0x00507a)
    private static let tracearrNavy = Color(hex: 0x061019)
    private static let tracearrLightBlue = Color(hex: 0x48cae4)

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 8) {
                Image(systemName: "calendar")
                    .font(.title3)
                Text(MR.strings().today.localized())
                    .font(.title3.bold())
                Spacer()
            }

            let columns = isExpanded ? Array(repeating: GridItem(.flexible(), spacing: 12), count: 4) : [GridItem(.flexible(), spacing: 12), GridItem(.flexible(), spacing: 12)]

            LazyVGrid(columns: columns, spacing: 12) {
                CountStatItem(
                    iconName: "exclamationmark.triangle",
                    label: MR.strings().alerts.localized(),
                    count: Int(stats.alertsLast24h),
                    containerColor: stats.alertsLast24h > 0 ? Color.red.opacity(0.8) : Self.tracearrDarkBlue,
                    contentColor: .white,
                    onClick: onNavigateToViolations
                )

                SplitStatCard(
                    iconName: "play.fill",
                    firstLabel: MR.strings().plays.localized(),
                    firstValue: "\(stats.todayPlays)",
                    secondLabel: MR.strings().sessions.localized(),
                    secondValue: "\(stats.todaySessions)",
                    containerColor: Self.tracearrBlue,
                    contentColor: Self.tracearrNavy,
                    onClick: onNavigateToHistory
                )

                CompactStatCard(
                    iconName: "clock",
                    label: MR.strings().watch_time.localized(),
                    value: stats.formattedWatchTime,
                    containerColor: Self.tracearrLightBlue,
                    contentColor: Self.tracearrDarkBlue
                )

                CountStatItem(
                    iconName: "person.2",
                    label: MR.strings().active_users.localized(),
                    count: Int(stats.activeUsersToday),
                    containerColor: Self.tracearrNavy,
                    contentColor: .white,
                    onClick: onNavigateToAllUsers
                )
            }
        }
    }
}

struct TracearrHistoryCardView: View {
    let item: TracearrHistoryItem

    private var percent: Double {
        if let pc = item.percentComplete?.doubleValue {
            return pc
        }
        let total = Double(item.totalDurationMs?.int64Value ?? item.durationMs?.int64Value ?? 0)
        let prog = Double(item.progressMs?.int64Value ?? 0)
        return total > 0 ? (prog / total * 100.0) : 0.0
    }

    private var progressFraction: Double {
        min(max(percent / 100.0, 0.0), 1.0)
    }

    private var isWatched: Bool {
        item.watched?.boolValue == true || percent >= 90.0
    }

    private var isAbandoned: Bool {
        !isWatched && percent < 10.0
    }

    private var isSampled: Bool {
        !isWatched && !isAbandoned
    }

    private var edgeColor: Color {
        Color.serverColor(type: item.serverType, name: item.serverName)
    }

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(edgeColor)
                .frame(width: 4)

            VStack(alignment: .leading, spacing: 10) {
                HStack(alignment: .top, spacing: 12) {
                    ZStack {
                        let imageUrl = item.posterUrl ?? item.thumbPath
                        TracearrImage(urlString: imageUrl) {
                            Color(UIColor.secondarySystemBackground)
                        }
                        .aspectRatio(contentMode: .fill)
                    }
                    .frame(width: 60, height: 90)
                    .clipShape(RoundedRectangle(cornerRadius: 8))

                    VStack(alignment: .leading, spacing: 4) {
                        HStack(spacing: 6) {
                            let username = item.effectiveUsername.isEmpty ? MR.strings().user.localized() : item.effectiveUsername
                            let avatarUrl = item.effectiveUserAvatar

                            if let avatarStr = avatarUrl, !avatarStr.isEmpty {
                                TracearrImage(urlString: avatarStr) {
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

                            TracearrDeviceIconView(session: item.toStreamSession())

                            statusChip
                        }

                        let displayTitle = item.grandparentTitle ?? item.showTitle ?? item.mediaTitle ?? MR.strings().unknown.localized()
                        Text(displayTitle)
                            .font(.headline)
                            .bold()
                            .lineLimit(1)

                        Text(subtitleText)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .lineLimit(1)

                        HStack(spacing: 8) {
                            ProgressView(value: progressFraction)
                                .progressViewStyle(.linear)
                                .tint(Color.accentColor)

                            if let dur = item.durationMs?.int64Value {
                                Text(formatDurationMs(dur))
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .padding(.top, 4)
                    }
                }

                Divider()

                HStack {
                    let serverName = item.effectiveServerName.isEmpty ? MR.strings().server.localized() : item.effectiveServerName
                    let isTranscoding = item.isTranscode?.boolValue == true || item.videoDecision == .transcode || item.audioDecision == .transcode
                    let decisionText = isTranscoding ? MR.strings().transcode.localized() : MR.strings().direct_play.localized()

                    Text("\(serverName) · \(decisionText)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)

                    Spacer()

                    let resolution = item.resolution ?? "1080p"
                    let dateText = item.startedAt?.format(pattern: "MMM d, yyyy, HH:mm")
                    let rightText = dateText != nil ? "\(dateText!) · \(resolution)" : resolution

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

    @ViewBuilder
    private var statusChip: some View {
        let (label, bgColor, fgColor): (String, Color, Color) = {
            if isWatched {
                return (MR.strings().watched.localized(), Color.green.opacity(0.2), .green)
            } else if isSampled {
                return (MR.strings().sampled.localized(), Color.orange.opacity(0.2), .orange)
            } else {
                return (MR.strings().abandoned.localized(), Color.red.opacity(0.2), .red)
            }
        }()

        Text(label)
            .font(.caption2.bold())
            .foregroundColor(fgColor)
            .padding(.horizontal, 6)
            .padding(.vertical, 2)
            .background(bgColor)
            .cornerRadius(4)
    }

    private var subtitleText: String {
        if item.mediaType == .episode || (item.seasonNumber != nil && item.episodeNumber != nil) {
            let s = item.seasonNumber?.intValue ?? 0
            let e = item.episodeNumber?.intValue ?? 0
            let sStr = String(format: "%02d", s)
            let eStr = String(format: "%02d", e)
            let epTitle = item.mediaTitle ?? ""
            return "S\(sStr) E\(eStr) · \(epTitle)"
        } else if item.mediaType == .movie {
            let yearStr = item.year?.stringValue ?? ""
            let movieTitle = item.mediaTitle ?? ""
            return "\(yearStr) · \(movieTitle)"
        } else {
            let parts = [item.artistName, item.albumName, item.mediaTitle].compactMap { $0 }
            return parts.joined(separator: " · ")
        }
    }

    private func formatDurationMs(_ ms: Int64) -> String {
        let totalSeconds = ms / 1000
        let minutes = totalSeconds / 60
        let seconds = totalSeconds % 60
        if minutes > 0 {
            return "\(minutes)m \(seconds)s"
        } else {
            return "\(seconds)s"
        }
    }
}

struct TracearrHistoryTableView: View {
    let items: [TracearrHistoryItem]
    var hasMore: Bool = false
    var isLoadingMore: Bool = false
    var onLoadMore: (() -> Void)? = nil
    var onClickItem: ((TracearrHistoryItem) -> Void)? = nil

    var body: some View {
        LazyVStack(alignment: .leading, spacing: 8) {
            ForEach(items, id: \.id) { historyItem in
                TracearrHistoryTableRowView(item: historyItem)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        onClickItem?(historyItem)
                    }
                    .onAppear {
                        if historyItem.id == items.last?.id && hasMore && !isLoadingMore {
                            onLoadMore?()
                        }
                    }
            }

            if isLoadingMore {
                ProgressView()
                    .padding(16)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct TracearrHistoryTableRowView: View {
    let item: TracearrHistoryItem

    private var percent: Double {
        if let pc = item.percentComplete?.doubleValue {
            return pc
        }
        let total = Double(item.totalDurationMs?.int64Value ?? item.durationMs?.int64Value ?? 0)
        let prog = Double(item.progressMs?.int64Value ?? 0)
        return total > 0 ? (prog / total * 100.0) : 0.0
    }

    private var progressFraction: Double {
        min(max(percent / 100.0, 0.0), 1.0)
    }

    private var isWatched: Bool {
        item.watched?.boolValue == true || percent >= 90.0
    }

    private var isAbandoned: Bool {
        !isWatched && percent < 10.0
    }

    private var isSampled: Bool {
        !isWatched && !isAbandoned
    }

    private var edgeColor: Color {
        Color.serverColor(type: item.serverType, name: item.serverName)
    }

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(edgeColor)
                .frame(width: 4)

            HStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 6) {
                        let username = item.effectiveUsername.isEmpty ? MR.strings().user.localized() : item.effectiveUsername
                        let avatarUrl = item.effectiveUserAvatar

                        if let avatarStr = avatarUrl, !avatarStr.isEmpty {
                            TracearrImage(urlString: avatarStr) {
                                Circle().fill(Color.orange)
                            }
                            .aspectRatio(contentMode: .fill)
                            .frame(width: 20, height: 20)
                            .clipShape(Circle())
                        } else {
                            Circle()
                                .fill(Color.orange)
                                .frame(width: 20, height: 20)
                                .overlay(
                                    Text(String(username.prefix(1)).uppercased())
                                        .font(.caption2.bold())
                                        .foregroundColor(.white)
                                )
                        }

                        Text(username)
                            .font(.subheadline.bold())
                            .lineLimit(1)
                    }

                    Text(item.startedAt?.format(pattern: "MMM d, yyyy, HH:mm") ?? "-")
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
                .frame(minWidth: 100, maxWidth: 140, alignment: .leading)

                HStack(spacing: 8) {
                    let mediaIcon = item.mediaType == .episode ? "tv" : (item.mediaType == .movie ? "film" : "music.note")
                    Image(systemName: mediaIcon)
                        .font(.system(size: 14))
                        .foregroundColor(.accentColor)

                    VStack(alignment: .leading, spacing: 2) {
                        tableStatusChip

                        let displayTitle = item.grandparentTitle ?? item.showTitle ?? item.mediaTitle ?? MR.strings().unknown.localized()
                        Text(displayTitle)
                            .font(.subheadline.bold())
                            .lineLimit(1)

                        Text(subtitleText)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .lineLimit(1)

                        HStack(spacing: 6) {
                            HStack(spacing: 4) {
                                Image(systemName: "clock")
                                    .font(.system(size: 10))
                                    .foregroundColor(.secondary)
                                let dur = item.durationMs?.int64Value ?? item.totalDurationMs?.int64Value ?? 0
                                Text(formatTableDurationMs(dur))
                                    .font(.caption.bold())
                                    .foregroundColor(.secondary)
                                    .lineLimit(1)
                            }

                            ProgressView(value: progressFraction)
                                .progressViewStyle(.linear)
                                .tint(Color.accentColor)

                            Text("\(Int(percent))%")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                        }
                        .padding(.top, 2)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                VStack(alignment: .leading, spacing: 2) {
                    HStack(spacing: 4) {
                        Circle()
                            .fill(edgeColor)
                            .frame(width: 6, height: 6)
                        let serverName = item.effectiveServerName.isEmpty ? MR.strings().server.localized() : item.effectiveServerName
                        Text(serverName)
                            .font(.caption.bold())
                            .lineLimit(1)
                    }

                    HStack(spacing: 3) {
                        Image(systemName: "globe")
                            .font(.system(size: 10))
                            .foregroundColor(.secondary)
                        Text(MR.strings().local_network.localized())
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                }
                .frame(minWidth: 100, maxWidth: 130, alignment: .leading)

                VStack(alignment: .leading, spacing: 4) {
                    qualityChip

                    let platformName = item.platform ?? item.device ?? MR.strings().unknown.localized()
                    Text(platformName)
                        .font(.subheadline.bold())
                        .lineLimit(1)

                    let productText = item.product ?? item.player ?? ""
                    if !productText.isEmpty {
                        Text(productText)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                }
                .frame(minWidth: 110, maxWidth: 140, alignment: .leading)
            }
            .padding(12)
        }
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(8)
    }

    @ViewBuilder
    private var tableStatusChip: some View {
        let (label, bgColor, fgColor): (String, Color, Color) = {
            if isWatched {
                return (MR.strings().watched.localized(), Color.green.opacity(0.2), .green)
            } else if isSampled {
                return (MR.strings().sampled.localized(), Color.orange.opacity(0.2), .orange)
            } else {
                return (MR.strings().abandoned.localized(), Color.red.opacity(0.2), .red)
            }
        }()

        Text(label)
            .font(.caption2.bold())
            .foregroundColor(fgColor)
            .padding(.horizontal, 4)
            .padding(.vertical, 1)
            .background(bgColor)
            .cornerRadius(4)
    }

    @ViewBuilder
    private var qualityChip: some View {
        let isTranscoding = item.isTranscode?.boolValue == true || item.videoDecision == .transcode || item.audioDecision == .transcode
        let (iconName, label, bgColor, fgColor) = isTranscoding ?
            ("bolt.fill", MR.strings().transcode.localized(), Color.yellow.opacity(0.2), Color.yellow) :
            ("play.fill", MR.strings().direct_play.localized(), Color.green.opacity(0.2), Color.green)

        HStack(spacing: 2) {
            Image(systemName: iconName)
                .font(.system(size: 8))
            Text(label)
                .font(.caption2.bold())
        }
        .foregroundColor(fgColor)
        .padding(.horizontal, 4)
        .padding(.vertical, 1)
        .background(bgColor)
        .cornerRadius(4)
    }

    private var subtitleText: String {
        if item.mediaType == .episode || (item.seasonNumber != nil && item.episodeNumber != nil) {
            let s = item.seasonNumber?.intValue ?? 0
            let e = item.episodeNumber?.intValue ?? 0
            let sStr = String(format: "%02d", s)
            let eStr = String(format: "%02d", e)
            let epTitle = item.mediaTitle ?? ""
            return "S\(sStr) E\(eStr) · \(epTitle)"
        } else if item.mediaType == .movie {
            let yearStr = item.year?.stringValue ?? ""
            let movieTitle = item.mediaTitle ?? ""
            return "\(yearStr) · \(movieTitle)"
        } else {
            let parts = [item.artistName, item.albumName, item.mediaTitle].compactMap { $0 }
            return parts.joined(separator: " · ")
        }
    }

    private func formatTableDurationMs(_ ms: Int64) -> String {
        if ms <= 0 { return "-" }
        let totalSeconds = ms / 1000
        let hours = totalSeconds / 3600
        let minutes = (totalSeconds % 3600) / 60
        let seconds = totalSeconds % 60
        if hours > 0 {
            return "\(hours)h \(minutes)m"
        } else if minutes > 0 {
            return "\(minutes)m \(seconds)s"
        } else {
            return "\(seconds)s"
        }
    }
}
