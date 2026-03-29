import { ElMessage } from 'element-plus'

export const openAdminApp = ({ isAdmin, accessToken, router, targetPath = '/admin/overview' }) => {
  if (!isAdmin) {
    ElMessage.info('当前账号暂无后台管理权限')
    return false
  }

  if (!accessToken) {
    ElMessage.warning('登录状态已失效，请重新登录')
    router?.push('/login')
    return false
  }

  router?.push(targetPath)
  return true
}
