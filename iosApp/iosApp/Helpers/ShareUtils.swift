//
//  ShareUtils.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-25.
//

import UIKit

func shareUrl(url: String) {
    guard let urlObj = URL(string: url) else { return }
    
    let activityVC = UIActivityViewController(
        activityItems: [urlObj],
        applicationActivities: nil
    )

    if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
       let rootVC = windowScene.windows.first?.rootViewController {
    
        if let popover = activityVC.popoverPresentationController {
            popover.sourceView = rootVC.view
            popover.sourceRect = CGRect(x: rootVC.view.bounds.midX, y: rootVC.view.bounds.midY, width: 0, height: 0)
            popover.permittedArrowDirections = []
        }
        
        rootVC.present(activityVC, animated: true, completion: nil)
    }
}
