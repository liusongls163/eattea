<template>
  <div class="dashboard">
    <div class="page-header">
      <div>
        <h2>项目管理驾驶舱</h2>
        <p class="subtitle">AI 驱动的项目健康度监控与报告生成</p>
      </div>
      <el-button type="primary" @click="showCreateProject = true">
        <el-icon><Plus /></el-icon> 新建项目
      </el-button>
    </div>

    <!-- 项目卡片 -->
    <div v-loading="loading" class="project-grid">
      <div v-for="p in projects" :key="p.projectId" class="project-card" @click="router.push(`/projects/${p.projectId}`)">
        <div class="card-header">
          <div class="health-dot" :class="p.health || 'gray'"></div>
          <h3>{{ p.projectName }}</h3>
          <el-tag :type="statusTag(p.status)" size="small">{{ statusLabel(p.status) }}</el-tag>
        </div>
        <p class="card-desc">{{ (p.description || '').substring(0, 80) }}{{ (p.description || '').length > 80 ? '…' : '' }}</p>
        <div class="card-stats">
          <div class="stat-item">
            <span class="stat-value">{{ p.completionPct ?? 0 }}%</span>
            <span class="stat-label">完成</span>
          </div>
          <div class="stat-item" v-if="p.overdueTasks > 0">
            <span class="stat-value red">{{ p.overdueTasks }}</span>
            <span class="stat-label">逾期</span>
          </div>
          <div class="stat-item" v-if="p.blockedTasks > 0">
            <span class="stat-value orange">{{ p.blockedTasks }}</span>
            <span class="stat-label">阻塞</span>
          </div>
        </div>
        <div class="card-footer">
          <span>{{ p.startDate }} ~ {{ p.endDate }}</span>
        </div>
      </div>

      <el-empty v-if="!loading && projects.length === 0" description="暂无项目，点击右上角新建" />
    </div>

    <!-- 新建项目对话框 -->
    <el-dialog v-model="showCreateProject" title="新建项目" width="480px">
      <el-form label-width="80px">
        <el-form-item label="项目名称">
          <el-input v-model="form.name" placeholder="如：监管报送平台一期" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateProject = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { getActiveProjectStats, createProject } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const projects = ref([])
const loading = ref(false)
const showCreateProject = ref(false)
const creating = ref(false)

const form = ref({ name: '', description: '', startDate: null, endDate: null })

function statusTag(s) { return s === 'active' ? 'success' : s === 'paused' ? 'warning' : 'info' }
function statusLabel(s) { return s === 'active' ? '进行中' : s === 'paused' ? '已暂停' : '已关闭' }

function formatDate(d) {
  if (!d) return ''
  const dt = new Date(d)
  return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`
}

async function loadData() {
  loading.value = true
  try {
    const res = await getActiveProjectStats()
    projects.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!form.value.name) { ElMessage.warning('请输入项目名称'); return }
  creating.value = true
  try {
    await createProject({
      name: form.value.name,
      description: form.value.description,
      startDate: formatDate(form.value.startDate),
      endDate: formatDate(form.value.endDate)
    })
    ElMessage.success('创建成功')
    showCreateProject.value = false
    form.value = { name: '', description: '', startDate: null, endDate: null }
    loadData()
  } catch (e) {
    ElMessage.error('创建失败')
  } finally {
    creating.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}

.page-header h2 {
  font-size: 22px;
  color: #1a2332;
}

.subtitle {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.project-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.15s;
}

.project-card:hover {
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.health-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.health-dot.green { background: #67c23a; box-shadow: 0 0 0 3px rgba(103,194,58,0.2); }
.health-dot.yellow { background: #e6a23c; box-shadow: 0 0 0 3px rgba(230,162,60,0.2); }
.health-dot.red { background: #f56c6c; box-shadow: 0 0 0 3px rgba(245,108,108,0.2); }
.health-dot.gray { background: #c0c4cc; }

.card-header h3 {
  font-size: 16px;
  flex: 1;
}

.card-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 16px;
  min-height: 20px;
}

.card-stats {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}

.stat-value.red { color: #f56c6c; }
.stat-value.orange { color: #e6a23c; }

.stat-label {
  font-size: 12px;
  color: #909399;
  display: block;
}

.card-footer {
  font-size: 12px;
  color: #c0c4cc;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}
</style>
