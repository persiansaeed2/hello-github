package com.example.solmaz

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Boots the real [MainActivity] on the JVM (Robolectric) and walks the single screen:
 * today's list → review card → answer. Data comes from the bundled words.json.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], qualifiers = "w411dp-h891dp")
class MainActivityTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `today screen shows the date, the summary and the first lesson`() {
        rule.onNodeWithText("امروز").assertIsDisplayed()
        rule.onNodeWithText("برای امروز").assertIsDisplayed()
        // lesson 1 of the book
        rule.onAllNodesWithText("Schule und Bildung", substring = true)[0].assertIsDisplayed()
        rule.onAllNodesWithText("تازه")[0].assertIsDisplayed()
    }

    @Test
    fun `review flow reveals the meaning and accepts a green answer`() {
        rule.onNodeWithText("شروع مرور", substring = true).performClick()

        rule.onNodeWithText("نمایش معنی").assertIsDisplayed()
        rule.onNodeWithText("نمایش معنی").performClick()

        rule.onNodeWithText("بلدم").assertIsDisplayed()
        rule.onNodeWithText("بلدم").performClick()

        // still inside the session, with the next card ready
        rule.onNodeWithText("نمایش معنی").assertIsDisplayed()
    }

    @Test
    fun `red answer keeps the card flow alive`() {
        rule.onNodeWithText("شروع مرور", substring = true).performClick()
        rule.onNodeWithText("نمایش معنی").performClick()
        rule.onNodeWithText("بلد نبودم").performClick()

        rule.onNodeWithText("نمایش معنی").assertIsDisplayed()
    }
}
