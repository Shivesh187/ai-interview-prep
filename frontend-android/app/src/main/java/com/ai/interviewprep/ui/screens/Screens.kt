package com.ai.interviewprep.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.interviewprep.api.*
import kotlinx.coroutines.launch
import java.util.Locale

// --- Color System (Aesthetics) ---
val DarkBackground = Color(0xFF0F172A)
val CardBackground = Color(0xFF1E293B)
val AccentIndigo = Color(0xFF6366F1)
val AccentPink = Color(0xFFEC4899)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val BorderColor = Color(0xFF334155)
val SuccessGreen = Color(0xFF10B981)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(title: String, onLogout: (() -> Unit)? = null) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        actions = {
            if (onLogout != null) {
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Log Out",
                        tint = AccentPink
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = DarkBackground
        )
    )
}

// 1. --- LOGIN SCREEN ---
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Branding Icon and Title
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "AI Logo",
                tint = AccentIndigo,
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = "AI Interview Prep",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "Hone your skills. Ace your interview.",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Text Inputs
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentIndigo,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = AccentIndigo,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentIndigo,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = AccentIndigo,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Button
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        try {
                            val res = RetrofitClient.apiService.login(LoginRequest(email, password))
                            RetrofitClient.authToken = res.token
                            onLoginSuccess(res.token)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? Sign Up", color = AccentPink)
            }
        }
    }
}

// 2. --- REGISTER SCREEN ---
@Composable
fun RegisterScreen(
    onRegisterSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = "Register Logo",
                tint = AccentPink,
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "Join now to start practicing with AI",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPink,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = AccentPink,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPink,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = AccentPink,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPink,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = AccentPink,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        try {
                            val res = RetrofitClient.apiService.register(
                                RegisterRequest(username, email, password)
                            )
                            RetrofitClient.authToken = res.token
                            onRegisterSuccess(res.token)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Register", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Sign In", color = AccentIndigo)
            }
        }
    }
}

// 3. --- DASHBOARD SCREEN ---
@Composable
fun DashboardScreen(
    onStartNewInterview: () -> Unit,
    onViewDetail: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var history by remember { mutableStateOf<List<InterviewHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                history = RetrofitClient.apiService.getHistory()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load history: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = { AppHeader(title = "Dashboard", onLogout = onLogout) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartNewInterview,
                containerColor = AccentIndigo,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Practice", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = AccentIndigo,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (history.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Empty",
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No interview history yet.",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Tap 'Start Practice' to begin your first prep!",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Your Progress History",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(history) { item ->
                            HistoryCard(item = item, onClick = { onViewDetail(item.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(item: InterviewHistoryItem, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.job_role,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(item.difficulty, fontSize = 10.sp, color = TextPrimary) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = BorderColor),
                        border = null
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text(item.interview_type, fontSize = 10.sp, color = TextPrimary) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = BorderColor),
                        border = null
                    )
                }
            }

            // Score Badges
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (item.overall_score != null) {
                            Brush.verticalGradient(listOf(AccentIndigo, AccentPink))
                        } else {
                            Brush.verticalGradient(listOf(BorderColor, BorderColor))
                        }
                    )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (item.overall_score != null) "%.1f".format(item.overall_score) else "--",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "/10",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// 4. --- SETUP INTERVIEW SCREEN ---
@Composable
fun SetupScreen(
    onStartInterview: (String, String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var jobRole by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Mid Level") }
    var interviewType by remember { mutableStateOf("Technical") }

    val difficulties = listOf("Entry Level", "Mid Level", "Senior Level")
    val types = listOf("Technical", "Behavioral", "Coding & Architecture")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Setup AI Session", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Customize Your Interview",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            
            // Job Role Input
            OutlinedTextField(
                value = jobRole,
                onValueChange = { jobRole = it },
                label = { Text("What job role are you practicing for?") },
                placeholder = { Text("e.g. Android Developer, Product Manager") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentIndigo,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = AccentIndigo,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Difficulty Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Target Level:", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    difficulties.forEach { diff ->
                        val selected = difficulty == diff
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) AccentIndigo else CardBackground)
                                .border(1.dp, if (selected) AccentIndigo else BorderColor, RoundedCornerShape(8.dp))
                                .clickable { difficulty = diff }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(diff, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Interview Type Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Interview Focus Area:", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    types.forEach { type ->
                        val selected = interviewType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) AccentPink else CardBackground)
                                .border(1.dp, if (selected) AccentPink else BorderColor, RoundedCornerShape(8.dp))
                                .clickable { interviewType = type }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(type, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (jobRole.trim().isEmpty()) {
                        jobRole = "Software Engineer"
                    }
                    onStartInterview(jobRole, difficulty, interviewType)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Generate AI Interview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// 5. --- INTERVIEW SESSION SCREEN (ACTIVE INTERVIEW) ---
@Composable
fun InterviewSessionScreen(
    jobRole: String,
    difficulty: String,
    interviewType: String,
    onSessionFinished: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Screen States
    var interviewId by remember { mutableIntStateOf(-1) }
    var currentQuestion by remember { mutableStateOf<Question?>(null) }
    var questionNumber by remember { mutableIntStateOf(1) }
    var totalQuestions by remember { mutableIntStateOf(5) }
    var userAnswer by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Evaluation States
    var activeEvaluation by remember { mutableStateOf<Evaluation?>(null) }
    var isFinishedSession by remember { mutableStateOf(false) }

    // Voice Input Setup
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                if (!spokenText.isNullOrEmpty()) {
                    userAnswer = if (userAnswer.isEmpty()) spokenText else "$userAnswer $spokenText"
                }
            }
        }
    )

    fun launchVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your answer clear and concise...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice recording is not supported on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    // Initialize/Start session
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.apiService.startInterview(
                    StartInterviewRequest(jobRole, difficulty, interviewType)
                )
                interviewId = response.interviewId
                currentQuestion = response.question
                questionNumber = response.currentQuestionNumber
                totalQuestions = response.totalQuestions
            } catch (e: Exception) {
                Toast.makeText(context, "Error launching interview: ${e.message}", Toast.LENGTH_LONG).show()
                onNavigateBack()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Interview with AI", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = AccentIndigo,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question $questionNumber of $totalQuestions",
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        LinearProgressIndicator(
                            progress = questionNumber.toFloat() / totalQuestions,
                            color = AccentIndigo,
                            trackColor = BorderColor,
                            modifier = Modifier
                                .width(120.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }

                    // Question Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "QUESTION",
                                color = AccentIndigo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentQuestion?.questionText ?: "",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 24.sp
                            )
                        }
                    }

                    // Answer Input and Controls
                    if (activeEvaluation == null) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "YOUR ANSWER",
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )

                            OutlinedTextField(
                                value = userAnswer,
                                onValueChange = { userAnswer = it },
                                placeholder = { Text("Type your answer here or tap the microphone to dictate...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentIndigo,
                                    unfocusedBorderColor = BorderColor,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    placeholderColor = TextSecondary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )

                            // Dictation Trigger
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { launchVoiceRecognition() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                                    modifier = Modifier.border(1.dp, BorderColor, RoundedCornerShape(20.dp)),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Input", tint = AccentPink)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Speak Answer", color = TextPrimary)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (userAnswer.trim().isEmpty()) {
                                    Toast.makeText(context, "Please provide an answer first", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isSubmitting = true
                                scope.launch {
                                    try {
                                        val req = SubmitAnswerRequest(
                                            interviewId = interviewId,
                                            questionId = currentQuestion!!.id,
                                            userAnswer = userAnswer
                                        )
                                        val res = RetrofitClient.apiService.submitAnswer(req)
                                        activeEvaluation = res.evaluation
                                        isFinishedSession = res.isFinished
                                        
                                        // Save next details for later
                                        if (!res.isFinished && res.nextQuestion != null) {
                                            currentQuestion = res.nextQuestion
                                            questionNumber = res.currentQuestionNumber + 1
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Failed to submit answer: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            },
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Submit & Get AI Review", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    } else {
                        // AI Evaluation details screen overlay
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "AI EVALUATION FEEDBACK",
                                                color = AccentPink,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Badge(
                                                containerColor = if (activeEvaluation!!.score >= 7) SuccessGreen else AccentPink
                                            ) {
                                                Text(
                                                    "Score: ${activeEvaluation!!.score}/10",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(4.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = activeEvaluation!!.feedback,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            lineHeight = 20.dp
                                        )
                                    }
                                }
                            }

                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "SUGGESTED MODEL ANSWER",
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = activeEvaluation!!.modelAnswer,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            lineHeight = 20.dp
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (isFinishedSession) {
                                    Toast.makeText(context, "Session finished successfully!", Toast.LENGTH_SHORT).show()
                                    onSessionFinished()
                                } else {
                                    // Reset answer text, clear evaluation to reveal the next question input
                                    userAnswer = ""
                                    activeEvaluation = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFinishedSession) SuccessGreen else AccentIndigo
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = if (isFinishedSession) "Finish & View Overall Score" else "Next Question",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 6. --- INTERVIEW DETAILS / HISTORY REVIEW SCREEN ---
@Composable
fun HistoryDetailScreen(
    interviewId: Int,
    onNavigateBack: () -> Unit
) {
    var detail by remember { mutableStateOf<InterviewDetailsResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(interviewId) {
        scope.launch {
            try {
                detail = RetrofitClient.apiService.getInterviewDetails(interviewId)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not fetch details: ${e.message}", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Session Review", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = AccentIndigo,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (detail != null) {
                val data = detail!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Summary Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = data.job_role,
                                    color = TextPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${data.interview_type} • ${data.difficulty}",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = BorderColor)
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Cumulative AI Rating:",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = if (data.overall_score != null) "${data.overall_score}/10" else "Pending",
                                        color = AccentPink,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp
                                    )
                                }
                            }
                        }
                    }

                    items(data.questions) { q ->
                        QuestionReviewCard(q)
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionReviewCard(q: DetailedQuestionItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Question Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Q: ${q.question_text}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Badge(
                    containerColor = if ((q.score ?: 0) >= 7) SuccessGreen else AccentPink
                ) {
                    Text(
                        text = "Score: ${q.score ?: 0}/10",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderColor)
            Spacer(modifier = Modifier.height(12.dp))

            // User Answer Section
            Text("Your Response:", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(
                text = q.user_answer ?: "[No Answer Provided]",
                color = TextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // AI Feedback Section
            Text("AI Critical Feedback:", color = AccentPink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(
                text = q.ai_feedback ?: "N/A",
                color = TextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Model Answer Section
            Text("Suggested Exemplary Answer:", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(
                text = q.model_answer ?: "N/A",
                color = TextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
