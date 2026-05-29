package com.ai.interviewprep

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ai.interviewprep.api.RetrofitClient
import com.ai.interviewprep.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load persist token from SharedPreferences
        val sharedPref = getSharedPreferences("ai_interview_prefs", Context.MODE_PRIVATE)
        val savedToken = sharedPref.getString("auth_token", null)
        if (savedToken != null) {
            RetrofitClient.authToken = savedToken
        }

        setContent {
            AIInterviewPrepTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        startDestination = if (savedToken != null) "dashboard" else "login",
                        onSaveToken = { token ->
                            sharedPref.edit().putString("auth_token", token).apply()
                        },
                        onClearToken = {
                            sharedPref.edit().remove("auth_token").apply()
                            RetrofitClient.authToken = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AIInterviewPrepTheme(content: @Composable () -> Unit) {
    val darkColors = darkColorScheme(
        background = DarkBackground,
        surface = CardBackground,
        primary = AccentIndigo,
        secondary = AccentPink,
        onBackground = TextPrimary,
        onSurface = TextPrimary
    )

    MaterialTheme(
        colorScheme = darkColors,
        content = content
    )
}

@Composable
fun AppNavigation(
    startDestination: String,
    onSaveToken: (String) -> Unit,
    onClearToken: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { token ->
                    onSaveToken(token)
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { token ->
                    onSaveToken(token)
                    navController.navigate("dashboard") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                onStartNewInterview = {
                    navController.navigate("setup")
                },
                onViewDetail = { id ->
                    navController.navigate("history_detail/$id")
                },
                onLogout = {
                    onClearToken()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("setup") {
            SetupScreen(
                onStartInterview = { role, diff, type ->
                    navController.navigate("interview_session/$role/$diff/$type")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "interview_session/{jobRole}/{difficulty}/{interviewType}",
            arguments = listOf(
                navArgument("jobRole") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType },
                navArgument("interviewType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val jobRole = backStackEntry.arguments?.getString("jobRole") ?: "Software Engineer"
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "Mid Level"
            val interviewType = backStackEntry.arguments?.getString("interviewType") ?: "Technical"

            InterviewSessionScreen(
                jobRole = jobRole,
                difficulty = difficulty,
                interviewType = interviewType,
                onSessionFinished = {
                    navController.navigate("dashboard") {
                        popUpTo("setup") { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "history_detail/{interviewId}",
            arguments = listOf(
                navArgument("interviewId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val interviewId = backStackEntry.arguments?.getInt("interviewId") ?: -1
            HistoryDetailScreen(
                interviewId = interviewId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
