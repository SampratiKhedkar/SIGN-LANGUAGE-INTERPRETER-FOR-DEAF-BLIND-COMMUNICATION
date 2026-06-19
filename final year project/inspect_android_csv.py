import numpy as np

csv_path = "C:\\Users\\SAMPRATI\\Desktop\\final year project\\android_keypoints.csv"

data = np.loadtxt(csv_path, delimiter=",")

print("Total shape:", data.shape)

# Check which rows are fully zero
zero_rows = np.where(~np.any(data != 0, axis=1))[0]
print("Zero rows:", zero_rows)

print("\n--- SAMPLE FRAMES ---")

for i in [0, 5, 10, 15, 20, 25]:
    if i < data.shape[0]:
        print(f"\nFrame {i}:")
        print("Non-zero count:", np.count_nonzero(data[i]))
        print("First 20 values:", data[i][:20])
