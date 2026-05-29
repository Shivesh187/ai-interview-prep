const db = require('./db');
const geminiService = require('./geminiService');

const QUESTIONS_PER_INTERVIEW = 5; // Default interview length

// 1. Start Interview
exports.startInterview = async (req, res) => {
  const { jobRole, difficulty, interviewType } = req.body;
  const userId = req.user.id;

  if (!jobRole || !difficulty || !interviewType) {
    return res.status(400).json({ message: 'Missing jobRole, difficulty, or interviewType' });
  }

  try {
    // Insert new interview session
    const [result] = await db.query(
      'INSERT INTO interviews (user_id, job_role, difficulty, interview_type) VALUES (?, ?, ?, ?)',
      [userId, jobRole, difficulty, interviewType]
    );
    const interviewId = result.insertId;

    // Generate first question
    const questionText = await geminiService.generateQuestion(jobRole, difficulty, interviewType, []);

    // Save generated question to DB
    const [questionResult] = await db.query(
      'INSERT INTO interview_questions (interview_id, question_text) VALUES (?, ?)',
      [interviewId, questionText]
    );

    res.status(201).json({
      interviewId,
      question: {
        id: questionResult.insertId,
        questionText
      },
      currentQuestionNumber: 1,
      totalQuestions: QUESTIONS_PER_INTERVIEW
    });

  } catch (error) {
    console.error('Error starting interview:', error);
    res.status(500).json({ message: 'Failed to start interview session' });
  }
};

// 2. Submit Answer & Advance (or Finish)
exports.submitAnswer = async (req, res) => {
  const { interviewId, questionId, userAnswer } = req.body;

  if (!interviewId || !questionId) {
    return res.status(400).json({ message: 'Missing interviewId or questionId' });
  }

  try {
    // 1. Fetch current question and interview metadata
    const [questions] = await db.query(
      'SELECT q.*, i.job_role, i.difficulty, i.interview_type FROM interview_questions q JOIN interviews i ON q.interview_id = i.id WHERE q.id = ? AND q.interview_id = ?',
      [questionId, interviewId]
    );

    if (questions.length === 0) {
      return res.status(404).json({ message: 'Question or interview not found' });
    }

    const currentQuestion = questions[0];

    // 2. Evaluate answer using Gemini
    const evaluation = await geminiService.evaluateAnswer(
      currentQuestion.question_text,
      userAnswer,
      currentQuestion.job_role,
      currentQuestion.difficulty
    );

    // 3. Save evaluation results in DB
    await db.query(
      'UPDATE interview_questions SET user_answer = ?, ai_feedback = ?, score = ?, model_answer = ? WHERE id = ?',
      [userAnswer, evaluation.feedback, evaluation.score, evaluation.modelAnswer, questionId]
    );

    // 4. Fetch all questions for this interview to calculate progress & build past questions list
    const [allQuestions] = await db.query(
      'SELECT question_text, score FROM interview_questions WHERE interview_id = ?',
      [interviewId]
    );

    const answeredCount = allQuestions.filter(q => q.score !== null).length;
    const isFinished = answeredCount >= QUESTIONS_PER_INTERVIEW;

    let nextQuestion = null;

    if (!isFinished) {
      // Build past questions list to avoid duplicates
      const pastQuestions = allQuestions.map(q => q.question_text);

      // Generate next question
      const nextQuestionText = await geminiService.generateQuestion(
        currentQuestion.job_role,
        currentQuestion.difficulty,
        currentQuestion.interview_type,
        pastQuestions
      );

      // Save next question to DB (unanswered yet)
      const [nextQuestionResult] = await db.query(
        'INSERT INTO interview_questions (interview_id, question_text) VALUES (?, ?)',
        [interviewId, nextQuestionText]
      );

      nextQuestion = {
        id: nextQuestionResult.insertId,
        questionText: nextQuestionText
      };
    } else {
      // Calculate overall average score
      const validScores = allQuestions.map(q => q.score).filter(s => s !== null);
      const averageScore = validScores.reduce((sum, score) => sum + score, 0) / validScores.length;

      // Update interview score
      await db.query('UPDATE interviews SET overall_score = ? WHERE id = ?', [averageScore, interviewId]);
    }

    res.json({
      evaluation: {
        score: evaluation.score,
        feedback: evaluation.feedback,
        modelAnswer: evaluation.modelAnswer
      },
      isFinished,
      nextQuestion,
      currentQuestionNumber: answeredCount,
      totalQuestions: QUESTIONS_PER_INTERVIEW
    });

  } catch (error) {
    console.error('Error submitting answer:', error);
    res.status(500).json({ message: 'Failed to process answer submission' });
  }
};

// 3. Get Interview History
exports.getHistory = async (req, res) => {
  const userId = req.user.id;

  try {
    const [rows] = await db.query(
      'SELECT * FROM interviews WHERE user_id = ? ORDER BY created_at DESC',
      [userId]
    );
    res.json(rows);
  } catch (error) {
    console.error('Error fetching history:', error);
    res.status(500).json({ message: 'Failed to fetch interview history' });
  }
};

// 4. Get Interview Details
exports.getInterviewDetails = async (req, res) => {
  const { id } = req.params;
  const userId = req.user.id;

  try {
    // Verify interview belongs to user
    const [interviews] = await db.query(
      'SELECT * FROM interviews WHERE id = ? AND user_id = ?',
      [id, userId]
    );

    if (interviews.length === 0) {
      return res.status(404).json({ message: 'Interview not found or unauthorized' });
    }

    const interview = interviews[0];

    // Fetch questions & evaluations
    const [questions] = await db.query(
      'SELECT * FROM interview_questions WHERE interview_id = ? ORDER BY id ASC',
      [id]
    );

    res.json({
      ...interview,
      questions
    });

  } catch (error) {
    console.error('Error fetching interview details:', error);
    res.status(500).json({ message: 'Failed to fetch interview details' });
  }
};

exports.resumeInterview = async (req, res) => {
    const userId = req.user.id; 
    const { interviewId } = req.params;

    try {
        // 1. Fetch the interview session to ensure it exists and belongs to this user
        const [sessions] = await db.query(
            'SELECT job_role, difficulty, interview_type FROM interviews WHERE id = ? AND user_id = ?',
            [interviewId, userId]
        );

        if (sessions.length === 0) {
            return res.status(404).json({ message: 'Interview session not found or unauthorized' });
        }

        const session = sessions[0];

        // 2. Fetch all questions linked to this specific interview session
        const [allQuestions] = await db.query(
            'SELECT id, question_text, score FROM interview_questions WHERE interview_id = ? ORDER BY id ASC',
            [interviewId]
        );

        // Filter out how many questions have actually been completed (evaluated)
        const answeredQuestions = allQuestions.filter(q => q.score !== null);
        const currentQuestionNumber = answeredQuestions.length + 1;

        // 3. Pinpoint the current active, unanswered question
        // Look for the first question slot where an AI evaluation score has not been recorded yet
        const unansweredQuestion = allQuestions.find(q => q.score === null);

        let activeQuestion = null;

        if (unansweredQuestion) {
            // An active question is already generated and waiting for an answer response
            activeQuestion = {
                id: unansweredQuestion.id,
                questionText: unansweredQuestion.question_text
            };
        } else {
            // No current question generated yet (user closed app right after submitting an answer)
            if (answeredQuestions.length >= QUESTIONS_PER_INTERVIEW) {
                return res.status(400).json({ message: 'This interview session is already complete.' });
            }

            // Compile historically asked questions to prevent duplication loops
            const pastQuestions = allQuestions.map(q => q.question_text);

            // Ask Gemini to supply a brand new question prompt
            const nextQuestionText = await geminiService.generateQuestion(
                session.job_role,
                session.difficulty,
                session.interview_type,
                pastQuestions
            );

            // Store the uncompleted question line into the database
            const [insertResult] = await db.query(
                'INSERT INTO interview_questions (interview_id, question_text) VALUES (?, ?)',
                [interviewId, nextQuestionText]
            );

            activeQuestion = {
                id: insertResult.insertId,
                questionText: nextQuestionText
            };
        }

        // 4. Send back the exact key formatting ('questionText') your frontend app requires
        // Find this section at the bottom of exports.resumeInterview:
        return res.json({
            interviewId: parseInt(interviewId),
            currentQuestionNumber: currentQuestionNumber,
            totalQuestions: QUESTIONS_PER_INTERVIEW,
            question: {
                id: activeQuestion.id,
                // Providing both naming patterns ensures your frontend catches it perfectly!
                questionText: activeQuestion.questionText, 
                question_text: activeQuestion.questionText 
            }
        });

    } catch (error) {
        console.error('Error in resumeInterview controller:', error);
        return res.status(500).json({ message: 'Failed to resume interview session' });
    }
};

// 5. Delete an Interview Session & its linked questions
exports.deleteInterview = async (req, res) => {
  const { id } = req.params; // Matches the dynamic route parameter /:id
  const userId = req.user.id; // Extracted securely from your authMiddleware token validation

  try {
    // 1. Verify that the interview actually exists and belongs to the requesting user
    const [interviews] = await db.query(
      'SELECT id FROM interviews WHERE id = ? AND user_id = ?',
      [id, userId]
    );

    if (interviews.length === 0) {
      return res.status(404).json({ message: 'Interview not found or unauthorized' });
    }

    // 2. Clear out all dependent questions linked to this interview ID first
    await db.query('DELETE FROM interview_questions WHERE interview_id = ?', [id]);

    // 3. Delete the parent row from the main interviews session ledger table
    await db.query('DELETE FROM interviews WHERE id = ?', [id]);

    // 4. Send back the exact JSON string success confirmation your mobile frontend expects
    return res.json({ message: 'Session deleted successfully' });

  } catch (error) {
    console.error('Error executing interview deletion:', error);
    return res.status(500).json({ message: 'Failed to delete interview session' });
  }
};