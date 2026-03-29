import request from '@/utils/request'

export function fetchPublicResources() {
  return request({
    url: '/resources',
    method: 'get',
  })
}

export function fetchMyResources() {
  return request({
    url: '/user/resources',
    method: 'get',
  })
}

export function fetchAdminResources(status) {
  return request({
    url: '/user/admin/resources',
    method: 'get',
    params: status ? { status } : undefined,
  })
}

export function uploadResourceFile(formData, config = {}) {
  const { headers = {}, ...restConfig } = config

  return request({
    ...restConfig,
    url: '/user/resources',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
      ...headers,
    },
  })
}

export function deleteResourceFile(resourceId) {
  return request({
    url: `/user/resources/${resourceId}`,
    method: 'delete',
  })
}

export function reviewResourceFile(resourceId, data) {
  return request({
    url: `/user/admin/resources/${resourceId}/review`,
    method: 'put',
    data,
  })
}

export function recordResourceDownload(resourceId) {
  return request({
    url: `/resources/${resourceId}/downloads`,
    method: 'post',
    showErrorMessage: false,
  })
}
