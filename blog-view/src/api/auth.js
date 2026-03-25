import request from '@/utils/request'

export function usernamePasswordLogin(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data,
  })
}

export function fetchGithubAuthorizeUrl() {
  return request({
    url: '/auth/github/url',
    method: 'get',
  })
}

export function githubCodeLogin(data) {
  return request({
    url: '/auth/github/login',
    method: 'post',
    data,
  })
}

export function completeGithubRegistration(data) {
  return request({
    url: '/auth/github/register',
    method: 'post',
    data,
  })
}
