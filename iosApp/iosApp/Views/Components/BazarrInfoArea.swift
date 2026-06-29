//
//  BazarrInfoArea.swift
//  iosApp
//

import SwiftUI
import Shared

struct BazarrInfoArea: View {
    let details: BazarrMedia
    
    private var infoItems: [InfoItem] {
        if let movie = details as? BazarrMovie {
            return [
                InfoItem(label: MR.strings().path.localized(), value: movie.path),
                InfoItem(label: MR.strings().language.localized(), value: movie.audioLanguage.map { $0.name }.joined(separator: ", "))
            ]
        } else if let series = details as? BazarrSeries {
            var items = [
                InfoItem(label: MR.strings().path.localized(), value: series.path),
                InfoItem(label: MR.strings().files.localized(), value: MR.strings().bazarr_files_count.formatted(args: [series.episodeFileCount])),
                InfoItem(label: MR.strings().missing.localized(), value: MR.strings().bazarr_missing_count.formatted(args: [series.episodeMissingCount])),
                InfoItem(label: MR.strings().status.localized(), value: (series.ended ? MR.strings().ended : MR.strings().continuing).localized())
            ]
            
            if let lastAired = series.lastAired {
                items.append(InfoItem(label: MR.strings().previous_airing.localized(), value: lastAired))
            }
            
            items.append(InfoItem(label: MR.strings().series_type.localized(), value: series.seriesType))
            
            return items
        }
        return []
    }
    
    var body: some View {
        Section {
            VStack(spacing: 12) {
                ForEach(infoItems) { info in
                    HStack(alignment: .center) {
                        Text(info.label)
                            .font(.system(size: 14))
                        Spacer(minLength: 2.0)
                        Text(info.value)
                            .font(.system(size: 14))
                            .foregroundColor(.themePrimary)
                            .lineLimit(2)
                            .truncationMode(.tail)
                            .multilineTextAlignment(.trailing)
                            .frame(maxWidth: .infinity, alignment: .trailing)
                    }
                    
                    if info != infoItems.last {
                        Divider()
                    }
                }
            }
        } header: {
            Text(MR.strings().information.localized())
                .font(.system(size: 26, weight: .bold))
        }
    }
}
