import { Navigate } from "react-router-dom";
import { getCurrentUser } from "../api/authStorage";

export default function StartupRedirect() {
  const user = getCurrentUser();
  const userId = user?.id;

  return userId
    ? <Navigate to="/dashboard" replace />
    : <Navigate to="/login" replace />;
}