"""Train and save the student mental-health score regression pipeline."""
from pathlib import Path

import joblib
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder

ROOT = Path(__file__).resolve().parent
DATA_PATH = ROOT / "data" / "students.csv"
MODEL_PATH = ROOT / "artifacts" / "mental_health_model.joblib"
TARGET = "Mental_Health_Score"
NUMERIC = ["Age", "Avg_Daily_Usage_Hours", "Daily_Unlocks", "Study_Hours",
           "Physical_Activity_Hours", "Sleep_Hours_Per_Night"]
CATEGORICAL = ["Gender", "Country", "Academic_Level", "Most_Used_Platform",
               "Purpose_Of_Use", "Stress_Level"]
FEATURES = NUMERIC + CATEGORICAL


def train() -> None:
    data = pd.read_csv(DATA_PATH)
    data = data.dropna(subset=FEATURES + [TARGET])
    x_train, x_test, y_train, y_test = train_test_split(
        data[FEATURES], data[TARGET], test_size=0.2, random_state=42
    )
    preprocessing = ColumnTransformer([
        ("numeric", "passthrough", NUMERIC),
        ("categorical", OneHotEncoder(handle_unknown="ignore"), CATEGORICAL),
    ])
    model = Pipeline([
        ("preprocessing", preprocessing),
        ("regressor", RandomForestRegressor(
            n_estimators=300, min_samples_leaf=2, random_state=42, n_jobs=-1
        )),
    ])
    model.fit(x_train, y_train)
    predictions = model.predict(x_test)
    MODEL_PATH.parent.mkdir(parents=True, exist_ok=True)
    joblib.dump({"model": model, "features": FEATURES, "target": TARGET}, MODEL_PATH)
    print(f"Trained on {len(x_train)} rows; evaluated on {len(x_test)} rows.")
    print(f"MAE: {mean_absolute_error(y_test, predictions):.3f}")
    print(f"RMSE: {mean_squared_error(y_test, predictions) ** 0.5:.3f}")
    print(f"R²: {r2_score(y_test, predictions):.3f}")
    print(f"Saved model: {MODEL_PATH}")


if __name__ == "__main__":
    train()
