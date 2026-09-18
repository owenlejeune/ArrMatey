//
//  SetupChoicePageView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-18.
//

import SwiftUI
import Shared

struct SetupChoicePageView: View {
    let onRestoreBackup: () -> Void
    let onManualSetup: () -> Void
    let onSkip: () -> Void

    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 24) {
                    Spacer(minLength: 20)

                    VStack(spacing: 8) {
                        Text(MR.strings().onboarding_setup_choice_title.localized())
                            .font(.title2.bold())
                            .multilineTextAlignment(.center)

                        Text(MR.strings().onboarding_setup_choice_subtitle.localized())
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 20)
                    }

                    VStack(spacing: 16) {
                        Button(action: onManualSetup) {
                            HStack(spacing: 16) {
                                Image(systemName: "sparkles")
                                    .font(.system(size: 28))
                                    .foregroundColor(.themePrimary)
                                    .frame(width: 52, height: 52)
                                    .background(Color.themePrimary.opacity(0.12))
                                    .clipShape(RoundedRectangle(cornerRadius: 14))

                                VStack(alignment: .leading, spacing: 4) {
                                    Text(MR.strings().onboarding_choice_fresh_title.localized())
                                        .font(.headline)
                                        .foregroundColor(.primary)

                                    Text(MR.strings().onboarding_choice_fresh_desc.localized())
                                        .font(.subheadline)
                                        .foregroundColor(.secondary)
                                        .multilineTextAlignment(.leading)
                                }

                                Spacer()

                                Image(systemName: "chevron.right")
                                    .font(.subheadline.bold())
                                    .foregroundColor(.secondary)
                            }
                            .padding(18)
                            .background(Color(UIColor.secondarySystemGroupedBackground))
                            .cornerRadius(16)
                            .overlay(
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(Color.themePrimary.opacity(0.2), lineWidth: 1)
                            )
                        }
                        .buttonStyle(.plain)

                        Button(action: onRestoreBackup) {
                            HStack(spacing: 16) {
                                Image(systemName: "arrow.down.doc.fill")
                                    .font(.system(size: 28))
                                    .foregroundColor(.themePrimary)
                                    .frame(width: 52, height: 52)
                                    .background(Color.themePrimary.opacity(0.12))
                                    .clipShape(RoundedRectangle(cornerRadius: 14))

                                VStack(alignment: .leading, spacing: 4) {
                                    Text(MR.strings().onboarding_choice_backup_title.localized())
                                        .font(.headline)
                                        .foregroundColor(.primary)

                                    Text(MR.strings().onboarding_choice_backup_desc.localized())
                                        .font(.subheadline)
                                        .foregroundColor(.secondary)
                                        .multilineTextAlignment(.leading)
                                }

                                Spacer()

                                Image(systemName: "chevron.right")
                                    .font(.subheadline.bold())
                                    .foregroundColor(.secondary)
                            }
                            .padding(18)
                            .background(Color(UIColor.secondarySystemGroupedBackground))
                            .cornerRadius(16)
                            .overlay(
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(Color.themePrimary.opacity(0.2), lineWidth: 1)
                            )
                        }
                        .buttonStyle(.plain)

                        Button(action: onSkip) {
                            HStack(spacing: 16) {
                                Image(systemName: "arrow.right")
                                    .font(.system(size: 28))
                                    .foregroundColor(.themePrimary)
                                    .frame(width: 52, height: 52)
                                    .background(Color.themePrimary.opacity(0.12))
                                    .clipShape(RoundedRectangle(cornerRadius: 14))

                                VStack(alignment: .leading, spacing: 4) {
                                    Text(MR.strings().onboarding_choice_skip_title.localized())
                                        .font(.headline)
                                        .foregroundColor(.primary)

                                    Text(MR.strings().onboarding_choice_skip_desc.localized())
                                        .font(.subheadline)
                                        .foregroundColor(.secondary)
                                        .multilineTextAlignment(.leading)
                                }

                                Spacer()

                                Image(systemName: "chevron.right")
                                    .font(.subheadline.bold())
                                    .foregroundColor(.secondary)
                            }
                            .padding(18)
                            .background(Color(UIColor.secondarySystemGroupedBackground))
                            .cornerRadius(16)
                            .overlay(
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(Color.themePrimary.opacity(0.2), lineWidth: 1)
                            )
                        }
                        .buttonStyle(.plain)
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
