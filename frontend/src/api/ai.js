import request from '@/utils/request'

export function getAISuggestions() {
  return request({
    url: '/ai/suggestions',
    method: 'get'
  })
}

/**
 * 获取社区排行榜
 * @param {number} topN 排名前 N 名，默认 5
 * @returns Promise
 */
export function getCommunityRanking() {
  return request({
    url: '/ai/ranking',
    method: 'get'
  })
}
