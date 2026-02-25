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

    private val buffer = StringBuilder()

    fun handleKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false

        return when (event.keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                if (buffer.isNotEmpty()) {
                    val barcode = buffer.toString()
                    buffer.clear()
                    _barcodeFlow.tryEmit(barcode)
                }
                true
            }
            else -> {
                val char = event.unicodeChar.toChar()
                if (char.isDigit() || char.isLetter()) {
                    buffer.append(char)
                    true
                } else {
                    false
                }
            }
        }
    }
}
