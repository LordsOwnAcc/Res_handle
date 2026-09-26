import { Routes, Route } from "react-router-dom";
import { NavShell } from "./components/NavShell";
import DashboardPage from "./pages/DashboardPage";
import ResumesPage from "./pages/ResumesPage";
import ResumeDetailPage from "./pages/ResumeDetailPage";
import NewResumePage from "./pages/NewResumePage";
import FindBestResumePage from "./pages/FindBestResumePage";
import ApplicationsPage from "./pages/ApplicationsPage";

export default function App() {
  return (
    <NavShell>
      <Routes>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/resumes" element={<ResumesPage />} />
        <Route path="/resumes/new" element={<NewResumePage />} />
        <Route path="/resumes/:id" element={<ResumeDetailPage />} />
        <Route path="/jobs" element={<FindBestResumePage />} />
        <Route path="/applications" element={<ApplicationsPage />} />
      </Routes>
    </NavShell>
  );
}
