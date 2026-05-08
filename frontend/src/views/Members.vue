<template>
  <div class="members-page">
    <div class="page-header">
      <h2>团队成员</h2>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon> 新增成员
      </el-button>
    </div>

    <el-table :data="members" stripe v-loading="loading">
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="role" label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="roleTag(row.role)" size="small">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column prop="department" label="部门" width="160" />
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

    <el-dialog v-model="showDialog" :title="editing ? '编辑成员' : '新增成员'" width="440px">
      <el-form label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role">
            <el-option label="PM" value="PM" />
            <el-option label="开发" value="开发" />
            <el-option label="测试" value="测试" />
            <el-option label="实施" value="实施" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="form.department" />
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
import { Plus } from '@element-plus/icons-vue'
import { getMembers, createMember, updateMember, deleteMember } from '../api'
import { ElMessage } from 'element-plus'

const members = ref([])
const loading = ref(false)
const showDialog = ref(false)
const editing = ref(false)
const saving = ref(false)

const form = reactive({ id: null, name: '', role: '开发', email: '', department: '' })

function roleTag(role) {
  return role === 'PM' ? 'danger' : role === '开发' ? 'primary' : role === '测试' ? 'success' : 'warning'
}

async function load() {
  loading.value = true
  try {
    const res = await getMembers()
    members.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = false
  Object.assign(form, { id: null, name: '', role: '开发', email: '', department: '' })
  showDialog.value = true
}

function openEdit(row) {
  editing.value = true
  Object.assign(form, row)
  showDialog.value = true
}

async function handleSave() {
  if (!form.name) { ElMessage.warning('请输入姓名'); return }
  saving.value = true
  try {
    if (editing.value) {
      await updateMember(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createMember(form)
      ElMessage.success('新增成功')
    }
    showDialog.value = false
    load()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteMember(id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(load)
</script>

<style scoped>
.members-page {
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
</style>
