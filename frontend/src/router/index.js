import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Dashboard',
      component: () => import('../views/Dashboard.vue'),
      meta: { title: '项目管理驾驶舱' }
    },
    {
      path: '/projects/:id',
      name: 'ProjectDetail',
      component: () => import('../views/ProjectDetail.vue'),
      meta: { title: '项目详情' }
    },
    {
      path: '/members',
      name: 'Members',
      component: () => import('../views/Members.vue'),
      meta: { title: '团队成员' }
    }
  ]
})

export default router
