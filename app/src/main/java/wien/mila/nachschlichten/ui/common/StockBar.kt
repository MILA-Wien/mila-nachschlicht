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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import wien.mila.nachschlichten.R

@Composable
fun StockBar(
    totalStock: Int,
    modifier: Modifier = Modifier
) {
    val maxStock = 100
    val progress = (totalStock.toFloat() / maxStock).coerceIn(0f, 1f)

    Column(modifier = modifier) {
        Text(
            text = "${stringResource(R.string.article_check_stock)}: $totalStock",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = when {
                totalStock == 0 -> MaterialTheme.colorScheme.error
                totalStock < 10 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            }
        )
    }
}
