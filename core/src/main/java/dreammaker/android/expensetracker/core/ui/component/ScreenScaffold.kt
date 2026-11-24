package dreammaker.android.expensetracker.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    title: @Composable ()-> Unit = {},
    actions: @Composable RowScope.()-> Unit = {},
    navigationIcon: @Composable ()-> Unit = {},
    floatingActionButton: @Composable ()-> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    content: @Composable ()-> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = title,
                actions = actions,
                navigationIcon = navigationIcon,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun NavigationUpIcon(
    contentDescription: String = "navigation up",
    onClick: ()-> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = contentDescription
        )
    }
}

@Composable
fun TopBarTitle(title: String) {
    Text(
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun TopBarIconAction(
    icon: Painter,
    title: String = "",
    enabled: Boolean = true,
    onClick: ()-> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = title)
    }
}

@Composable
fun TopBarIconAction(
    icon: ImageVector,
    title: String = "",
    enabled: Boolean = true,
    onClick: ()-> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Icon(icon, contentDescription = title)
    }
}


@PreviewLightDark
@Composable
private fun ScreenScaffoldPreview() {
    ExpenseTrackerTheme {
        ScreenScaffold(
            title = { TopBarTitle("Example Title") },
            actions = {
                TopBarIconAction(Icons.Default.Delete) {}
            },
            navigationIcon = { NavigationUpIcon {} }
        ) {
            Text("This is a simple text as the content of the screen")
        }
    }
}