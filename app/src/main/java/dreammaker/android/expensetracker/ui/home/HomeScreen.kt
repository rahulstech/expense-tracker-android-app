package dreammaker.android.expensetracker.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.core.ui.secondaryButtonColors
import dreammaker.android.expensetracker.util.toCurrencyString
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group

@Composable
fun HomeScreen(
    state: HomeScreenState,
    onEvent: (HomeScreenEvent) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(HomeScreenEvent.AddHistory) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(text = stringResource(R.string.label_add_history)) },
                shape = MaterialTheme.shapes.extraLarge
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SectionTotalBalance(totalBalance = state.totalBalance.toCurrencyString())

            SectionRecentAccounts(
                accounts = state.recentAccounts,
                onViewAll = { onEvent(HomeScreenEvent.ViewAllAccounts) },
                onAccountClick = { onEvent(HomeScreenEvent.ClickAccount(it)) },
                onAddNewAccount = { onEvent(HomeScreenEvent.AddNewAccount) }
            )

            SectionRecentGroups(
                groups = state.recentGroups,
                onViewAll = { onEvent(HomeScreenEvent.ViewAllGroups) },
                onGroupClick = { onEvent(HomeScreenEvent.ClickGroup(it)) },
                onAddNewGroup = { onEvent(HomeScreenEvent.AddNewGroup) }
            )
        }
    }
}

@Composable
fun SectionTotalBalance(totalBalance: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.label_total_balance),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Normal,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = totalBalance,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("tag_totalBalance_content")
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onViewAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Button(
            onClick = onViewAll,
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier.height(28.dp),
            colors = secondaryButtonColors()
        ) {
            Text(
                text = stringResource(R.string.label_view_all),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun SectionRecentAccounts(
    accounts: List<Account>,
    onViewAll: () -> Unit,
    onAccountClick: (Account) -> Unit,
    onAddNewAccount: ()-> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(
            title = stringResource(R.string.label_accounts),
            onViewAll = onViewAll
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp),
            contentAlignment = Alignment.Center
        ) {
            if (accounts.isEmpty()) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.label_empty_recent_account_list),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("tag_noRecentAccounts_label")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        modifier = Modifier.height(32.dp)
                            .testTag("tag_createAccount_button"),
                        onClick = onAddNewAccount,
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text(
                            stringResource(R.string.label_add_account),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            } else {
                LazyRow (
                    modifier = Modifier.fillMaxWidth().testTag("tag_recentAccounts_list"),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.Start)
                ) {

                    items(
                        items = accounts,
                        key = { it.id }
                    ) { account ->
                        RecentItemView(
                            icon = painterResource(R.drawable.ic_account_black),
                            title = account.name,
                            subtitle = account.balance.toCurrencyString(),
                            onClick = { onAccountClick(account) },
                            testTag = "account_${account.id}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionRecentGroups(
    groups: List<Group>,
    onViewAll: () -> Unit,
    onGroupClick: (Group) -> Unit,
    onAddNewGroup: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(
            title = stringResource(R.string.label_groups),
            onViewAll = onViewAll
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp),
            contentAlignment = Alignment.Center
        ) {
            if (groups.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.label_empty_recent_group_list),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("tag_noRecentGroups_label")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        modifier = Modifier.height(32.dp).testTag("tag_createGroup_button"),
                        onClick = onAddNewGroup,
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text(
                            stringResource(R.string.label_add_group),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().testTag("tag_recentGroups_list"),
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp, alignment = Alignment.Start)
                ) {
                    items(
                        items = groups,
                        key = { it.id }
                    ) { group ->
                        RecentItemView(
                            icon = painterResource(R.drawable.ic_group_64),
                            title = group.name,
                            subtitle = group.balance.toCurrencyString(),
                            onClick = { onGroupClick(group) },
                            testTag = "group_${group.id}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentItemView(
    icon: Painter,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String? = null
) {
    val modifier = Modifier
        .width(84.dp)
        .height(120.dp)
        .clickable(onClick = onClick)
        .background(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.20f),
            shape = MaterialTheme.shapes.large
        )
        .padding(horizontal = 4.dp, vertical = 8.dp)
    
    Column(
        modifier = if (testTag != null) modifier.testTag(testTag) else modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(percent = 50))
                .background(color = MaterialTheme.colorScheme.primary)
                .padding(8.dp)
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f).testTag("${testTag ?: "tag_recentItemView"}_title"),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("${testTag ?: "tag_recentItemView"}_subtitle")
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ExpenseTrackerTheme {
        HomeScreen(
            state = HomeScreenState(
                totalBalance = 35369.35,
                recentAccounts = listOf(
                    Account(id = 1, name = "Savings Bank Account", balance = 5000.0),
                    Account(id = 2, name = "Checking", balance = 1200.0)
                ),
                recentGroups = listOf(
                    Group(id = 1, name = "Household", balance = 450.0),
                    Group(id = 2, name = "Medical", balance = 530.0)
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenEmptyPreview() {
    ExpenseTrackerTheme {
        HomeScreen(
            state = HomeScreenState(
                totalBalance = 0.0,
                recentAccounts = emptyList(),
                recentGroups = emptyList()
            ),
            onEvent = {}
        )
    }
}
