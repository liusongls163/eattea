import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 全局搜索
export function searchAll(keyword) {
  return api.get('/documents/search', { params: { keyword } })
}

// 文档相关
export function getDocuments(params) {
  return api.get('/documents', { params })
}

export function getDocument(id) {
  return api.get(`/documents/${id}`)
}

export function uploadDocument(formData) {
  // 不手动设置 Content-Type，让浏览器/axios 自动生成带 boundary 的 multipart/form-data
  return api.post('/documents/upload', formData)
}

export function updateDocument(id, data) {
  return api.put(`/documents/${id}`, data)
}

export function deleteDocument(id) {
  return api.delete(`/documents/${id}`)
}

// 知识库相关
export function getKnowledgeList(category) {
  return api.get('/knowledge', { params: { category } })
}

export function getKnowledgeEntry(id) {
  return api.get(`/knowledge/${id}`)
}

export function createKnowledgeEntry(data) {
  return api.post('/knowledge', data)
}

export function updateKnowledgeEntry(id, data) {
  return api.put(`/knowledge/${id}`, data)
}

export function deleteKnowledgeEntry(id) {
  return api.delete(`/knowledge/${id}`)
}

export function getKnowledgeCategories() {
  return api.get('/knowledge/categories')
}

export function importKnowledgeEntries(formData) {
  return api.post('/knowledge/import', formData)
}
