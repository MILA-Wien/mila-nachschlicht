package wien.mila.nachschlichten

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import wien.mila.nachschlichten.ui.common.BarcodeInputHandler
import wien.mila.nachschlichten.ui.theme.MILANachschlichtenTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var barcodeInputHandler: BarcodeInputHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MILANachschlichtenTheme {
                NachschlichtenApp()
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (barcodeInputHandler.handleKeyEvent(event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}
