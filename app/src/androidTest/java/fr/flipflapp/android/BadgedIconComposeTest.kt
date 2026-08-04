package fr.flipflapp.android

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.flipflapp.android.core.designsystem.components.FfBadgedIcon
import fr.flipflapp.android.core.designsystem.theme.FlipflappTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BadgedIconComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsUnreadCountBadge() {
        composeRule.setContent {
            FlipflappTheme {
                FfBadgedIcon(count = 4) {
                    Text("Notifications")
                }
            }
        }
        composeRule.onNodeWithText("4").assertExists()
        composeRule.onNodeWithText("Notifications").assertExists()
    }
}
