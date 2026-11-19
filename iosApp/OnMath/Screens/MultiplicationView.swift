import SwiftUI
import Shared

struct MultiplicationView: View {
    @EnvironmentObject private var store: AppStore
    @Environment(\.dismiss) private var dismiss

    @State private var problems: [MultiplicationProblem] = []
    @State private var currentIndex = 0
    @State private var inputText = ""
    @State private var correctFirstTry = 0
    @State private var hadMistakeOnCurrent = false
    @State private var feedback: FeedbackState = .none
    @State private var isAdvancing = false
    @State private var isFinished = false

    private let generator = MultiplicationGenerator()

    var body: some View {
        Group {
            if isFinished {
                SessionSummaryView(
                    topicTitle: store.tr("topic_multiplication"),
                    correctFirstTry: correctFirstTry,
                    totalProblems: problems.count,
                    onRestart: startNewSession,
                    onBackHome: { dismiss() }
                )
            } else {
                VStack(spacing: 0) {
                    PracticeProgressHeader(currentIndex: currentIndex, totalCount: problems.count)
                        .padding(.horizontal, 16)
                        .padding(.top, 8)

                    Spacer()

                    VStack(spacing: 18) {
                        Text(problems.indices.contains(currentIndex) ? problems[currentIndex].displayText : "")
                            .font(.system(size: 36, weight: .bold))
                            .multilineTextAlignment(.center)

                        Text(inputText.isEmpty ? "?" : inputText)
                            .font(.system(size: 32, weight: .bold))
                            .foregroundColor(inputText.isEmpty ? Color.gray.opacity(0.4) : .primary)
                            .frame(maxWidth: 220)
                            .frame(height: 62)
                            .background(AppThemeColors.surfaceLight)
                            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                            .overlay(
                                RoundedRectangle(cornerRadius: 16, style: .continuous)
                                    .stroke(AppThemeColors.primaryBlue, lineWidth: 2)
                            )

                        Button(action: showHint) {
                            Label(store.tr("action_hint"), systemImage: "lightbulb")
                                .font(.subheadline.weight(.semibold))
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                                .overlay(
                                    Capsule().stroke(AppThemeColors.primaryBlue, lineWidth: 1.2)
                                )
                        }
                        .disabled(isAdvancing)

                        FeedbackBannerView(state: feedback)
                            .frame(minHeight: 56, alignment: .top)
                            .padding(.horizontal, 16)
                    }

                    Spacer()

                    MathKeypadView(
                        onDigit: onDigit,
                        onBackspace: onBackspace,
                        onSubmit: onSubmit,
                        submitEnabled: !inputText.isEmpty && !isAdvancing
                    )
                    .padding(.horizontal, 16)
                    .padding(.bottom, 12)
                }
            }
        }
        .navigationTitle(store.tr("topic_multiplication"))
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            if problems.isEmpty { startNewSession() }
        }
    }

    private func startNewSession() {
        let tables = Set(store.multiplicationTables.map { KotlinInt(int: Int32($0)) })
        let config = MultiplicationConfig(selectedTables: tables, includeDivision: store.multiplicationIncludeDivision)
        problems = generator.generateSet(config: config, count: 10)
        currentIndex = 0
        inputText = ""
        correctFirstTry = 0
        hadMistakeOnCurrent = false
        feedback = .none
        isAdvancing = false
        isFinished = false
    }

    private func onDigit(_ digit: Int) {
        guard !isAdvancing, !isFinished, inputText.count < 4 else { return }
        let next = (inputText + "\(digit)").drop(while: { $0 == "0" })
        inputText = next.isEmpty ? "0" : String(next)
        feedback = .none
    }

    private func onBackspace() {
        guard !isAdvancing, !isFinished, !inputText.isEmpty else { return }
        inputText.removeLast()
        feedback = .none
    }

    private func showHint() {
        guard problems.indices.contains(currentIndex) else { return }
        let p = problems[currentIndex]
        let text: String
        if p.operation == .multiply {
            text = store.fmt("mult_hint_multiply", Int(p.left), Int(p.right), Int(p.right), Int(p.left))
        } else {
            text = store.fmt("mult_hint_divide", Int(p.right), Int(p.left), Int(p.right), Int(p.left))
        }
        feedback = .hint(text)
    }

    private func onSubmit() {
        guard !isAdvancing, !isFinished, problems.indices.contains(currentIndex) else { return }
        guard let given = Int32(inputText) else { return }
        let current = problems[currentIndex]

        if given == current.answer {
            if !hadMistakeOnCurrent { correctFirstTry += 1 }
            feedback = .correct(store.tr("feedback_correct"))
            isAdvancing = true
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.65) {
                advanceToNext()
            }
        } else {
            if !hadMistakeOnCurrent {
                hadMistakeOnCurrent = true
                store.recordMistake(
                    topic: .multiplication,
                    displayText: current.displayText,
                    expectedAnswer: Int(current.answer)
                )
            }
            inputText = ""
            feedback = .wrong(store.tr("mult_wrong"))
        }
    }

    private func advanceToNext() {
        if currentIndex + 1 >= problems.count {
            store.recordSession(
                topic: .multiplication,
                totalProblems: problems.count,
                correctFirstTry: correctFirstTry
            )
            isAdvancing = false
            isFinished = true
        } else {
            currentIndex += 1
            hadMistakeOnCurrent = false
            inputText = ""
            feedback = .none
            isAdvancing = false
        }
    }
}
