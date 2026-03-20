import request from '@/utils/request'

export function getAllCategories() {
  return request({
    baseURL: '',
    url: '/admin/categories',
    method: 'get',
  })
}
