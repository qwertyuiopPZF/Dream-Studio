import request from '@/utils/request'

/**
 * 获取文章列表（支持分页和筛选）
 * @param {object} params - 查询参数
 * @param {number} [params.page] - 当前页码
 * @param {number} [params.size] - 每页数量
 * @param {number} [params.categoryId] - 分类ID
 * @param {string} [params.category] - 分类名
 * @param {string} [params.keyword] - 关键词
 * @param {boolean} [params.featuredOnly] - 是否只查询精华文章
 * @param {string} [params.sortBy] - 排序字段
 * @param {string} [params.order] - 排序方式
 */
export function fetchArticles(params) {
  return request({
    url: '/articles',
    method: 'get',
    params,
  })
}

/**
 * 根据ID获取文章详情
 * @param {number} id - 文章ID
 */
export function fetchArticleById(id) {
  return request({
    url: `/articles/${id}`,
    method: 'get',
  })
}

export function fetchManagedArticles(params) {
  return request({
    baseURL: '',
    url: '/admin/articles/list',
    method: 'get',
    params,
  })
}

export function createManagedArticle(data) {
  return request({
    baseURL: '',
    url: '/admin/articles',
    method: 'post',
    data,
  })
}

export function updateManagedArticle(id, data) {
  return request({
    baseURL: '',
    url: `/admin/articles/${id}`,
    method: 'put',
    data,
  })
}

export function fetchManagedArticleById(id) {
  return request({
    baseURL: '',
    url: `/admin/articles/${id}`,
    method: 'get',
  })
}

export function deleteManagedArticle(id) {
  return request({
    baseURL: '',
    url: `/admin/articles/${id}`,
    method: 'delete',
  })
}
