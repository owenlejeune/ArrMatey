//
//  DiscoverSection.swift
//  iosApp
//

import SwiftUI
import Shared

struct DiscoverSection: View {
    let title: String
    let icon: String?
    let data: PagedData<DiscoverResult>
    let onItemClick: (DiscoverResult) -> Void
    let onItemClickArr: ((SearchResult) -> Void)?
    let onLoadMore: () -> Void
    let onSeeMore: (() -> Void)?
    var showOverlays: Bool = true

    init(
        title: String,
        icon: String? = nil,
        data: PagedData<DiscoverResult>,
        onItemClick: @escaping (DiscoverResult) -> Void,
        onItemClickArr: ((SearchResult) -> Void)? = nil,
        onLoadMore: @escaping () -> Void,
        onSeeMore: (() -> Void)? = nil,
        showOverlays: Bool = true
    ) {
        self.title = title
        self.icon = icon
        self.data = data
        self.onItemClick = onItemClick
        self.onItemClickArr = onItemClickArr
        self.onLoadMore = onLoadMore
        self.onSeeMore = onSeeMore
        self.showOverlays = showOverlays
    }

    var body: some View {
        if !data.items.isEmpty {
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    HStack(spacing: 8) {
                        if let ic = icon {
                            Image(systemName: ic)
                                .font(.system(size: 20))
                        }
                        Text(title)
                            .font(.headline)
                    }
                    Spacer()
                    if let onSeeMore = onSeeMore {
                        Button(action: onSeeMore) {
                            Text(MR.strings().see_more.localized())
                                .font(.subheadline)
                        }
                    }
                }
                .padding(.horizontal, 16)

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(alignment: .top, spacing: 12) {
                        ForEach(data.items as! [DiscoverResult], id: \.id) { item in
                            DiscoverPosterItem(
                                item: item,
                                elevation: .none,
                                posterHeight: 180,
                                onItemClick: { result in
                                    if let onItemClickArr = onItemClickArr {
                                        onItemClickArr(SearchResultSeerrMediaResult(result: item, originalRank: 0))
                                    } else {
                                        onItemClick(item)
                                    }
                                },
                                showOverlays: showOverlays
                            )
                            .onAppear {
                                if item.id == (data.items.last as? DiscoverResult)?.id {
                                    onLoadMore()
                                }
                            }
                        }

                        if data.isLoadingMore {
                            ProgressView()
                                .padding(.horizontal, 16)
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12) // Ensure shadows aren't cut off
                }
            }
        }
    }
}
