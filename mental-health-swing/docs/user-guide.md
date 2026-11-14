# Mental Stress Meter User Guide

## Start the application

1. Open a terminal in the `mental-health-swing` project folder.
2. Run `./run.sh`.
3. Wait for the model training and backend startup messages. The Java Swing window opens when the backend is ready.

The launcher creates or reuses the Python virtual environment, installs the required Python packages, trains the model from `python/data/students.csv`, starts FastAPI, and opens the desktop application. Closing the desktop window stops the backend.

## Use the Stress Meter

1. Open the **Stress Meter** tab.
2. Enter age, country, study and activity habits, social media use, sleep, and self-reported stress.
3. Select **Check my score**.
4. Read the estimated score and the result message. A successful request saves the submitted values and score to the local SQLite history.

The score is a model estimate based on the supplied dataset. It is not a diagnosis or a clinical assessment.

## Review Statistics and Probability

Open **Student Wellbeing Insights** to see descriptive measures for the CSV, score-band counts, observed proportions, conditional probabilities, and three graphs: score bands, average score by reported stress, and a quartile spread plot. Use **Refresh figures** to reload the calculations from the dataset.

For an empirical probability, the application divides the count of records matching an event by the number of applicable records. For a conditional probability, the denominator is restricted to the stated group. For example, `P(score ≥ 7 | low stress)` uses only the records marked low stress.

These calculations summarize the sample. They do not estimate a particular user's likelihood of a mental health outcome.

## Local data and files

- The trained model is saved at `python/artifacts/mental_health_model.joblib`.
- Student check-ins and predictions are stored in `database/mental_health.sqlite3`.
- The model training data is `python/data/students.csv`.
- The backend serves `/health`, `/predict`, `/analytics`, and `/history` on `http://127.0.0.1:8000` while the application is running.

Protect the SQLite database file. Do not enter names, student numbers, or other identifying details for this classroom demonstration.
