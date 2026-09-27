//
//  ReadyPageView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-18.
//

import SwiftUI
import Shared

struct ReadyPageView: View {
    let instancesCount: Int
    let downloadClientsCount: Int
    let onFinish: () -> Void

    init(instancesCount: Int = 0, downloadClientsCount: Int = 0, onFinish: @escaping () -> Void) {
        self.instancesCount = instancesCount
        self.downloadClientsCount = downloadClientsCount
        self.onFinish = onFinish
    }

    private var connectedSummary: String? {
        if instancesCount > 0 && downloadClientsCount > 0 {
            let servicesStr = MR.plurals().services_count.localized(instancesCount)
            let clientsStr = MR.plurals().clients_count.localized(downloadClientsCount)
            return MR.strings().onboarding_services_and_clients_connected.formatted(args: [servicesStr, clientsStr])
        } else if instancesCount > 0 {
            let servicesStr = MR.plurals().services_count.localized(instancesCount)
            return MR.strings().onboarding_connected_format.formatted(args: [servicesStr])
        } else if downloadClientsCount > 0 {
            let clientsStr = MR.plurals().clients_count.localized(downloadClientsCount)
            return MR.strings().onboarding_connected_format.formatted(args: [clientsStr])
        }
        return nil
    }

    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 24) {
                    Spacer(minLength: 20)

                    ZStack {
                        Circle()
                            .fill(Color.themePrimary.opacity(0.15))
                            .frame(width: 120, height: 120)

                        Image(systemName: "checkmark.seal.fill")
                            .font(.system(size: 60))
                            .foregroundColor(.themePrimary)
                    }

                    VStack(spacing: 8) {
                        Text(MR.strings().onboarding_ready_title.localized())
                            .font(.title.bold())
                            .multilineTextAlignment(.center)

                        Text(MR.strings().onboarding_ready_subtitle.localized())
                            .font(.body)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 32)
                    }

                    if let summary = connectedSummary {
                        HStack(spacing: 8) {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundColor(.themePrimary)
                            Text(summary)
                                .font(.subheadline.weight(.semibold))
                        }
                        .padding(.horizontal, 18)
                        .padding(.vertical, 10)
                        .background(Color(UIColor.secondarySystemGroupedBackground))
                        .cornerRadius(12)
                    }

                    Spacer(minLength: 20)
                }
                .frame(maxWidth: 540)
                .frame(minHeight: geometry.size.height)
                .frame(maxWidth: .infinity, alignment: .center)
            }
        }
    }
}
