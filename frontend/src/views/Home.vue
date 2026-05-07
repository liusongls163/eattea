<template>
  <div class="home-page">
    <!-- 搜索区域 -->
    <div class="search-section">
      <h2 class="section-title">金融监管知识检索</h2>
      <p class="section-desc">一站式搜索监管制度、发文和金融业务知识</p>
      <el-input
        v-model="keyword"
        size="large"
        placeholder="输入关键词，如：票据贴现、资本充足率、同业拆借..."
        @keyup.enter="doSearch"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button type="primary" @click="doSearch" :loading="searching">
            搜索
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 搜索结果 -->
    <div v-if="searched" class="results-section">
      <div class="results-header">
        <span>搜索 "{{ lastKeyword }}" 共找到 {{ results.length }} 条结果</span>
      </div>

      <el-empty v-if="results.length === 0" description="未找到相关结果" />

      <div v-else class="result-list">
        <div
          v-for="item in results"
          :key="`${item.type}-${item.id}`"
          class="result-card"
          @click="goDetail(item)"
        >
          <div class="result-title">
            <el-tag :type="item.type === 'document' ? 'success' : 'warning'" size="small">
              {{ item.type === 'document' ? '文档' : '知识' }}
            </el-tag>
            <span class="title-text">{{ item.title }}</span>
          </div>
          <div class="result-meta">
            <span v-if="item.department">部门：{{ item.department }}</span>
            <span v-if="item.category">分类：{{ item.category }}</span>
          </div>
          <p class="result-highlight" v-html="item.highlight || '...'"></p>
        </div>
      </div>
    </div>

    <!-- 默认快速入口 -->
    <div v-else class="quick-section">
      <h3>快速入口</h3>
      <div class="quick-cards">
        <div class="quick-card" @click="router.push('/knowledge')">
          <el-icon size="28"><Collection /></el-icon>
          <span>金融知识词条</span>
          <small>票据、同业、债券等</small>
        </div>
        <div class="quick-card" @click="router.push('/documents')">
          <el-icon size="28"><Folder /></el-icon>
          <span>监管制度文档</span>
          <small>上传、检索、管理</small>
        </div>
        <div class="quick-card" @click="quickSearch('票据')">
          <el-icon size="28"><Document /></el-icon>
          <span>票据业务</span>
          <small>承兑、贴现、转贴现</small>
        </div>
        <div class="quick-card" @click="quickSearch('资本充足率')">
          <el-icon size="28"><Monitor /></el-icon>
          <span>监管指标</span>
          <small>资本充足率、不良贷款率等</small>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Collection, Folder, Document, Monitor } from '@element-plus/icons-vue'
import { searchAll } from '../api'

const router = useRouter()
const keyword = ref('')
const lastKeyword = ref('')
const results = ref([])
const searched = ref(false)
const searching = ref(false)

function quickSearch(word) {
  keyword.value = word
  doSearch()
}

async function doSearch() {
  if (!keyword.value.trim()) return
  searching.value = true
  lastKeyword.value = keyword.value
  try {
    const res = await searchAll(keyword.value)
    results.value = res.data || []
    searched.value = true
  } catch (e) {
    console.error(e)
    results.value = []
    searched.value = true
  } finally {
    searching.value = false
  }
}

function goDetail(item) {
  if (item.type === 'document') {
    router.push(`/documents/${item.id}`)
  } else {
    router.push(`/knowledge?id=${item.id}`)
  }
}
</script>

<style scoped>
.home-page {
  max-width: 900px;
  margin: 0 auto;
}

.search-section {
  text-align: center;
  padding: 48px 0 32px;
}

.section-title {
  font-size: 28px;
  color: #1a2332;
  margin-bottom: 8px;
}

.section-desc {
  color: #909399;
  font-size: 14px;
  margin-bottom: 24px;
}

.results-section {
  margin-top: 16px;
}

.results-header {
  padding: 12px 0;
  color: #606266;
  font-size: 14px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.result-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.result-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.result-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.result-meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  display: flex;
  gap: 16px;
}

.result-highlight {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.quick-section {
  margin-top: 24px;
}

.quick-section h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 16px;
}

.quick-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.quick-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px 16px;
  text-align: center;
  cursor: pointer;
  transition: box-shadow 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.quick-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.quick-card span {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.quick-card small {
  font-size: 12px;
  color: #909399;
}
</style>
