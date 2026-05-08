<template>
  <div id="app" class="app-container">
    <header class="app-header">
      <div class="header-content">
        <h1 class="app-logo" @click="router.push('/')">
          <el-icon size="24"><Monitor /></el-icon>
          PM 驾驶舱
        </h1>
        <nav class="header-nav">
          <el-button text @click="router.push('/')">仪表盘</el-button>
          <el-button text @click="router.push('/members')">团队成员</el-button>
        </nav>
      </div>
    </header>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Monitor } from '@element-plus/icons-vue'

const router = useRouter()
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f5f7fa;
  color: #303133;
}

.app-container {
  min-height: 100vh;
}

.app-header {
  background: #1a2332;
  color: #fff;
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app-logo {
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.header-nav {
  display: flex;
  gap: 8px;
}

.header-nav .el-button {
  color: #c0c4cc;
}

.header-nav .el-button:hover {
  color: #fff;
}

.app-main {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}
</style>
