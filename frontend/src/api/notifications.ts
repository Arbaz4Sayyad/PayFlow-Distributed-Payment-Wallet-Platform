import { apiClient } from './client';
import { ApiResponse, NotificationItem } from '../types';

export async function getNotifications(): Promise<NotificationItem[]> {
  const response = await apiClient.get<ApiResponse<NotificationItem[]>>('/v1/notifications');
  return response.data.data;
}

export async function markNotificationAsRead(id: string): Promise<void> {
  await apiClient.patch(`/v1/notifications/${id}/read`);
}

export async function markAllNotificationsAsRead(): Promise<void> {
  await apiClient.post('/v1/notifications/read-all');
}
