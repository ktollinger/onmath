package cz.kt.onmath.domain.multiplication

import kotlin.random.Random

enum class MultiplicationOperation(val symbol: String) {
    MULTIPLY("×"),
    DIVIDE("÷"),
}

data class MultiplicationConfig(
    val selectedTables: Set<Int> = (1..10).toSet(),
    val includeDivision: Boolean = false,
) {
    init {
        require(selectedTables.isNotEmpty()) { "At least one multiplication table must be selected." }
        require(selectedTables.all { it in 1..10 }) { "Tables must be in range 1..10." }
    }
}

data class MultiplicationProblem(
    val left: Int,
    val right: Int,
    val operation: MultiplicationOperation,
    val answer: Int,
) {
    val displayText: String
        get() = "$left ${operation.symbol} $right = ?"

    val key: String
        get() = "$left${operation.symbol}$right"

    val hintText: String
        get() = when (operation) {
            MultiplicationOperation.MULTIPLY ->
                "$left × $right znamená ${left}krát sečíst číslo $right."
            MultiplicationOperation.DIVIDE ->
                "Kolikrát se vejde $right do $left? ($right × ? = $left)"
        }
}

class MultiplicationGenerator(
    private val random: Random = Random.Default,
) {
    constructor() : this(Random.Default)
    fun generate(config: MultiplicationConfig = MultiplicationConfig()): MultiplicationProblem {
        val table = config.selectedTables.random(random)
        val factor = random.nextInt(1, 11)
        val useDivision = config.includeDivision && random.nextBoolean()

        return if (useDivision) {
            val dividend = table * factor
            MultiplicationProblem(
                left = dividend,
                right = table,
                operation = MultiplicationOperation.DIVIDE,
                answer = factor,
            )
        } else {
            val swapOrder = random.nextBoolean()
            val left = if (swapOrder) factor else table
            val right = if (swapOrder) table else factor
            MultiplicationProblem(
                left = left,
                right = right,
                operation = MultiplicationOperation.MULTIPLY,
                answer = left * right,
            )
        }
    }

    fun generateSet(
        config: MultiplicationConfig = MultiplicationConfig(),
        count: Int = 10,
    ): List<MultiplicationProblem> {
        require(count > 0) { "Count must be positive." }
        val result = mutableListOf<MultiplicationProblem>()
        var attempts = 0
        while (result.size < count && attempts < count * 10) {
            attempts++
            val candidate = generate(config)
            if (result.lastOrNull()?.key != candidate.key) {
                result.add(candidate)
            }
        }
        while (result.size < count) {
            result.add(generate(config))
        }
        return result
    }
}
