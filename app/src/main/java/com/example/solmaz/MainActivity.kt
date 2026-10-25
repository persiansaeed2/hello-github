package com.example.solmaz

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.solmaz.data.PrefsProgressStore
import com.example.solmaz.data.ProfileStore
import com.example.solmaz.data.WordSource
import com.example.solmaz.ui.AndroidSpeaker
import com.example.solmaz.ui.NamePrompt
import com.example.solmaz.ui.ReviewSessionScreen
import com.example.solmaz.ui.SpeechLang
import com.example.solmaz.ui.TodayScreen
import com.example.solmaz.ui.TodayViewModel
import com.example.solmaz.ui.TodayViewModelFactory
import com.example.solmaz.ui.components.AppBackground
import com.example.solmaz.ui.theme.SolmazTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolmazTheme {
                val context = LocalContext.current
                val load = remember { WordSource(context.applicationContext).load() }
                val factory = remember {
                    TodayViewModelFactory(
                        words = load.words,
                        store = PrefsProgressStore(context.applicationContext)
                    )
                }
                val viewModel: TodayViewModel = viewModel(factory = factory)
                val speaker = remember {
                    AndroidSpeaker(context.applicationContext) { lang ->
                        val message = if (lang == SpeechLang.GERMAN) {
                            "صدای آلمانی روی گوشی نصب نیست. در تنظیمات › تبدیل متن به گفتار، داده‌ی زبان آلمانی را دانلود کنید."
                        } else {
                            "صدای فارسی روی گوشی نصب نیست."
                        }
                        Toast.makeText(context.applicationContext, message, Toast.LENGTH_LONG).show()
                    }
                }
                val profile = remember { ProfileStore(context.applicationContext) }
                var userName by remember { mutableStateOf(profile.name) }
                var speechRate by remember { mutableStateOf(profile.speechRate) }
                LaunchedEffect(speechRate) { speaker.speechRate = speechRate }
                DisposableEffect(speaker) {
                    onDispose { speaker.shutdown() }
                }

                if (userName == null) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AppBackground()
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .windowInsetsPadding(WindowInsets.safeDrawing)
                            ) {
                                NamePrompt(onSubmit = { entered ->
                                    profile.name = entered
                                    userName = entered
                                })
                            }
                        }
                    }
                } else {
                    SolmazApp(
                        viewModel = viewModel,
                        userName = userName,
                        loadError = load.error,
                        onSpeak = speaker::speak,
                        speechRate = speechRate,
                        onSpeechRate = {
                            speechRate = it
                            profile.speechRate = it
                        }
                    )
                }
            }
        }
    }
}

/**
 * The app is deliberately a single screen: today's colour-coded word list, with the
 * review cards layered on top of it. The Jalali calendar arrives later as a separate
 * destination once it is asked for.
 */
@Composable
fun SolmazApp(
    viewModel: TodayViewModel,
    userName: String? = null,
    loadError: String? = null,
    onSpeak: (String, SpeechLang) -> Unit = { _, _ -> },
    speechRate: Float = 0.7f,
    onSpeechRate: (Float) -> Unit = {}
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppBackground()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                if (viewModel.sessionActive) {
                    ReviewSessionScreen(
                        vm = viewModel,
                        onExit = { viewModel.exitSession() },
                        onSpeak = onSpeak,
                        userName = userName
                    )
                } else {
                    TodayScreen(
                        vm = viewModel,
                        onStart = { viewModel.startSession() },
                        userName = userName,
                        loadError = loadError,
                        onSpeak = onSpeak,
                        speechRate = speechRate,
                        onSpeechRate = onSpeechRate
                    )
                }
            }
        }
    }
}
