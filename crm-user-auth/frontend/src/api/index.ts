import axios from '@/utils/request'
import type { Org, UserDetail, PageResponse, PagePermissionItem, CreateUserRequest } from '@/types'

export const orgApi = {
  list: (params?: any) => axios.get<PageResponse<Org>>('/api/orgs', { params }),
  getById: (id: number) => axios.get<Org>(`/api/orgs/${id}`),
  create: (data: { name: string; contactName?: string; contactPhone?: string }) => axios.post<Org>('/api/orgs', data),
  update: (id: number, data: Partial<Org>) => axios.put<Org>(`/api/orgs/${id}`, data),
  delete: (id: number) => axios.delete(`/api/orgs/${id}`)
}

export const userApi = {
  list: (params?: any) => axios.get<PageResponse<UserDetail>>('/api/users', { params }),
  getById: (id: number) => axios.get<UserDetail>(`/api/users/${id}`),
  create: (data: CreateUserRequest) => axios.post<UserDetail>('/api/users', data),
  update: (id: number, data: Partial<CreateUserRequest>) => axios.put<UserDetail>(`/api/users/${id}`, data),
  delete: (id: number) => axios.delete(`/api/users/${id}`),
  resetPassword: (id: number, newPassword: string) => axios.post(`/api/users/${id}/reset-password`, { newPassword }),
  getPermissions: (userId: number) => axios.get(`/api/users/${userId}/permissions`),
  setPermissions: (userId: number, permissions: PagePermissionItem[]) =>
    axios.put(`/api/users/${userId}/permissions`, { userId, permissions })
}
