import SwiftUI
import Shared

struct SubtractionView: View {
    @EnvironmentObject private var store: AppStore
    @Environment(\.dismiss) private var dismiss

    @State private var problems: [SubtractionProblem] = []
    @State private var currentIndex = 0
    @State private var resultDigits: [Int: Int] = [:]
    @State private var activePlaceIndex = 0
    @State private var markedBorrows: Set<Int> = []
    @State private var wrongPlaces: Set<Int> = []
    @State private var hadMistakeOnCurrent = false
    @State private var correctFirstTry = 0
    @State private var feedback: FeedbackState = .none
    @State private var isSolved = false
    @State private var isFinished = false

    private let generator = SubtractionGenerator()

    private var compactMode: Bool {
        UIScreen.main.bounds.height < 700
    }

    var body: some View {
        Group {
            if isFinished {
                SessionSummaryView(
                    topicTitle: store.tr("topic_subtraction"),
                    correctFirstTry: correctFirstTry,
                    totalProblems: problems.count,
                    onRestart: startNewSession,
                    onBackHome: { dismiss() }
                )
            } else {
                VStack(spacing: 0) {
                    PracticeProgressHeader(currentIndex: currentIndex, totalCount: problems.count)
                        .padding(.horizontal, 16)
                        .padding(.top, 6)

                    if problems.indices.contains(currentIndex) {
                        let problem = problems[currentIndex]

                        VStack(spacing: compactMode ? 8 : 12) {
                            SubtractionBoardView(
                                problem: problem,
                                method: store.subtractionMethod,
                                resultDigits: resultDigits,
                                activePlaceIndex: activePlaceIndex,
                                markedBorrows: markedBorrows,
                                wrongPlaces: wrongPlaces,
                                isSolved: isSolved,
                                onSelectPlace: { place in
                                    guard !isSolved, !isFinished else { return }
                                    activePlaceIndex = place
                                    feedback = .none
                                },
                                onToggleBorrow: { place in
                                    guard !isSolved, !isFinished else { return }
                                    if markedBorrows.contains(place) {
                                        markedBorrows.remove(place)
                                    } else {
                                        markedBorrows.insert(place)
                                    }
                                }
                            )
                            .padding(.top, compactMode ? 8 : 16)

                            Text(
                                store.subtractionMethod == .borrowTop
                                    ? store.tr("sub_tip_borrow_top")
                                    : store.tr("sub_tip_carry_bottom")
                            )
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 16)

                            if isSolved {
                                Button(action: onNextProblem) {
                                    Label(store.tr("action_next_problem"), systemImage: "arrow.right")
                                        .font(.subheadline.weight(.semibold))
                                        .foregroundColor(.white)
                                        .padding(.horizontal, 20)
                                        .padding(.vertical, 10)
                                        .background(AppThemeColors.primaryBlue)
                                        .clipShape(Capsule())
                                }
                            } else {
                                Button(action: showHint) {
                                    Label(store.tr("action_hint_column"), systemImage: "lightbulb")
                                        .font(.subheadline.weight(.semibold))
                                        .padding(.horizontal, 14)
                                        .padding(.vertical, 8)
                                        .overlay(Capsule().stroke(AppThemeColors.primaryBlue, lineWidth: 1.2))
                                }
                            }

                            FeedbackBannerView(state: feedback)
                                .frame(minHeight: 56, alignment: .top)
                                .padding(.horizontal, 16)
                        }
                    }

                    Spacer(minLength: 4)

                    MathKeypadView(
                        onDigit: onDigit,
                        onBackspace: onBackspace,
                        onSubmit: onSubmit,
                        submitEnabled: !resultDigits.isEmpty
                    )
                    .padding(.horizontal, 16)
                    .padding(.bottom, 10)
                }
            }
        }
        .navigationTitle(store.tr("topic_subtraction"))
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            if problems.isEmpty { startNewSession() }
        }
    }

    private func startNewSession() {
        let config = SubtractionConfig(
            minDigits: Int32(store.subtractionMinDigits),
            maxDigits: Int32(store.subtractionMaxDigits)
        )
        problems = generator.generateSet(config: config, count: 10)
        currentIndex = 0
        resultDigits = [:]
        activePlaceIndex = 0
        markedBorrows = []
        wrongPlaces = []
        hadMistakeOnCurrent = false
        correctFirstTry = 0
        feedback = .none
        isSolved = false
        isFinished = false
    }

    private func onDigit(_ digit: Int) {
        guard !isSolved, !isFinished, problems.indices.contains(currentIndex) else { return }
        let problem = problems[currentIndex]
        resultDigits[activePlaceIndex] = digit
        wrongPlaces.remove(activePlaceIndex)
        activePlaceIndex = min(activePlaceIndex + 1, Int(problem.digitCount) - 1)
        feedback = .none
    }

    private func onBackspace() {
        guard !isSolved, !isFinished else { return }
        let targetPlace = resultDigits[activePlaceIndex] != nil ? activePlaceIndex : max(0, activePlaceIndex - 1)
        resultDigits.removeValue(forKey: targetPlace)
        wrongPlaces.remove(targetPlace)
        activePlaceIndex = targetPlace
        feedback = .none
    }

    private func showHint() {
        guard problems.indices.contains(currentIndex) else { return }
        let problem = problems[currentIndex]
        guard activePlaceIndex < problem.columnsRightToLeft.count else { return }
        let col = problem.columnsRightToLeft[activePlaceIndex]
        let placeName: String
        switch col.placeIndex {
        case 0: placeName = store.tr("place_ones")
        case 1: placeName = store.tr("place_tens")
        case 2: placeName = store.tr("place_hundreds")
        case 3: placeName = store.tr("place_thousands")
        case 4: placeName = store.tr("place_ten_thousands")
        case 5: placeName = store.tr("place_hundred_thousands")
        default: placeName = store.fmt("place_other", Int(col.placeIndex) + 1)
        }

        let text: String
        if store.subtractionMethod == .borrowTop {
            let topAfterLend = Int(col.effectiveUpperAfterLend)
            if col.requiresBorrowFromNext {
                text = store.fmt(
                    "sub_hint_borrow_top_with_borrow",
                    placeName,
                    topAfterLend + 10,
                    Int(col.subtrahendDigit),
                    Int(col.expectedResultDigit)
                )
            } else {
                text = store.fmt(
                    "sub_hint_borrow_top_plain",
                    placeName,
                    topAfterLend,
                    Int(col.subtrahendDigit),
                    Int(col.expectedResultDigit)
                )
            }
        } else {
            let bottomWithCarry = Int(col.effectiveLowerWithCarry)
            if col.requiresBorrowFromNext {
                text = store.fmt(
                    "sub_hint_carry_bottom_with_carry",
                    placeName,
                    Int(col.minuendDigit) + 10,
                    bottomWithCarry,
                    Int(col.expectedResultDigit)
                )
            } else {
                text = store.fmt(
                    "sub_hint_carry_bottom_plain",
                    placeName,
                    Int(col.minuendDigit),
                    bottomWithCarry,
                    Int(col.expectedResultDigit)
                )
            }
        }
        feedback = .hint(text)
    }

    private func onSubmit() {
        guard !isFinished, problems.indices.contains(currentIndex) else { return }
        if isSolved {
            onNextProblem()
            return
        }
        guard !resultDigits.isEmpty else { return }
        let problem = problems[currentIndex]

        var givenAnswer = 0
        var power = 1
        for place in 0..<Int(problem.digitCount) {
            let d = resultDigits[place] ?? 0
            givenAnswer += d * power
            power *= 10
        }

        if givenAnswer == Int(problem.answer) {
            if !hadMistakeOnCurrent { correctFirstTry += 1 }
            wrongPlaces = []
            feedback = .correct(store.tr("feedback_correct_inspect"))
            isSolved = true
        } else {
            let wrong = Set(
                problem.columnsRightToLeft
                    .filter { (resultDigits[Int($0.placeIndex)] ?? 0) != Int($0.expectedResultDigit) }
                    .map { Int($0.placeIndex) }
            )
            if !hadMistakeOnCurrent {
                hadMistakeOnCurrent = true
                store.recordMistake(
                    topic: .subtraction,
                    displayText: "\(problem.minuend) − \(problem.subtrahend)",
                    expectedAnswer: Int(problem.answer)
                )
            }
            wrongPlaces = wrong
            activePlaceIndex = wrong.min() ?? 0
            feedback = .wrong(store.tr("sub_wrong_columns"))
        }
    }

    private func onNextProblem() {
        guard isSolved, !isFinished else { return }
        if currentIndex + 1 >= problems.count {
            store.recordSession(
                topic: .subtraction,
                totalProblems: problems.count,
                correctFirstTry: correctFirstTry
            )
            isSolved = false
            isFinished = true
        } else {
            currentIndex += 1
            resultDigits = [:]
            activePlaceIndex = 0
            markedBorrows = []
            wrongPlaces = []
            hadMistakeOnCurrent = false
            feedback = .none
            isSolved = false
        }
    }
}

private struct SubtractionBoardView: View {
    let problem: SubtractionProblem
    let method: SubtractionMethod
    let resultDigits: [Int: Int]
    let activePlaceIndex: Int
    let markedBorrows: Set<Int>
    let wrongPlaces: Set<Int>
    let isSolved: Bool
    let onSelectPlace: (Int) -> Void
    let onToggleBorrow: (Int) -> Void

    private var cellWidth: CGFloat {
        problem.digitCount >= 6 ? 42 : 48
    }

    var body: some View {
        let colsLeftToRight = problem.columnsRightToLeft.reversed()
        let subtrahendDigitsCount = String(problem.subtrahend).count

        VStack(alignment: .trailing, spacing: 4) {
            if method == .borrowTop {
                HStack(spacing: 4) {
                    Spacer().frame(width: 26)
                    ForEach(Array(colsLeftToRight), id: \.placeIndex) { col in
                        let place = Int(col.placeIndex)
                        let canLend = place > 0
                        let isMarked = markedBorrows.contains(place)
                        Button {
                            if canLend { onToggleBorrow(place) }
                        } label: {
                            Group {
                                if isMarked && canLend {
                                    Text("\(max(0, Int(col.minuendDigit) - 1))")
                                        .font(.system(size: 16, weight: .bold, design: .monospaced))
                                        .foregroundColor(AppThemeColors.primaryBlue)
                                } else {
                                    Text(" ")
                                        .font(.system(size: 16, design: .monospaced))
                                }
                            }
                            .frame(width: cellWidth, height: 24)
                            .background(
                                canLend
                                    ? (isMarked ? AppThemeColors.primaryContainerBlue.opacity(0.6) : Color.gray.opacity(0.08))
                                    : Color.clear
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 6, style: .continuous))
                        }
                        .buttonStyle(.plain)
                        .disabled(!canLend)
                    }
                }
            }

            // Minuend row
            HStack(spacing: 4) {
                Spacer().frame(width: 26)
                ForEach(Array(colsLeftToRight), id: \.placeIndex) { col in
                    let place = Int(col.placeIndex)
                    let canLend = method == .borrowTop && place > 0
                    let crossedOut = canLend && markedBorrows.contains(place)
                    Button {
                        if canLend { onToggleBorrow(place) }
                    } label: {
                        Text("\(col.minuendDigit)")
                            .font(.system(size: 30, weight: .bold, design: .monospaced))
                            .strikethrough(crossedOut)
                            .foregroundColor(crossedOut ? .secondary : .primary)
                            .frame(width: cellWidth)
                    }
                    .buttonStyle(.plain)
                    .disabled(!canLend)
                }
            }

            // Subtrahend row
            HStack(spacing: 4) {
                Text("−")
                    .font(.system(size: 28, weight: .bold, design: .monospaced))
                    .foregroundColor(AppThemeColors.primaryBlue)
                    .frame(width: 26)

                ForEach(Array(colsLeftToRight), id: \.placeIndex) { col in
                    let place = Int(col.placeIndex)
                    let hasSubDigit = place < subtrahendDigitsCount
                    let canCarry = method == .carryBottom && place > 0
                    let isCarryMarked = canCarry && markedBorrows.contains(place)

                    Button {
                        if canCarry { onToggleBorrow(place) }
                    } label: {
                        ZStack(alignment: .bottomTrailing) {
                            Text(hasSubDigit ? "\(col.subtrahendDigit)" : " ")
                                .font(.system(size: 30, weight: .bold, design: .monospaced))
                                .frame(width: cellWidth)

                            if isCarryMarked {
                                Text("1")
                                    .font(.system(size: 12, weight: .bold, design: .monospaced))
                                    .foregroundColor(.white)
                                    .frame(width: 16, height: 16)
                                    .background(AppThemeColors.primaryBlue)
                                    .clipShape(Circle())
                            }
                        }
                        .frame(width: cellWidth)
                    }
                    .buttonStyle(.plain)
                    .disabled(!canCarry)
                }
            }

            // Divider line
            Rectangle()
                .fill(Color.primary)
                .frame(width: 26 + CGFloat(problem.digitCount) * (cellWidth + 4), height: 2.5)
                .padding(.vertical, 2)

            // Result row
            HStack(spacing: 4) {
                Spacer().frame(width: 26)
                ForEach(Array(colsLeftToRight), id: \.placeIndex) { col in
                    let place = Int(col.placeIndex)
                    let isActive = place == activePlaceIndex && !isSolved
                    let isWrong = wrongPlaces.contains(place)
                    let digit = resultDigits[place]

                    Button {
                        onSelectPlace(place)
                    } label: {
                        Text(digit.map(String.init) ?? "")
                            .font(.system(size: 28, weight: .bold, design: .monospaced))
                            .foregroundColor(isWrong ? AppThemeColors.errorRed : .primary)
                            .frame(width: cellWidth, height: 50)
                            .background(
                                isWrong
                                    ? AppThemeColors.errorRed.opacity(0.20)
                                    : (isActive ? AppThemeColors.primaryBlue.opacity(0.20) : AppThemeColors.surfaceLight)
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                            .overlay(
                                RoundedRectangle(cornerRadius: 10, style: .continuous)
                                    .stroke(
                                        isWrong ? AppThemeColors.errorRed : (isActive ? AppThemeColors.primaryBlue : Color.gray.opacity(0.35)),
                                        lineWidth: isActive || isWrong ? 2 : 1
                                    )
                            )
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }
}
