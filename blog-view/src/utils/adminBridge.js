import { ElMessage } from 'element-plus'

<<<<<<< HEAD
export const openAdminApp = ({ isAdmin, accessToken, router, targetPath = '/admin/home' }) => {
=======
export const openAdminApp = ({ isAdmin, accessToken, router, targetPath = '/admin/articlemgmt' }) => {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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
