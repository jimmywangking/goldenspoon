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

export interface PageInstance {
  id: number
  userId: number
  pageCode: string
  title: string | null
  content: string
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export interface ModuleConfig {
  id: string
  name: string
  position: { x: number; y: number; z: number }
  rotation: { x: number; y: number; z: number }
  scale: { x: number; y: number; z: number }
  color: string
}

export interface ModuleTemplate {
  type: string
  name: string
  color: string
  sizeX: number
  sizeY: number
  sizeZ: number
}

export interface DesignExport {
  version: string
  type: string
  exportTime: string
  modules: ModuleConfig[]
}

export const MODULE_TEMPLATES: ModuleTemplate[] = [
  { type: 'wall', name: '墙体模块', color: '#E8E8E8', sizeX: 4, sizeY: 3, sizeZ: 0.2 },
  { type: 'floor', name: '地板模块', color: '#D4A574', sizeX: 4, sizeY: 0.15, sizeZ: 4 },
  { type: 'roof', name: '屋顶模块', color: '#C0392B', sizeX: 4.5, sizeY: 0.2, sizeZ: 4.5 },
  { type: 'room', name: '房间模块', color: '#3498DB', sizeX: 4, sizeY: 3, sizeZ: 4 },
  { type: 'garage', name: '车库模块', color: '#9B59B6', sizeX: 5, sizeY: 3, sizeZ: 4 },
  { type: 'extension', name: '扩展模块', color: '#27AE60', sizeX: 3, sizeY: 3, sizeZ: 3 },
]
