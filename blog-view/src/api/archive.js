import request from '@/utils/request';

/**
 * 获取归档数据
 * 用户名
 */
export function fetchArchive() {
  return request({
    url: '/archive',
    method: 'get',
  });
}


