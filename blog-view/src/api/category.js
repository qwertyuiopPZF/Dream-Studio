import request from '@/utils/request'

export function getAllCategories() {
  return request({
<<<<<<< HEAD
    baseURL: '',
    url: '/admin/categories',
=======
    url: '/categories',
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
    method: 'get',
  })
}
