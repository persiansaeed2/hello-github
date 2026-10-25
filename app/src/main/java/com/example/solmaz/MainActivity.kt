package com.example.solmaz

import android.os.Bundle
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.solmaz.data.PrefsProgressStore
import com.example.solmaz.data.WordSource
import com.example.solmaz.ui.ReviewSessionScreen
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
                SolmazApp(viewModel = viewModel, loadError = load.error)
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
fun SolmazApp(viewModel: TodayViewModel, loadError: String? = null) {
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
                        onExit = { viewModel.exitSession() }
                    )
                } else {
                    TodayScreen(
                        vm = viewModel,
                        onStart = { viewModel.startSession() },
                        loadError = loadError
                    )
                }
            }
        }
    }
}
