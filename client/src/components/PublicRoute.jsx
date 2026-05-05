import { Navigate } from "react-router-dom";
import { isLoggedIn } from "../api/authStorage";

export default function PublicRoute({ children }) {
  if (isLoggedIn()) {
    return <Navigate to="/dashboard" replace />;
  }
  return children;
}