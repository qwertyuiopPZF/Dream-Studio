import request from '@/utils/request'

export function fetchCurrentUserProfile() {
  return request({
    url: '/user/me',
    method: 'get',
  })
}

export function fetchCurrentUserOverview() {
  return request({
    url: '/user/me/overview',
    method: 'get',
  })
}

export function updateCurrentUserPassword(data) {
  return request({
    url: '/user/password',
    method: 'post',
    data,
  })
}

export function updateCurrentUserAvatar(data) {
  return request({
    url: '/user/avatar',
    method: 'post',
    data,
  })
}

export function createForumReport(data) {
  return request({
    url: '/user/reports',
    method: 'post',
    data,
  })
}

export function markCurrentUserNotificationRead(id) {
  return request({
    url: `/user/notifications/${id}/read`,
    method: 'post',
  })
}

export function updateAdminForumPost(id, data) {
  return request({
    url: `/user/admin/posts/${id}`,
    method: 'put',
    data,
  })
}

export function deleteAdminForumPost(id) {
  return request({
    url: `/user/admin/posts/${id}`,
    method: 'delete',
  })
}

export function reviewForumReport(id, data) {
  return request({
    url: `/user/admin/reports/${id}`,
    method: 'put',
    data,
  })
}
