import { createRouter, createWebHistory } from 'vue-router'
import errorPage from '@/components/common/Error.vue'
import { getRolesPermissions } from '@/api/user.ts';
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
          path: '/login',
          component: () => import('@/components/common/Login.vue').catch(() => errorPage),
        },
        {
          path: '/test',
          component: () => import('@/views/photo/Test.vue').catch(() => errorPage),
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
              component: () => import('@/views/home/photo/Photo.vue').catch(() => errorPage),
              props: { photoType: 1 }
            },
            {
              path: 'photos',
              component: () => import('@/views/home/photo/Photo.vue').catch(() => errorPage),
              props: { photoType: 0 }
            },
            {
              path: 'timeline',
              component: () => import('@/views/home/timeline/Timeline.vue').catch(() => errorPage)
            },
            {
              path: 'data',
              component: () => import('@/views/home/data/Data.vue').catch(() => errorPage)
            },
            {
              path: 'groupPhotos',
              component: () => import('@/views/home/group-photos/GroupPhotos.vue').catch(() => errorPage)
            },
            {
              path: 'hidden',
              component: () => import('@/views/home/photo/Photo.vue').catch(() => errorPage),
              props: { photoType: 2 }
            },
            {
              path: 'weather',
              component: () => import('@/views/home/photo/Photo.vue').catch(() => errorPage),
              props: { photoType: 3 }
            },
            {
              path: 'sunriseGlow',
              component: () => import('@/views/home/meteorology/MeteorologyTimeLine.vue').catch(() => errorPage),
              props: { meteorologyType: '1' }
            },
            {
              path: 'sunsetGlow',
              component: () => import('@/views/home/meteorology/MeteorologyTimeLine.vue').catch(() => errorPage),
              props: { meteorologyType: '2' }
            },
            {
              path: 'sunrise',
              component: () => import('@/views/home/meteorology/MeteorologyTimeLine.vue').catch(() => errorPage),
              props: { meteorologyType: '3' }
            }, {
              path: 'sunset',
              component: () => import('@/views/home/meteorology/MeteorologyTimeLine.vue').catch(() => errorPage),
              props: { meteorologyType: '4' }
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
              component: () => import('@/views/manage/photo-manage/PhotoManage.vue').catch(() => errorPage)
            },
            {
              path: 'timelineList',
              component: () => import('@/views/manage/timeline-manage/TimelineManage.vue').catch(() => errorPage)
            },
            {
              path: 'groupPhotosList',
              component: () => import('@/views/manage/group-photos-manage/GroupPhotosManage.vue').catch(() => errorPage)
            },
            {
              path: 'shareList',
              component: () => import('@/views/manage/share-manage/ShareManage.vue').catch(() => errorPage)
            },
            {
              path: 'userInfo',
              component: () => import('@/views/manage/user-manage/UserManage.vue').catch(() => errorPage)
            },
            {
              path: 'developer',
              component: () => import('@/views/manage/develop-manage/DevelopManage.vue').catch(() => errorPage)
            }
          ]
        },

        {
          path: '/share',
          name: 'Share',
          component: () => import('@/views/share/Share.vue')
        },
        {
          path: '/:pathMatch(.*)*',
          component: () => import('@/components/common/Error.vue')
        }
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
        ElMessage.success("授权访问管理");
        return next(); // 如果是 ADMIN，继续处理路由
      }
      if (userRoles.includes('VIEWER')) {
        ElMessage.success("仅授权查看");
        return next(); // 如果是 VIEWER，继续处理路由
      }
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