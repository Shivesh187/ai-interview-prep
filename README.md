# AI Interview Preparation Application 🚀

An intelligent, full-stack mobile simulator that helps users hone their skills and ace technical or behavioral job interviews. Built with a sleek, modern dark-themed Android client using Jetpack Compose and backed by a robust Node.js REST API integrated with Gemini AI for real-time evaluation.

---

## 📱 Key Features

*   **Custom Interview Architecture:** Configure practice sessions tailored to specific job roles (e.g., Android Developer), target experience tiers, and focus areas.
*   **Dynamic AI Question Generation:** Leverages Gemini to generate distinct questions on the fly, referencing past context to prevent duplicates.
*   **Voice & Text Inputs:** Write out answers or use native speech-to-text recognition to answer queries seamlessly.
*   **Real-Time AI Evaluation:** Instant grading on a scale of 1-10 with critical diagnostic feedback and structural model answer suggestions.
*   **Dashboard & Progress Tracking:** Track overall scores and seamlessly **Resume** pending sessions or **Delete** unwanted records straight from your history panel.

---

## 🛠️ Tech Stack & Architecture

### Frontend (Android)
*   **UI Framework:** Jetpack Compose (Declarative UI with native dark-theme layout system)
*   **Networking:** Retrofit2 & Gson Converter
*   **Async Processing:** Kotlin Coroutines & `LaunchedEffect` State Synchronization

### Backend (Node.js API)
*   **Runtime & Server:** Node.js, Express.js
*   **Database Integration:** MySQL (utilizing a resilient connection pooling strategy)
*   **AI Engine:** Google Gen AI SDK (`gemini-1.5-flash-002`)
*   **Security:** JSON Web Token (JWT) stateless route validation middleware

---

## 📦 Repository Structure

```text
ai-interview-prep/
├── backend/                  # Node.js Express REST API
│   ├── server.js             # Server entry point & API route mappings
│   ├── authController.js     # User registration & login endpoints
│   ├── interviewController.js# Session management (Start, Resume, Answer, History, Delete)
│   ├── geminiService.js      # Gemini AI prompt integration logic
│   ├── authMiddleware.js     # JWT token validation layer
│   └── db.js                 # MySQL Pool client instance configurations
└── frontend/                 # Android Native Studio Workspace
    └── app/src/main/java/com/ai/interviewprep/
        ├── api/
        │   ├── ApiService.kt # Retrofit HTTP endpoint mappings interface
        │   └── RetrofitClient.kt# App network singleton client setup
        └── ui/screens/
            └── screens.kt    # Unified Composable UI views (Dashboard, Session, Login)
