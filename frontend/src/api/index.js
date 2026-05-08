import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000
})

// ========== 项目 ==========
export function getProjects(status) {
  return api.get('/projects', { params: { status } })
}

export function getProject(id) {
  return api.get(`/projects/${id}`)
}

export function getProjectStats(id) {
  return api.get(`/projects/${id}/stats`)
}

export function getActiveProjectStats() {
  return api.get('/projects/stats/active')
}

export function createProject(data) {
  return api.post('/projects', data)
}

export function updateProject(id, data) {
  return api.put(`/projects/${id}`, data)
}

export function deleteProject(id) {
  return api.delete(`/projects/${id}`)
}

// ========== 任务 ==========
export function getTasks(projectId) {
  return api.get('/tasks', { params: { projectId } })
}

export function getTask(id) {
  return api.get(`/tasks/${id}`)
}

export function createTask(data) {
  return api.post('/tasks', data)
}

export function updateTask(id, data) {
  return api.put(`/tasks/${id}`, data)
}

export function deleteTask(id) {
  return api.delete(`/tasks/${id}`)
}

export function importTasks(formData) {
  return api.post('/tasks/import', formData)
}

export function getMemberLoad(projectId) {
  return api.get('/tasks/load', { params: { projectId } })
}

// ========== 成员 ==========
export function getMembers() {
  return api.get('/members')
}

export function createMember(data) {
  return api.post('/members', data)
}

export function updateMember(id, data) {
  return api.put(`/members/${id}`, data)
}

export function deleteMember(id) {
  return api.delete(`/members/${id}`)
}

// ========== AI 诊断 ==========
export function runHealthCheck(projectId) {
  return api.post(`/ai/health-check/${projectId}`)
}

export function getLatestHealthCheck(projectId) {
  return api.get(`/ai/health-check/${projectId}`)
}

// ========== AI 报告 ==========
export function generateReport(projectId, type) {
  return api.post(`/ai/report/${projectId}?type=${type || 'weekly'}`)
}

export function getReports(projectId, type) {
  return api.get(`/ai/reports/${projectId}`, { params: { type } })
}

export function getReportDetail(id) {
  return api.get(`/ai/report/detail/${id}`)
}

export function deleteReport(id) {
  return api.delete(`/ai/report/${id}`)
}

// ========== 分析 ==========
export function getBurnChart(projectId) {
  return api.get(`/analytics/burn-chart/${projectId}`)
}

export function getDelayPrediction(projectId) {
  return api.get(`/analytics/delay-prediction/${projectId}`)
}

export function getBlockedImpact(projectId) {
  return api.get(`/analytics/blocked-impact/${projectId}`)
}

export function getHourDeviations(projectId) {
  return api.get(`/analytics/hour-deviation/${projectId}`)
}

// ========== 里程碑 ==========
export function getMilestones(projectId) {
  return api.get('/milestones', { params: { projectId } })
}

export function createMilestone(data) {
  return api.post('/milestones', data)
}

export function updateMilestone(id, data) {
  return api.put(`/milestones/${id}`, data)
}

export function deleteMilestone(id) {
  return api.delete(`/milestones/${id}`)
}

// ========== 干系人 ==========
export function getStakeholders(projectId) {
  return api.get('/stakeholders', { params: { projectId } })
}

export function createStakeholder(data) {
  return api.post('/stakeholders', data)
}

export function updateStakeholder(id, data) {
  return api.put(`/stakeholders/${id}`, data)
}

export function deleteStakeholder(id) {
  return api.delete(`/stakeholders/${id}`)
}

// ========== 风险 ==========
export function getRisks(projectId) {
  return api.get('/risks', { params: { projectId } })
}

export function createRisk(data) {
  return api.post('/risks', data)
}

export function updateRisk(id, data) {
  return api.put(`/risks/${id}`, data)
}

export function deleteRisk(id) {
  return api.delete(`/risks/${id}`)
}
