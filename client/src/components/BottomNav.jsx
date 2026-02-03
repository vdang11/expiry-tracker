import { NavLink } from "react-router-dom";
import {
  HomeIcon,
  BookOpenIcon,
  BellIcon,
  UserIcon,
} from "@heroicons/react/24/outline";

const tabs = [
  { to: "/", label: "Home", icon: HomeIcon },
  { to: "/menu", label: "Menu", icon: BookOpenIcon },
  { to: "/notifications", label: "Alerts", icon: BellIcon },
  { to: "/profile", label: "Profile", icon: UserIcon },
];

export default function BottomNav() {
  return (
    <nav className="fixed bottom-0 left-0 right-0 border-t border-line bg-bg/90 backdrop-blur">
      <div className="mx-auto flex max-w-3xl justify-around py-2">
        {tabs.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `flex flex-col items-center gap-1 text-xs transition
               ${isActive ? "text-accent" : "text-muted hover:text-white"}`
            }
          >
            <Icon className="h-6 w-6" />
            {label}
          </NavLink>
        ))}
      </div>
    </nav>
  );
}
