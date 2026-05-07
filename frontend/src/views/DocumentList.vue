<template>
  <div class="doc-list-page">
    <div class="page-header">
      <h2>监管制度文档</h2>
      <el-button type="primary" @click="showUpload = true">
        <el-icon><Upload /></el-icon>
        上传文档
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索文档..."
        clearable
        style="width: 260px"
        @change="loadData"
      />
      <el-input
        v-model="filters.department"
        placeholder="部门"
        clearable
        style="width: 160px"
        @change="loadData"
      />
      <el-input
        v-model="filters.docCategory"
        placeholder="分类"
        clearable
        style="width: 160px"
        @change="loadData"
      />
    </div>

    <!-- 文档列表 -->
    <el-table :data="documents" style="width: 100%" v-loading="loading" stripe>
      <el-table-column prop="title" label="文档标题" min-width="240">
        <template #default="{ row }">
          <el-link type="primary" @click="router.push(`/documents/${row.id}`)">
            {{ row.title }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="fileType" label="格式" width="80">
        <template #default="{ row }">
          <el-tag size="small">{{ row.fileType?.toUpperCase() }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="department" label="部门" width="120" />
      <el-table-column prop="docCategory" label="分类" width="120" />
      <el-table-column prop="publishDate" label="发布日期" width="120" />
      <el-table-column prop="tags" label="标签" width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button text type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 上传对话框 -->
    <el-dialog v-model="showUpload" title="上传监管文档" width="520px">
      <el-form label-width="80px">
        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".pdf,.doc,.docx,.xls,.xlsx"
            :on-change="handleFileChange"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 PDF / Word / Excel</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="uploadForm.department" placeholder="如：风险管理部" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="uploadForm.docCategory" placeholder="如：1104、大额风险暴露" />
        </el-form-item>
        <el-form-item label="发布日期">
          <el-date-picker v-model="uploadForm.publishDate" type="date" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="uploadForm.tags" placeholder="逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUpload = false">取消</el-button>
        <el-button type="primary" @click="handleUpload" :loading="uploading">上传</el-button>
      </template>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog v-model="showEdit" title="编辑文档信息" width="480px">
      <el-form label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="editForm.department" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="editForm.docCategory" />
        </el-form-item>
        <el-form-item label="发布日期">
          <el-date-picker v-model="editForm.publishDate" type="date" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="editForm.tags" placeholder="逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Upload } from '@element-plus/icons-vue'
import { getDocuments, uploadDocument, updateDocument, deleteDocument } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()

const documents = ref([])
const loading = ref(false)
const showUpload = ref(false)
const showEdit = ref(false)
const uploading = ref(false)

const filters = reactive({ keyword: '', department: '', docCategory: '' })

const uploadForm = reactive({
  department: '', docCategory: '', publishDate: null, tags: ''
})
let uploadFile = null

const editForm = reactive({
  id: null, title: '', department: '', docCategory: '', publishDate: null, tags: ''
})

async function loadData() {
  loading.value = true
  try {
    const res = await getDocuments(filters)
    documents.value = res.data || []
  } finally {
    loading.value = false
  }
}

// 将 Date 对象格式化为 yyyy-MM-dd
function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function handleFileChange(file) {
  uploadFile = file.raw
}

async function handleUpload() {
  if (!uploadFile) {
    ElMessage.warning('请选择文件')
    return
  }
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', uploadFile)
    if (uploadForm.department) fd.append('department', uploadForm.department)
    if (uploadForm.docCategory) fd.append('docCategory', uploadForm.docCategory)
    if (uploadForm.publishDate) fd.append('publishDate', formatDate(uploadForm.publishDate))
    if (uploadForm.tags) fd.append('tags', uploadForm.tags)

    await uploadDocument(fd)
    ElMessage.success('上传成功')
    showUpload.value = false
    uploadFile = null
    Object.assign(uploadForm, { department: '', docCategory: '', publishDate: null, tags: '' })
    loadData()
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

function openEdit(row) {
  Object.assign(editForm, {
    id: row.id,
    title: row.title,
    department: row.department,
    docCategory: row.docCategory,
    publishDate: row.publishDate,
    tags: row.tags
  })
  showEdit.value = true
}

async function handleUpdate() {
  try {
    await updateDocument(editForm.id, {
      title: editForm.title,
      department: editForm.department,
      docCategory: editForm.docCategory,
      publishDate: formatDate(editForm.publishDate),
      tags: editForm.tags
    })
    ElMessage.success('保存成功')
    showEdit.value = false
    loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function handleDelete(id) {
  try {
    await deleteDocument(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.doc-list-page {
  max-width: 1200px;
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

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
</style>
