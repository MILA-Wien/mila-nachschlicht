package wien.mila.nachschlichten.ui.common

import android.view.KeyEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarcodeInputHandler @Inject constructor() {

    private val _barcodeFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val barcodeFlow: SharedFlow<String> = _barcodeFlow.asSharedFlow()

    @Volatile
    var isEnabled: Boolean = true

    private val buffer = StringBuilder()

    fun handleKeyEvent(event: KeyEvent): Boolean {
        if (!isEnabled) return false

        return when (event.keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                if (event.action == KeyEvent.ACTION_DOWN && buffer.isNotEmpty()) {
                    val barcode = buffer.toString()
                    buffer.clear()
                    _barcodeFlow.tryEmit(barcode)
                }
                true // consume both DOWN and UP so ENTER never reaches the view hierarchy
            }
            else -> {
                val unicodeChar = event.unicodeChar
                if (unicodeChar != 0) {
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        buffer.append(unicodeChar.toChar())
                    }
                    true // consume both DOWN and UP
                } else {
                    false
                }
            }
        }
    }
}
