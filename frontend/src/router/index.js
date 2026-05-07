import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('../views/Home.vue'),
      meta: { title: '首页搜索' }
    },
    {
      path: '/documents',
      name: 'Documents',
      component: () => import('../views/DocumentList.vue'),
      meta: { title: '监管文档' }
    },
    {
      path: '/documents/:id',
      name: 'DocumentDetail',
      component: () => import('../views/DocumentDetail.vue'),
      meta: { title: '文档详情' }
    },
    {
      path: '/knowledge',
      name: 'Knowledge',
      component: () => import('../views/KnowledgeBase.vue'),
      meta: { title: '金融知识库' }
    }
  ]
})

export default router
