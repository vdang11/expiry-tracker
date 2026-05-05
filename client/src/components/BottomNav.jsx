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
            end={to === "/"} // 🔥 fix active bug
            aria-label={label}
            className={({ isActive }) =>
              `flex flex-col items-center gap-1 text-xs transition px-2 py-1 rounded-lg
               ${
                 isActive
                   ? "text-accent bg-accent/10 scale-105"
                   : "text-muted hover:text-white"
               }`
            }
          >
            <Icon className="h-5 w-5 sm:h-6 sm:w-6" />
            {label}
          </NavLink>
        ))}
      </div>
    </nav>
  );
}