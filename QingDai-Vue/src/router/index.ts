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
            },
            {
              path: '/home/groupPhotos',
              component: () => import('@/views/home/GroupPhotos.vue').catch(() => errorPage)
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
            },
            {
              path: '/manage/groupPhotosList',
              component: () => import('@/views/manage/GroupPhotosList.vue').catch(() => errorPage)
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

  // 权限检查逻辑
  if (to.path.startsWith('/manage')) {
    try {
      // 获取用户角色
      const userRoleResponse = await getRolesPermissions();
      const userRoles = userRoleResponse?.roles || '';
      if (userRoles.includes('ADMIN')) {
        ElMessage.success("授权访问");
        return next(); // 如果是 ADMIN，继续处理路由
      }
      // 如果不是ADMIN
      ElMessage.error("权限不足");
      return next(from.path !== '/manage' ? from.path : '/'); 
    } catch (error: any) {
      // 检查是否为401错误
      if (error.response && error.response.status === 401) {
        ElMessage.error('请登录');
      } else {
        ElMessage.error('获取用户角色失败');
        console.error('获取用户角色失败:', error);
      }
      return next(from.path !== '/manage' ? from.path : '/');
    }
  }

  // 其他路径，直接通过
  return next();
});

export default router