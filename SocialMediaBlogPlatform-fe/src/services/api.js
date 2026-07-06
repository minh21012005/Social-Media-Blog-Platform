export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
export const CORRELATION_ID_HEADER = 'X-Correlation-ID'

export async function apiRequest(path, { method = 'GET', body, token, credentials, silent = false } = {}) {
  const isFormData = body instanceof FormData
  const correlationId = createCorrelationId()
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    credentials,
    headers: {
      ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      [CORRELATION_ID_HEADER]: correlationId,
    },
    body: body ? (isFormData ? body : JSON.stringify(body)) : undefined,
  })
  const payload = await response.json().catch(() => null)
  const responseCorrelationId = response.headers.get(CORRELATION_ID_HEADER) ?? correlationId

  if (!response.ok) {
    const error = new Error(payload?.message ?? payload?.error ?? 'Request failed')
    error.status = payload?.status ?? response.status
    error.payload = payload
    error.correlationId = responseCorrelationId
    if (!silent) {
      console.error('API request failed', {
        correlationId: responseCorrelationId,
        method,
        path,
        status: error.status,
        message: error.message,
      })
    }
    throw error
  }

  return payload?.data
}

function createCorrelationId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 12)}`
}
