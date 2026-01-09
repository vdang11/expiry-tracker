import { Routes, Route } from "react-router-dom";
import AppLayout from "./layouts/AppLayout";

import Dashboard from "./pages/Dashboard";
import MenuSuggestions from "./pages/MenuSuggestions";
import AddItem from "./pages/AddItem";
import ItemDetail from "./pages/ItemDetail";
import Notifications from "./pages/Notifications";
import Profile from "./pages/Profile";

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<Dashboard />} />
        <Route path="menu" element={<MenuSuggestions />} />
        <Route path="add" element={<AddItem />} />
        <Route path="items/:id" element={<ItemDetail />} />
        <Route path="notifications" element={<Notifications />} />
        <Route path="profile" element={<Profile />} />
      </Route>
    </Routes>

  );
}
