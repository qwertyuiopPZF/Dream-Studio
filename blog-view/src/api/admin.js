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
