import React from "react";
import { NavLink } from "react-router-dom";

function NavItem({ to, icon, label }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) => (isActive ? "navitem active" : "navitem")}
      end={to === "/"}
    >
      <span className="icon">{icon}</span>
      <span style={{ fontSize: 12 }}>{label}</span>
    </NavLink>
  );
}

export default function BottomNav() {
  return (
    <nav className="bottomnav">
      <div className="bottomnav-inner">
        <NavItem to="/" icon="🏠" label="Home" />
        <NavItem to="/menu" icon="🍳" label="Menu" />
        <NavItem to="/notifications" icon="🔔" label="Alerts" />
        <NavItem to="/profile" icon="⚙️" label="Profile" />
      </div>
    </nav>
  );
}
