import { apiRequest } from './api'

/**
 * Lấy danh sách notification của user đang đăng nhập.
 * @param {string} token - JWT access token
 * @returns {Promise<Array>} danh sách notification
 */
export async function getMyNotifications(token) {
  return apiRequest('/api/v1/notifications/me', { token })
}

/**
 * Đánh dấu một notification là đã đọc.
 * @param {string} notificationId - UUID của notification
 * @param {string} token - JWT access token
 * @returns {Promise<Object>} notification đã được cập nhật
 */
export async function markNotificationRead(notificationId, token) {
  return apiRequest(`/api/v1/notifications/${notificationId}/read`, {
    method: 'PATCH',
    token,
  })
}
