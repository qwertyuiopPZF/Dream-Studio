import request from '@/utils/request'

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
