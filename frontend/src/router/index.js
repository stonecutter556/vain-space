import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true }
  },
  { path: '/goals', name: 'Goals', component: () => import('../views/Goals.vue'), meta: { requiresAuth: true } },
  { path: '/articles', name: 'Articles', component: () => import('../views/Articles.vue'), meta: { requiresAuth: true } },
  { path: '/articles/:id', name: 'ArticleDetail', component: () => import('../views/ArticleDetail.vue'), meta: { requiresAuth: true } },
  { path: '/workout', name: 'Workout', component: () => import('../views/Workout.vue'), meta: { requiresAuth: true } },
  { path: '/pomodoro', name: 'Pomodoro', component: () => import('../views/Pomodoro.vue'), meta: { requiresAuth: true } },
  { path: '/bookmarks', name: 'Bookmarks', component: () => import('../views/Bookmarks.vue'), meta: { requiresAuth: true } },
  { path: '/discover', name: 'Discover', component: () => import('../views/Discover.vue'), meta: { requiresAuth: true } },
  { path: '/profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { requiresAuth: true } },
  { path: '/users/:id', name: 'UserPublic', component: () => import('../views/UserPublic.vue'), meta: { requiresAuth: true } },
  { path: '/messages', name: 'Messages', component: () => import('../views/Messages.vue'), meta: { requiresAuth: true } },
  { path: '/messages/:userId', name: 'Chat', component: () => import('../views/Chat.vue'), meta: { requiresAuth: true } },
  { path: '/admin', name: 'Admin', component: () => import('../views/Admin.vue'), meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('vs_token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
