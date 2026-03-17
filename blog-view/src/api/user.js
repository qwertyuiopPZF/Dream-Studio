import request from '@/utils/request'

export function fetchCurrentUserProfile() {
  return request({
    url: '/user/me',
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
