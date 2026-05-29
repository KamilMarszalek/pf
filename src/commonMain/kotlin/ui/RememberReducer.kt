package ui

import androidx.compose.runtime.*

data class ReducerStore<S, A>(
    val state: S,
    val dispatch: (A) -> Unit,
)

@Composable
fun <S, A> rememberReducer(
    initialState: S,
    reducer: (S, A) -> S,
): ReducerStore<S, A> {
    val stateHolder = remember { mutableStateOf(initialState) }
    val currentReducer: State<(S, A) -> S> = rememberUpdatedState(reducer)

    val dispatch: (A) -> Unit = remember {
        { action: A ->
            stateHolder.value = currentReducer.value(stateHolder.value, action)
        }
    }

    return ReducerStore(
        state = stateHolder.value,
        dispatch = dispatch,
    )
}