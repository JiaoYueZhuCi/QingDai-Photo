/**
 * 以下是需要更新的导入路径：
 * 
 * 原始路径 -> 新路径
 * 
 * 通用组件:
 * @/components/Error.vue -> @/components/common/Error.vue
 * @/components/Login.vue -> @/components/common/Login.vue
 * @/components/ScrollReveal.vue -> @/components/common/ScrollReveal.vue
 * 
 * 照片相关组件:
 * @/components/PhotoViewer.vue -> @/components/photo/PhotoViewer.vue
 * @/components/PhotoPreview.vue -> @/components/photo/PhotoPreview.vue
 * @/components/FilmPreview.vue -> @/components/photo/FilmPreview.vue
 * 
 * 群组照片相关组件:
 * @/components/GroupPhotoPreview.vue -> @/components/group/GroupPhotoPreview.vue
 * @/components/GroupFilmPreview.vue -> @/components/group/GroupFilmPreview.vue
 * 
 * 需要修改的文件包括：
 * 1. router/index.ts 中的导入路径
 * 2. 所有视图文件中的导入路径
 * 3. 组件文件中的相互引用路径
 * 
 * 使用搜索命令找出所有需要更新的文件：
 * grep -r "@/components/Error.vue" .
 * grep -r "@/components/Login.vue" .
 * grep -r "@/components/ScrollReveal.vue" .
 * grep -r "@/components/PhotoViewer.vue" .
 * grep -r "@/components/PhotoPreview.vue" .
 * grep -r "@/components/FilmPreview.vue" .
 * grep -r "@/components/GroupPhotoPreview.vue" .
 * grep -r "@/components/GroupFilmPreview.vue" .
 * 
 * 然后在每个文件中更新路径
 */ 