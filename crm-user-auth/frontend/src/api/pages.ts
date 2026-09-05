import axios from '@/utils/request'
import type { ModuleConfig } from '@/types'

export function getPageContent(pageCode: string) {
  return axios.get(`/api/pages/${pageCode}`)
}

export function savePageContent(pageCode: string, content: string) {
  return axios.put(`/api/pages/${pageCode}`, { content })
}

export function getAllPageContent(pageCode: string) {
  return axios.get(`/api/pages/${pageCode}/all`)
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

export function saveModules(modules: ModuleConfig[]): Promise<void> {
  return savePageContent('PAGE_1', JSON.stringify(modules)).then(() => {})
}

export function loadAllDesigns(): Promise<any[]> {
  return getAllPageContent('PAGE_1').then(r => r.data?.data || [])
}
