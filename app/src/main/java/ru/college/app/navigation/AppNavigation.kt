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
import ru.college.app.presentation.LoginViewModel
import ru.college.app.presentation.MainViewModel
import ru.college.app.ui.city.CityScreen
import ru.college.app.ui.login.LoginScreen
import ru.college.app.ui.main.MainScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Main : Screen("main/{userId}") {
        fun createRoute(userId: Long) = "main/$userId"
    }
    data object City : Screen("city/{userId}") {
        fun createRoute(userId: Long) = "city/$userId"
    }
}

private const val ARG_USER_ID = "userId"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val container = (LocalContext.current.applicationContext as App).container

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.factory(container.userRepository)
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
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.City.route,
            arguments = listOf(navArgument(ARG_USER_ID) { type = NavType.LongType })
        ) {
            CityScreen(onBack = { navController.popBackStack() })
        }
    }
}
