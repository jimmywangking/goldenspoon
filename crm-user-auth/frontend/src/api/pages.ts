import axios from '@/utils/request'
import type { ModuleConfig } from '@/types'

const STORAGE_KEY = 'page1_design_modules'

export function getPageContent(pageCode: string) {
  return axios.get(`/api/pages/${pageCode}`)
}

export function savePageContent(pageCode: string, content: string, versionName?: string) {
  return axios.post(`/api/pages/${pageCode}`, { content, versionName })
}

export function getAllPageContent(pageCode: string) {
  return axios.get(`/api/pages/${pageCode}/all`)
}

export function getPageVersions(pageCode: string) {
  return axios.get(`/api/pages/${pageCode}/versions`)
}

export function restorePageVersion(pageCode: string, targetId: number) {
  return axios.post(`/api/pages/${pageCode}/versions/${targetId}/restore`, {})
}

export function getModules(): Promise<ModuleConfig[]> {
  return getPageContent('PAGE_1').then(r => {
    const content = r.data?.data || ''
    if (!content) return []
    try {
      return JSON.parse(content)
    } catch {
      return []
    }
  })
}

export function saveModules(modules: ModuleConfig[], versionName?: string): Promise<void> {
  return savePageContent('PAGE_1', JSON.stringify(modules), versionName).then(() => {})
}

export function loadAllDesigns(): Promise<any[]> {
  return getAllPageContent('PAGE_1').then(r => r.data?.data || [])
}

export function loadVersions(): Promise<any[]> {
  return getPageVersions('PAGE_1').then(r => r.data?.data || [])
}

export function restoreVersion(targetId: number): Promise<void> {
  return restorePageVersion('PAGE_1', targetId).then(() => {})
}

export function saveToLocalStorage(modules: ModuleConfig[]) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(modules))
  } catch {}
}

export function loadFromLocalStorage(): ModuleConfig[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return []
    return JSON.parse(raw)
  } catch {
    return []
  }
}

export function clearLocalStorage() {
  localStorage.removeItem(STORAGE_KEY)
}
