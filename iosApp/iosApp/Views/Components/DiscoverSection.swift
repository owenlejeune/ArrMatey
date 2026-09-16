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
    var showOverlays: Bool = true

    init(
        title: String,
        icon: String? = nil,
        data: PagedData<DiscoverResult>,
        onItemClick: @escaping (DiscoverResult) -> Void,
        onItemClickArr: ((SearchResult) -> Void)? = nil,
        onLoadMore: @escaping () -> Void,
        showOverlays: Bool = true
    ) {
        self.title = title
        self.icon = icon
        self.data = data
        self.onItemClick = onItemClick
        self.onItemClickArr = onItemClickArr
        self.onLoadMore = onLoadMore
        self.showOverlays = showOverlays
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 8) {
                if let ic = icon {
                    Image(systemName: ic)
                        .font(.system(size: 20))
                }
                Text(title)
                    .font(.headline)
            }
            .padding(.horizontal, 16)

            if data.isLoading && data.items.isEmpty {
                HStack {
                    Spacer()
                    ProgressView()
                    Spacer()
                }
                .padding(.vertical, 24)
            } else if !data.items.isEmpty {
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
            } else if let error = data.error {
                Text(error)
                    .foregroundColor(.red)
                    .padding(.horizontal, 16)
            }
        }
    }
}
