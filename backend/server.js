const express = require('express');
const cors = require('cors');
const db = require('./db');
require('dotenv').config();

const authController = require('./authController');
const interviewController = require('./interviewController');
const authMiddleware = require('./authMiddleware');

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(express.json());

// Base Route
app.get('/', (req, res) => {
  res.json({ message: 'Welcome to the AI Interview Preparation API' });
});

// Authentication Routes
app.post('/api/auth/register', authController.register);
app.post('/api/auth/login', authController.login);

// Protected Interview Routes
app.post('/api/interviews/start', authMiddleware, interviewController.startInterview);
app.post('/api/interviews/answer', authMiddleware, interviewController.submitAnswer);
app.get('/api/interviews/history', authMiddleware, interviewController.getHistory);
app.get('/api/interviews/:id', authMiddleware, interviewController.getInterviewDetails);

app.get('/api/interviews/resume/:interviewId', authMiddleware, interviewController.resumeInterview);

app.get('/api/interviews/:id', authMiddleware, interviewController.getInterviewDetails);

app.delete('/api/interviews/:id', authMiddleware, interviewController.deleteInterview);
// Remove the old db.connect((err) => { ... }) block and paste this instead:
db.getConnection()
    .then((connection) => {
        console.log("MySQL Database Connected successfully via Pool! 🤝");
        connection.release(); // Always release the connection back to the pool!
    })
    .catch((err) => {
        console.error("❌ Database connection failed:", err.message);
    });

// Start Server
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
