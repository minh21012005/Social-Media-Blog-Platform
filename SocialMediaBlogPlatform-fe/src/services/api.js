export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export async function apiRequest(path, { method = 'GET', body, token, credentials } = {}) {
  const isFormData = body instanceof FormData
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    credentials,
    headers: {
      ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? (isFormData ? body : JSON.stringify(body)) : undefined,
  })
  const payload = await response.json().catch(() => null)

  if (!response.ok) {
    const error = new Error(payload?.message ?? payload?.error ?? 'Request failed')
    error.status = payload?.status ?? response.status
    error.payload = payload
    throw error
  }

  return payload?.data
}
