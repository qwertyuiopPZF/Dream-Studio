import request from '@/utils/request'

export function fetchAdminUsers() {
  return request({
    baseURL: '',
    url: '/admin/users',
    method: 'get',
  })
}

export function updateAdminUserAdminStatus(id, admin) {
  return request({
    baseURL: '',
    url: `/admin/users/${id}/admin-status`,
    method: 'put',
    params: { admin },
  })
}

export function deleteAdminUser(id) {
  return request({
    baseURL: '',
    url: `/admin/users/${id}`,
    method: 'delete',
  })
}
