import request from '@/utils/request'

/**
 * 获取动态列表
 */
export function fetchMoments(params) {
  return request({
    url: '/moments',
    method: 'get',
    params,
  })
}

export function fetchManagedMoments(params) {
  return request({
    baseURL: '',
    url: '/admin/moment',
    method: 'get',
    params,
  })
}

export function createManagedMoment(data) {
  return request({
    baseURL: '',
    url: '/admin/moment',
    method: 'post',
    data,
  })
}

export function deleteManagedMoment(id) {
  return request({
    baseURL: '',
    url: `/admin/moment/${id}`,
    method: 'delete',
  })
}

export const getMoments = fetchMoments
