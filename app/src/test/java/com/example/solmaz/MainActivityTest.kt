package com.example.solmaz

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Boots the real [MainActivity] on the JVM (Robolectric): first-run name prompt,
 * today's list, then the review card flow. Data comes from the bundled words.json.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], qualifiers = "w411dp-h891dp")
class MainActivityTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private fun passOnboarding(name: String = "سولماز") {
        rule.onNodeWithText("اسم تو").performTextInput(name)
        rule.onNodeWithText("شروع").performClick()
    }

    @Test
    fun `first launch asks for the name and then greets the learner`() {
        rule.onNodeWithText("خوش آمدی 👋").assertIsDisplayed()

        passOnboarding("سولماز")

        rule.onNodeWithText("سلام سولماز 👋").assertIsDisplayed()
    }

    @Test
    fun `today screen shows the date, the summary and the first lesson`() {
        passOnboarding()

        rule.onNodeWithText("برای امروز").assertIsDisplayed()
        rule.onAllNodesWithText("Schule und Bildung", substring = true)[0].assertIsDisplayed()
        rule.onAllNodesWithText("تازه")[0].assertIsDisplayed()
    }

    @Test
    fun `review flow reveals the meaning and accepts a green answer`() {
        passOnboarding()

        rule.onNodeWithText("شروع مرور", substring = true).performClick()
        rule.onNodeWithText("نمایش معنی").assertIsDisplayed()
        rule.onNodeWithText("تلفظ آلمانی").assertIsDisplayed()

        rule.onNodeWithText("نمایش معنی").performClick()
        rule.onNodeWithText("تلفظ فارسی").assertIsDisplayed()

        rule.onNodeWithText("بلدم").performClick()

        // still inside the session, next card ready
        rule.onNodeWithText("نمایش معنی").assertIsDisplayed()
    }

    @Test
    fun `red answer keeps the card flow alive`() {
        passOnboarding()

        rule.onNodeWithText("شروع مرور", substring = true).performClick()
        rule.onNodeWithText("نمایش معنی").performClick()
        rule.onNodeWithText("بلد نبودم").performClick()

        rule.onNodeWithText("نمایش معنی").assertIsDisplayed()
    }
}
