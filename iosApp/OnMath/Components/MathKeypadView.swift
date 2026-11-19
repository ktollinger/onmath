import SwiftUI

struct MathKeypadView: View {
    let onDigit: (Int) -> Void
    let onBackspace: () -> Void
    let onSubmit: () -> Void
    var submitEnabled: Bool = true

    private var keyHeight: CGFloat {
        UIScreen.main.bounds.height < 700 ? 48 : 56
    }

    private let rows = [
        [1, 2, 3],
        [4, 5, 6],
        [7, 8, 9],
    ]

    var body: some View {
        VStack(spacing: 8) {
            ForEach(rows, id: \.self) { row in
                HStack(spacing: 8) {
                    ForEach(row, id: \.self) { digit in
                        Button {
                            onDigit(digit)
                        } label: {
                            Text("\(digit)")
                                .font(.system(size: 24, weight: .semibold))
                                .foregroundColor(AppThemeColors.onPrimaryContainerBlue)
                                .frame(maxWidth: .infinity)
                                .frame(height: keyHeight)
                                .background(AppThemeColors.primaryContainerBlue.opacity(0.65))
                                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                        }
                        .buttonStyle(.plain)
                    }
                }
            }

            HStack(spacing: 8) {
                Button(action: onBackspace) {
                    Image(systemName: "delete.left")
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(AppThemeColors.onPrimaryContainerBlue)
                        .frame(maxWidth: .infinity)
                        .frame(height: keyHeight)
                        .background(AppThemeColors.primaryContainerBlue.opacity(0.65))
                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                }
                .buttonStyle(.plain)

                Button {
                    onDigit(0)
                } label: {
                    Text("0")
                        .font(.system(size: 24, weight: .semibold))
                        .foregroundColor(AppThemeColors.onPrimaryContainerBlue)
                        .frame(maxWidth: .infinity)
                        .frame(height: keyHeight)
                        .background(AppThemeColors.primaryContainerBlue.opacity(0.65))
                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                }
                .buttonStyle(.plain)

                Button(action: onSubmit) {
                    Image(systemName: "checkmark")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: keyHeight)
                        .background(submitEnabled ? AppThemeColors.correctGreen : Color.gray.opacity(0.35))
                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                }
                .buttonStyle(.plain)
                .disabled(!submitEnabled)
            }
        }
    }
}
