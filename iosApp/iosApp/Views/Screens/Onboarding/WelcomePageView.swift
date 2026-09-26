//
//  WelcomePageView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-18.
//

import SwiftUI
import Shared

struct WelcomePageView: View {
    private struct SupportedService: Identifiable {
        let id: String
        let name: String
        let icon: Shared.ImageResource?
    }

    private var supportedServices: [SupportedService] {
        var list: [SupportedService] = []
        for type in InstanceType.allCases {
            list.append(SupportedService(id: "instance_\(type.name)", name: type.name, icon: type.icon))
        }
        for client in DownloadClientType.allCases {
            list.append(SupportedService(id: "client_\(client.displayName)", name: client.displayName, icon: client.icon))
        }
        return list
    }

    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 24) {
                    Spacer(minLength: 20)

                    if let icon = Bundle.main.icon {
                        Image(uiImage: icon)
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 88, height: 88)
                            .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
                            .shadow(color: Color.black.opacity(0.12), radius: 8, x: 0, y: 4)
                    } else {
                        ZStack {
                            Circle()
                                .fill(Color.accentColor.opacity(0.12))
                                .frame(width: 88, height: 88)

                            Image(systemName: "sailboat.fill")
                                .font(.system(size: 44))
                                .foregroundStyle(Color.accentColor)
                        }
                    }

                    VStack(spacing: 8) {
                        Text(MR.strings().onboarding_welcome_title.localized())
                            .font(.title.bold())
                            .multilineTextAlignment(.center)

                        Text(MR.strings().onboarding_welcome_subtitle.localized())
                            .font(.body)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 24)
                    }

                    VStack(alignment: .leading, spacing: 12) {
                        Text(MR.strings().onboarding_supported_services.localized())
                            .font(.caption.bold())
                            .foregroundColor(.secondary)
                            .padding(.horizontal, 4)

                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                            ForEach(supportedServices) { service in
                                HStack(spacing: 10) {
                                    if let logo = service.icon {
                                        logo.toImage(renderingMode: .original)
                                            .frame(width: 20, height: 20)
                                    } else {
                                        Image(systemName: "server.rack")
                                            .foregroundColor(.themePrimary)
                                    }

                                    Text(service.name)
                                        .font(.subheadline.weight(.medium))

                                    Spacer()
                                }
                                .padding(.horizontal, 14)
                                .padding(.vertical, 10)
                                .background(Color(UIColor.secondarySystemGroupedBackground))
                                .cornerRadius(12)
                            }
                        }
                    }
                    .padding(.horizontal, 20)

                    Spacer(minLength: 20)
                }
                .frame(maxWidth: 540)
                .frame(minHeight: geometry.size.height)
                .frame(maxWidth: .infinity, alignment: .center)
            }
        }
    }
}
