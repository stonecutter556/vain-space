import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/goals', name: 'Goals', component: () => import('../views/Goals.vue') },
  { path: '/articles', name: 'Articles', component: () => import('../views/Articles.vue') },
  { path: '/articles/:id', name: 'ArticleDetail', component: () => import('../views/ArticleDetail.vue') },
  { path: '/workout', name: 'Workout', component: () => import('../views/Workout.vue') },
  { path: '/pomodoro', name: 'Pomodoro', component: () => import('../views/Pomodoro.vue') },
  { path: '/bookmarks', name: 'Bookmarks', component: () => import('../views/Bookmarks.vue') },
  { path: '/playground', name: 'ClothSim', component: () => import('../views/ClothSim.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
