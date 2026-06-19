import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'

import warnings
warnings.filterwarnings("ignore")

try:
    import absl.logging
    absl.logging.set_verbosity(absl.logging.ERROR)
except ImportError:
    pass

import cv2
import random
import joblib
import numpy as np
import mediapipe as mp
import tensorflow as tf

from tensorflow.keras.callbacks import EarlyStopping


from sklearn.preprocessing import LabelEncoder
from tensorflow.keras.utils import to_categorical
from sklearn.model_selection import train_test_split
from tensorflow.keras.layers import LSTM, Dense, Dropout
from tensorflow.keras.models import Sequential, load_model

# ---------------- PATHS ----------------
VIDEO_DATA_PATH = os.path.join(os.getcwd(), "ISL_CSLRT_Corpus", "Videos_Sentence_Level")

MODEL_FOLDER = os.path.join(os.getcwd(), "Models")
os.makedirs(MODEL_FOLDER, exist_ok=True)

MODEL_PATH = os.path.join(MODEL_FOLDER, "sign_language_lstm_model2_sol.h5")
TFLITE_PATH = os.path.join(MODEL_FOLDER, "sign_language_lstm_model2_sol.tflite")
LABEL_ENCODER_PATH = os.path.join(MODEL_FOLDER, "label_encoder2_sol.pkl")
LABEL_TXT_PATH = os.path.join(MODEL_FOLDER, "label_encoder2.txt")

mp_hands = mp.solutions.hands

# ---------------- KEYPOINT EXTRACTION ----------------
def extract_keypoints(results):
    lh = np.zeros(63)
    rh = np.zeros(63)

    if results.multi_hand_landmarks and results.multi_handedness:
        for hand_landmarks, handedness in zip(
            results.multi_hand_landmarks, results.multi_handedness
        ):
            label = handedness.classification[0].label
            keypoints = np.array(
                [[lm.x, lm.y, lm.z] for lm in hand_landmarks.landmark]
            ).flatten()

            if label == 'Left':
                lh = keypoints
            elif label == 'Right':
                rh = keypoints

    return np.concatenate([lh, rh])

# ---------------- AUGMENTATION ----------------
def augment_sequence(sequence):
    seq = np.copy(sequence)

    if random.random() < 0.5:
        seq = seq.reshape(-1, 3)
        seq[:, 0] = 1 - seq[:, 0]
        seq = seq.reshape(sequence.shape)

    if random.random() < 0.3:
        seq += np.random.normal(0, 0.01, seq.shape)

    return seq

# ---------------- FRAME SAMPLING ----------------
def sample_frames(frames, target_len=50):
    if len(frames) > target_len:
        idx = np.linspace(0, len(frames) - 1, target_len, dtype=int)
        frames = [frames[i] for i in idx]
    elif len(frames) < target_len:
        while len(frames) < target_len:
            frames.append(frames[-1])
    return frames

# ---------------- FEATURE EXTRACTION ----------------
def extract_features_from_videos(base_path, frames_per_video=50):
    X, y = [], []

    for label_folder in os.listdir(base_path):
        label_path = os.path.join(base_path, label_folder)
        if not os.path.isdir(label_path):
            continue

        for video_file in os.listdir(label_path):
            video_path = os.path.join(label_path, video_file)
            cap = cv2.VideoCapture(video_path)

            frames = []
            with mp_hands.Hands(
                static_image_mode=False,
                max_num_hands=2,
                min_detection_confidence=0.5
            ) as hands:

                while cap.isOpened():
                    ret, frame = cap.read()
                    if not ret:
                        break

                    frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                    results = hands.process(frame)
                    frames.append(extract_keypoints(results))

            cap.release()
            frames = sample_frames(frames, frames_per_video)
            sequence = np.array(frames)

            X.append(sequence)
            y.append(label_folder)

            for _ in range(2):  # augmentation
                X.append(augment_sequence(sequence))
                y.append(label_folder)

    return np.array(X), np.array(y)

# ---------------- TRAIN MODEL ----------------
def train_model():
    X, y = extract_features_from_videos(VIDEO_DATA_PATH, frames_per_video=50)
    print("✅ Features:", X.shape, "Labels:", len(np.unique(y)))

    le = LabelEncoder()
    y_encoded = le.fit_transform(y)
    y_cat = to_categorical(y_encoded)

    joblib.dump(le, LABEL_ENCODER_PATH)

    # ✅ Save label order for Android
    with open(LABEL_TXT_PATH, "w", encoding="utf-8") as f:
        for label in le.classes_:
            f.write(label + "\n")

    print("✅ label_encoder2.txt saved")

    X_train, X_test, y_train, y_test = train_test_split(
        X, y_cat, test_size=0.2, stratify=y_encoded, random_state=42
    )

    model = Sequential([
        LSTM(256, return_sequences=True, input_shape=(50, 126)),
        Dropout(0.4),
        LSTM(128),
        Dropout(0.3),
        Dense(128, activation='relu'),
        Dropout(0.2),
        Dense(y_cat.shape[1], activation='softmax')
    ])

    model.compile(
        optimizer='adam',
        loss='categorical_crossentropy',
        metrics=['accuracy']
    )

    early_stop = EarlyStopping(
    monitor="val_accuracy",
    patience=15,
    restore_best_weights=True
  )


    model.fit(
    X_train, y_train,
    validation_data=(X_test, y_test),
    epochs=100,
    batch_size=16,
    callbacks=[early_stop]
  )


    model.save(MODEL_PATH)
    print("✅ Model saved")

    convert_to_tflite()

# ---------------- TFLITE CONVERSION ----------------
def convert_to_tflite():
    model = tf.keras.models.load_model(MODEL_PATH)

    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.target_spec.supported_ops = [
        tf.lite.OpsSet.TFLITE_BUILTINS,
        tf.lite.OpsSet.SELECT_TF_OPS
    ]
    converter._experimental_lower_tensor_list_ops = False

    tflite_model = converter.convert()

    with open(TFLITE_PATH, "wb") as f:
        f.write(tflite_model)

    print("✅ TFLite model generated")

# ---------------- PREDICTION (TESTING) ----------------
def predict_sentence(video_path):
    model = load_model(MODEL_PATH)
    le = joblib.load(LABEL_ENCODER_PATH)

    cap = cv2.VideoCapture(video_path)
    frames = []

    with mp_hands.Hands(max_num_hands=2) as hands:
        while cap.isOpened():
            ret, frame = cap.read()
            if not ret:
                break

            frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            results = hands.process(frame)
            frames.append(extract_keypoints(results))

    cap.release()

    frames = sample_frames(frames, 50)
    sequence = np.expand_dims(np.array(frames), axis=0)

    pred = model.predict(sequence)[0]
    idx = np.argmax(pred)

    print(f"\n🎯 Prediction: {le.inverse_transform([idx])[0]}")
    print(f"🔥 Confidence: {pred[idx]*100:.2f}%")

# ---------------- MAIN ----------------
if __name__ == "__main__":
    while True:
        print("\n1. Train Model")
        print("2. Predict Video")
        print("3. Exit")

        choice = input("Choose: ").strip()

        if choice == "1":
            train_model()
        elif choice == "2":
            path = input("Video path: ").strip()
            predict_sentence(path)
        elif choice == "3":
            break
        else:
            print("Invalid option")
        