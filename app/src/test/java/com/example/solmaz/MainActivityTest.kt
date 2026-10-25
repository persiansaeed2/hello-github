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
 * Boots the real [MainActivity] (Robolectric, no emulator) and drives the UI: this
 * catches composition crashes, bad resource references and broken navigation.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], qualifiers = "w411dp-h891dp")
class MainActivityTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `app launches on the today screen`() {
        rule.onNodeWithText("سلام 👋").assertIsDisplayed()
        rule.onNodeWithText("هدف امروز").assertIsDisplayed()
        rule.onNodeWithText("هفت روز گذشته").assertIsDisplayed()
        rule.onNodeWithText("هنوز امروز مطالعه‌ای ثبت نکردی").assertIsDisplayed()
    }

    @Test
    fun `all three tabs render`() {
        rule.onNodeWithText("تقویم").performClick()
        rule.onNodeWithText("برو به امروز").assertIsDisplayed()

        rule.onNodeWithText("آمار").performClick()
        rule.onNodeWithText("مجموع مطالعه").assertIsDisplayed()

        rule.onNodeWithText("امروز").performClick()
        rule.onNodeWithText("هدف امروز").assertIsDisplayed()
    }

    @Test
    fun `adding a session updates today, calendar and stats`() {
        // Today's bar in the 7-day chart opens the add dialog. It sits in the middle of
        // the screen, away from the floating tab bar.
        val todayBar = rule.onNodeWithText("ی")
        todayBar.performClick()

        rule.onNodeWithText("ثبت مطالعه").assertExists()

        // Type a subject, pick a preset duration and save.
        rule.onNodeWithText("درس / موضوع").performTextInput("ریاضی")
        rule.onNodeWithText("۴۵").performClick()
        rule.onNodeWithText("ذخیره").performClick()

        // Today screen now lists the session instead of the empty state.
        rule.onNodeWithText("ریاضی").assertExists()
        rule.onAllNodesWithText("۴۵ دقیقه")[0].assertExists()

        // The same session shows up in the calendar day list.
        rule.onNodeWithText("تقویم").performClick()
        rule.onAllNodesWithText("ریاضی")[0].assertExists()

        // And it is counted in the stats screen.
        rule.onNodeWithText("آمار").performClick()
        rule.onNodeWithText("به‌تفکیک درس").assertIsDisplayed()
        rule.onAllNodesWithText("۴۵ دقیقه")[0].assertExists()
    }
}
