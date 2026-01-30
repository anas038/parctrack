import { Outlet, Link, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '@/contexts/AuthContext'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { QrCode, LayoutDashboard, Package, LogOut, Globe, Settings, Users, MapPin, Tag } from 'lucide-react'
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

  const adminNavItems = [
    { path: '/admin/customers', label: t('nav.customers'), icon: Users },
    { path: '/admin/sites', label: t('nav.sites'), icon: MapPin },
    { path: '/admin/equipment-types', label: t('nav.equipmentTypes'), icon: Tag },
  ]

  const visibleNavItems = navItems.filter(
    (item) => !item.managerOnly || user?.role === 'MANAGER' || user?.role === 'ADMIN'
  )

  const isAdmin = user?.role === 'ADMIN'
  const isAdminPath = location.pathname.startsWith('/admin')

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
              {isAdmin && (
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant={isAdminPath ? 'secondary' : 'ghost'} className="gap-2">
                      <Settings className="h-4 w-4" />
                      {t('nav.admin')}
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="start">
                    {adminNavItems.map((item) => (
                      <DropdownMenuItem key={item.path} asChild>
                        <Link to={item.path} className="flex items-center gap-2 cursor-pointer">
                          <item.icon className="h-4 w-4" />
                          {item.label}
                        </Link>
                      </DropdownMenuItem>
                    ))}
                  </DropdownMenuContent>
                </DropdownMenu>
              )}
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
          {isAdmin && (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button
                  variant={isAdminPath ? 'secondary' : 'ghost'}
                  size="sm"
                  className={cn(
                    "flex-col h-auto py-2 gap-1",
                    isAdminPath && "bg-secondary"
                  )}
                >
                  <Settings className="h-5 w-5" />
                  <span className="text-xs">{t('nav.admin')}</span>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                {adminNavItems.map((item) => (
                  <DropdownMenuItem key={item.path} asChild>
                    <Link to={item.path} className="flex items-center gap-2 cursor-pointer">
                      <item.icon className="h-4 w-4" />
                      {item.label}
                    </Link>
                  </DropdownMenuItem>
                ))}
              </DropdownMenuContent>
            </DropdownMenu>
          )}
        </div>
      </nav>

      <main className="container mx-auto px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}
