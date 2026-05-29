export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export async function apiRequest(path, { method = 'GET', body, token, credentials } = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    credentials,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  })
  const payload = await response.json().catch(() => null)

  if (!response.ok || payload?.success === false) {
    throw new Error(payload?.message ?? 'Request failed')
  }

  return payload?.data
}
