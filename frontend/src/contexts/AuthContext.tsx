import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { api } from '@/lib/api'
import type { User, AuthResponse } from '@/types'

interface AuthContextType {
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (email: string, password: string, extendedSession?: boolean) => Promise<void>
  logout: () => Promise<void>
  requestMagicLink: (email: string) => Promise<void>
  verifyMagicLink: (token: string) => Promise<void>
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const accessToken = localStorage.getItem('accessToken')
    const refreshToken = localStorage.getItem('refreshToken')
    const storedUser = localStorage.getItem('user')

    if (accessToken && storedUser) {
      api.setAccessToken(accessToken)
      setUser(JSON.parse(storedUser))
    }

    if (refreshToken && !accessToken) {
      refreshAccessToken(refreshToken)
    } else {
      setIsLoading(false)
    }
  }, [])

  const refreshAccessToken = async (refreshToken: string) => {
    try {
      const response = await api.post<AuthResponse>('/auth/refresh', { refreshToken })
      handleAuthSuccess(response)
    } catch {
      clearAuth()
    } finally {
      setIsLoading(false)
    }
  }

  const handleAuthSuccess = (response: AuthResponse) => {
    localStorage.setItem('accessToken', response.accessToken)
    localStorage.setItem('refreshToken', response.refreshToken)
    localStorage.setItem('user', JSON.stringify(response.user))
    api.setAccessToken(response.accessToken)
    setUser(response.user)
  }

  const clearAuth = () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    api.setAccessToken(null)
    setUser(null)
  }

  const login = async (email: string, password: string, extendedSession = false) => {
    const response = await api.post<AuthResponse>('/auth/login', {
      email,
      password,
      extendedSession,
    })
    handleAuthSuccess(response)
  }

  const logout = async () => {
    const refreshToken = localStorage.getItem('refreshToken')
    try {
      await api.post('/auth/logout', { refreshToken })
    } catch {
      // Ignore errors on logout
    }
    clearAuth()
  }

  const requestMagicLink = async (email: string) => {
    await api.post('/auth/magic-link/request', { email })
  }

  const verifyMagicLink = async (token: string) => {
    const response = await api.get<AuthResponse>(`/auth/magic-link/verify?token=${token}`)
    handleAuthSuccess(response)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        requestMagicLink,
        verifyMagicLink,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
