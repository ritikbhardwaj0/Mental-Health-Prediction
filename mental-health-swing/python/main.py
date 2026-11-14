"""FastAPI prediction service and SQLite persistence layer."""
from pathlib import Path
import sqlite3
from typing import Literal

import joblib
import pandas as pd
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

ROOT = Path(__file__).resolve().parent
MODEL_PATH = ROOT / "artifacts" / "mental_health_model.joblib"
DATA_PATH = ROOT / "data" / "students.csv"
DB_PATH = ROOT.parent / "database" / "mental_health.sqlite3"
DB_PATH.parent.mkdir(parents=True, exist_ok=True)

if not MODEL_PATH.exists():
    raise RuntimeError("Trained model missing. Run python train_model.py first.")
bundle = joblib.load(MODEL_PATH)
model = bundle["model"]
dataset = pd.read_csv(DATA_PATH)

app = FastAPI(title="Student Mental Health Score API", version="1.0.0")


def initialize_database() -> None:
    with sqlite3.connect(DB_PATH) as conn:
        conn.execute("""CREATE TABLE IF NOT EXISTS predictions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            age INTEGER NOT NULL,
            gender TEXT NOT NULL,
            country TEXT NOT NULL,
            academic_level TEXT NOT NULL,
            most_used_platform TEXT NOT NULL,
            purpose_of_use TEXT NOT NULL,
            avg_daily_usage_hours REAL NOT NULL,
            daily_unlocks INTEGER NOT NULL,
            study_hours REAL NOT NULL,
            physical_activity_hours REAL NOT NULL,
            sleep_hours_per_night REAL NOT NULL,
            stress_level TEXT NOT NULL,
            predicted_score REAL NOT NULL,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
        )""")


initialize_database()


class StudentInput(BaseModel):
    age: int = Field(ge=10, le=100)
    gender: Literal["Male", "Female"]
    country: str = Field(min_length=1, max_length=100)
    academic_level: Literal["Undergraduate", "Graduate", "High School"]
    most_used_platform: Literal["Facebook", "LinkedIn", "Instagram", "Snapchat", "Twitter", "YouTube", "TikTok", "LINE", "KakaoTalk", "VKontakte", "WhatsApp", "WeChat"]
    purpose_of_use: Literal["Networking", "Education", "Entertainment", "News"]
    avg_daily_usage_hours: float = Field(ge=0, le=24)
    daily_unlocks: int = Field(ge=0, le=10000)
    study_hours: float = Field(ge=0, le=24)
    physical_activity_hours: float = Field(ge=0, le=24)
    sleep_hours_per_night: float = Field(ge=0, le=24)
    stress_level: Literal["Medium", "Low", "Very High", "High"]


@app.get("/health")
def health():
    return {"status": "ok", "model_loaded": True, "database": str(DB_PATH)}


@app.get("/analytics")
def analytics():
    """Descriptive statistics and observed sample proportions from the source CSV."""
    scores = dataset["Mental_Health_Score"].dropna()
    n = int(scores.size)
    below_4 = int((scores < 4).sum())
    from_4_to_7 = int(((scores >= 4) & (scores < 7)).sum())
    at_least_7 = int((scores >= 7).sum())
    very_high = dataset[dataset["Stress_Level"] == "Very High"]["Mental_Health_Score"].dropna()
    low_stress = dataset[dataset["Stress_Level"] == "Low"]["Mental_Health_Score"].dropna()
    modes = scores.mode()
    means_by_stress = dataset.groupby("Stress_Level")["Mental_Health_Score"].mean()
    return {
        "sample_size": n,
        "mean": float(scores.mean()),
        "median": float(scores.median()),
        "mode": float(modes.iloc[0]),
        "std_dev": float(scores.std()),
        "variance": float(scores.var()),
        "min": float(scores.min()),
        "q1": float(scores.quantile(0.25)),
        "q3": float(scores.quantile(0.75)),
        "max": float(scores.max()),
        "below_4_count": below_4,
        "from_4_to_7_count": from_4_to_7,
        "at_least_7_count": at_least_7,
        "p_below_4": below_4 / n if n else 0.0,
        "p_from_4_to_7": from_4_to_7 / n if n else 0.0,
        "p_at_least_7": at_least_7 / n if n else 0.0,
        "very_high_stress_count": int(very_high.size),
        "below_4_given_very_high_count": int((very_high < 4).sum()),
        "p_below_4_given_very_high_stress": float((very_high < 4).mean()) if very_high.size else 0.0,
        "low_stress_count": int(low_stress.size),
        "at_least_7_given_low_count": int((low_stress >= 7).sum()),
        "p_at_least_7_given_low_stress": float((low_stress >= 7).mean()) if low_stress.size else 0.0,
        "mean_low_stress": float(means_by_stress.get("Low", 0.0)),
        "mean_medium_stress": float(means_by_stress.get("Medium", 0.0)),
        "mean_high_stress": float(means_by_stress.get("High", 0.0)),
        "mean_very_high_stress": float(means_by_stress.get("Very High", 0.0)),
    }


@app.post("/predict")
def predict(data: StudentInput):
    row = pd.DataFrame([{
        "Age": data.age, "Gender": data.gender, "Country": data.country,
        "Academic_Level": data.academic_level,
        "Most_Used_Platform": data.most_used_platform,
        "Purpose_Of_Use": data.purpose_of_use,
        "Avg_Daily_Usage_Hours": data.avg_daily_usage_hours,
        "Daily_Unlocks": data.daily_unlocks, "Study_Hours": data.study_hours,
        "Physical_Activity_Hours": data.physical_activity_hours,
        "Sleep_Hours_Per_Night": data.sleep_hours_per_night,
        "Stress_Level": data.stress_level,
    }])
    score = round(float(model.predict(row)[0]), 2)
    try:
        with sqlite3.connect(DB_PATH) as conn:
            cursor = conn.execute("""INSERT INTO predictions (
                age, gender, country, academic_level, most_used_platform,
                purpose_of_use, avg_daily_usage_hours, daily_unlocks,
                study_hours, physical_activity_hours, sleep_hours_per_night,
                stress_level, predicted_score
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""", (
                data.age, data.gender, data.country, data.academic_level,
                data.most_used_platform, data.purpose_of_use,
                data.avg_daily_usage_hours, data.daily_unlocks, data.study_hours,
                data.physical_activity_hours, data.sleep_hours_per_night,
                data.stress_level, score,
            ))
            record_id = cursor.lastrowid
    except sqlite3.Error as exc:
        raise HTTPException(status_code=500, detail=f"Database save failed: {exc}") from exc
    return {"predicted_mental_health_score": score, "record_id": record_id}


@app.get("/history")
def history(limit: int = 50):
    limit = max(1, min(limit, 200))
    with sqlite3.connect(DB_PATH) as conn:
        conn.row_factory = sqlite3.Row
        rows = conn.execute(
            "SELECT * FROM predictions ORDER BY id DESC LIMIT ?", (limit,)
        ).fetchall()
    return [dict(row) for row in rows]
