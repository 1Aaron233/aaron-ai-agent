import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: 'AI 算命大师',
    }
  },
  {
    path: '/fortune-master',
    name: 'FortuneMaster',
    component: () => import('../views/FortuneMaster.vue'),
    meta: {
      title: 'AI 算命大师对话'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router
