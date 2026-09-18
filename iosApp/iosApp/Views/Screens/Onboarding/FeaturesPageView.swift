//
//  FeaturesPageView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-18.
//

import SwiftUI
import Shared

struct MediaFeaturesPageView: View {
    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 20) {
                    Spacer(minLength: 16)

                    VStack(spacing: 8) {
                        Text(MR.strings().onboarding_features_media_title.localized())
                            .font(.title2.bold())
                            .multilineTextAlignment(.center)

                        Text(MR.strings().onboarding_features_media_subtitle.localized())
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 20)
                    }

                    VStack(spacing: 12) {
                        FeatureCard(
                            icon: "books.vertical.fill",
                            title: MR.strings().onboarding_feature_unified_library_title.localized(),
                            description: MR.strings().onboarding_feature_unified_library_desc.localized()
                        )

                        FeatureCard(
                            icon: "sparkles.rectangle.stack.fill",
                            title: MR.strings().onboarding_feature_cross_service_title.localized(),
                            description: MR.strings().onboarding_feature_cross_service_desc.localized()
                        )

                        FeatureCard(
                            icon: "arrow.down.circle.fill",
                            title: MR.strings().onboarding_feature_activity_title.localized(),
                            description: MR.strings().onboarding_feature_activity_desc.localized()
                        )

                        FeatureCard(
                            icon: "calendar",
                            title: MR.strings().onboarding_feature_calendar_title.localized(),
                            description: MR.strings().onboarding_feature_calendar_desc.localized()
                        )
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

struct PowerFeaturesPageView: View {
    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 20) {
                    Spacer(minLength: 16)

                    VStack(spacing: 8) {
                        Text(MR.strings().onboarding_features_power_title.localized())
                            .font(.title2.bold())
                            .multilineTextAlignment(.center)

                        Text(MR.strings().onboarding_features_power_subtitle.localized())
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 20)
                    }

                    VStack(spacing: 12) {
                        FeatureCard(
                            icon: "magnifyingglass",
                            title: MR.strings().onboarding_feature_search_indexers_title.localized(),
                            description: MR.strings().onboarding_feature_search_indexers_desc.localized()
                        )

                        FeatureCard(
                            icon: "wifi",
                            title: MR.strings().onboarding_feature_local_network_title.localized(),
                            description: MR.strings().onboarding_feature_local_network_desc.localized()
                        )

                        FeatureCard(
                            icon: "server.rack",
                            title: MR.strings().onboarding_feature_multi_instance_title.localized(),
                            description: MR.strings().onboarding_feature_multi_instance_desc.localized()
                        )

                        FeatureCard(
                            icon: "lock.shield.fill",
                            title: MR.strings().onboarding_feature_backup_sync_title.localized(),
                            description: MR.strings().onboarding_feature_backup_sync_desc.localized()
                        )
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

private struct FeatureCard: View {
    let icon: String
    let title: String
    let description: String

    var body: some View {
        HStack(alignment: .top, spacing: 16) {
            Image(systemName: icon)
                .font(.system(size: 22))
                .foregroundColor(.themePrimary)
                .frame(width: 40, height: 40)
                .background(Color.themePrimary.opacity(0.12))
                .clipShape(RoundedRectangle(cornerRadius: 10))

            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.headline)

                Text(description)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }

            Spacer()
        }
        .padding(14)
        .background(Color(UIColor.secondarySystemGroupedBackground))
        .cornerRadius(14)
    }
}
