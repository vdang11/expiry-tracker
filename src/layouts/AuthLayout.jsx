import React from "react";
import { Link } from "react-router-dom";

export default function AuthLayout({ title, subtitle, children, footer }) {
  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-900">
      <div className="w-full max-w-md bg-slate-800 rounded-2xl p-6 shadow-lg">
        
        {/* Header */}
        <div className="mb-6 text-center">
          <h1 className="text-2xl font-semibold text-white">{title}</h1>
          {subtitle && (
            <p className="mt-2 text-sm text-slate-300">{subtitle}</p>
          )}
        </div>

        {/* Content */}
        <div>{children}</div>

        {/* Footer (optional) */}
        {footer && (
          <div className="mt-6 text-center text-sm text-slate-300">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
}
