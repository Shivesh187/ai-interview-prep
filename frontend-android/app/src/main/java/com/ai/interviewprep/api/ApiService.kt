package com.ai.interviewprep.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// --- Request / Response Models ---

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class User(
    val id: Int,
    val username: String,
    val email: String
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class StartInterviewRequest(
    val jobRole: String,
    val difficulty: String,
    val interviewType: String
)

data class Question(
    val id: Int,
    val questionText: String
)

data class StartInterviewResponse(
    val interviewId: Int,
    val question: Question,
    val currentQuestionNumber: Int,
    val totalQuestions: Int
)

data class SubmitAnswerRequest(
    val interviewId: Int,
    val questionId: Int,
    val userAnswer: String
)

data class Evaluation(
    val score: Int,
    val feedback: String,
    val modelAnswer: String
)

data class SubmitAnswerResponse(
    val evaluation: Evaluation,
    val isFinished: Boolean,
    val nextQuestion: Question?,
    val currentQuestionNumber: Int,
    val totalQuestions: Int
)

data class InterviewHistoryItem(
    val id: Int,
    val user_id: Int,
    val job_role: String,
    val difficulty: String,
    val interview_type: String,
    val overall_score: Double?,
    val created_at: String
)

data class DetailedQuestionItem(
    val id: Int,
    val interview_id: Int,
    val question_text: String,
    val user_answer: String?,
    val ai_feedback: String?,
    val score: Int?,
    val model_answer: String?,
    val created_at: String
)

data class InterviewDetailsResponse(
    val id: Int,
    val user_id: Int,
    val job_role: String,
    val difficulty: String,
    val interview_type: String,
    val overall_score: Double?,
    val created_at: String,
    val questions: List<DetailedQuestionItem>
)

// --- Retrofit Endpoints Mappings ---

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/interviews/start")
    suspend fun startInterview(@Body request: StartInterviewRequest): StartInterviewResponse

    @POST("api/interviews/answer")
    suspend fun submitAnswer(@Body request: SubmitAnswerRequest): SubmitAnswerResponse

    @GET("api/interviews/history")
    suspend fun getHistory(): List<InterviewHistoryItem>

    @GET("api/interviews/{id}")
    suspend fun getInterviewDetails(@Path("id") id: Int): InterviewDetailsResponse
}
