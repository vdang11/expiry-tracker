import { Navigate } from "react-router-dom";
import { getCurrentUserId } from "../api/authStorage";

export default function StartupRedirect() {
  const userId = getCurrentUserId();

  return userId
    ? <Navigate to="/dashboard" replace />
    : <Navigate to="/login" replace />;
}