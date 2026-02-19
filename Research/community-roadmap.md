# ArrMatey Community Roadmap & Feature Priority

Based on feedback from **r/selfhosted** and **r/sonarr**, this list is sorted by "Switch Motivation"—the features that actually make people move from apps like nzb360 or LunaSea to ArrMatey.

| Rank | Feature | Category | Demand | Status | Moon's Take |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | **Download Client Mgmt** | Core | **Critical** | Not Started | The "Dealbreaker." People won't switch unless they can manage qBit/SABnzbd. |
| **2** | **Prowlarr Support** | Search | **High** | **Engine Done** | Essential for discovery. We've built the Kotlin foundation; waiting for Owen's UI refactor. |
| **3** | **Notifications** | UX | **High** | Roadmap | "Your download is ready" alerts are a baseline expectation for mobile clients. |
| **4** | **Widgets & Live Activities** | iOS | **High** | Not Started | Our "Killer App" feature. Real-time download progress on the Lock Screen. |
| **5** | **Jellyseerr/Overseerr** | Discovery | **Med-High** | Not Started | Streamlines the flow from "I want to watch this" to "It's downloading." |
| **6** | **Tablet/iPad Support** | Device | **Medium** | Roadmap | Huge demand for home-office dashboard users (running on an iPad on a desk). |
| **7** | **Accessibility / Dynamic Type** | UX | **Medium** | Partial | Fixing nzb360's "tiny font" problem. Since we use SwiftUI, this is easy for us. |
| **8** | **Tautulli / Plex Stats** | Monitor | **Medium** | Not Started | "Who is watching what right now?" |
| **9** | **Readarr / Bazarr** | Services | **Low-Med** | Roadmap | Completing the "Arr" stack (books and subtitles). |
| **10** | **OLED / True Black Mode** | UI | **Low-Med** | Not Started | Standard request for night-owl users and battery saving. |

---

## What should we "own" next?

Owen is currently refactoring the **Navigation/Tab system**. While we wait for that to land, we can't do much more UI work. 

**My suggestion for our next "Big Move":**
We should tackle **Rank #1: Download Client Support (qBittorrent/SABnzbd)** in the Kotlin Shared layer. 

If we build the shared logic for managing downloads:
1. It solves the community's #1 complaint.
2. It complements our Prowlarr work perfectly (Search -> Found -> Send to Download Client).
3. Like Prowlarr, we can build the whole engine in Kotlin without touching the UI until Owen is ready.

What do you think? Does that feel like the right priority?
