import React from "react";
import ReactDOM from "react-dom/client";
import { HashRouter } from "react-router-dom";
import App from "./App.jsx";
import "./styles.css";
import { Toaster } from "react-hot-toast";

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <HashRouter>
      <App />
      <Toaster
        position="top-center"
        gutter={8}
        toastOptions={{
          duration: 2000,
          style: {
            background: "#1e293b",
            color: "#fff",
            fontSize: "14px",
            padding: "10px 14px",
            borderRadius: "10px",
            border: "1px solid #334155",
            maxWidth: "420px"
          }
        }}
      /> 
    </HashRouter>
  </React.StrictMode>
);
