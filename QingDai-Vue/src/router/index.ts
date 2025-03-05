import { createRouter, createWebHistory } from 'vue-router'
import errorPage from '@/components/errorpage.vue' // 新增错误页面组件

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('../../App.vue'),
      children: [
        {
          path: '',
          redirect: '/home'
        },
        {
          path: '/home',
          component: () => import('@/components/homepage.vue').catch(() => errorPage),
          children: [
            {
              path: '',
              redirect: '/home/featured'
            },
            {
              path: '/home/featured',
              component: () => import('@/components/waterfall.vue').catch(() => errorPage)
            },
            {
              path: '/home/photos',
              component: () => import('@/components/waterfall.vue').catch(() => errorPage)
            },
            {
              path: '/home/timeline',
              component: () => import('@/components/timeline.vue').catch(() => errorPage)
            },
            {
              path: '/home/data',
              component: () => import('@/components/data.vue').catch(() => errorPage)
            }
          ]
        },
        {
          path: '/manage',
          component: () => import('@/components/managePage.vue').catch(() => errorPage),
          children: [
            {
              path: '',
              redirect: '/manage/photoList'
            },
            {
              path: '/manage/photoList',
              component: () => import('@/components/photoList.vue').catch(() => errorPage)
            }
          ]
        },
        // ,{
        //   path: '/login',
        //   component: () => import('@/components/login.vue').catch(() => errorPage)
        // }
      ]
    },
    // {
    //   path: '/404',
    //   component: () => import('@/components/errorpage.vue')
    // },
    // {
    //   path: '/:pathMatch(.*)*', 
    //   redirect: '/404' // 将未匹配路径重定向到明确地址
    // }
  ]
})

router.beforeEach((to, from, next) => {
  if (to.path.startsWith('blob:')) {
    return next(false); // 阻止路由处理
  }
  next();
});

export default router