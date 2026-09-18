//
//  PreferencesPageView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-18.
//

import SwiftUI
import Shared

struct PreferencesPageView: View {
    @ObservedObject var preferences: PreferencesViewModel

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

                    VStack(spacing: 14) {
                        VStack(alignment: .leading, spacing: 8) {
                            Toggle(isOn: Binding(
                                get: { preferences.useServiceNavLogos },
                                set: { preferences.setUseServiceNavLogos($0) }
                            )) {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(MR.strings().onboarding_pref_nav_logos.localized())
                                        .font(.headline)
                                    Text(MR.strings().onboarding_pref_nav_logos_desc.localized())
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                            }
                            .tint(.themePrimary)
                        }
                        .padding(16)
                        .background(Color(UIColor.secondarySystemGroupedBackground))
                        .cornerRadius(14)

                        VStack(alignment: .leading, spacing: 8) {
                            Toggle(isOn: Binding(
                                get: { preferences.enableAcitivityPolling },
                                set: { preferences.setActivityPolling($0) }
                            )) {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(MR.strings().onboarding_pref_polling.localized())
                                        .font(.headline)
                                    Text(MR.strings().onboarding_pref_polling_desc.localized())
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                            }
                            .tint(.themePrimary)
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
    }
}
