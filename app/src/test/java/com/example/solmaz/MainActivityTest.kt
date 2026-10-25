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
 * list → review card → answer.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], qualifiers = "w411dp-h891dp")
class MainActivityTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `today screen shows the date, the summary and the words`() {
        rule.onNodeWithText("امروز").assertIsDisplayed()
        // Jalali date of today, e.g. «یکشنبه ۳ آبان ۱۴۰۵»
        rule.onAllNodesWithText("امروز", substring = true)[0].assertIsDisplayed()
        rule.onNodeWithText("برای امروز").assertIsDisplayed()
        rule.onNodeWithText("apple").assertIsDisplayed()
        rule.onNodeWithText("سیب").assertIsDisplayed()
        rule.onAllNodesWithText("تازه")[0].assertIsDisplayed()
    }

    @Test
    fun `review flow reveals the meaning and accepts a green answer`() {
        rule.onNodeWithText("شروع مرور ۶ لغت").performClick()

        rule.onNodeWithText("apple").assertIsDisplayed()
        rule.onNodeWithText("نمایش معنی").performClick()
        rule.onNodeWithText("سیب").assertIsDisplayed()

        rule.onNodeWithText("بلدم").performClick()

        // the next card is shown, so «apple» is gone from the card
        rule.onNodeWithText("book").assertIsDisplayed()
    }

    @Test
    fun `red answer keeps the word for another round`() {
        rule.onNodeWithText("شروع مرور ۶ لغت").performClick()
        rule.onNodeWithText("نمایش معنی").performClick()
        rule.onNodeWithText("بلد نبودم").performClick()

        rule.onAllNodesWithText("از", substring = true)[0].assertIsDisplayed()
    }
}
