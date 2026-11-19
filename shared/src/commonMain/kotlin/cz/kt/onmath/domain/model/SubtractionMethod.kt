package cz.kt.onmath.domain.model

/**
 * How borrowing/carrying is visualized and hinted in column subtraction.
 */
enum class SubtractionMethod {
    /**
     * Montessori / Anglo-Saxon method: cross out and decrease the higher upper digit by 1.
     */
    BORROW_TOP,

    /**
     * Traditional Czech school method: add 10 to the upper digit and carry 1 down to the subtrahend.
     */
    CARRY_BOTTOM,
}
