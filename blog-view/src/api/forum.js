import request from '@/utils/request'

export function fetchForumPosts(params) {
  return request({
    url: '/post',
    method: 'get',
    params,
  })
}

export function fetchForumPostById(id) {
  return request({
    url: `/post/${id}`,
    method: 'get',
  })
}

export function createForumPost(data) {
  return request({
    url: '/post',
    method: 'post',
    data,
  })
}

export function fetchManagedForumPosts(params) {
  return request({
    baseURL: '',
    url: '/admin/forum/posts',
    method: 'get',
    params,
  })
}

export function updateManagedForumPostMeta(id, data) {
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
