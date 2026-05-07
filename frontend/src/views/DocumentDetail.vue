<template>
  <div class="doc-detail-page" v-loading="loading">
    <div class="back-link">
      <el-link @click="router.push('/documents')">
        <el-icon><ArrowLeft /></el-icon>
        返回文档列表
      </el-link>
    </div>

    <template v-if="doc">
      <div class="doc-info">
        <h2>{{ doc.title }}</h2>
        <div class="meta-row">
          <el-tag>{{ doc.fileType?.toUpperCase() }}</el-tag>
          <span v-if="doc.department">部门：{{ doc.department }}</span>
          <span v-if="doc.docCategory">分类：{{ doc.docCategory }}</span>
          <span v-if="doc.publishDate">发布：{{ doc.publishDate }}</span>
          <span v-if="doc.tags">标签：{{ doc.tags }}</span>
          <span>上传时间：{{ doc.createTime }}</span>
        </div>
      </div>

      <div class="doc-content">
        <h4>文档内容</h4>
        <pre>{{ doc.content || '（未能提取文本内容）' }}</pre>
      </div>
    </template>

    <el-empty v-else description="文档不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getDocument } from '../api'

const route = useRoute()
const router = useRouter()
const doc = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getDocument(route.params.id)
    doc.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.doc-detail-page {
  max-width: 900px;
  margin: 0 auto;
}

.back-link {
  margin-bottom: 20px;
}

.doc-info h2 {
  font-size: 22px;
  color: #1a2332;
  margin-bottom: 12px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #909399;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.doc-content {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.doc-content h4 {
  font-size: 15px;
  color: #303133;
  margin-bottom: 16px;
}

.doc-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  font-family: inherit;
}
</style>
