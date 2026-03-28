import request from '@/utils/request'

export function fetchAdminSiteInfo() {
  return request({
    baseURL: '',
    url: '/admin/site/info',
    method: 'get',
  })
}

export function updateAdminSiteInfo(data) {
  return request({
    baseURL: '',
    url: '/admin/site/info',
    method: 'put',
    data,
  })
}

export function fetchAdminAnnouncements() {
  return request({
    baseURL: '',
    url: '/admin/site/announcements',
    method: 'get',
  })
}

export function createAdminAnnouncement(data) {
  return request({
    baseURL: '',
    url: '/admin/site/announcements',
    method: 'post',
    data,
  })
}

export function deleteAdminAnnouncement(id) {
  return request({
    baseURL: '',
    url: `/admin/site/announcements/${id}`,
    method: 'delete',
  })
}
