package com.example.solmaz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import com.example.solmaz.data.StudyRepository
import com.example.solmaz.model.JalaliCalendar
import com.example.solmaz.ui.AddSessionDialog
import com.example.solmaz.ui.CalendarScreen
import com.example.solmaz.ui.StatsScreen
import com.example.solmaz.ui.StudyViewModel
import com.example.solmaz.ui.StudyViewModelFactory
import com.example.solmaz.ui.TodayScreen
import com.example.solmaz.ui.components.AppBackground
import com.example.solmaz.ui.components.GlassTabBar
import com.example.solmaz.ui.theme.SolmazTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolmazTheme {
                val context = LocalContext.current
                val factory = remember {
                    StudyViewModelFactory(StudyRepository(context.applicationContext))
                }
                val viewModel: StudyViewModel = viewModel(factory = factory)
                SolmazApp(viewModel)
            }
        }
    }
}

@Composable
fun SolmazApp(vm: StudyViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    // null = dialog closed, otherwise the day the session is being added to.
    var addForDay by remember { mutableStateOf<Long?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppBackground()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                        )
                    )
            ) {
                when (tab) {
                    0 -> TodayScreen(
                        vm = vm,
                        onAdd = { addForDay = it },
                        onDelete = vm::deleteSession
                    )

                    1 -> CalendarScreen(
                        vm = vm,
                        onAdd = { addForDay = it },
                        onDelete = vm::deleteSession,
                        onShiftMonth = vm::shiftMonth,
                        onToday = { vm.goToMonthOf(vm.today.epochDay) }
                    )

                    else -> StatsScreen(vm = vm)
                }
            }

            GlassTabBar(
                selected = tab,
                onSelect = { tab = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 26.dp, vertical = 12.dp)
                    .fillMaxWidth()
            )
        }

        addForDay?.let { day ->
            AddSessionDialog(
                date = JalaliCalendar.fromEpochDay(day),
                onDismiss = { addForDay = null },
                onSave = { subject, minutes ->
                    vm.addSession(day, subject, minutes)
                    vm.selectDay(day)
                    addForDay = null
                }
            )
        }
    }
}
