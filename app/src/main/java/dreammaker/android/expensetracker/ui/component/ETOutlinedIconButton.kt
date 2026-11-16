package dreammaker.android.expensetracker.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp


@Composable
fun ETOutlinedIconButton(
    onClick: ()-> Unit,
    text: String,
    icon: Painter,
    enabled: Boolean = true,
) {
    Box(
        modifier = Modifier.clip(MaterialTheme.shapes.medium)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                role = Role.Button
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            ).padding(12.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}