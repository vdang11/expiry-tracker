import { Navigate } from "react-router-dom";

export default function StartupRedirect() {
  const user = localStorage.getItem("currentUser");

  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Navigate to="/login" replace />;
}
