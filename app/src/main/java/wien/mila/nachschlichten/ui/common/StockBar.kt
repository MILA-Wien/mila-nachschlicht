package wien.mila.nachschlichten.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import wien.mila.nachschlichten.R

@Composable
fun StockBar(
    totalStock: Double,
    unit: String,
    modifier: Modifier = Modifier
) {
    val maxStock = 20
    val progress = (totalStock.toFloat() / maxStock).coerceIn(0f, 1f)
    val stockDisplay = if (totalStock % 1.0 == 0.0) totalStock.toInt().toString() else totalStock.toString()

    Column(modifier = modifier) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = when {
                    totalStock <= 0 -> MaterialTheme.colorScheme.error
                    else -> Color.Unspecified
                })) {
                    append("$stockDisplay $unit")
                }
                append(" ")
                append(stringResource(R.string.article_check_stock))
            },
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = when {
                totalStock <= 0 -> MaterialTheme.colorScheme.error
                totalStock < 10 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            }
        )
    }
}
