import request from '@/utils/request';

/**
 * 获取标签列表
 */
export function fetchTags() {
  return request({
    url: '/tags',
    method: 'get',
  });
}


/**
 * 根据标签ID获取文章列表
 * @param {number} id - 标签ID
 * @param {object} params - 查询参数（如分页）
 */
export function fetchArticlesByTagId(id, params) {
  return request({
    url: `/tags/${id}/articles`,
    method: 'get',
    params,
  });
}

export function getAllTags() {
  return request({
<<<<<<< HEAD
    baseURL: '',
    url: '/admin/tags/list',
    method: 'get',
  })
}

=======
    url: '/tags',
    method: 'get',
  })
}
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
