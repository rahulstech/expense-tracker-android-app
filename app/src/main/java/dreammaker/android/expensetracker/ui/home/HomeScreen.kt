package dreammaker.android.expensetracker.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.core.ui.component.ScreenScaffold
import dreammaker.android.expensetracker.core.ui.component.TopBarTitle
import dreammaker.android.expensetracker.core.ui.helper.PreviewScreen
import dreammaker.android.expensetracker.core.ui.helper.shimmer
import dreammaker.android.expensetracker.ui.navigation.AppDestination
import dreammaker.android.expensetracker.ui.navigation.NavigationEvent
import dreammaker.android.expensetracker.util.toCurrencyString
import kotlinx.coroutines.delay
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group


@Composable
fun HomeRoute(
    onNavigation: (NavigationEvent) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onNavigateTo = { onNavigation(NavigationEvent.To(it)) },
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNavigateTo: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenScaffold(
        modifier = modifier,
        title = {
            TopBarTitle(stringResource(R.string.app_name))
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateTo(AppDestination.AddHistory) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(text = stringResource(R.string.label_add_history)) },
                shape = MaterialTheme.shapes.extraLarge
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SectionTotalBalance(
                isLoading = uiState.isTotalAccountBalanceLoading,
                totalBalance = uiState.totalAccountBalance.toCurrencyString()
            )

            SectionRecentAccounts(
                isLoading = uiState.isAccountsLoading,
                accounts = uiState.accounts,
                onViewAll = { onNavigateTo(AppDestination.AccountList) },
                onAccountClick = { onNavigateTo(AppDestination.ViewAccount(it.id)) },
                onAddNewAccount = { onNavigateTo(AppDestination.AddAccount) }
            )

            SectionRecentGroups(
                isLoading = uiState.isGroupsLoading,
                groups = uiState.groups,
                onViewAll = { onNavigateTo(AppDestination.GroupList) },
                onGroupClick = { onNavigateTo(AppDestination.ViewGroup(it.id)) },
                onAddNewGroup = { onNavigateTo(AppDestination.AddGroup) }
            )

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun SectionTotalBalance(
    isLoading: Boolean,
    totalBalance: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(24.dp)
                        .shimmer()
                )
            } else {
                Text(
                    text = totalBalance,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("tag_totalBalance_content")
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
    enableViewAll: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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

        TextButton(
            onClick = onViewAll,
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier.height(28.dp),
            enabled = enableViewAll
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
    isLoading: Boolean,
    accounts: List<Account>,
    onViewAll: () -> Unit,
    onAccountClick: (Account) -> Unit,
    onAddNewAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(
            title = stringResource(R.string.label_accounts),
            onViewAll = onViewAll,
            enableViewAll = !isLoading
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 144.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                RecentItemsLoadingRow()
            } else if (accounts.isEmpty()) {
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
                        modifier = Modifier
                            .height(32.dp)
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
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tag_recentAccounts_list"),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.Start)
                ) {
                    items(
                        items = accounts,
                        key = { it.id }
                    ) { account ->
                        RecentItemComponent(
                            icon = painterResource(R.drawable.ic_account_black),
                            title = account.name,
                            subtitle = account.balance.toCurrencyString(),
                            onClick = { onAccountClick(account) },
                            modifier = Modifier.testTag("account_${account.id}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionRecentGroups(
    isLoading: Boolean,
    groups: List<Group>,
    onViewAll: () -> Unit,
    onGroupClick: (Group) -> Unit,
    onAddNewGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(
            title = stringResource(R.string.label_groups),
            onViewAll = onViewAll,
            enableViewAll = !isLoading
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 144.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                RecentItemsLoadingRow()
            } else if (groups.isEmpty()) {
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
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("tag_createGroup_button"),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tag_recentGroups_list"),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.Start)
                ) {
                    items(
                        items = groups,
                        key = { it.id }
                    ) { group ->
                        RecentItemComponent(
                            icon = painterResource(R.drawable.ic_group_64),
                            title = group.name,
                            subtitle = group.balance.toCurrencyString(),
                            onClick = { onGroupClick(group) },
                            modifier = Modifier.testTag("group_${group.id}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentItemsLoadingRow(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.Start)
    ) {
        repeat(3) {
            RecentItemLoadingComponent()
        }
    }
}

@Composable
fun RecentItemComponent(
    icon: Painter,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(90.dp)
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(percent = 50))
                    .background(color = MaterialTheme.colorScheme.secondary)
                    .padding(6.dp)
            ) {
                Image(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun RecentItemLoadingComponent(
    modifier: Modifier = Modifier
) {
    val containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
    val contentColor = MaterialTheme.colorScheme.onSecondaryContainer

    Column(
        modifier = modifier
            .width(90.dp)
            .height(115.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(color = containerColor)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(percent = 50))
                .size(20.dp)
                .shimmer(contentColor)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .height(8.dp)
                .width(72.dp)
                .shimmer(contentColor)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .height(8.dp)
                .width(58.dp)
                .shimmer(contentColor)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .height(8.dp)
                .width(64.dp)
                .shimmer(contentColor)
        )
    }
}


@PreviewScreen
@Composable
fun HomeScreenPreview() {

    var uiState by remember {
        mutableStateOf(
            HomeUiState(
                isTotalAccountBalanceLoading = true,
                isAccountsLoading = true,
                isGroupsLoading = true
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(2500)
        uiState = uiState.copy(
            isAccountsLoading = false,
            accounts = listOf(
                Account(id = 1, name = "Savings Bank Account", balance = 5000.0),
                Account(id = 2, name = "Checking", balance = 1200.0)
            )
        )

        delay(1500)
        uiState = uiState.copy(
            isTotalAccountBalanceLoading = false,
            totalAccountBalance = 350005.00
        )

        delay(1500)
        uiState = uiState.copy(
            isGroupsLoading = false,
            groups = listOf(
                Group(id = 1, name = "Household", balance = 450.0),
                Group(id = 2, name = "Medical", balance = 530.0),
                Group(id = 3, name = "Grocery", balance = 1500.0)
            )
        )
    }

    ExpenseTrackerTheme {
        HomeScreen(
            uiState = uiState,
            onNavigateTo = {}
        )
    }
}
