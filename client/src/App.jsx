import { Routes, Route, Navigate } from "react-router-dom";
import AppLayout from "./layouts/AppLayout";
import Dashboard from "./pages/Dashboard";
import MenuSuggestions from "./pages/MenuSuggestions";
import AddItem from "./pages/AddItem";
import ItemDetail from "./pages/ItemDetail";
import Notifications from "./pages/Notifications";
import Profile from "./pages/Profile";
import SignUp from "./pages/SignUp";
import StartupRedirect from "./components/StartupRedirect";
import Login from "./pages/Login";
import ProtectedRoute from "./components/ProtectedRoute";
import PublicRoute from "./components/PublicRoute";

export default function App() {
  return (
    <Routes>
      {/* ===== PUBLIC ===== */}
      <Route
        path="/signup"
        element={
          <PublicRoute>
            <SignUp />
          </PublicRoute>
        }
      />

      <Route
        path="/login"
        element={
          <PublicRoute>
            <Login />
          </PublicRoute>
        }
      />

      {/* ===== PROTECTED (có layout) ===== */}
      <Route
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/menu" element={<MenuSuggestions />} />
        <Route path="/add" element={<AddItem />} />
        <Route path="/items/:id" element={<ItemDetail />} />
        <Route path="/notifications" element={<Notifications />} />
        <Route path="/profile" element={<Profile />} />
      </Route>

      {/* ===== ROOT ===== */}
      <Route path="/" element={<StartupRedirect />} />

      {/* ===== FALLBACK ===== */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}