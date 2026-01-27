import { Outlet, Link, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '@/contexts/AuthContext'
import { Button } from '@/components/ui/button'
import { QrCode, LayoutDashboard, Package, LogOut, Globe } from 'lucide-react'
import { cn } from '@/lib/utils'

export function Layout() {
  const { t, i18n } = useTranslation()
  const { user, logout } = useAuth()
  const location = useLocation()

  const toggleLanguage = () => {
    i18n.changeLanguage(i18n.language === 'en' ? 'fr' : 'en')
  }

  const navItems = [
    { path: '/scanner', label: t('nav.scanner'), icon: QrCode },
    { path: '/dashboard', label: t('nav.dashboard'), icon: LayoutDashboard, managerOnly: true },
    { path: '/equipment', label: t('nav.equipment'), icon: Package, managerOnly: true },
  ]

  const visibleNavItems = navItems.filter(
    (item) => !item.managerOnly || user?.role === 'MANAGER' || user?.role === 'ADMIN'
  )

  return (
    <div className="min-h-screen bg-background">
      <header className="border-b bg-card">
        <div className="container mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-6">
            <Link to="/" className="text-xl font-bold text-primary">
              ParcTrack
            </Link>
            <nav className="hidden md:flex items-center gap-1">
              {visibleNavItems.map((item) => (
                <Link key={item.path} to={item.path}>
                  <Button
                    variant={location.pathname === item.path ? 'secondary' : 'ghost'}
                    className="gap-2"
                  >
                    <item.icon className="h-4 w-4" />
                    {item.label}
                  </Button>
                </Link>
              ))}
            </nav>
          </div>
          <div className="flex items-center gap-4">
            <span className="text-sm text-muted-foreground hidden sm:inline">
              {user?.username} ({user?.organizationName})
            </span>
            <Button variant="ghost" size="icon" onClick={toggleLanguage}>
              <Globe className="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon" onClick={logout}>
              <LogOut className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </header>

      {/* Mobile navigation */}
      <nav className="md:hidden border-b bg-card">
        <div className="container mx-auto px-4 py-2 flex justify-around">
          {visibleNavItems.map((item) => (
            <Link key={item.path} to={item.path}>
              <Button
                variant={location.pathname === item.path ? 'secondary' : 'ghost'}
                size="sm"
                className={cn(
                  "flex-col h-auto py-2 gap-1",
                  location.pathname === item.path && "bg-secondary"
                )}
              >
                <item.icon className="h-5 w-5" />
                <span className="text-xs">{item.label}</span>
              </Button>
            </Link>
          ))}
        </div>
      </nav>

      <main className="container mx-auto px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}
