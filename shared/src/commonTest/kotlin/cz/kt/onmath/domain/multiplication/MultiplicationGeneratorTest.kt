package cz.kt.onmath.domain.multiplication

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.random.Random

class MultiplicationGeneratorTest {

    @Test
    fun generate_respectsSelectedTablesAndNoDivision() {
        val generator = MultiplicationGenerator(Random(42))
        val config = MultiplicationConfig(selectedTables = setOf(6, 7), includeDivision = false)

        repeat(50) {
            val problem = generator.generate(config)
            assertEquals(MultiplicationOperation.MULTIPLY, problem.operation)
            assertTrue(problem.left in setOf(6, 7) || problem.right in setOf(6, 7))
            assertEquals(problem.left * problem.right, problem.answer)
        }
    }

    @Test
    fun generate_withDivision_producesExactIntegerDivision() {
        val generator = MultiplicationGenerator(Random(123))
        val config = MultiplicationConfig(selectedTables = setOf(3, 8), includeDivision = true)

        val problems = List(100) { generator.generate(config) }
        val divisions = problems.filter { it.operation == MultiplicationOperation.DIVIDE }
        assertTrue(divisions.isNotEmpty(), "Expected some division problems")

        divisions.forEach { problem ->
            assertEquals(0, problem.left % problem.right)
            assertEquals(problem.left / problem.right, problem.answer)
            assertTrue(problem.right in setOf(3, 8))
            assertTrue(problem.answer in 1..10)
        }
    }

    @Test
    fun generateSet_returnsRequestedCount() {
        val generator = MultiplicationGenerator(Random(7))
        val set = generator.generateSet(count = 10)
        assertEquals(10, set.size)
    }
}
