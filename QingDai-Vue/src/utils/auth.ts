import { getRolesPermissions } from '@/api/user'

/**
 * 获取当前用户的角色权限
 * @returns 用户角色列表，如果用户未登录或获取失败则返回空数组
 */
export const getUserRoles = async (): Promise<string[]> => {
    const token = localStorage.getItem('token')
    if (!token) return []
    
    try {
        const userRoleResponse = await getRolesPermissions()
        return userRoleResponse?.roles || []
    } catch (error) {
        console.error('获取用户角色失败:', error)
        return []
    }
}

/**
 * 检查用户是否拥有指定角色
 * @param role 需要检查的角色名称
 * @returns 是否拥有该角色
 */
export const hasRole = async (role: string): Promise<boolean> => {
    const roles = await getUserRoles()
    return roles.includes(role)
}

/**
 * 检查用户是否拥有查看全尺寸照片的权限
 * @returns 是否有查看全尺寸照片权限
 */
export const canViewFullSizePhoto = async (): Promise<boolean> => {
    return await hasRole('VIEWER')
} 