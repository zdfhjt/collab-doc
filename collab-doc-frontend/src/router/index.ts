import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guest: true },
    },
    {
      path: '/workspaces',
      name: 'Workspaces',
      component: () => import('@/views/WorkspaceListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/workspace/:workspaceId',
      name: 'Editor',
      component: () => import('@/views/EditorView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/workspace/:workspaceId/document/:documentId',
      name: 'EditorDocument',
      component: () => import('@/views/EditorView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/',
      redirect: '/workspaces',
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/workspaces',
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  const isAuthenticated = authStore.isAuthenticated

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else if (to.meta.guest && isAuthenticated) {
    next('/workspaces')
  } else {
    next()
  }
})

export default router
