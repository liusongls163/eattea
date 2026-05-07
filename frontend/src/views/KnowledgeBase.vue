<template>
  <div class="knowledge-page">
    <div class="page-header">
      <h2>金融业务知识库</h2>
      <div>
        <el-upload
          :auto-upload="false"
          :show-file-list="false"
          accept=".xls,.xlsx"
          :on-change="handleImportFile"
          style="display: inline-block; margin-right: 10px;"
        >
          <el-button type="success">
            <el-icon><Upload /></el-icon>
            批量导入
          </el-button>
        </el-upload>
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>
          新增词条
        </el-button>
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="category-bar">
      <el-radio-group v-model="activeCategory" @change="loadData">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button
          v-for="cat in categories"
          :key="cat"
          :value="cat"
        >{{ cat }}</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 词条列表 -->
    <div class="entry-list" v-loading="loading">
      <el-card
        v-for="entry in entries"
        :key="entry.id"
        class="entry-card"
        shadow="hover"
      >
        <template #header>
          <div class="entry-header">
            <span class="entry-term">{{ entry.term }}</span>
            <el-tag size="small">{{ entry.category }}</el-tag>
          </div>
        </template>
        <p class="entry-definition">{{ entry.definition }}</p>
        <div class="entry-meta" v-if="entry.source">
          <span>出处：{{ entry.source }}</span>
        </div>
        <div class="entry-actions">
          <el-button text type="primary" size="small" @click="openEdit(entry)">编辑</el-button>
          <el-popconfirm title="确认删除？" @confirm="handleDelete(entry.id)">
            <template #reference>
              <el-button text type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </el-card>

      <el-empty v-if="!loading && entries.length === 0" description="暂无词条" />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="isEdit ? '编辑词条' : '新增词条'"
      width="560px"
    >
      <el-form label-width="80px">
        <el-form-item label="术语">
          <el-input v-model="form.term" placeholder="如：票据贴现" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="如：票据" />
        </el-form-item>
        <el-form-item label="定义/解释">
          <el-input
            v-model="form.definition"
            type="textarea"
            :rows="5"
            placeholder="详细解释该金融术语的含义..."
          />
        </el-form-item>
        <el-form-item label="关联术语">
          <el-input v-model="form.relatedTerms" placeholder="相关术语，逗号分隔" />
        </el-form-item>
        <el-form-item label="出处">
          <el-input v-model="form.source" placeholder="参考来源" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Plus, Upload } from '@element-plus/icons-vue'
import {
  getKnowledgeList, createKnowledgeEntry, updateKnowledgeEntry,
  deleteKnowledgeEntry, getKnowledgeCategories, importKnowledgeEntries
} from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()

const entries = ref([])
const categories = ref([])
const activeCategory = ref('')
const loading = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const saving = ref(false)

const form = reactive({
  id: null, term: '', category: '', definition: '', relatedTerms: '', source: ''
})

async function loadData() {
  loading.value = true
  try {
    const res = await getKnowledgeList(activeCategory.value || undefined)
    entries.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const res = await getKnowledgeCategories()
    categories.value = res.data || []
  } catch (e) { /* ignore */ }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: null, term: '', category: '', definition: '', relatedTerms: '', source: '' })
  showDialog.value = true
}

function openEdit(entry) {
  isEdit.value = true
  Object.assign(form, entry)
  showDialog.value = true
}

async function handleSave() {
  if (!form.term || !form.definition) {
    ElMessage.warning('术语和定义为必填')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateKnowledgeEntry(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createKnowledgeEntry(form)
      ElMessage.success('新增成功')
    }
    showDialog.value = false
    loadData()
    loadCategories()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteKnowledgeEntry(id)
    ElMessage.success('删除成功')
    loadData()
    loadCategories()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

async function handleImportFile(file) {
  if (!file || !file.raw) return
  const fd = new FormData()
  fd.append('file', file.raw)
  try {
    const res = await importKnowledgeEntries(fd)
    if (res.data.success) {
      ElMessage.success(`成功导入 ${res.data.count} 条词条`)
      loadData()
      loadCategories()
    } else {
      ElMessage.error(res.data.error || '导入失败')
    }
  } catch (e) {
    ElMessage.error('导入失败')
  }
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<style scoped>
.knowledge-page {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  color: #1a2332;
}

.category-bar {
  margin-bottom: 24px;
}

.entry-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.entry-card {
  border-radius: 8px;
}

.entry-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.entry-term {
  font-size: 16px;
  font-weight: 700;
  color: #1a2332;
}

.entry-definition {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  margin-bottom: 8px;
}

.entry-meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.entry-actions {
  display: flex;
  gap: 8px;
}
</style>
