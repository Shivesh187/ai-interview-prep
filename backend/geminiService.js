const { GoogleGenerativeAI } = require('@google/generative-ai');
require('dotenv').config();

// Initialize the Gemini API client
const apiKey = process.env.GEMINI_API_KEY;
if (!apiKey) {
  console.warn('WARNING: GEMINI_API_KEY is not defined in the environment variables.');
}

const genAI = new GoogleGenerativeAI(apiKey || 'DUMMY_KEY');

// 1. Generate a single interview question
exports.generateQuestion = async (jobRole, difficulty, interviewType, pastQuestions = []) => {
  try {
    //  UPDATED: Swapped 'gemini-1.5-flash' for the supported 'gemini-2.5-flash'
    const model = genAI.getGenerativeModel({ model: 'gemini-2.5-flash' });

    const pastQuestionsText = pastQuestions.length > 0 
      ? pastQuestions.map((q, i) => `${i + 1}. "${q}"`).join('\n')
      : 'None';

    const prompt = `You are a professional HR interviewer and technical recruiter. 
Generate exactly ONE relevant interview question for a candidate applying for the position of:
Job Role: ${jobRole}
Experience/Difficulty Level: ${difficulty}
Interview Category: ${interviewType}

Rules:
1. Make the question challenging, modern, and realistic for this role and level.
2. Avoid repeating or asking questions similar to the ones already asked in this session:
---
${pastQuestionsText}
---
3. Output ONLY the question text itself. Do not include any introductory remarks, explanations, greeting text, or markdown code blocks.`;

    const result = await model.generateContent(prompt);
    // Modern syntax optimization for result response parsing
    return result.response.text().trim();
  } catch (error) {
    console.error('Error generating question from Gemini:', error);
    throw new Error('Could not generate question from Gemini AI service');
  }
};

// 2. Evaluate candidate response
exports.evaluateAnswer = async (question, userAnswer, jobRole, difficulty) => {
  try {
    const model = genAI.getGenerativeModel({
      model: 'gemini-2.5-flash',
      // Ask Gemini to output JSON
      generationConfig: {
        responseMimeType: 'application/json'
      }
    });

    const prompt = `You are a professional hiring manager evaluating a candidate for the position of:
Role: ${jobRole}
Level: ${difficulty}

Evaluate the candidate's answer based on correctness, terminology usage, detail, and relevance.

Question: "${question}"
Candidate's Answer: "${userAnswer || '[Candidate remained silent or left answer blank]'}"

Please provide your evaluation in JSON format with the following keys:
- "score": (integer 1-10) An objective rating of their response. If they didn't answer or gave a completely unrelated response, score it 1.
- "feedback": (string) Constructive, encouraging feedback. Point out what was correct, what crucial points were missed, and actionable tips to improve.
- "modelAnswer": (string) A comprehensive, exemplary response showing how a top-tier candidate would answer this question.

Output JSON only. Do not add markdown wrappers.`;

    const result = await model.generateContent(prompt);
    const responseText = result.response.text().trim();
    
    // Parse the JSON returned by Gemini
    return JSON.parse(responseText);
  } catch (error) {
    console.error('Error evaluating answer with Gemini:', error);
    // Fallback response in case of API failure or JSON parse failure
    return {
      score: 5,
      feedback: 'Could not compute AI feedback due to a system error. Please try again.',
      modelAnswer: 'A model answer is unavailable at this moment.'
    };
  }
};