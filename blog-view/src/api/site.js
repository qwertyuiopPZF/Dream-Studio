import request from '@/utils/request'

export function fetchSiteInfo() {
  return request({
    url: '/site/info',
    method: 'get',
  })
}

export function fetchSiteAnnouncements() {
  return request({
    url: '/site/announcements',
    method: 'get',
  })
}
