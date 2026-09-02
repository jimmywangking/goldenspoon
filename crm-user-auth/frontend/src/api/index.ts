import axios from '@/utils/request'
import type { Org, UserDetail, PageResponse, PagePermissionItem, CreateUserRequest, PageInstance } from '@/types'

export const orgApi = {
  list: (params?: any) => axios.get<PageResponse<Org>>('/api/orgs', { params }),
  getById: (id: number) => axios.get<Org>(`/api/orgs/${id}`),
  create: (data: { name: string; contactName?: string; contactPhone?: string }) => axios.post<Org>('/api/orgs', data),
  update: (id: number, data: Partial<Org>) => axios.put<Org>(`/api/orgs/${id}`, data),
  delete: (id: number) => axios.delete(`/api/orgs/${id}`)
}

export const instanceApi = {
  list: (params?: { pageCode?: string; current?: number; size?: number }) =>
    axios.get<PageResponse<PageInstance>>('/api/instances', { params }),
  getById: (id: number) => axios.get<PageInstance>(`/api/instances/${id}`),
  create: (data: { pageCode: string; title?: string; content: string }) =>
    axios.post<PageInstance>('/api/instances', data),
  update: (id: number, data: { title?: string; content?: string }) =>
    axios.put<void>(`/api/instances/${id}`, data),
  delete: (id: number) => axios.delete<void>(`/api/instances/${id}`),
  adminList: (params?: { pageCode?: string; current?: number; size?: number }) =>
    axios.get<PageResponse<PageInstance>>('/api/instances/admin/all', { params })
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
