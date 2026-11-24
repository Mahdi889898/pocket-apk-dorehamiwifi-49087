package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.test.core.app.ApplicationProvider
import com.example.games.GameEngineDispatcher
import com.example.model.GameType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appNameMatchesLocalizedTitle() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("دورهمی وای‌فای", appName)
    }

    @Test
    fun allEightGameEnginesInitializeAndSerializeCleanly() {
        for (game in GameType.entries) {
            val state = GameEngineDispatcher.createInitialState(game, game.minPlayers)
            val json = state.toJson()
            val restored = GameEngineDispatcher.deserialize(game, json)
            assertNotNull("Failed to deserialize ${game.id}", restored)
            assertEquals(game, restored?.gameType)
        }
    }

    @Test
    fun enterAllEightGamesAndWifiTabsWithoutFreezeOrCrash() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.mainClock.advanceTimeBy(200)

        composeTestRule.onNodeWithTag("hero_host_wifi_button").assertIsDisplayed()

        // Enter EVERY SINGLE ONE of the 8 games from HomeScreen, toggle 3D, interact, and return
        for (game in GameType.entries) {
            composeTestRule.onNodeWithTag("home_games_grid").performScrollToKey(game.id)
            composeTestRule.mainClock.advanceTimeBy(120)

            composeTestRule.onNodeWithTag("game_card_${game.id}").performClick()
            composeTestRule.mainClock.advanceTimeBy(200)

            composeTestRule.onNodeWithTag("sheet_play_bot_btn").performClick()
            composeTestRule.mainClock.advanceTimeBy(250)

            // Verify 3D toggle works in every game
            composeTestRule.onNodeWithTag("toggle_3d_view_chip").assertIsDisplayed()
            composeTestRule.onNodeWithTag("toggle_3d_view_chip").performClick()
            composeTestRule.mainClock.advanceTimeBy(250)

            // Roll dice if dice game
            if (composeTestRule.onAllNodesWithTag("dice_roll_button").fetchSemanticsNodes().isNotEmpty()) {
                composeTestRule.onAllNodesWithTag("dice_roll_button")[0].performClick()
                composeTestRule.mainClock.advanceTimeBy(250)
            }

            // Pick Hokm suit if suit picker is shown
            if (composeTestRule.onAllNodesWithTag("pick_suit_0").fetchSemanticsNodes().isNotEmpty()) {
                composeTestRule.onNodeWithTag("pick_suit_0").performClick()
                composeTestRule.mainClock.advanceTimeBy(200)
            }

            // Return back to Home cleanly
            composeTestRule.onNodeWithTag("game_back_button").performClick()
            composeTestRule.mainClock.advanceTimeBy(200)
        }

        // Verify Wi-Fi Hub & Profile tabs render cleanly
        composeTestRule.onNodeWithTag("nav_wifi_tab").performClick()
        composeTestRule.mainClock.advanceTimeBy(250)
        composeTestRule.onNodeWithTag("wifi_create_room_btn").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_profile_tab").performClick()
        composeTestRule.mainClock.advanceTimeBy(250)
        composeTestRule.onNodeWithTag("profile_name_input").assertIsDisplayed()
    }
}
