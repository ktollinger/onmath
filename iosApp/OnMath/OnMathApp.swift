import SwiftUI

@main
struct OnMathApp: App {
    @StateObject private var store = AppStore()

    var body: some Scene {
        WindowGroup {
            NavigationStack {
                HomeView()
            }
            .environmentObject(store)
            .tint(AppThemeColors.primaryBlue)
        }
    }
}
