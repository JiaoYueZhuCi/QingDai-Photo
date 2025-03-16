import { createRouter, createWebHistory } from 'vue-router'
import errorPage from '@/components/ErrorPage.vue' // 新增错误页面组件

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
          component: () => import('@/views/home/Home.vue').catch(() => errorPage),
          children: [
            {
              path: '',
              redirect: '/home/featured'
            },
            {
              path: '/home/featured',
              component: () => import('@/views/home/Waterfall.vue').catch(() => errorPage)
            },
            {
              path: '/home/photos',
              component: () => import('@/views/home/Waterfall.vue').catch(() => errorPage)
            },
            {
              path: '/home/timeline',
              component: () => import('@/views/home/Timeline.vue').catch(() => errorPage)
            },
            {
              path: '/home/data',
              component: () => import('@/views/home/Data.vue').catch(() => errorPage)
            }
          ]
        },
        
        {
          path: '/manage',
          component: () => import('@/views/manage/Manage.vue').catch(() => errorPage),
          children: [
            {
              path: '',
              redirect: '/manage/photoList'
            },
            {
              path: '/manage/photoList',
              component: () => import('@/views/manage/PhotoList.vue').catch(() => errorPage)
            },
            {
              path: '/manage/timelineList',
              component: () => import('@/views/manage/TimelineList.vue').catch(() => errorPage)
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