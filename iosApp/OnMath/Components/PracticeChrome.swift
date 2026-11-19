import SwiftUI

enum FeedbackState: Equatable {
    case none
    case correct(String)
    case wrong(String)
    case hint(String)
}

struct FeedbackBannerView: View {
    let state: FeedbackState

    var body: some View {
        switch state {
        case .none:
            EmptyView()
        case .correct(let message):
            banner(
                text: message,
                icon: "checkmark.circle.fill",
                bg: AppThemeColors.correctContainerGreen,
                fg: AppThemeColors.correctGreen
            )
        case .wrong(let message):
            banner(
                text: message,
                icon: nil,
                bg: AppThemeColors.errorContainerRed,
                fg: AppThemeColors.errorRed
            )
        case .hint(let message):
            banner(
                text: message,
                icon: "lightbulb.fill",
                bg: AppThemeColors.hintContainerAmber,
                fg: AppThemeColors.hintAmber
            )
        }
    }

    private func banner(text: String, icon: String?, bg: Color, fg: Color) -> some View {
        HStack(spacing: 8) {
            if let icon {
                Image(systemName: icon)
                    .foregroundColor(fg)
                    .font(.system(size: 16, weight: .semibold))
            }
            Text(text)
                .font(.subheadline.weight(.medium))
                .foregroundColor(fg)
                .multilineTextAlignment(.leading)
            Spacer(minLength: 0)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .frame(maxWidth: .infinity)
        .background(bg)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
    }
}

struct PracticeProgressHeader: View {
    let currentIndex: Int
    let totalCount: Int

    var body: some View {
        VStack(spacing: 6) {
            HStack {
                Spacer()
                Text("\(min(currentIndex + 1, max(1, totalCount))) / \(totalCount)")
                    .font(.subheadline.weight(.semibold))
                    .foregroundColor(AppThemeColors.primaryBlue)
            }
            ProgressView(value: Double(currentIndex), total: Double(max(1, totalCount)))
                .tint(AppThemeColors.primaryBlue)
        }
    }
}

struct SessionSummaryView: View {
    @EnvironmentObject private var store: AppStore
    let topicTitle: String
    let correctFirstTry: Int
    let totalProblems: Int
    let onRestart: () -> Void
    let onBackHome: () -> Void

    private var accuracy: Int {
        totalProblems == 0 ? 0 : (correctFirstTry * 100) / totalProblems
    }

    private var stars: Int {
        if accuracy >= 90 { return 3 }
        if accuracy >= 70 { return 2 }
        if accuracy >= 40 { return 1 }
        return 0
    }

    var body: some View {
        VStack(spacing: 20) {
            Spacer()

            HStack(spacing: 10) {
                ForEach(0..<3, id: \.self) { index in
                    Image(systemName: "star.fill")
                        .font(.system(size: 40))
                        .foregroundColor(index < stars ? AppThemeColors.hintAmber : Color.gray.opacity(0.3))
                }
            }

            VStack(spacing: 6) {
                Text(store.tr("summary_title"))
                    .font(.title.bold())
                Text(topicTitle)
                    .font(.headline)
                    .foregroundColor(.secondary)
            }

            VStack(spacing: 6) {
                Text("\(correctFirstTry) / \(totalProblems)")
                    .font(.system(size: 36, weight: .bold))
                    .foregroundColor(AppThemeColors.primaryBlue)
                Text(store.fmt("summary_first_try", accuracy))
                    .font(.body)
                    .multilineTextAlignment(.center)
            }
            .padding(20)
            .frame(maxWidth: .infinity)
            .background(AppThemeColors.surfaceLight)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .stroke(Color.gray.opacity(0.15), lineWidth: 1)
            )

            VStack(spacing: 12) {
                Button(action: onRestart) {
                    Text(store.tr("summary_restart"))
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(AppThemeColors.primaryBlue)
                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                }

                Button(action: onBackHome) {
                    Text(store.tr("summary_back_home"))
                        .font(.headline)
                        .foregroundColor(AppThemeColors.primaryBlue)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .overlay(
                            RoundedRectangle(cornerRadius: 14, style: .continuous)
                                .stroke(AppThemeColors.primaryBlue, lineWidth: 1.5)
                        )
                }
            }

            Spacer()
        }
        .padding(24)
    }
}
