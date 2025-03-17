import { createRouter, createWebHistory } from 'vue-router'
import errorPage from '@/components/ErrorPage.vue' // 新增错误页面组件
import { getRolesPermissions } from '@/api/user.ts'; // 引入获取用户角色的方法
import { ElMessage } from 'element-plus';
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

router.beforeEach(async (to, from, next) => {
  if (to.path.startsWith('blob:')) {
    return next(false); // 阻止路由处理
  }

  try {
    // 权限检查逻辑
    if (to.path.startsWith('/manage')) {
      // 获取用户角色
      const userRoleResponse = await getRolesPermissions();
      const userRoles = userRoleResponse?.roles || '';
      if (userRoles.includes('ADMIN')) {
        ElMessage.success("授权访问");
        return next(); // 如果是 ADMIN，继续处理路由
      }
      ElMessage.error("请登录");
      return next('/'); // 如果不是 ADMIN，重定向到首页或其他页面
    }
  } catch (error) {
    ElMessage.error('获取用户角色失败');
    console.error('获取用户角色失败:', error);
    next('/'); // 如果获取角色失败，重定向到首页
  }

  next();
});

export default router