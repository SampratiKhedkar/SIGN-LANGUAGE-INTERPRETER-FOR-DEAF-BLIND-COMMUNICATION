import numpy as np

def load_android_csv(csv_path):
    """
    Loads keypoints saved from Android (CSV)
    Removes frames that are all zeros.
    """
    data = np.loadtxt(csv_path, delimiter=",")
    print("Original shape:", data.shape)

    # Find rows that are not all zeros
    non_zero_rows = np.any(data != 0, axis=1)
    data_clean = data[non_zero_rows]

    print("Cleaned shape (removed zero rows):", data_clean.shape)
    return data_clean

if __name__ == "__main__":
    android_csv = "C:\\Users\\SAMPRATI\\Desktop\\final year project\\android_keypoints.csv"
    kp = load_android_csv(android_csv)

    print("First frame (first 10 values):")
    print(kp[0][:10])

    # Optional: see which frames were removed
    print("Removed frames at indices:", np.where(~np.any(np.loadtxt(android_csv, delimiter=",") != 0, axis=1))[0])
