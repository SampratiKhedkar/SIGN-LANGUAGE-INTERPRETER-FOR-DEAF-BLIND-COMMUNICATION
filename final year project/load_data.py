import numpy as np

def load_android_csv(csv_path):
    """
    Loads keypoints saved from Android (CSV)
    Shape expected: (50, 126)
    """
    data = np.loadtxt(csv_path, delimiter=",")
    print("Loaded shape:", data.shape)
    return data


if __name__ == "__main__":
    android_csv = "C:\\Users\\SAMPRATI\\Desktop\\final year project\\android_keypoints.csv"  # change path if needed
    kp = load_android_csv(android_csv)

    print("First frame (first 10 values):")
    print(kp[0][:10])
