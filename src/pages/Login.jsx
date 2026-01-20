import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthLayout from "../layouts/AuthLayout";
import { login } from "../api/authApi";

export default function Login() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");

    if (!form.username || !form.password) {
      setError("Please fill in all fields.");
      return;
    }

    const result = await login(form);

    if (!result.ok) {
      setError(result.error);
      return;
    }

    navigate("/dashboard", { replace: true });
  }

  return (
    <AuthLayout title="Welcome back" subtitle="Log in to manage your food items.">
      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          name="username"
          placeholder="Username"
          value={form.username}
          onChange={handleChange}
          className="w-full rounded-lg px-3 py-2 bg-slate-900 text-white border border-slate-700"
        />

        <input
          name="password"
          type="password"
          placeholder="Password"
          value={form.password}
          onChange={handleChange}
          className="w-full rounded-lg px-3 py-2 bg-slate-900 text-white border border-slate-700"
        />

        {error && <div className="text-red-400 text-sm">{error}</div>}

        <button className="w-full bg-amber-400 text-black font-medium py-2 rounded-lg">
          Login
        </button>
      </form>

      <div className="pt-3 text-center text-sm text-muted">
        Don&apos;t have an account?{" "}
        <Link to="/signup" className="text-amber-400 hover:text-amber-300 font-medium">
          Sign Up
        </Link>
      </div>
    </AuthLayout>
  );
}
