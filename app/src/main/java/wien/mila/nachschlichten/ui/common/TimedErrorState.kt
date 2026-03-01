package wien.mila.nachschlichten.ui.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimedErrorState(
    private val scope: CoroutineScope,
    private val dismissAfterMs: Long = 3000
) {
    private val _value = MutableStateFlow<String?>(null)
    val value: StateFlow<String?> = _value.asStateFlow()
    private var job: Job? = null

    fun show(message: String) {
        job?.cancel()
        _value.value = message
        job = scope.launch {
            delay(dismissAfterMs)
            _value.value = null
        }
    }

    fun clear() {
        job?.cancel()
        _value.value = null
    }
}
