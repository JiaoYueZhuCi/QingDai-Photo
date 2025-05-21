import { createRouter, createWebHistory } from 'vue-router'
import { h } from 'vue'
import errorPage from '@/components/common/error/Error.vue'
import { getRolesPermissions } from '@/api/user.ts';
import { ElMessage } from 'element-plus';
import { MeteorologyGroupType } from '@/config/groupPhotos';
import { PhotoStarRating } from '@/config/photo';

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
          path: '/show',
          component: () => import('@/views/show/Show.vue').catch((error) => {
            console.error('组件加载失败:', error);
            return h(errorPage, { errorMessage: error.toString() });
          }),
        },
        {
          path: '/login',
          component: () => import('@/components/common/login/Login.vue').catch((error) => {
            console.error('组件加载失败:', error);
            return h(errorPage, { errorMessage: error.toString() });
          }),
        },
        {
          path: '/test',
          component: () => import('@/views/test/Test.vue').catch((error) => {
            console.error('组件加载失败:', error);
            return h(errorPage, { errorMessage: error.toString() });
          }),
        },
        {
          path: '/home',
          component: () => import('@/views/home/Home.vue').catch((error) => {
            console.error('组件加载失败:', error);
            return h(errorPage, { errorMessage: error.toString() });
          }),
          props: true,
          children: [
            {
              path: '',
              redirect: '/home/featured'
            },
            {
              path: 'featured',
              component: () => import('@/views/home/photo/Photo.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              }),
              props: { photoType: PhotoStarRating.STAR }
            },
            {
              path: 'photos',
              component: () => import('@/views/home/photo/Photo.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              }),
              props: { photoType: PhotoStarRating.VISIBLE }
            },
            {
              path: 'timeline',
              component: () => import('@/views/home/timeline/Timeline.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              })
            },
            {
              path: 'data',
              component: () => import('@/views/home/data/Data.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              })
            },
            {
              path: 'groupPhotos',
              component: () => import('@/views/home/group-photos/GroupPhotos.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              })
            },
            {
              path: 'hidden',
              component: () => import('@/views/home/photo/Photo.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              }),
              props: { photoType: PhotoStarRating.HIDDEN }
            },
            {
              path: 'meteorology',
              component: () => import('@/views/home/photo/Photo.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              }),
              props: { photoType: PhotoStarRating.METEOROLOGY }
            },
            {
              path: 'sunriseGlow',
              component: () => import('@/views/home/meteorology/MeteorologyTimeLine.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              }),
              props: { meteorologyType: MeteorologyGroupType.SUNRISE_GLOW }
            },
            {
              path: 'sunsetGlow',
              component: () => import('@/views/home/meteorology/MeteorologyTimeLine.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              }),
              props: { meteorologyType: MeteorologyGroupType.SUNSET_GLOW }
            },
            {
              path: 'sunrise',
              component: () => import('@/views/home/meteorology/MeteorologyTimeLine.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              }),
              props: { meteorologyType: MeteorologyGroupType.SUNRISE }
            },
            {
              path: 'sunset',
              component: () => import('@/views/home/meteorology/MeteorologyTimeLine.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              }),
              props: { meteorologyType: MeteorologyGroupType.SUNSET }
            },
            {
              path: 'groupOnly',
              component: () => import('@/views/home/photo/Photo.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              }),
              props: { photoType: PhotoStarRating.GROUP_ONLY }
            }
          ]
        },

        {
          path: '/manage',
          component: () => import('@/views/manage/Manage.vue').catch((error) => {
            console.error('组件加载失败:', error);
            return h(errorPage, { errorMessage: error.toString() });
          }),
          children: [
            {
              path: '',
              redirect: '/manage/photoList'
            },
            {
              path: 'photoList',
              component: () => import('@/views/manage/photo-manage/PhotoManage.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              })
            },
            {
              path: 'timelineList',
              component: () => import('@/views/manage/timeline-manage/TimelineManage.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              })
            },
            {
              path: 'groupPhotosList',
              component: () => import('@/views/manage/group-photos-manage/GroupPhotosManage.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              })
            },
            {
              path: 'shareList',
              component: () => import('@/views/manage/share-manage/ShareManage.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              })
            },
            {
              path: 'userInfo',
              component: () => import('@/views/manage/user-manage/UserManage.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              })
            },
            {
              path: 'control',
              component: () => import('@/views/manage/control-manage/ControlManage.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              })
            },
            {
              path: 'develop',
              component: () => import('@/views/manage/develop-manage/DevelopManage.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              })
            },
            {
              path: 'view',
              component: () => import('@/views/manage/view-stats-manage/ViewManage.vue').catch((error) => {
                console.error('组件加载失败:', error);
                return h(errorPage, { errorMessage: error.toString() });
              })
            }
          ]
        },

        {
          path: '/share',
          name: 'Share',
          component: () => import('@/views/share/Share.vue').catch((error) => {
            console.error('组件加载失败:', error);
            return h(errorPage, { errorMessage: error.toString() });
          })
        },
        {
          path: '/:pathMatch(.*)*',
          component: () => import('@/components/common/empty/Empty.vue').catch((error) => {
            console.error('组件加载失败:', error);
            return h(errorPage, { errorMessage: error.toString() });
          })
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