//
//  DownloadedMediaItemView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-27.
//

import Shared
import SwiftUI

struct DownloadedMediaItemView: View {
    let item: DownloadedMediaItem
    
    private var associatedColor: Color {
        item.instanceType.associatedSwiftColor
    }
    
    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(associatedColor)
                .frame(width: 5)
            
            HStack(alignment: .top, spacing: 12) {
                if let media = item.media {
                    PosterItem(
                        item: media,
                        instanceType: item.instanceType,
                        aspectRatio: item.instanceType.aspectRatio,
                        elevation: .none,
                        radius: .small,
                        posterHeight: 80
                    )
                    .frame(width: 55, height: 80)
                } else {
                    ZStack {
                        Color(.secondarySystemBackground)
                        Image(systemName: "arrow.down.circle.fill")
                            .font(.system(size: 24))
                            .foregroundColor(.secondary)
                    }
                    .frame(width: 55, height: 80)
                    .clipShape(RoundedRectangle(cornerRadius: 6, style: .continuous))
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    HStack(alignment: .top) {
                        Text(item.title.breakable())
                            .font(.system(size: 15, weight: .bold))
                            .lineLimit(2)
                            .foregroundColor(.primary)
                        
                        Spacer()
                        
                        if !item.instanceName.isEmpty {
                            Text(item.instanceName)
                                .font(.system(size: 11, weight: .medium))
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color(.secondarySystemBackground))
                                .cornerRadius(4)
                                .foregroundColor(.secondary)
                        }
                    }
                    
                    if let subtitle = item.subtitle, !subtitle.isEmpty {
                        Text(subtitle.breakable())
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(.themePrimary)
                            .lineLimit(2)
                    }
                    
                    FlowLayout(spacing: 4) {
                        if let quality = item.quality, !quality.isEmpty {
                            Text(quality)
                                .font(.system(size: 10, weight: .semibold))
                                .padding(.horizontal, 5)
                                .padding(.vertical, 2)
                                .background(Color.accentColor.opacity(0.15))
                                .foregroundColor(.accentColor)
                                .cornerRadius(4)
                        }
                        
                        if let size = item.size?.int64Value, size > 0 {
                            Text(size.bytesAsFileSizeString())
                                .font(.system(size: 10, weight: .medium))
                                .padding(.horizontal, 5)
                                .padding(.vertical, 2)
                                .background(Color(.secondarySystemBackground))
                                .foregroundColor(.secondary)
                                .cornerRadius(4)
                        }
                        
                        ForEach(item.languages, id: \.self) { lang in
                            Text(lang)
                                .font(.system(size: 10, weight: .medium))
                                .padding(.horizontal, 5)
                                .padding(.vertical, 2)
                                .background(Color(.secondarySystemBackground))
                                .foregroundColor(.secondary)
                                .cornerRadius(4)
                        }
                        
                        if let indexer = item.indexer, !indexer.isEmpty {
                            Text(indexer)
                                .font(.system(size: 10, weight: .medium))
                                .padding(.horizontal, 5)
                                .padding(.vertical, 2)
                                .background(Color(.secondarySystemBackground))
                                .foregroundColor(.secondary)
                                .cornerRadius(4)
                        }
                        
                        ForEach(item.customFormats, id: \.self) { cf in
                            Text(cf)
                                .font(.system(size: 10, weight: .medium))
                                .padding(.horizontal, 5)
                                .padding(.vertical, 2)
                                .background(Color(.secondarySystemBackground))
                                .foregroundColor(.secondary)
                                .cornerRadius(4)
                        }
                    }
                    
                    if let date = item.date {
                        Text("Downloaded \(date.format(pattern: "MMM d, yyyy"))")
                            .font(.system(size: 11))
                            .foregroundColor(.secondary)
                            .padding(.top, 2)
                    }
                }
            }
            .padding(.vertical, 10)
            .padding(.horizontal, 12)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .background(Color(uiColor: .secondarySystemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(Color.primary.opacity(0.06), lineWidth: 0.5)
        )
    }
}
