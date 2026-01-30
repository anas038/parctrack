import { useState, useEffect, useCallback } from 'react'
import { cache, formatCacheAge } from '@/lib/cache'

interface UseSessionCacheOptions<T> {
  key: string
  fetcher: () => Promise<T>
  ttlMs?: number
}

interface UseSessionCacheResult<T> {
  data: T | null
  loading: boolean
  error: Error | null
  isOffline: boolean
  isStale: boolean
  lastUpdated: string | null
  refresh: () => Promise<void>
}

export function useSessionCache<T>({
  key,
  fetcher,
  ttlMs = 60 * 60 * 1000, // 1 hour default
}: UseSessionCacheOptions<T>): UseSessionCacheResult<T> {
  const [data, setData] = useState<T | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<Error | null>(null)
  const [isOffline, setIsOffline] = useState(!navigator.onLine)
  const [isStale, setIsStale] = useState(false)
  const [lastUpdated, setLastUpdated] = useState<string | null>(null)

  // Monitor online status
  useEffect(() => {
    const handleOnline = () => {
      setIsOffline(false)
      // Refresh when coming back online
      refresh()
    }
    const handleOffline = () => setIsOffline(true)

    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)

    return () => {
      window.removeEventListener('online', handleOnline)
      window.removeEventListener('offline', handleOffline)
    }
  }, [])

  const updateFromCache = useCallback(() => {
    const cached = cache.getWithMeta<T>(key)
    if (cached) {
      setData(cached.data)
      setIsStale(cached.isStale)
      setLastUpdated(formatCacheAge(Date.now() - cached.timestamp))
      return true
    }
    return false
  }, [key])

  const refresh = useCallback(async () => {
    if (isOffline) {
      // When offline, try to use cached data
      updateFromCache()
      setLoading(false)
      return
    }

    setLoading(true)
    setError(null)

    try {
      const freshData = await fetcher()
      cache.set(key, freshData, ttlMs)
      setData(freshData)
      setIsStale(false)
      setLastUpdated('just now')
    } catch (e) {
      const err = e instanceof Error ? e : new Error('Unknown error')
      setError(err)

      // Fall back to cached data on error
      if (!updateFromCache()) {
        setData(null)
      }
    } finally {
      setLoading(false)
    }
  }, [key, fetcher, ttlMs, isOffline, updateFromCache])

  // Initial load
  useEffect(() => {
    // First, try to load from cache for instant display
    const hasCached = updateFromCache()

    if (hasCached && !isStale) {
      setLoading(false)
    }

    // Then fetch fresh data
    refresh()
  }, [key])

  // Update lastUpdated periodically
  useEffect(() => {
    const interval = setInterval(() => {
      const age = cache.getAge(key)
      if (age !== null) {
        setLastUpdated(formatCacheAge(age))
      }
    }, 60000) // Update every minute

    return () => clearInterval(interval)
  }, [key])

  return {
    data,
    loading,
    error,
    isOffline,
    isStale,
    lastUpdated,
    refresh,
  }
}
