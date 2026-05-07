<template>
  <div id="app-container">
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="app-header">
        <div class="header-left">
          <span class="logo">🍵 eattea</span>
          <span class="subtitle">金融监管知识平台</span>
        </div>
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          router
          background-color="#1a2332"
          text-color="#c0c4cc"
          active-text-color="#409eff"
        >
          <el-menu-item index="/">
            <el-icon><Search /></el-icon>
            <span>首页搜索</span>
          </el-menu-item>
          <el-menu-item index="/documents">
            <el-icon><Folder /></el-icon>
            <span>监管文档</span>
          </el-menu-item>
          <el-menu-item index="/knowledge">
            <el-icon><Collection /></el-icon>
            <span>金融知识库</span>
          </el-menu-item>
        </el-menu>
      </el-header>

      <!-- 主体内容 -->
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Search, Folder, Collection } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => {
  if (route.path.startsWith('/documents')) return '/documents'
  if (route.path.startsWith('/knowledge')) return '/knowledge'
  return '/'
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
    "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  background: #f0f2f5;
}

.app-header {
  background: #1a2332 !important;
  display: flex;
  align-items: center;
  padding: 0 24px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  margin-right: 40px;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  margin-right: 12px;
}

.subtitle {
  font-size: 13px;
  color: #909399;
}

.el-menu--horizontal {
  border-bottom: none !important;
}

.el-menu--horizontal > .el-menu-item {
  height: 60px;
  line-height: 60px;
}

.el-main {
  min-height: calc(100vh - 60px);
  padding: 24px;
}
</style>
