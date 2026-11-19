import SwiftUI
import Shared

struct HomeView: View {
    @EnvironmentObject private var store: AppStore

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                Text(store.tr("home_subtitle"))
                    .font(.headline)
                    .foregroundColor(.secondary)

                let tablesDesc: String = {
                    let base = store.multiplicationTables.count == 10
                        ? store.tr("home_tables_all")
                        : store.fmt("home_tables_selected", store.multiplicationTables.sorted().map(String.init).joined(separator: ", "))
                    return base + (store.multiplicationIncludeDivision ? store.tr("home_with_division") : "")
                }()

                NavigationLink {
                    MultiplicationView()
                } label: {
                    TopicCardView(
                        title: store.tr("topic_multiplication"),
                        subtitle: tablesDesc,
                        systemImage: "multiply",
                        stats: store.stats(for: .multiplication),
                        statsFormatter: { store.fmt("home_topic_stats", $0.sessionCount, $0.accuracyPercent) }
                    )
                }
                .buttonStyle(.plain)

                NavigationLink {
                    ExpressionsView()
                } label: {
                    TopicCardView(
                        title: store.tr("topic_expressions"),
                        subtitle: store.tr(store.expressionWithParentheses ? "home_expr_with_parens" : "home_expr_without_parens"),
                        systemImage: "function",
                        stats: store.stats(for: .expressions),
                        statsFormatter: { store.fmt("home_topic_stats", $0.sessionCount, $0.accuracyPercent) }
                    )
                }
                .buttonStyle(.plain)

                let methodLabel = store.tr(
                    store.subtractionMethod == .borrowTop ? "home_method_borrow_top" : "home_method_carry_bottom"
                )
                NavigationLink {
                    SubtractionView()
                } label: {
                    TopicCardView(
                        title: store.tr("topic_subtraction"),
                        subtitle: store.fmt(
                            "home_subtraction_subtitle",
                            store.subtractionMinDigits,
                            store.subtractionMaxDigits,
                            methodLabel
                        ),
                        systemImage: "minus.forwardslash.plus",
                        stats: store.stats(for: .subtraction),
                        statsFormatter: { store.fmt("home_topic_stats", $0.sessionCount, $0.accuracyPercent) }
                    )
                }
                .buttonStyle(.plain)

                if !store.recentMistakes.isEmpty {
                    Text(store.tr("home_recent_mistakes"))
                        .font(.subheadline.weight(.semibold))
                        .foregroundColor(.secondary)
                        .padding(.top, 6)

                    VStack(spacing: 8) {
                        ForEach(store.recentMistakes) { mistake in
                            HStack {
                                Text(mistake.displayText)
                                    .font(.subheadline)
                                Spacer()
                                Text(store.fmt("home_correct_answer", mistake.expectedAnswer))
                                    .font(.subheadline.weight(.semibold))
                                    .foregroundColor(AppThemeColors.primaryBlue)
                            }
                        }
                    }
                    .padding(14)
                    .background(AppThemeColors.surfaceLight)
                    .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                    .overlay(
                        RoundedRectangle(cornerRadius: 14, style: .continuous)
                            .stroke(Color.gray.opacity(0.15), lineWidth: 1)
                    )
                }
            }
            .padding(16)
        }
        .navigationTitle(store.tr("app_name"))
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                NavigationLink {
                    SettingsView()
                } label: {
                    Image(systemName: "gearshape")
                        .accessibilityLabel(store.tr("settings_title"))
                }
            }
        }
    }
}

private struct TopicCardView: View {
    let title: String
    let subtitle: String
    let systemImage: String
    let stats: TopicStatsSummary?
    let statsFormatter: (TopicStatsSummary) -> String

    var body: some View {
        HStack(spacing: 14) {
            ZStack {
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .fill(AppThemeColors.primaryContainerBlue)
                    .frame(width: 52, height: 52)
                Image(systemName: systemImage)
                    .font(.system(size: 22, weight: .semibold))
                    .foregroundColor(AppThemeColors.onPrimaryContainerBlue)
            }

            VStack(alignment: .leading, spacing: 3) {
                Text(title)
                    .font(.title3.weight(.semibold))
                    .foregroundColor(.primary)
                Text(subtitle)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                if let stats, stats.sessionCount > 0 {
                    Text(statsFormatter(stats))
                        .font(.caption.weight(.semibold))
                        .foregroundColor(AppThemeColors.primaryBlue)
                        .padding(.top, 2)
                }
            }
            Spacer()
            Image(systemName: "chevron.right")
                .font(.caption.weight(.semibold))
                .foregroundColor(.secondary)
        }
        .padding(16)
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .shadow(color: .black.opacity(0.06), radius: 6, x: 0, y: 2)
    }
}
