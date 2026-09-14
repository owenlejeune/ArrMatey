//
//  UnifiedMediaDetailsActions.swift
//  iosApp
//

import SwiftUI
import Shared

struct UnifiedMediaDetailsActions: View {
    let buttonState: MediaButtonState
    let onWatch: (String) -> Void
    let onWatchTrailer: (String) -> Void
    let onRequest: () -> Void
    let onRequest4k: () -> Void
    let onViewRequest: () -> Void
    let onApproveRequest: () -> Void
    let onDeclineRequest: () -> Void
    let onManage: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            // Watch Button / Trailer Button
            if buttonState.showWatchButton, let url = buttonState.watchButtonUrl {
                if buttonState.showWatchTrailerOption, let trailerUrl = buttonState.trailerUrl {
                    HStack(spacing: 0) {
                        Button(action: { onWatch(url) }) {
                            HStack {
                                Image(systemName: "play.fill")
                                Text(buttonState.watchButtonLabel.localized())
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                        }

                        Divider()
                            .frame(height: 24)
                            .background(Color.white.opacity(0.5))

                        Menu {
                            Button(action: { onWatchTrailer(trailerUrl) }) {
                                Label(MR.strings().watch_trailer.localized(), systemImage: "film")
                            }
                        } label: {
                            Image(systemName: "chevron.down")
                                .padding(.horizontal, 12)
                                .padding(.vertical, 12)
                        }
                    }
                    .background(Color.accentColor)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                } else {
                    Button(action: { onWatch(url) }) {
                        HStack {
                            Image(systemName: "play.fill")
                            Text(buttonState.watchButtonLabel.localized())
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.accentColor)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                    }
                }
            } else if buttonState.showWatchTrailerOption, let url = buttonState.trailerUrl {
                Button(action: { onWatchTrailer(url) }) {
                    HStack {
                        Image(systemName: "film")
                        Text(MR.strings().watch_trailer.localized())
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color(.secondarySystemBackground))
                    .foregroundColor(.primary)
                    .cornerRadius(8)
                }
            }

            // View Request Button
            if buttonState.showViewRequestButton {
                if buttonState.showApproveRequestButton || buttonState.showDeclineRequestButton {
                    HStack(spacing: 0) {
                        Button(action: onViewRequest) {
                            HStack {
                                Image(systemName: "clock")
                                Text(MR.strings().view_request.localized())
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                        }

                        Divider()
                            .frame(height: 24)
                            .background(Color.primary.opacity(0.2))

                        Menu {
                            if buttonState.showApproveRequestButton {
                                Button(action: onApproveRequest) {
                                    Label(MR.strings().approve_request.localized(), systemImage: "checkmark")
                                }
                            }
                            if buttonState.showDeclineRequestButton {
                                Button(role: .destructive, action: onDeclineRequest) {
                                    Label(MR.strings().decline_request.localized(), systemImage: "xmark")
                                }
                            }
                        } label: {
                            Image(systemName: "chevron.down")
                                .padding(.horizontal, 12)
                                .padding(.vertical, 12)
                        }
                    }
                    .background(Color(.secondarySystemBackground))
                    .foregroundColor(.primary)
                    .cornerRadius(8)
                } else {
                    Button(action: onViewRequest) {
                        HStack {
                            Image(systemName: "clock")
                            Text(MR.strings().view_request.localized())
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color(.secondarySystemBackground))
                        .foregroundColor(.primary)
                        .cornerRadius(8)
                    }
                }
            }

            // Request More Button
            if buttonState.showRequestMoreButton {
                Button(action: onRequest) {
                    HStack {
                        Image(systemName: "plus")
                        Text(MR.strings().request_more.localized())
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.accentColor)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                }
            }

            // Request Button
            if buttonState.showRequestButton {
                if buttonState.showRequest4kButton {
                    HStack(spacing: 0) {
                        Button(action: onRequest) {
                            HStack {
                                Image(systemName: "plus")
                                Text(MR.strings().request.localized())
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                        }

                        Divider()
                            .frame(height: 24)
                            .background(Color.white.opacity(0.5))

                        Menu {
                            Button(action: onRequest4k) {
                                Label(MR.strings().request_in_4k.localized(), systemImage: "aqi.medium")
                            }
                        } label: {
                            Image(systemName: "chevron.down")
                                .padding(.horizontal, 12)
                                .padding(.vertical, 12)
                        }
                    }
                    .background(Color.accentColor)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                } else {
                    Button(action: onRequest) {
                        HStack {
                            Image(systemName: "plus")
                            Text(MR.strings().request.localized())
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.accentColor)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                    }
                }
            } else if buttonState.showRequest4kButton {
                Button(action: onRequest4k) {
                    HStack {
                        Image(systemName: "plus")
                        Text(MR.strings().request_in_4k.localized())
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.accentColor)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                }
            }
        }
    }
}
