package com.aniruddha.dreamsave.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aniruddha.dreamsave.data.Goal
import com.aniruddha.dreamsave.logic.Calc
import com.aniruddha.dreamsave.logic.formatMoney
import com.aniruddha.dreamsave.ui.components.AppBackground
import com.aniruddha.dreamsave.ui.components.ConfettiOverlay
import com.aniruddha.dreamsave.ui.components.GlassCard
import com.aniruddha.dreamsave.ui.components.GradientButton
import com.aniruddha.dreamsave.ui.screens.GoalDetailScreen
import com.aniruddha.dreamsave.ui.screens.HomeScreen
import com.aniruddha.dreamsave.ui.screens.SettingsScreen
import com.aniruddha.dreamsave.ui.screens.StatsScreen
import com.aniruddha.dreamsave.ui.theme.DreamSaveTheme
import com.aniruddha.dreamsave.ui.theme.ElectricBlue
import com.aniruddha.dreamsave.ui.theme.LocalDarkTheme

private const val ROUTE_HOME = "home"
private const val ROUTE_STATS = "stats"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_GOAL = "goal/{goalId}"

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val Tabs = listOf(
    TabItem(ROUTE_HOME, "Goals", Icons.Rounded.Home),
    TabItem(ROUTE_STATS, "Stats", Icons.Rounded.BarChart),
    TabItem(ROUTE_SETTINGS, "Settings", Icons.Rounded.Settings)
)

@Composable
fun AppRoot(viewModel: DreamSaveViewModel) {
    val data by viewModel.data.collectAsState()
    val celebrationId by viewModel.celebration.collectAsState()

    DreamSaveTheme(themeMode = data.settings.themeMode) {
        AppBackground {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val showBar = currentRoute == ROUTE_HOME || currentRoute == ROUTE_STATS || currentRoute == ROUTE_SETTINGS

            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    AnimatedVisibility(
                        visible = showBar,
                        enter = slideInVertically(tween(300)) { it } + fadeIn(tween(300)),
                        exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200))
                    ) {
                        FloatingNavBar(
                            currentRoute = currentRoute,
                            onSelect = { route -> navigateToTab(navController, route) }
                        )
                    }
                }
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = ROUTE_HOME,
                    modifier = Modifier
                        .padding(padding)
                        .consumeWindowInsets(padding),
                    enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 14 } },
                    exitTransition = { fadeOut(tween(180)) },
                    popEnterTransition = { fadeIn(tween(300)) },
                    popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(250)) { it / 14 } }
                ) {
                    composable(ROUTE_HOME) {
                        HomeScreen(
                            data = data,
                            onOpenGoal = { id -> navController.navigate("goal/$id") },
                            onCreateGoal = { name, target, starting, deadline, description ->
                                val id = viewModel.addGoal(name, target, starting, deadline, description)
                                navController.navigate("goal/$id")
                            }
                        )
                    }
                    composable(ROUTE_STATS) {
                        StatsScreen(data = data)
                    }
                    composable(ROUTE_SETTINGS) {
                        SettingsScreen(
                            settings = data.settings,
                            onThemeMode = viewModel::setThemeMode,
                            onCelebrations = viewModel::setCelebrations,
                            onResetAll = viewModel::resetAllData
                        )
                    }
                    composable(
                        route = ROUTE_GOAL,
                        arguments = listOf(navArgument("goalId") { type = NavType.StringType })
                    ) { entry ->
                        val goalId = entry.arguments?.getString("goalId").orEmpty()
                        GoalDetailScreen(
                            goal = data.goals.firstOrNull { it.id == goalId },
                            allDeposits = data.deposits,
                            onBack = { navController.popBackStack() },
                            onAddDeposit = { amount, note -> viewModel.addDeposit(goalId, amount, note) },
                            onDeleteDeposit = { id -> viewModel.deleteDeposit(id) },
                            onUpdateGoal = { name, target, starting, deadline, description ->
                                viewModel.updateGoal(goalId, name, target, starting, deadline, description)
                            },
                            onDeleteGoal = { viewModel.deleteGoal(goalId) }
                        )
                    }
                }
            }

            val celebrationGoal = data.goals.firstOrNull { it.id == celebrationId }
            if (celebrationGoal != null) {
                CelebrationOverlay(
                    goal = celebrationGoal,
                    saved = Calc.savedOf(celebrationGoal, data.deposits),
                    onDismiss = viewModel::dismissCelebration
                )
            }
        }
    }
}

private fun navigateToTab(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun FloatingNavBar(currentRoute: String?, onSelect: (String) -> Unit) {
    val dark = LocalDarkTheme.current
    val shape = RoundedCornerShape(30.dp)
    val barColor = if (dark) Color(0xEE0E1428) else Color(0xF2FFFFFF)
    val borderColor = if (dark) Color.White.copy(alpha = 0.14f) else Color(0x262A3568)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(barColor, shape)
                .border(1.dp, borderColor, shape)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (tab in Tabs) {
                NavItem(
                    selected = currentRoute == tab.route,
                    icon = tab.icon,
                    label = tab.label,
                    onClick = { onSelect(tab.route) }
                )
            }
        }
    }
}

@Composable
private fun NavItem(selected: Boolean, icon: ImageVector, label: String, onClick: () -> Unit) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250),
        label = "navContent"
    )
    val background by animateColorAsState(
        targetValue = if (selected) ElectricBlue.copy(alpha = 0.85f) else Color.Transparent,
        animationSpec = tween(250),
        label = "navBackground"
    )
    Row(
        modifier = Modifier
            .background(background, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = contentColor)
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally() + fadeIn(),
            exit = shrinkHorizontally() + fadeOut()
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(start = 8.dp),
                color = contentColor,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun CelebrationOverlay(goal: Goal, saved: Double, onDismiss: () -> Unit) {
    BackHandler(onBack = onDismiss)
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        ConfettiOverlay()
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + scaleIn(
                initialScale = 0.7f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        ) {
            GlassCard(modifier = Modifier.padding(32.dp).fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("\uD83C\uDF89", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Goal achieved!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "You saved ${formatMoney(saved)}. Your dream is now reality.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    GradientButton(text = "Awesome!", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
