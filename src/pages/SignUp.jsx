import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthLayout from "../layouts/AuthLayout";
import { signup } from "../api/authApi";

export default function SignUp() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: "",
    password: "",
    confirmPassword: "",
  });
  const [error, setError] = useState("");

  function handleChange(e) {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");

    if (!form.username || !form.password || !form.confirmPassword) {
      setError("Please fill in all fields.");
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }



    const result = signup({
      username: form.username,
      password: form.password,
    });

    if (!result.ok) {
      setError(result.error);
      return;
    }

    navigate("/login");
  }

  return (
    <AuthLayout
      title="Create Account"
      subtitle="Track and manage your food items effortlessly."
    >
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

        <input
          name="confirmPassword"
          type="password"
          placeholder="Confirm Password"
          value={form.confirmPassword}
          onChange={handleChange}
          className="w-full rounded-lg px-3 py-2 bg-slate-900 text-white border border-slate-700"
        />

        {error && <div className="text-red-400 text-sm">{error}</div>}

        <button className="w-full bg-amber-400 text-black font-medium py-2 rounded-lg">
          Sign Up
        </button>
      </form>

      <div className="pt-3 text-center text-sm text-muted">
        Already have an account?{" "}
        <Link to="/login" className="text-amber-400 hover:text-amber-300 font-medium">
          Login
        </Link>
      </div>
    </AuthLayout>
  );
}
