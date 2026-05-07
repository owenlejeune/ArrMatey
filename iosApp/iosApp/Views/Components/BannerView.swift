//
//  BannerView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-10.
//

import SwiftUI
import Shared

struct BannerView: View {
    let item: ArrMedia
    var imageResource: Shared.ImageResource? = nil
    var instanceType: Shared.InstanceType? = nil
    
    var body: some View {
        if let resource = imageResource {
            Image(resource: resource)
                .resizable()
                .aspectRatio(contentMode: .fill)
        } else if let bannerUrl = item.getBanner()?.remoteUrl {
            AsyncImage(url: URL(string: bannerUrl)) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Color.gray.opacity(0.3)
            }
        } else {
            Color.clear
        }
    }
}
