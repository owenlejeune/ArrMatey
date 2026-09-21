//
//  TimeoutErrorView.swift
//  iosApp
//
//  Created for ArrMatey
//

import SwiftUI
import Shared

struct ErrorView: View {
    let errorType: HttpErrorType
    let message: String
    let onOpenSettings: () -> Void
    let onRetry: () -> Void

    init(
        errorType: HttpErrorType,
        message: String,
        onOpenSettings: @escaping () -> Void,
        onRetry: @escaping () -> Void
    ) {
        self.errorType = errorType
        self.message = message
        self.onOpenSettings = onOpenSettings
        self.onRetry = onRetry
    }

    var body: some View {
        ContentUnavailableView {
            Label(title, systemImage: iconName)
                .foregroundStyle(iconColor)
        } description: {
            VStack(spacing: 8) {
                Text(detailMessage)
                    .foregroundStyle(.secondary)

                if errorType == .timeout {
                    Text(
                        MR.strings().error_timeout_tip.formatted(
                            args: [MR.strings().slow_instance.localized()]
                        )
                    )
                    .font(.caption)
                    .foregroundStyle(.tertiary)
                    .padding(.top, 4)
                }
            }
        } actions: {
            VStack(spacing: 12) {
                if errorType == .timeout {
                    Button(action: onOpenSettings) {
                        Label(MR.strings().error_timeout_configure_instance.localized(), systemImage: "gearshape.fill")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                }

                if errorType == .timeout {
                    Button(action: onRetry) {
                        Label(MR.strings().retry.localized(), systemImage: "arrow.clockwise")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                } else {
                    Button(action: onRetry) {
                        Label(MR.strings().retry.localized(), systemImage: "arrow.clockwise")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                }
            }
            .frame(maxWidth: 280)
            .padding(.top, 8)
        }
    }

    private var iconName: String {
        switch errorType {
        case .timeout:
            return "clock.badge.exclamationmark"
        case .network:
            return "wifi.exclamationmark"
        default:
            return "exclamationmark.triangle"
        }
    }

    private var iconColor: Color {
        switch errorType {
        case .timeout, .network:
            return .orange
        default:
            return .red
        }
    }

    private var title: String {
        switch errorType {
        case .timeout:
            return MR.strings().error_timeout_title.localized()
        case .network:
            return MR.strings().error_network_title.localized()
        default:
            return MR.strings().error_generic_title.localized()
        }
    }

    private var detailMessage: String {
        if errorType == .timeout {
            return MR.strings().error_timeout_description.localized()
        }
        return message
    }
}

#Preview("Timeout Error") {
    ErrorView(
        errorType: .timeout,
        message: MR.strings().error_timeout_description.localized(),
        onOpenSettings: {},
        onRetry: {}
    )
}

#Preview("Generic Error") {
    ErrorView(
        errorType: .unexpected,
        message: MR.strings().instance_connect_error_ios.localized(),
        onOpenSettings: {},
        onRetry: {}
    )
}
