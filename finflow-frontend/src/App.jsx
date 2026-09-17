import { useState } from "react";
import "./App.css";

function App() {

  const [file, setFile] = useState(null);
  const [context, setContext] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleUpload = async () => {

    if (!file) {
      setError("Please select a receipt.");
      return;
    }

    setLoading(true);
    setError("");
    setResult(null);

    const formData = new FormData();

    formData.append("file", file);
    formData.append("context", context);

    try {

      const response = await fetch(
        "http://localhost:8080/upload",
        {
          method: "POST",
          body: formData
        }
      );

      if (!response.ok) {
        throw new Error("Upload failed");
      }

      const data = await response.json();

      setResult(data);

    } catch (err) {

      setError(err.message);

    } finally {

      setLoading(false);

    }
  };

  return (
    <div className="app">

      <header>
        <h1>FinFlow</h1>
      </header>

      <main>

        <div className="card">

          <h2>Upload Expense</h2>

          <label>Receipt</label>

          <input
            type="file"
            accept=".pdf,image/*"
            onChange={(e) => setFile(e.target.files[0])}
          />

          {file && (
            <p className="file-name">
              Selected: {file.name}
            </p>
          )}

          <label>Expense Context</label>

          <textarea
            placeholder="Example: Travel expense for attending a client meeting in Chennai"
            value={context}
            onChange={(e) => setContext(e.target.value)}
          />

          <button
            onClick={handleUpload}
            disabled={loading}
          >
            {loading ? "Processing..." : "Upload Expense"}
          </button>

          {error && (
            <p className="error">
              {error}
            </p>
          )}

        </div>


        {result && (

          <div className="result-card">

            <h2>Expense Details</h2>

            <div className="result-item">
              <span>ID</span>
              <strong>{result.id}</strong>
            </div>

            <div className="result-item">
              <span>Context</span>
              <strong>{result.context_by_user}</strong>
            </div>

            <div className="result-item">
              <span>Summary</span>
              <strong>{result.summary}</strong>
            </div>

            <div className="result-item">
              <span>Amount</span>
              <strong>
                {result.currency} {result.amount}
              </strong>
            </div>

            <div className="result-item">
              <span>Expense Reason</span>
              <strong>{result.expense_reason}</strong>
            </div>

            <div className="rag">
              <h3>RAG Summary</h3>
              <p>{result.rag_summary_data}</p>
            </div>

          </div>

        )}

      </main>

    </div>
  );
}

export default App;