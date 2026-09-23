//
//  PreferencesPageView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-18.
//

import SwiftUI
import Shared
import UserNotifications

struct PreferencesPageView: View {
    @ObservedObject var preferences: PreferencesViewModel
    @State private var hasNotificationPermission: Bool = false

    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 20) {
                    Spacer(minLength: 16)

                    VStack(spacing: 8) {
                        Text(MR.strings().onboarding_preferences_title.localized())
                            .font(.title2.bold())
                            .multilineTextAlignment(.center)

                        Text(MR.strings().onboarding_preferences_subtitle.localized())
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 20)
                    }

                    VStack(spacing: 16) {
                        // Notification Permissions Card
                        VStack(alignment: .leading, spacing: 10) {
                            HStack(alignment: .center, spacing: 14) {
                                Image(systemName: "bell.badge.fill")
                                    .font(.title2)
                                    .foregroundColor(.themePrimary)

                                VStack(alignment: .leading, spacing: 2) {
                                    Text(MR.strings().onboarding_pref_notifications_title.localized())
                                        .font(.headline)
                                    Text(MR.strings().onboarding_pref_notifications_desc.localized())
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }

                                Spacer()

                                if hasNotificationPermission {
                                    HStack(spacing: 4) {
                                        Image(systemName: "checkmark.circle.fill")
                                            .foregroundColor(.themePrimary)
                                        Text(MR.strings().onboarding_notifications_enabled.localized())
                                            .font(.caption.bold())
                                            .foregroundColor(.themePrimary)
                                    }
                                } else {
                                    Button {
                                        requestNotificationPermission()
                                    } label: {
                                        Text(MR.strings().onboarding_notifications_enable_button.localized())
                                            .font(.caption.bold())
                                            .foregroundColor(.white)
                                            .padding(.horizontal, 12)
                                            .padding(.vertical, 6)
                                            .background(Color.themePrimary)
                                            .cornerRadius(16)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                        .padding(16)
                        .background(Color(UIColor.secondarySystemGroupedBackground))
                        .cornerRadius(14)
                    }
                    .padding(.horizontal, 20)

                    Spacer(minLength: 20)
                }
                .frame(minHeight: geometry.size.height)
                .frame(maxWidth: .infinity)
            }
        }
        .onAppear {
            checkNotificationPermission()
        }
    }

    private func checkNotificationPermission() {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            DispatchQueue.main.async {
                self.hasNotificationPermission = (settings.authorizationStatus == .authorized || settings.authorizationStatus == .provisional)
            }
        }
    }

    private func requestNotificationPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
            DispatchQueue.main.async {
                self.hasNotificationPermission = granted
            }
        }
    }
}
