const CACHE_PREFIX = 'parctrack_cache_'
const DEFAULT_TTL_MS = 60 * 60 * 1000 // 1 hour

interface CacheEntry<T> {
  data: T
  timestamp: number
  expiresAt: number
}

export const cache = {
  set<T>(key: string, data: T, ttlMs: number = DEFAULT_TTL_MS): void {
    const entry: CacheEntry<T> = {
      data,
      timestamp: Date.now(),
      expiresAt: Date.now() + ttlMs,
    }
    try {
      localStorage.setItem(CACHE_PREFIX + key, JSON.stringify(entry))
    } catch (e) {
      console.warn('Failed to write to cache:', e)
    }
  },

  get<T>(key: string): T | null {
    try {
      const raw = localStorage.getItem(CACHE_PREFIX + key)
      if (!raw) return null

      const entry: CacheEntry<T> = JSON.parse(raw)

      // Check if expired
      if (Date.now() > entry.expiresAt) {
        this.remove(key)
        return null
      }

      return entry.data
    } catch (e) {
      console.warn('Failed to read from cache:', e)
      return null
    }
  },

  getWithMeta<T>(key: string): { data: T; timestamp: number; isStale: boolean } | null {
    try {
      const raw = localStorage.getItem(CACHE_PREFIX + key)
      if (!raw) return null

      const entry: CacheEntry<T> = JSON.parse(raw)
      const isStale = Date.now() > entry.expiresAt

      return {
        data: entry.data,
        timestamp: entry.timestamp,
        isStale,
      }
    } catch (e) {
      console.warn('Failed to read from cache:', e)
      return null
    }
  },

  remove(key: string): void {
    localStorage.removeItem(CACHE_PREFIX + key)
  },

  clear(): void {
    const keys = Object.keys(localStorage).filter((k) => k.startsWith(CACHE_PREFIX))
    keys.forEach((k) => localStorage.removeItem(k))
  },

  getAge(key: string): number | null {
    try {
      const raw = localStorage.getItem(CACHE_PREFIX + key)
      if (!raw) return null

      const entry: CacheEntry<unknown> = JSON.parse(raw)
      return Date.now() - entry.timestamp
    } catch (e) {
      return null
    }
  },
}

export function formatCacheAge(ageMs: number): string {
  const minutes = Math.floor(ageMs / 60000)
  if (minutes < 1) return 'just now'
  if (minutes === 1) return '1 minute ago'
  if (minutes < 60) return `${minutes} minutes ago`
  const hours = Math.floor(minutes / 60)
  if (hours === 1) return '1 hour ago'
  return `${hours} hours ago`
}
