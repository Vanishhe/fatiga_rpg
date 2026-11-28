package ru.college.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.college.app.App
import ru.college.app.presentation.BattleOutcome
import ru.college.app.presentation.BattleViewModel
import ru.college.app.presentation.CityViewModel
import ru.college.app.presentation.LoginViewModel
import ru.college.app.presentation.MainViewModel
import ru.college.app.presentation.SettingsViewModel
import ru.college.app.presentation.SkillTreeViewModel
import ru.college.app.ui.battle.BattleScreen
import ru.college.app.ui.city.CityScreen
import ru.college.app.ui.login.LoginScreen
import ru.college.app.ui.main.MainScreen
import ru.college.app.ui.settings.SettingsScreen
import ru.college.app.ui.skilltree.SkillTreeScreen
import ru.college.app.ui.splash.SplashScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Main : Screen("main/{userId}") {
        fun createRoute(userId: Long) = "main/$userId"
    }
    data object Settings : Screen("settings/{userId}") {
        fun createRoute(userId: Long) = "settings/$userId"
    }
    data object City : Screen("city/{userId}") {
        fun createRoute(userId: Long) = "city/$userId"
    }
    data object SkillTree : Screen("skills/{userId}") {
        fun createRoute(userId: Long) = "skills/$userId"
    }
    data object Battle : Screen("battle/{userId}") {
        fun createRoute(userId: Long) = "battle/$userId"
    }
}

private const val ARG_USER_ID = "userId"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val container = (LocalContext.current.applicationContext as App).container

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            LaunchedEffect(Unit) {
                // Restore the last signed-in profile only if it still exists in Room.
                val savedId = container.sessionManager.userId
                val user = savedId?.let { container.userRepository.findUserById(it) }
                if (user != null) {
                    navController.navigate(Screen.Main.createRoute(user.id)) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    container.sessionManager.clearSession()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            SplashScreen()
        }

        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.factory(
                    userRepository = container.userRepository,
                    sessionManager = container.sessionManager
                )
            )
            val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

            // Clear the back stack after a successful login: BACK must not return here.
            LaunchedEffect(uiState.isLoggedIn, uiState.userId) {
                val userId = uiState.userId
                if (uiState.isLoggedIn && userId != null) {
                    navController.navigate(Screen.Main.createRoute(userId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            LoginScreen(
                uiState = uiState,
                onModeSelected = loginViewModel::onModeSelected,
                onEmailChange = loginViewModel::onEmailChange,
                onPasswordChange = loginViewModel::onPasswordChange,
                onPlayerNameChange = loginViewModel::onPlayerNameChange,
                onSubmit = loginViewModel::onSubmit
            )
        }

        composable(
            route = Screen.Main.route,
            arguments = listOf(navArgument(ARG_USER_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong(ARG_USER_ID) ?: return@composable

            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModel.factory(
                    userId = userId,
                    userRepository = container.userRepository,
                    runHistoryRepository = container.runHistoryRepository,
                    playerProgressRepository = container.playerProgressRepository
                )
            )
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.navigateToCity) {
                if (uiState.navigateToCity) {
                    mainViewModel.onNavigationHandled()
                    navController.navigate(Screen.City.createRoute(userId))
                }
            }

            MainScreen(
                uiState = uiState,
                onClassSelected = mainViewModel::onClassSelected,
                onPlay = mainViewModel::onPlay,
                onSettings = {
                    navController.navigate(Screen.Settings.createRoute(userId))
                }
            )
        }

        composable(
            route = Screen.Settings.route,
            arguments = listOf(navArgument(ARG_USER_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong(ARG_USER_ID) ?: return@composable

            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(
                    userId = userId,
                    userRepository = container.userRepository,
                    sessionManager = container.sessionManager
                )
            )
            val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.loggedOut) {
                if (uiState.loggedOut) {
                    // Wipe the profile stack: BACK must never reopen a signed-out account.
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            SettingsScreen(
                uiState = uiState,
                onNameChange = settingsViewModel::onNameChange,
                onSave = settingsViewModel::onSave,
                onToggleSound = settingsViewModel::onToggleSound,
                onToggleVibration = settingsViewModel::onToggleVibration,
                onLanguageSelected = settingsViewModel::onLanguageSelected,
                onLogout = settingsViewModel::onLogout,
                onDeleteRequested = settingsViewModel::onDeleteRequested,
                onDeleteDismissed = settingsViewModel::onDeleteDismissed,
                onDeleteConfirmed = settingsViewModel::onDeleteConfirmed,
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.City.route,
            arguments = listOf(navArgument(ARG_USER_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong(ARG_USER_ID) ?: return@composable

            val cityViewModel: CityViewModel = viewModel(
                factory = CityViewModel.factory(
                    userId = userId,
                    userRepository = container.userRepository,
                    playerProgressRepository = container.playerProgressRepository
                )
            )
            val uiState by cityViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.navigateToSkillTree, uiState.navigateToBattle) {
                when {
                    uiState.navigateToSkillTree -> {
                        cityViewModel.onNavigationHandled()
                        navController.navigate(Screen.SkillTree.createRoute(userId))
                    }

                    uiState.navigateToBattle -> {
                        cityViewModel.onNavigationHandled()
                        navController.navigate(Screen.Battle.createRoute(userId))
                    }
                }
            }

            CityScreen(
                uiState = uiState,
                onShrine = cityViewModel::onShrine,
                onToggleArmory = cityViewModel::onToggleArmory,
                onUpgradeWeapon = cityViewModel::onUpgradeWeapon,
                onUpgradeArmor = cityViewModel::onUpgradeArmor,
                onBuyPotion = cityViewModel::onBuyPotion,
                onPortal = cityViewModel::onPortal,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SkillTree.route,
            arguments = listOf(navArgument(ARG_USER_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong(ARG_USER_ID) ?: return@composable

            val skillTreeViewModel: SkillTreeViewModel = viewModel(
                factory = SkillTreeViewModel.factory(
                    userId = userId,
                    playerProgressRepository = container.playerProgressRepository,
                    unlockedSkillRepository = container.unlockedSkillRepository
                )
            )
            val uiState by skillTreeViewModel.uiState.collectAsStateWithLifecycle()

            SkillTreeScreen(
                uiState = uiState,
                onUnlock = skillTreeViewModel::onUnlock,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Battle.route,
            arguments = listOf(navArgument(ARG_USER_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong(ARG_USER_ID) ?: return@composable

            val battleViewModel: BattleViewModel = viewModel(
                factory = BattleViewModel.factory(
                    userId = userId,
                    userRepository = container.userRepository,
                    playerProgressRepository = container.playerProgressRepository,
                    runHistoryRepository = container.runHistoryRepository
                )
            )
            val uiState by battleViewModel.uiState.collectAsStateWithLifecycle()

            BattleScreen(
                uiState = uiState,
                onAttack = battleViewModel::onAttack,
                onSpecial = battleViewModel::onSpecial,
                onFlee = battleViewModel::onFlee,
                onUseItem = battleViewModel::onUseItem,
                onPause = battleViewModel::onPause,
                onResume = battleViewModel::onResume,
                onAbandon = battleViewModel::onAbandon,
                onOutcomeConfirmed = {
                    when (uiState.outcome) {
                        // Death erased the hero: rebuild the profile from scratch.
                        BattleOutcome.FELL -> navController.navigate(
                            Screen.Main.createRoute(userId)
                        ) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                            launchSingleTop = true
                        }

                        // Victory or escape returns to the city hub underneath.
                        BattleOutcome.CLEAR, BattleOutcome.FLED -> navController.popBackStack()

                        null -> Unit
                    }
                }
            )
        }
    }
}
