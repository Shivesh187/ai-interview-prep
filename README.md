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
*   **AI Engine:** Google Gen AI SDK (`gemini-2.5-flash-002`)
*   **Security:** JSON Web Token (JWT) stateless route validation middleware
*   
## 📐 Architectural Design & System Data Flow

This project follows a decoupled, three-tier client-server architecture. Data flows across the stack through structured interfaces, ensuring a clean separation of concerns between presentation, business logic, and persistence layers.

### 1. The Persistence Layer: Relational Database Design (MySQL)
The database serves as the single source of truth for user authentication and historical interview states. It relies on a relational schema with structural enforcement:
* **Data Integrity via Constraints:** Foreign keys link tables (e.g., `interview_questions` belongs to an `interviews` session), utilizing `ON DELETE CASCADE` to guarantee automatic database cleanup when an interview is removed.
* **Connection Pooling Optimization:** Rather than opening and closing a new database connection for every single HTTP request (which introduces high latency and overhead), the backend implements **MySQL Connection Pooling**. Connections are kept alive, borrowed to execute a query, and immediately released back to the pool (`connection.release()`), allowing the app to scale efficiently under heavy concurrent traffic.

### 2. The Communication Bridge: RESTful API (Node.js & Express)
The backend transforms raw SQL data sets into predictable JSON payloads, abstracting the complex relational schema away from the mobile client.
* **State Management & Aggregation:** When a user calls the `resumeInterview` endpoint, the controller runs complex state aggregation logic inside the database. It reviews the `interview_questions` schema, filters out completed questions where scores exist, computes the `currentQuestionNumber`, and determines whether to serve an unanswered query or prompt the Gemini engine for a fresh one.
* **Security & Isolation:** Databases should never be exposed directly to client applications. The Node.js application layer acts as a security guard, using `authMiddleware` to decode JWT tokens, extract the `userId`, and strictly scope queries to guarantee that users can only access or delete records belonging specifically to them.

### 3. The Presentation Layer: Declarative Reactive UI (Jetpack Compose & Retrofit)
The frontend application consumes the structured endpoints using safe type mapping frameworks.
* **Type-Safe Network Serialization:** Using Retrofit, raw JSON responses are mapped directly into type-safe Kotlin Data Classes. The client utilizes Google's Gson `@SerializedName` annotations to decouple backend database naming conventions (snake_case like `question_text`) from frontend codebase standards (camelCase like `questionText`).
* **Single Source of Truth UI State:** The user interface reacts immediately to network mutations. For example, during a **Delete** operation:
    1. The UI sends an asynchronous request via Kotlin Coroutines (`viewModelScope.launch`).
    2. The backend intercepts it, drops child and parent rows safely via cascading database execution, and responds with an HTTP status confirmation.
    3. The frontend's reactive list state (`mutableStateListOf`) modifies its items locally in-memory, updating the interface instantly without requiring a full pull-to-refresh network cycle.
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
