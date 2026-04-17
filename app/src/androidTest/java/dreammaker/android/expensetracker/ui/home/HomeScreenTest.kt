package dreammaker.android.expensetracker.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import org.junit.Rule
import org.junit.Test
import rahulstech.android.expensetracker.domain.model.Account
import rahulstech.android.expensetracker.domain.model.Group

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun totalBalance() {
        composeTestRule.setContent {
            ExpenseTrackerTheme {
                HomeScreen(
                    state = HomeScreenState(totalBalance = 1000.0),
                    onEvent = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("tag_totalBalance_content")
            .assertIsDisplayed()
            .assertTextEquals("$1,000.00")
    }

    @Test
    fun emptyAccounts() {
        composeTestRule.setContent {
            ExpenseTrackerTheme {
                HomeScreen(
                    state = HomeScreenState(recentAccounts = emptyList()),
                    onEvent = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("tag_createAccount_button")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("tag_noRecentAccounts_label")
            .assertIsDisplayed()
            .assertTextEquals("No Recent Account")
    }

    @Test
    fun nonEmptyAccounts() {
        val accounts = listOf(
            Account(id = 1, name = "Account 1", balance = 100f),
            Account(id = 2, name = "Account 2", balance = 200f)
        )
        composeTestRule.setContent {
            ExpenseTrackerTheme {
                HomeScreen(
                    state = HomeScreenState(recentAccounts = accounts),
                    onEvent = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("tag_recentAccounts_list")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("account_1")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("account_2")
            .assertIsDisplayed()
    }

    @Test
    fun emptyGroups() {
        composeTestRule.setContent {
            ExpenseTrackerTheme {
                HomeScreen(
                    state = HomeScreenState(recentGroups = emptyList()),
                    onEvent = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("tag_createGroup_button")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("tag_noRecentGroups_label")
            .assertIsDisplayed()
            .assertTextEquals("No Recent Group")
    }

    @Test
    fun nonEmptyGroups() {
        val groups = listOf(
            Group(id = 1, name = "Group 1", balance = 100f),
            Group(id = 2, name = "Group 2", balance = 200f)
        )
        composeTestRule.setContent {
            ExpenseTrackerTheme {
                HomeScreen(
                    state = HomeScreenState(recentGroups = groups),
                    onEvent = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("tag_recentGroups_list")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("group_1")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("group_2")
            .assertIsDisplayed()
    }
}
