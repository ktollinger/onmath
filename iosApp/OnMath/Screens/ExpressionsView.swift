import SwiftUI
import Shared

struct ExpressionsView: View {
    @EnvironmentObject private var store: AppStore
    @Environment(\.dismiss) private var dismiss

    @State private var problems: [ExpressionProblem] = []
    @State private var currentIndex = 0
    @State private var intermediates: [String: Int] = [:]
    @State private var intermediateOrder: [String] = []
    @State private var selectedNodeId: String? = nil
    @State private var selectedSubExpression = ""
    @State private var intermediateInputText = ""
    @State private var inputText = ""
    @State private var correctFirstTry = 0
    @State private var hadMistakeOnCurrent = false
    @State private var feedback: FeedbackState = .none
    @State private var isSolved = false
    @State private var isFinished = false

    private let generator = ExpressionGenerator()

    private var activeInputText: String {
        selectedNodeId != nil ? intermediateInputText : inputText
    }

    private var compactMode: Bool {
        UIScreen.main.bounds.height < 700
    }

    var body: some View {
        Group {
            if isFinished {
                SessionSummaryView(
                    topicTitle: store.tr("topic_expressions"),
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

                    VStack(spacing: 0) {
                        // Tip line (fixed height)
                        Group {
                            if intermediates.isEmpty && selectedNodeId == nil && !isSolved {
                                Text(store.tr("expr_tip_tap_operator"))
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .frame(height: 20)
                        .padding(.top, 6)

                        // Paper-style expression tree (fixed height)
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(alignment: .top, spacing: 4) {
                                if problems.indices.contains(currentIndex) {
                                    ExpressionTreeNodeView(
                                        node: problems[currentIndex].root,
                                        nodeId: "root",
                                        wrapWithParens: false,
                                        intermediates: intermediates,
                                        selectedNodeId: selectedNodeId,
                                        enabled: !isSolved,
                                        onSelectOperator: onSelectOperator,
                                        onSelectSubstituted: onSelectSubstituted
                                    )

                                    Button {
                                        if selectedNodeId != nil && !isSolved {
                                            onFocusFinalAnswer()
                                        }
                                    } label: {
                                        Text(isSolved ? "= \(inputText)" : "= ?")
                                            .font(.system(size: 26, weight: .bold))
                                            .foregroundColor(isSolved ? AppThemeColors.primaryBlue : .secondary)
                                            .frame(height: 40)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            .padding(.horizontal, 12)
                        }
                        .frame(height: compactMode ? 112 : 128, alignment: .top)
                        .padding(.top, 6)

                        // Label above input box (fixed height)
                        Text(
                            selectedNodeId != nil
                                ? store.fmt("expr_label_intermediate", selectedSubExpression)
                                : store.tr("expr_label_final")
                        )
                        .font(.subheadline.weight(.semibold))
                        .foregroundColor(selectedNodeId != nil ? AppThemeColors.primaryBlue : .secondary)
                        .frame(height: 22)

                        // Input box
                        Button {
                            if selectedNodeId != nil && !isSolved {
                                onFocusFinalAnswer()
                            }
                        } label: {
                            Text(activeInputText.isEmpty ? "?" : activeInputText)
                                .font(.system(size: 30, weight: .bold))
                                .foregroundColor(activeInputText.isEmpty ? Color.gray.opacity(0.4) : .primary)
                                .frame(maxWidth: 220)
                                .frame(height: compactMode ? 52 : 60)
                                .background(
                                    selectedNodeId != nil
                                        ? AppThemeColors.hintAmber.opacity(0.18)
                                        : AppThemeColors.surfaceLight
                                )
                                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                                        .stroke(
                                            selectedNodeId != nil ? AppThemeColors.hintAmber : AppThemeColors.primaryBlue,
                                            lineWidth: 2
                                        )
                                )
                        }
                        .buttonStyle(.plain)
                        .padding(.top, 4)

                        // Cancel intermediate slot (fixed height)
                        Group {
                            if selectedNodeId != nil && !isSolved {
                                Button(action: onCancelIntermediate) {
                                    Label(store.tr("expr_back_to_final"), systemImage: "xmark")
                                        .font(.subheadline)
                                }
                            }
                        }
                        .frame(height: 32)

                        // Action buttons (fixed height)
                        HStack(spacing: 10) {
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
                                    Label(store.tr("action_hint"), systemImage: "lightbulb")
                                        .font(.subheadline.weight(.semibold))
                                        .padding(.horizontal, 14)
                                        .padding(.vertical, 8)
                                        .overlay(Capsule().stroke(AppThemeColors.primaryBlue, lineWidth: 1.2))
                                }

                                if !intermediateOrder.isEmpty {
                                    Button(action: onUndoIntermediate) {
                                        Label(store.tr("action_undo_step"), systemImage: "arrow.uturn.backward")
                                            .font(.subheadline.weight(.semibold))
                                            .padding(.horizontal, 14)
                                            .padding(.vertical, 8)
                                            .overlay(Capsule().stroke(AppThemeColors.primaryBlue, lineWidth: 1.2))
                                    }
                                }
                            }
                        }
                        .frame(height: 42)
                        .padding(.top, 2)

                        // Feedback slot (fixed minHeight)
                        FeedbackBannerView(state: feedback)
                            .frame(minHeight: 62, alignment: .top)
                            .padding(.horizontal, 16)
                            .padding(.top, 6)
                    }

                    Spacer(minLength: 4)

                    MathKeypadView(
                        onDigit: onDigit,
                        onBackspace: onBackspace,
                        onSubmit: onSubmit,
                        submitEnabled: !activeInputText.isEmpty && !isSolved
                    )
                    .padding(.horizontal, 16)
                    .padding(.bottom, 10)
                }
            }
        }
        .navigationTitle(store.tr("topic_expressions"))
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            if problems.isEmpty { startNewSession() }
        }
    }

    private func startNewSession() {
        let difficulty: ExpressionDifficulty = store.expressionWithParentheses ? .withParentheses : .withoutParentheses
        problems = generator.generateSet(
            difficulty: difficulty,
            count: 10,
            allowLargeIntermediate: store.expressionLargeIntermediate
        )
        currentIndex = 0
        intermediates = [:]
        intermediateOrder = []
        selectedNodeId = nil
        selectedSubExpression = ""
        intermediateInputText = ""
        inputText = ""
        correctFirstTry = 0
        hadMistakeOnCurrent = false
        feedback = .none
        isSolved = false
        isFinished = false
    }

    private func kotlinIntermediatesMap(_ map: [String: Int]) -> [String: KotlinInt] {
        map.mapValues { KotlinInt(int: Int32($0)) }
    }

    private func onSelectOperator(_ nodeId: String) {
        guard !isSolved, !isFinished, problems.indices.contains(currentIndex) else { return }
        let current = problems[currentIndex]

        if selectedNodeId == nodeId {
            if intermediateInputText.isEmpty { onCancelIntermediate() }
            return
        }

        if !tryAutoCommitPendingIntermediate(current: current) {
            return
        }

        let kMap = kotlinIntermediatesMap(intermediates)
        let tokens = ExpressionInteractiveKt.tokenize(current.root, intermediates: kMap, selectedNodeId: nil)
        guard let opToken = tokens.compactMap({ $0 as? ExpressionTokenOperator }).first(where: { $0.nodeId == nodeId }) else {
            return
        }

        if !opToken.isReady {
            selectedNodeId = nil
            selectedSubExpression = ""
            intermediateInputText = ""
            feedback = .hint(store.tr("expr_need_inner_first"))
            return
        }

        selectedNodeId = nodeId
        selectedSubExpression = opToken.subExpression
        intermediateInputText = ""
        feedback = .none
    }

    private func onSelectSubstituted(_ nodeId: String) {
        guard !isSolved, !isFinished, problems.indices.contains(currentIndex) else { return }
        let current = problems[currentIndex]

        let toRemove = Set(intermediates.keys.filter { nodeId == $0 || nodeId.hasPrefix("\($0).") })
        intermediates = intermediates.filter { !toRemove.contains($0.key) }
        intermediateOrder.removeAll { toRemove.contains($0) }

        let kMap = kotlinIntermediatesMap(intermediates)
        let tokens = ExpressionInteractiveKt.tokenize(current.root, intermediates: kMap, selectedNodeId: nodeId)
        let subExpr = tokens.compactMap({ $0 as? ExpressionTokenOperator }).first(where: { $0.nodeId == nodeId })?.subExpression ?? ""

        selectedNodeId = nodeId
        selectedSubExpression = subExpr
        intermediateInputText = ""
        feedback = .none
    }

    private func onFocusFinalAnswer() {
        guard !isSolved, !isFinished, selectedNodeId != nil, problems.indices.contains(currentIndex) else { return }
        let current = problems[currentIndex]
        if !tryAutoCommitPendingIntermediate(current: current) {
            return
        }
        selectedNodeId = nil
        selectedSubExpression = ""
        intermediateInputText = ""
        feedback = .none
    }

    private func tryAutoCommitPendingIntermediate(current: ExpressionProblem) -> Bool {
        guard let pendingId = selectedNodeId, !intermediateInputText.isEmpty else { return true }
        guard let prevNode = ExpressionInteractiveKt.findBinaryNode(current.root, targetId: pendingId, currentId: "root"),
              let given = Int(intermediateInputText) else { return true }

        if given != Int(prevNode.evaluate()) {
            intermediateInputText = ""
            feedback = .wrong(store.fmt("expr_wrong_intermediate", selectedSubExpression))
            return false
        }

        intermediates[pendingId] = given
        intermediateOrder.removeAll { $0 == pendingId }
        intermediateOrder.append(pendingId)
        return true
    }

    private func onCancelIntermediate() {
        guard !isSolved, !isFinished else { return }
        selectedNodeId = nil
        selectedSubExpression = ""
        intermediateInputText = ""
        feedback = .none
    }

    private func onUndoIntermediate() {
        guard !isSolved, !isFinished, let lastId = intermediateOrder.last else { return }
        let toRemove = Set(intermediates.keys.filter { lastId == $0 || lastId.hasPrefix("\($0).") })
        intermediates = intermediates.filter { !toRemove.contains($0.key) }
        intermediateOrder.removeAll { toRemove.contains($0) }
        selectedNodeId = nil
        selectedSubExpression = ""
        intermediateInputText = ""
        feedback = .none
    }

    private func onDigit(_ digit: Int) {
        guard !isSolved, !isFinished else { return }
        if selectedNodeId != nil {
            guard intermediateInputText.count < 4 else { return }
            let next = (intermediateInputText + "\(digit)").drop(while: { $0 == "0" })
            intermediateInputText = next.isEmpty ? "0" : String(next)
        } else {
            guard inputText.count < 4 else { return }
            let next = (inputText + "\(digit)").drop(while: { $0 == "0" })
            inputText = next.isEmpty ? "0" : String(next)
        }
        feedback = .none
    }

    private func onBackspace() {
        guard !isSolved, !isFinished else { return }
        if selectedNodeId != nil {
            if !intermediateInputText.isEmpty { intermediateInputText.removeLast() }
        } else {
            if !inputText.isEmpty { inputText.removeLast() }
        }
        feedback = .none
    }

    private func showHint() {
        guard problems.indices.contains(currentIndex) else { return }
        let current = problems[currentIndex]
        let reduced = ExpressionInteractiveKt.reduceWith(
            current.root,
            intermediates: kotlinIntermediatesMap(intermediates),
            nodeId: "root"
        )
        let info = ExpressionNodeKt.firstStepHintInfo(reduced)
        let text: String
        switch info {
        case let p as ExpressionStepHintParentheses:
            text = store.fmt("expr_hint_parens", p.equation)
        case let p as ExpressionStepHintPrecedence:
            text = store.fmt("expr_hint_precedence", p.equation)
        case let p as ExpressionStepHintLeftToRight:
            text = store.fmt("expr_hint_left_to_right", p.equation)
        default:
            text = store.tr("expr_hint_solve")
        }
        feedback = .hint(text)
    }

    private func onSubmit() {
        guard !isSolved, !isFinished, problems.indices.contains(currentIndex) else { return }
        let current = problems[currentIndex]

        if let selectedId = selectedNodeId {
            guard let given = Int(intermediateInputText),
                  let targetNode = ExpressionInteractiveKt.findBinaryNode(current.root, targetId: selectedId, currentId: "root") else {
                return
            }
            if given == Int(targetNode.evaluate()) {
                intermediates[selectedId] = given
                intermediateOrder.removeAll { $0 == selectedId }
                intermediateOrder.append(selectedId)
                if selectedId == "root" {
                    inputText = "\(given)"
                }
                selectedNodeId = nil
                selectedSubExpression = ""
                intermediateInputText = ""
                feedback = .none
            } else {
                intermediateInputText = ""
                feedback = .wrong(store.fmt("expr_wrong_intermediate", selectedSubExpression))
            }
            return
        }

        guard let given = Int32(inputText) else { return }
        if given == current.answer {
            if !hadMistakeOnCurrent { correctFirstTry += 1 }
            feedback = .correct(store.tr("feedback_correct_inspect"))
            isSolved = true
        } else {
            if !hadMistakeOnCurrent {
                hadMistakeOnCurrent = true
                store.recordMistake(
                    topic: .expressions,
                    displayText: current.displayText,
                    expectedAnswer: Int(current.answer)
                )
            }
            inputText = ""
            feedback = .wrong(store.tr("expr_wrong_order"))
        }
    }

    private func onNextProblem() {
        guard isSolved, !isFinished else { return }
        if currentIndex + 1 >= problems.count {
            store.recordSession(
                topic: .expressions,
                totalProblems: problems.count,
                correctFirstTry: correctFirstTry
            )
            isSolved = false
            isFinished = true
        } else {
            currentIndex += 1
            intermediates = [:]
            intermediateOrder = []
            selectedNodeId = nil
            selectedSubExpression = ""
            intermediateInputText = ""
            inputText = ""
            hadMistakeOnCurrent = false
            feedback = .none
            isSolved = false
        }
    }
}

private struct ExpressionTreeNodeView: View {
    let node: ExpressionNode
    let nodeId: String
    let wrapWithParens: Bool
    let intermediates: [String: Int]
    let selectedNodeId: String?
    let enabled: Bool
    let onSelectOperator: (String) -> Void
    let onSelectSubstituted: (String) -> Void

    var body: some View {
        if let num = node as? NumberNode {
            Text("\(num.value)")
                .font(.system(size: 26, weight: .bold))
                .frame(height: 40)
        } else if let bin = node as? BinaryNode {
            let leftId = "\(nodeId).L"
            let rightId = "\(nodeId).R"
            let intermediateVal = intermediates[nodeId]
            let isSelected = selectedNodeId == nodeId
            let isReady = isNodeResolved(bin.left, leftId) && isNodeResolved(bin.right, rightId)
            let showParens = bin.explicitParentheses || wrapWithParens

            VStack(spacing: 3) {
                HStack(alignment: .top, spacing: 3) {
                    if showParens {
                        Text("(")
                            .font(.system(size: 26, weight: .bold))
                            .foregroundColor(.secondary)
                            .frame(height: 40)
                    }

                    ExpressionTreeNodeView(
                        node: bin.left,
                        nodeId: leftId,
                        wrapWithParens: needsImplicitParens(bin, bin.left, isRight: false),
                        intermediates: intermediates,
                        selectedNodeId: selectedNodeId,
                        enabled: enabled,
                        onSelectOperator: onSelectOperator,
                        onSelectSubstituted: onSelectSubstituted
                    )

                    OperatorChipView(
                        symbol: bin.operator_.symbol,
                        isReady: isReady,
                        isSelected: isSelected,
                        isSolved: intermediateVal != nil,
                        enabled: enabled
                    ) {
                        if intermediateVal != nil {
                            onSelectSubstituted(nodeId)
                        } else {
                            onSelectOperator(nodeId)
                        }
                    }

                    ExpressionTreeNodeView(
                        node: bin.right,
                        nodeId: rightId,
                        wrapWithParens: needsImplicitParens(bin, bin.right, isRight: true),
                        intermediates: intermediates,
                        selectedNodeId: selectedNodeId,
                        enabled: enabled,
                        onSelectOperator: onSelectOperator,
                        onSelectSubstituted: onSelectSubstituted
                    )

                    if showParens {
                        Text(")")
                            .font(.system(size: 26, weight: .bold))
                            .foregroundColor(.secondary)
                            .frame(height: 40)
                    }
                }
                .padding(.horizontal, isSelected ? 4 : 0)
                .background(
                    isSelected
                        ? AppThemeColors.primaryContainerBlue.opacity(0.4)
                        : Color.clear
                )
                .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))

                if let val = intermediateVal {
                    Rectangle()
                        .fill(AppThemeColors.primaryBlue.opacity(0.45))
                        .frame(height: 2)
                        .padding(.horizontal, 4)

                    Button {
                        if enabled { onSelectSubstituted(nodeId) }
                    } label: {
                        Text("\(val)")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(AppThemeColors.onPrimaryContainerBlue)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 2)
                            .background(AppThemeColors.primaryContainerBlue)
                            .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                            .overlay(
                                RoundedRectangle(cornerRadius: 8, style: .continuous)
                                    .stroke(AppThemeColors.primaryBlue, lineWidth: 1.5)
                            )
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }

    private func isNodeResolved(_ child: ExpressionNode, _ id: String) -> Bool {
        child is NumberNode || intermediates[id] != nil
    }

    private func needsImplicitParens(_ parent: BinaryNode, _ child: ExpressionNode, isRight: Bool) -> Bool {
        guard let c = child as? BinaryNode, !c.explicitParentheses else { return false }
        if c.operator_.precedence < parent.operator_.precedence { return true }
        if isRight && c.operator_.precedence == parent.operator_.precedence &&
            (parent.operator_ == .subtract || parent.operator_ == .divide) {
            return true
        }
        return false
    }
}

private struct OperatorChipView: View {
    let symbol: String
    let isReady: Bool
    let isSelected: Bool
    let isSolved: Bool
    let enabled: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(symbol)
                .font(.system(size: isSelected || (isReady && !isSolved) ? 24 : 26, weight: .bold))
                .foregroundColor(
                    isSelected ? .white : (isReady || isSolved ? AppThemeColors.primaryBlue : .primary)
                )
                .frame(minWidth: isSelected || (isReady && !isSolved) ? 34 : 22, minHeight: 40)
                .padding(.horizontal, isSelected || (isReady && !isSolved) ? 4 : 1)
                .background(
                    isSelected
                        ? AppThemeColors.primaryBlue
                        : (isReady && !isSolved ? Color(.systemGray6) : Color.clear)
                )
                .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 8, style: .continuous)
                        .stroke(
                            isReady && !isSolved && !isSelected ? Color.gray.opacity(0.5) : Color.clear,
                            lineWidth: 1.5
                        )
                )
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
    }
}
