# 🧠 FeelSpeak

Assistive Communication System for Deaf, Blind, and Normal Users using Machine Learning

---

## 🚀 Overview
FeelSpeak is an assistive communication system designed to bridge the gap between deaf, blind, and normal users.

The system uses machine learning (LSTM-based sequence modeling) for gesture recognition and supports bidirectional communication using:
- Text-to-Speech (TTS)
- Speech-to-Text (STT)
- Gesture-to-text conversion

---

## 🎯 Objective
To develop a multi-user communication system that enables seamless interaction between deaf, blind, and normal users using AI-based gesture recognition and speech processing.

---

## 🧠 System Methodology

### 1️⃣ Deaf–Blind Communication Mode
This is the complete communication loop between deaf and blind users.

- System initialization and input capture  
- Video frame sampling and preprocessing  
- Hand landmark detection  
- Feature extraction from landmarks  
- Sequential feature formation  
- LSTM-based gesture/language recognition  
- Sentence generation from predicted sequences  
- Text-to-Speech (TTS) output for blind user  
- Speech input from blind user  
- Speech-to-text conversion (STT)  
- Response display for deaf user  
- Continuous communication loop until termination  

---

### 2️⃣ Normal User Mode
- System startup and home screen display  
- User selects input action (record/upload)  
- Video capture and frame extraction  
- Preprocessing and landmark detection  
- Feature sequence generation  
- LSTM-based gesture recognition  
- Output text display  
- System termination  

---

### 3️⃣ Deaf User Mode
- System initialization  
- Gesture input via camera  
- Frame preprocessing  
- Hand landmark detection  
- Feature sequence formation  
- LSTM-based classification  
- Text output generation  
- Display of recognized message  

---

### 4️⃣ Blind User Mode
- System initialization  
- Voice input capture  
- Speech-to-text conversion (ASR concept)  
- Processing of user input  
- Response generation  
- Text-to-Speech (TTS) output  
- Audio response delivered to user  

---

## 🔁 Overall System Flow
- Video input → Frame extraction → Landmark detection  
- Feature extraction → Sequence formation  
- LSTM model → Gesture prediction  
- Prediction → Text output  
- Text → Speech (TTS for blind users)  
- Speech → Text (STT for response loop)  
- Continuous bidirectional communication  

---

## 🛠 Tech Stack
- Android (Java )  
- Python (Model training)  
- TensorFlow / Keras (LSTM model)  
- Machine Learning (Sequence classification)  
- Text-to-Speech (TTS engine)  
- Speech-to-Text (STT concept integration)  

---

## 📁 Project Structure


FeelSpeak/
├── app/ # Android application
├── Models/ # Trained ML models (LSTM)
├── label_encoder files # Label mappings
├── assets/ # Resources
└── utils/ # Helper scripts


---

## 🔊 Accessibility Features
- Text-to-Speech (TTS) for blind users  
- Speech-to-Text (STT-based communication concept)  
- Multi-user communication support  

---

## 📊 Model Information
- Model Type: LSTM-based sequence classification  
- Input: Hand landmark feature sequences  
- Output: Gesture / sign language class  
- Accuracy: 95.62 %  
- Focus: Functional recognition and communication flow  

---

## 🔮 Future Improvements
- Improve dataset size for better accuracy  
- Add real-time sentence-level recognition  
- Improve UI/UX design  
- Optimize model for mobile inference    

---

## ⭐ Project Impact
This project demonstrates:
- LSTM-based sequence modeling  
- Gesture recognition pipeline  
- Feature extraction from video frames  
- Android integration of ML models  
- Assistive communication system design  
- Accessibility using TTS and STT concepts

  
