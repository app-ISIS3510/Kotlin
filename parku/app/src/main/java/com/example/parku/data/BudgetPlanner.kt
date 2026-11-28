package com.example.parku.data

import java.util.Locale

/** Una estadia planeada no pasa de un dia, igual que el horario de los parqueaderos. */
const val BUDGET_MAX_MINUTES = 24 * 60

/** Tope del presupuesto para que el calculo no se desborde con entradas absurdas. */
const val BUDGET_MAX_AMOUNT = 10_000_000L

/**
 * Resultado del Parking Budget Planner. El total se calcula igual que el resumen de
 * la sesion (minutos x precio por minuto), para que lo planeado y lo cobrado coincidan.
 */
data class BudgetEstimate(
    val minutes: Int,
    val pricePerMinute: Double,
    val total: Double,
    val budget: Long?,
) {
    /** null mientras la persona no haya escrito un presupuesto. */
    val fitsBudget: Boolean? get() = budget?.let { total <= it }

    /** Lo que sobra (positivo) o lo que falta (negativo) frente al presupuesto. */
    val difference: Double? get() = budget?.let { it - total }
}

/**
 * Devuelve null si la duracion no es valida: sin minutos no hay nada que estimar.
 * El presupuesto es opcional; sin el solo se muestra el total.
 */
fun estimateBudget(pricePerMinute: Double, minutes: Int, budget: Long?): BudgetEstimate? {
    if (minutes <= 0 || minutes > BUDGET_MAX_MINUTES || pricePerMinute < 0) return null
    return BudgetEstimate(
        minutes = minutes,
        pricePerMinute = pricePerMinute,
        total = minutes * pricePerMinute,
        budget = budget?.takeIf { it > 0 },
    )
}

/** Mismo formato que SessionSummary.totalLabel: "COP 12,000". */
fun formatCop(value: Double): String = "COP " + String.format(Locale.US, "%,.0f", value)

/** "2h 30min", como SessionSummary.durationLabel. */
fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours > 0 && rest > 0 -> "${hours}h ${rest}min"
        hours > 0 -> "${hours}h"
        else -> "${rest}min"
    }
}

/** Solo digitos y con un largo maximo, para que el teclado no meta signos ni espacios. */
fun digitsOnly(value: String, maxLength: Int): String =
    value.filter { it.isDigit() }.take(maxLength)
