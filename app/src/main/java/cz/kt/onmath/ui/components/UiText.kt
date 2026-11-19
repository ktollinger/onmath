package cz.kt.onmath.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class Plain(val value: String) : UiText

    data class Resource(
        @param:StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText {
        constructor(@StringRes resId: Int, vararg args: Any) : this(resId, args.toList())
    }
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Plain -> value
    is UiText.Resource -> {
        if (args.isEmpty()) {
            stringResource(resId)
        } else {
            val resolvedArgs = args.map { arg ->
                if (arg is UiText) arg.asString() else arg
            }.toTypedArray()
            stringResource(resId, *resolvedArgs)
        }
    }
}
