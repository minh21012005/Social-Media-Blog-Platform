import { apiRequest } from './api'

export async function getPresenceStatus(userIds) {
  if (!userIds || userIds.length === 0) {
    return {}
  }
  const query = userIds.join(',')
  return apiRequest(`/api/v1/presence?userIds=${query}`, { method: 'GET' })
}
