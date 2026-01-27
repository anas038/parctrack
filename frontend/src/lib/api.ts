const API_BASE = '/api'

interface ApiError {
  code: string
  message: string
}

class ApiClient {
  private accessToken: string | null = null

  setAccessToken(token: string | null) {
    this.accessToken = token
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...options.headers,
    }

    if (this.accessToken) {
      headers['Authorization'] = `Bearer ${this.accessToken}`
    }

    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers,
    })

    if (!response.ok) {
      const error: ApiError = await response.json().catch(() => ({
        code: 'UNKNOWN_ERROR',
        message: 'An unknown error occurred',
      }))
      throw new Error(error.message)
    }

    if (response.status === 204) {
      return undefined as T
    }

    return response.json()
  }

  get<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET' })
  }

  post<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  put<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  delete<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' })
  }

  async upload<T>(endpoint: string, file: File, params?: Record<string, string>): Promise<T> {
    const formData = new FormData()
    formData.append('file', file)

    const queryString = params ? '?' + new URLSearchParams(params).toString() : ''

    const headers: HeadersInit = {}
    if (this.accessToken) {
      headers['Authorization'] = `Bearer ${this.accessToken}`
    }

    const response = await fetch(`${API_BASE}${endpoint}${queryString}`, {
      method: 'POST',
      headers,
      body: formData,
    })

    if (!response.ok) {
      const error: ApiError = await response.json().catch(() => ({
        code: 'UNKNOWN_ERROR',
        message: 'An unknown error occurred',
      }))
      throw new Error(error.message)
    }

    return response.json()
  }

  async download(endpoint: string, params?: Record<string, string>): Promise<Blob> {
    const queryString = params ? '?' + new URLSearchParams(params).toString() : ''

    const headers: HeadersInit = {}
    if (this.accessToken) {
      headers['Authorization'] = `Bearer ${this.accessToken}`
    }

    const response = await fetch(`${API_BASE}${endpoint}${queryString}`, {
      method: 'GET',
      headers,
    })

    if (!response.ok) {
      throw new Error('Download failed')
    }

    return response.blob()
  }
}

export const api = new ApiClient()
