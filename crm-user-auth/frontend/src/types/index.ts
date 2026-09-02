export interface UserInfo {
  id: number
  username: string
  realName: string
  role: string
  orgId: number | null
  orgName: string | null
  isAdmin: boolean
  permissions: PagePermission[]
}

export interface PagePermission {
  pageCode: string
  canView: boolean
  canEdit: boolean
}

export interface Org {
  id: number
  name: string
  contactName: string
  contactPhone: string
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateUserRequest {
  username: string
  password?: string
  email?: string
  phone?: string
  realName?: string
  orgId?: number
  role?: string
}

export interface UserDetail extends CreateUserRequest {
  id: number
  orgName?: string
  isActive: boolean
  createdAt: string
  updatedAt: string
  permissions: PagePermission[]
}

export interface PagePermissionItem {
  pageCode: string
  canView: boolean
  canEdit: boolean
}

export interface PageResponse<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

export interface ApiResult<T> {
  code: number
  message: string
  data: T
  timestamp: number
}
