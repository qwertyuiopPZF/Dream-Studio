import request from '@/utils/request'

export function fetchAdminDashboardStatistics() {
  return request({
    baseURL: '',
    url: '/admin/dashboard/statistics',
    method: 'get',
  })
}

export function fetchAdminCategories() {
  return request({
    baseURL: '',
    url: '/admin/categories',
    method: 'get',
  })
}

export function createAdminCategory(data) {
  return request({
    baseURL: '',
    url: '/admin/categories',
    method: 'post',
    data,
  })
}

export function updateAdminCategory(data) {
  return request({
    baseURL: '',
    url: '/admin/categories',
    method: 'put',
    data,
  })
}

export function deleteAdminCategory(id) {
  return request({
    baseURL: '',
    url: `/admin/categories/${id}`,
    method: 'delete',
  })
}

export function fetchAdminTags() {
  return request({
    baseURL: '',
    url: '/admin/tags/list',
    method: 'get',
  })
}

export function createAdminTag(data) {
  return request({
    baseURL: '',
    url: '/admin/tags',
    method: 'post',
    data,
  })
}

export function updateAdminTag(data) {
  return request({
    baseURL: '',
    url: '/admin/tags',
    method: 'put',
    data,
  })
}

export function deleteAdminTag(id) {
  return request({
    baseURL: '',
    url: `/admin/tags/${id}`,
    method: 'delete',
  })
}

export function fetchAdminArticles(params) {
  return request({
    baseURL: '',
    url: '/admin/articles/list',
    method: 'get',
    params,
  })
}

export function updateAdminArticle(id, data) {
  return request({
    baseURL: '',
    url: `/admin/articles/${id}`,
    method: 'put',
    data,
  })
}

export function deleteAdminArticle(id) {
  return request({
    baseURL: '',
    url: `/admin/articles/${id}`,
    method: 'delete',
  })
}

export function fetchAdminComments(params) {
  return request({
    baseURL: '',
    url: '/admin/comments',
    method: 'get',
    params,
  })
}

export function updateAdminCommentStatus(id, status) {
  return request({
    baseURL: '',
    url: '/admin/comments/status',
    method: 'put',
    params: { id, status },
  })
}

export function deleteAdminComment(id) {
  return request({
    baseURL: '',
    url: `/admin/comments/${id}`,
    method: 'delete',
  })
}

export function fetchAdminMoments(params) {
  return request({
    baseURL: '',
    url: '/admin/moment',
    method: 'get',
    params,
  })
}

export function createAdminMoment(data) {
  return request({
    baseURL: '',
    url: '/admin/moment',
    method: 'post',
    data,
  })
}

export function deleteAdminMoment(id) {
  return request({
    baseURL: '',
    url: `/admin/moment/${id}`,
    method: 'delete',
  })
}
<<<<<<< HEAD
=======

export function fetchAdminForumPosts(params) {
  return request({
    baseURL: '',
    url: '/admin/forum/posts',
    method: 'get',
    params,
  })
}

export function updateAdminForumPostMeta(id, data) {
  return request({
    baseURL: '',
    url: `/admin/forum/posts/${id}`,
    method: 'put',
    data,
  })
}

export function deleteManagedForumPost(id) {
  return request({
    baseURL: '',
    url: `/admin/forum/posts/${id}`,
    method: 'delete',
  })
}

export function fetchAdminSiteInfo() {
  return request({
    baseURL: '',
    url: '/admin/site/info',
    method: 'get',
  })
}

export function updateAdminSiteInfo(data) {
  return request({
    baseURL: '',
    url: '/admin/site/info',
    method: 'put',
    data,
  })
}

export function fetchAdminAnnouncements() {
  return request({
    baseURL: '',
    url: '/admin/site/announcements',
    method: 'get',
  })
}

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

export function createAdminAnnouncement(data) {
  return request({
    baseURL: '',
    url: '/admin/site/announcements',
    method: 'post',
    data,
  })
}

export function deleteAdminAnnouncement(id) {
  return request({
    baseURL: '',
    url: `/admin/site/announcements/${id}`,
    method: 'delete',
  })
}
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
