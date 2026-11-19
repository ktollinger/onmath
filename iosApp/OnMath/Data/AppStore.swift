import Foundation
import SwiftUI
import Shared

enum AppThemeColors {
    static let primaryBlue = Color(red: 0x1E / 255.0, green: 0x6B / 255.0, blue: 0xB8 / 255.0)
    static let primaryContainerBlue = Color(red: 0xD6 / 255.0, green: 0xE7 / 255.0, blue: 0xFF / 255.0)
    static let onPrimaryContainerBlue = Color(red: 0x00 / 255.0, green: 0x1C / 255.0, blue: 0x3B / 255.0)
    static let secondaryTeal = Color(red: 0x00 / 255.0, green: 0x6A / 255.0, blue: 0x60 / 255.0)
    static let correctGreen = Color(red: 0x1B / 255.0, green: 0x87 / 255.0, blue: 0x3F / 255.0)
    static let correctContainerGreen = Color(red: 0xD7 / 255.0, green: 0xF5 / 255.0, blue: 0xDF / 255.0)
    static let errorRed = Color(red: 0xC6 / 255.0, green: 0x28 / 255.0, blue: 0x28 / 255.0)
    static let errorContainerRed = Color(red: 0xFF / 255.0, green: 0xDA / 255.0, blue: 0xD6 / 255.0)
    static let hintAmber = Color(red: 0xB2 / 255.0, green: 0x6A / 255.0, blue: 0x00 / 255.0)
    static let hintContainerAmber = Color(red: 0xFF / 255.0, green: 0xF0 / 255.0, blue: 0xD4 / 255.0)
    static let surfaceLight = Color(.secondarySystemBackground)
}

enum L10n {
    static func tr(_ key: String, language: AppLanguage) -> String {
        let bundle: Bundle
        if let tag = language.languageTag,
           let path = Bundle.main.path(forResource: tag, ofType: "lproj"),
           let langBundle = Bundle(path: path) {
            bundle = langBundle
        } else {
            bundle = .main
        }
        return bundle.localizedString(forKey: key, value: nil, table: nil)
    }

    static func fmt(_ key: String, language: AppLanguage, _ args: CVarArg...) -> String {
        String(format: tr(key, language: language), arguments: args)
    }
}

struct StoredSession: Codable {
    let topic: String
    let totalProblems: Int
    let correctFirstTry: Int
}

struct StoredMistake: Codable, Identifiable {
    let id: UUID
    let topic: String
    let displayText: String
    let expectedAnswer: Int
    let createdAt: Date
}

struct TopicStatsSummary {
    let sessionCount: Int
    let accuracyPercent: Int
}

@MainActor
final class AppStore: ObservableObject {
    @Published var language: AppLanguage {
        didSet { UserDefaults.standard.set(language.name, forKey: Keys.language) }
    }
    @Published var subtractionMethod: SubtractionMethod {
        didSet { UserDefaults.standard.set(subtractionMethod.name, forKey: Keys.subtractionMethod) }
    }
    @Published var subtractionMinDigits: Int {
        didSet { UserDefaults.standard.set(subtractionMinDigits, forKey: Keys.subtractionMinDigits) }
    }
    @Published var subtractionMaxDigits: Int {
        didSet { UserDefaults.standard.set(subtractionMaxDigits, forKey: Keys.subtractionMaxDigits) }
    }
    @Published var multiplicationTables: Set<Int> {
        didSet {
            UserDefaults.standard.set(Array(multiplicationTables).sorted(), forKey: Keys.multiplicationTables)
        }
    }
    @Published var multiplicationIncludeDivision: Bool {
        didSet { UserDefaults.standard.set(multiplicationIncludeDivision, forKey: Keys.multiplicationIncludeDivision) }
    }
    @Published var expressionWithParentheses: Bool {
        didSet { UserDefaults.standard.set(expressionWithParentheses, forKey: Keys.expressionWithParentheses) }
    }
    @Published var expressionLargeIntermediate: Bool {
        didSet { UserDefaults.standard.set(expressionLargeIntermediate, forKey: Keys.expressionLargeIntermediate) }
    }

    @Published private(set) var sessions: [StoredSession] = []
    @Published private(set) var mistakes: [StoredMistake] = []

    private enum Keys {
        static let language = "app_language"
        static let subtractionMethod = "subtraction_method"
        static let subtractionMinDigits = "subtraction_min_digits"
        static let subtractionMaxDigits = "subtraction_max_digits"
        static let multiplicationTables = "multiplication_tables"
        static let multiplicationIncludeDivision = "multiplication_include_division"
        static let expressionWithParentheses = "expression_with_parentheses"
        static let expressionLargeIntermediate = "expression_large_intermediate"
        static let sessions = "practice_sessions"
        static let mistakes = "practice_mistakes"
    }

    init() {
        let ud = UserDefaults.standard

        let langStr = ud.string(forKey: Keys.language)
        self.language = AppLanguage.entries.first { $0.name == langStr } ?? .system

        let methodStr = ud.string(forKey: Keys.subtractionMethod)
        self.subtractionMethod = SubtractionMethod.entries.first { $0.name == methodStr } ?? .borrowTop

        let savedMin = ud.object(forKey: Keys.subtractionMinDigits) as? Int ?? 2
        let savedMax = ud.object(forKey: Keys.subtractionMaxDigits) as? Int ?? 6
        let clampedMin = max(2, min(6, savedMin))
        self.subtractionMinDigits = clampedMin
        self.subtractionMaxDigits = max(clampedMin, min(6, savedMax))

        if let arr = ud.array(forKey: Keys.multiplicationTables) as? [Int], !arr.isEmpty {
            self.multiplicationTables = Set(arr.filter { (1...10).contains($0) })
        } else {
            self.multiplicationTables = Set(1...10)
        }

        self.multiplicationIncludeDivision = ud.bool(forKey: Keys.multiplicationIncludeDivision)
        self.expressionWithParentheses = ud.object(forKey: Keys.expressionWithParentheses) as? Bool ?? true
        self.expressionLargeIntermediate = ud.bool(forKey: Keys.expressionLargeIntermediate)

        if let data = ud.data(forKey: Keys.sessions),
           let decoded = try? JSONDecoder().decode([StoredSession].self, from: data) {
            self.sessions = decoded
        }
        if let data = ud.data(forKey: Keys.mistakes),
           let decoded = try? JSONDecoder().decode([StoredMistake].self, from: data) {
            self.mistakes = decoded
        }
    }

    func stats(for topic: MathTopic) -> TopicStatsSummary? {
        let filtered = sessions.filter { $0.topic == topic.name }
        guard !filtered.isEmpty else { return nil }
        let total = filtered.reduce(0) { $0 + $1.totalProblems }
        let correct = filtered.reduce(0) { $0 + $1.correctFirstTry }
        let accuracy = total == 0 ? 0 : (correct * 100) / total
        return TopicStatsSummary(sessionCount: filtered.count, accuracyPercent: accuracy)
    }

    var recentMistakes: [StoredMistake] {
        Array(mistakes.prefix(5))
    }

    func recordSession(topic: MathTopic, totalProblems: Int, correctFirstTry: Int) {
        sessions.append(StoredSession(topic: topic.name, totalProblems: totalProblems, correctFirstTry: correctFirstTry))
        if let encoded = try? JSONEncoder().encode(sessions) {
            UserDefaults.standard.set(encoded, forKey: Keys.sessions)
        }
    }

    func recordMistake(topic: MathTopic, displayText: String, expectedAnswer: Int) {
        let item = StoredMistake(
            id: UUID(),
            topic: topic.name,
            displayText: displayText,
            expectedAnswer: expectedAnswer,
            createdAt: Date()
        )
        mistakes.insert(item, at: 0)
        if mistakes.count > 30 {
            mistakes = Array(mistakes.prefix(30))
        }
        if let encoded = try? JSONEncoder().encode(mistakes) {
            UserDefaults.standard.set(encoded, forKey: Keys.mistakes)
        }
    }

    func clearHistory() {
        sessions.removeAll()
        mistakes.removeAll()
        UserDefaults.standard.removeObject(forKey: Keys.sessions)
        UserDefaults.standard.removeObject(forKey: Keys.mistakes)
    }

    func tr(_ key: String) -> String {
        L10n.tr(key, language: language)
    }

    func fmt(_ key: String, _ args: CVarArg...) -> String {
        String(format: L10n.tr(key, language: language), arguments: args)
    }
}
