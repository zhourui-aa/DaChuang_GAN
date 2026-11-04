import { createRouter, createWebHashHistory } from 'vue-router';

/** 路由：登录页独立，其余挂在 Layout 下。 */
const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/login/index.vue') },
    // 兜底：未知路径回工作台，避免白屏
    { path: '/:pathMatch(.*)*', redirect: '/workbench' },
    {
      path: '/',
      component: () => import('@/layout/index.vue'),
      redirect: '/workbench',
      children: [
        {
          path: 'workbench', name: 'workbench',
          component: () => import('@/views/workbench/index.vue'),
          meta: { title: '教师工作台', icon: 'HomeFilled' },
        },
        {
          path: 'detect/:id', name: 'detectDetail',
          component: () => import('@/views/detect-detail/index.vue'),
          meta: { title: '检测详情', icon: 'Document', hidden: true, activeMenu: '/workbench' },
        },
        {
          path: 'appeal', name: 'appealDesk',
          component: () => import('@/views/appeal-desk/index.vue'),
          meta: { title: '申诉处理台', icon: 'ChatDotSquare' },
        },
        {
          path: 'config', name: 'config',
          component: () => import('@/views/config/index.vue'),
          meta: { title: '模型与阈值', icon: 'Setting' },
        },
        {
          path: 'audit', name: 'audit',
          component: () => import('@/views/audit/index.vue'),
          meta: { title: '审计日志', icon: 'Tickets' },
        },
        {
          path: 'dashboard', name: 'dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: { title: '课程统计看板', icon: 'DataLine' },
        },
      ],
    },
  ],
});

router.beforeEach((to) => {
  const token = localStorage.getItem('integrity_token');
  if (to.path !== '/login' && !token) return '/login';
  if (to.path === '/login' && token) return '/workbench';
  return true;
});

export default router;
