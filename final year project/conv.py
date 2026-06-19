import joblib

pkl_path = r"C:\Users\SAMPRATI\Desktop\final year project\Models\label_encoder2_sol.pkl"
txt_path = r"C:\Users\SAMPRATI\Desktop\final year project\Models\label_encoder2_sol.txt"

le = joblib.load(pkl_path)

with open(txt_path, "w", encoding="utf-8") as f:
    for idx, label in enumerate(le.classes_):
        f.write(f"{label}\n")

print("✅ Labels successfully written to text file")
