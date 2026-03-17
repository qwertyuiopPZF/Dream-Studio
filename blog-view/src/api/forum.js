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
