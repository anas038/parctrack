import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './contexts/AuthContext'
import { Layout } from './components/layout/Layout'
import { LoginPage } from './pages/LoginPage'
import { ScannerPage } from './pages/ScannerPage'
import { DashboardPage } from './pages/DashboardPage'
import { EquipmentPage } from './pages/EquipmentPage'
import { MagicLinkPage } from './pages/MagicLinkPage'
import { CustomersPage } from './pages/admin/CustomersPage'
import { SitesPage } from './pages/admin/SitesPage'
import { EquipmentTypesPage } from './pages/admin/EquipmentTypesPage'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}

function AdminRoute({ children }: { children: React.ReactNode }) {
  const { user, isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  if (user?.role !== 'ADMIN') {
    return <Navigate to="/dashboard" replace />
  }

  return <>{children}</>
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/auth/magic-link" element={<MagicLinkPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/scanner" replace />} />
        <Route path="scanner" element={<ScannerPage />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="equipment" element={<EquipmentPage />} />
        <Route
          path="admin/customers"
          element={
            <AdminRoute>
              <CustomersPage />
            </AdminRoute>
          }
        />
        <Route
          path="admin/sites"
          element={
            <AdminRoute>
              <SitesPage />
            </AdminRoute>
          }
        />
        <Route
          path="admin/equipment-types"
          element={
            <AdminRoute>
              <EquipmentTypesPage />
            </AdminRoute>
          }
        />
      </Route>
    </Routes>
  )
}

export default App
