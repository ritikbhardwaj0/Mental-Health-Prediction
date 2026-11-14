# Student Mental Health Score

A complete local project built from the supplied student social media and mental health CSV.

## Stack and flow

- **Machine learning:** scikit-learn regression pipeline trained from `python/data/students.csv`.
- **Backend:** Python FastAPI loads the trained model, validates requests, generates predictions, and exposes prediction history.
- **Frontend:** Java Swing desktop application calls the backend over HTTP.
- **Database:** SQLite relational database at `database/mental_health.sqlite3`. The backend creates its table on startup; no database server setup is needed.
- **Statistics and probability:** the desktop app summarizes the CSV with descriptive measures, empirical and conditional probabilities, a score-band graph, a stress-group mean graph, and a quartile spread plot.

`Swing form → FastAPI → trained model → SQLite`

## Requirements

- Python 3.10+
- Java 17+
- Maven

## Start the complete app

From this project directory, run:

```sh
./run.sh
```

The script creates a Python virtual environment, installs Python dependencies, trains and saves the model, starts the backend, waits for its health endpoint, compiles the Java app, and opens the Swing window. Closing the Swing app stops the backend process. First run requires internet access for Python and Maven dependencies.

## Train the model by itself

```sh
cd python
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python train_model.py
```

Training uses an 80/20 train/evaluation split and prints MAE, RMSE, and R². The fitted preprocessing and regressor are saved together to `python/artifacts/mental_health_model.joblib`.

## Run parts separately

After training, start the API with `cd python && uvicorn main:app --host 127.0.0.1 --port 8000`. Then run `cd java && mvn compile exec:java` in another terminal. Set `ML_API_URL` if the API uses a different address.

API endpoints: `GET /health`, `POST /predict`, `GET /analytics`, and `GET /history`.

## Data and limitations

The CSV contains 5,000 rows and the model predicts the dataset's `Mental_Health_Score` column. This is a student project demonstration, not a clinical assessment or diagnosis. Avoid collecting identifiable information, and treat stored wellbeing responses as sensitive data.
