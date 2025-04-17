import { createRouter, createWebHistory } from 'vue-router'
import errorPage from '@/components/Error.vue' 
import { getRolesPermissions } from '@/api/user.ts'; 
import { ElMessage } from 'element-plus';
import UserInfo from '@/views/manage/UserInfo.vue'

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
          path: '/login',
          component: () => import('@/components/Login.vue').catch(() => errorPage),
        },
        {
          path: '/home',
          component: () => import('@/views/home/Home.vue').catch(() => errorPage),
          props: true,
          children: [
            {
              path: '',
              redirect: '/home/featured'
            },
            {
              path: 'featured',
              component: () => import('@/views/home/Waterfall.vue').catch(() => errorPage),
              props: { photoType: 1 }
            },
            {
              path: 'photos',
              component: () => import('@/views/home/Waterfall.vue').catch(() => errorPage),
              props: { photoType: 0 }
            },
            {
              path: 'timeline',
              component: () => import('@/views/home/Timeline.vue').catch(() => errorPage)
            },
            {
              path: 'data',
              component: () => import('@/views/home/Data.vue').catch(() => errorPage)
            },
            {
              path: 'groupPhotos',
              component: () => import('@/views/home/GroupPhotos.vue').catch(() => errorPage)
            },
            {
              path: 'sunriseGlow',
              component: () => import('@/views/home/MeteorologyTimeLine.vue').catch(() => errorPage)
            },
            {
              path: 'sunsetGlow',
              component: () => import('@/views/home/MeteorologyTimeLine.vue').catch(() => errorPage)
            },
            {
              path: 'sunrise',
              component: () => import('@/views/home/MeteorologyTimeLine.vue').catch(() => errorPage)
            },{
              path: 'sunset',
              component: () => import('@/views/home/MeteorologyTimeLine.vue').catch(() => errorPage)
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
              path: 'photoList',
              component: () => import('@/views/manage/PhotoList.vue').catch(() => errorPage)
            },
            {
              path: 'timelineList',
              component: () => import('@/views/manage/TimelineList.vue').catch(() => errorPage)
            },
            {
              path: 'groupPhotosList',
              component: () => import('@/views/manage/GroupPhotosList.vue').catch(() => errorPage)
            },
            {
              path: 'userInfo',
              name: 'UserInfo',
              component: UserInfo
            }
          ]
        },
      ]
    },
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
      ElMessage.error("非管理员登陆");
      return next('/'); 
    } catch (error: any) {
      // 检查是否为401错误
      if (error.response && error.response.status === 401) {
        ElMessage.error('请登录');
      } else {
        ElMessage.error('获取用户角色失败');
        console.error('获取用户角色失败:', error);
      }
      return next('/login');
    }
  }

  // 其他路径，直接通过
  return next();
});

export default router