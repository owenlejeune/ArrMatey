//
//  SwiftUIExtensions.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-27.
//

import SwiftUI
import Shared

extension Image {
    init(resource: KeyPath<MR.images, Shared.ImageResource>) {
        let imageResource = MR.images()[keyPath: resource]
        self.init(imageResource.assetImageName, bundle: imageResource.bundle)
    }
    
    init(resource: Shared.ImageResource) {
        self.init(resource.assetImageName, bundle: resource.bundle)
    }
}

extension UIImage {
    func scaleToTabBarSize(size: CGFloat = 25) -> UIImage {
        let targetSize = CGSize(width: size, height: size)
        let renderer = UIGraphicsImageRenderer(size: targetSize)
        
        return renderer.image { _ in
            self.draw(in: CGRect(origin: .zero, size: targetSize))
        }
    }
}
