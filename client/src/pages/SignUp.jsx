import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthLayout from "../layouts/AuthLayout";
import { api } from "../api/apiClient";

export default function SignUp() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    email: "",
    password: "",
    confirmPassword: "",
  });

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  function handleChange(e) {
    const { name, value } = e.target;

    setForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (loading) return;

    setError("");

    if (!form.email || !form.password || !form.confirmPassword) {
      setError("Please fill in all fields.");
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    try {
      setLoading(true);

      await api.signup({
        email: form.email,
        name: form.email,
        password: form.password,
      });

      navigate("/login");
    } catch (err) {
      console.error(err);
      setError(err.message || "Cannot connect to server.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout
      title="Create Account"
      subtitle="Track and manage your food items effortlessly."
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          name="email"
          type="email"
          placeholder="Email"
          value={form.email}
          onChange={handleChange}
          className="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-white"
        />

        <input
          name="password"
          type="password"
          placeholder="Password"
          value={form.password}
          onChange={handleChange}
          className="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-white"
        />

        <input
          name="confirmPassword"
          type="password"
          placeholder="Confirm Password"
          value={form.confirmPassword}
          onChange={handleChange}
          className="w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-white"
        />

        {error && <div className="text-sm text-red-400">{error}</div>}

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-lg bg-amber-400 py-2 font-medium text-black disabled:opacity-60"
        >
          {loading ? "Signing Up..." : "Sign Up"}
        </button>
      </form>

      <div className="pt-3 text-center text-sm text-muted">
        Already have an account?{" "}
        <Link
          to="/login"
          className="font-medium text-amber-400 hover:text-amber-300"
        >
          Login
        </Link>
      </div>
    </AuthLayout>
  );
}