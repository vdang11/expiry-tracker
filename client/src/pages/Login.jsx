import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AuthLayout from "../layouts/AuthLayout";

export default function Login() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    email: "",
    password: ""
  });

  const [error, setError] = useState("");

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleLogin(e) {
    e.preventDefault();
    setError("");

    try {
      const res = await fetch("http://localhost:8080/api/users/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          email: form.email,
          password: form.password
        })
      });

      const user = await res.json();

      if (!res.ok) {
        setError(user.message || "Login failed");
        return;
      }

      // ✅ lưu đúng format
      localStorage.setItem("currentUser", JSON.stringify({
        id: user.id,
        email: user.email
      }));

      window.location.replace("/#/dashboard"); 
    } catch (err) {
      console.error(err);
      setError("Cannot connect to server");
    }
  }

  return (
    <AuthLayout title="Welcome back" subtitle="Log in to manage your food items.">
      <form onSubmit={handleLogin} className="space-y-4">

        <input
          name="email"
          placeholder="Email"
          value={form.email}
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