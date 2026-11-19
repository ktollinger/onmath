import SwiftUI
import Shared

struct SettingsView: View {
    @EnvironmentObject private var store: AppStore

    var body: some View {
        Form {
            Section(store.tr("settings_language_section")) {
                Picker(store.tr("settings_language_section"), selection: $store.language) {
                    Text(store.tr("settings_language_system")).tag(AppLanguage.system)
                    Text(store.tr("settings_language_czech")).tag(AppLanguage.czech)
                    Text(store.tr("settings_language_english")).tag(AppLanguage.english)
                }
                .pickerStyle(.segmented)
            }

            Section(store.tr("settings_subtraction_section")) {
                Picker(store.tr("settings_subtraction_method"), selection: $store.subtractionMethod) {
                    Text(store.tr("settings_method_borrow_top_title")).tag(SubtractionMethod.borrowTop)
                    Text(store.tr("settings_method_carry_bottom_title")).tag(SubtractionMethod.carryBottom)
                }

                Text(
                    store.subtractionMethod == .borrowTop
                        ? store.tr("settings_method_borrow_top_subtitle")
                        : store.tr("settings_method_carry_bottom_subtitle")
                )
                .font(.caption)
                .foregroundColor(.secondary)

                Stepper(
                    "\(store.tr("settings_min_digits")): \(store.subtractionMinDigits)",
                    value: Binding(
                        get: { store.subtractionMinDigits },
                        set: { newVal in
                            store.subtractionMinDigits = newVal
                            if store.subtractionMaxDigits < newVal {
                                store.subtractionMaxDigits = newVal
                            }
                        }
                    ),
                    in: 2...5
                )

                Stepper(
                    "\(store.tr("settings_max_digits")): \(store.subtractionMaxDigits)",
                    value: $store.subtractionMaxDigits,
                    in: store.subtractionMinDigits...6
                )
            }

            Section(store.tr("settings_multiplication_section")) {
                HStack {
                    Text(store.tr("settings_selected_tables"))
                    Spacer()
                    Button(store.tr("settings_select_all")) {
                        store.multiplicationTables = Set(1...10)
                    }
                    .font(.subheadline)
                }

                LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 8), count: 5), spacing: 8) {
                    ForEach(1...10, id: \.self) { table in
                        let isSelected = store.multiplicationTables.contains(table)
                        Button {
                            if isSelected {
                                if store.multiplicationTables.count > 1 {
                                    store.multiplicationTables.remove(table)
                                }
                            } else {
                                store.multiplicationTables.insert(table)
                            }
                        } label: {
                            Text("\(table)")
                                .font(.subheadline.weight(.semibold))
                                .frame(maxWidth: .infinity)
                                .frame(height: 36)
                                .background(isSelected ? AppThemeColors.primaryBlue : AppThemeColors.primaryContainerBlue.opacity(0.45))
                                .foregroundColor(isSelected ? .white : AppThemeColors.onPrimaryContainerBlue)
                                .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.vertical, 4)

                Toggle(store.tr("settings_include_division"), isOn: $store.multiplicationIncludeDivision)
            }

            Section(store.tr("settings_expressions_section")) {
                Toggle(store.tr("settings_use_parentheses"), isOn: $store.expressionWithParentheses)
                Toggle(store.tr("settings_large_intermediate"), isOn: $store.expressionLargeIntermediate)
            }

            Section(store.tr("settings_history_section")) {
                Button(role: .destructive) {
                    store.clearHistory()
                } label: {
                    Text(store.tr("settings_clear_history"))
                }
            }
        }
        .navigationTitle(store.tr("settings_title"))
        .navigationBarTitleDisplayMode(.inline)
    }
}
