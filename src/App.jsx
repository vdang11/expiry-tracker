import { BrowserRouter, Routes, Route } from "react-router-dom";
import AppLayout from "./layouts/AppLayout";

import Dashboard from "./pages/Dashboard";
import MenuSuggestions from "./pages/MenuSuggestions";
import AddItem from "./pages/AddItem";
import ItemDetail from "./pages/ItemDetail";
import Notifications from "./pages/Notifications";
import Profile from "./pages/Profile";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={
            <AppLayout>{(search) => <Dashboard search={search} />}</AppLayout>
          }
        />

        <Route
          path="/menu"
          element={
            <AppLayout>
              <MenuSuggestions />
            </AppLayout>
          }
        />
        <Route
          path="/add"
          element={
            <AppLayout>
              <AddItem />
            </AppLayout>
          }
        />
        <Route
          path="/items/:id"
          element={
            <AppLayout>
              <ItemDetail />
            </AppLayout>
          }
        />
        <Route
          path="/notifications"
          element={
            <AppLayout>
              <Notifications />
            </AppLayout>
          }
        />
        <Route
          path="/profile"
          element={
            <AppLayout>
              <Profile />
            </AppLayout>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}
