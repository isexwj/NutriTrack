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
/**
 * 文件路径: frontend/src/api/ai.js
 * 说明: 封装 AI 分析模块的所有前端请求（与登录注册模块风格一致）。
 *      导出函数：
 *        - getDailyAnalysis(date)
 */

import { useUserStore } from '@/store/user'
import axios from 'axios'

// 发送用户问题给 AI，返回 AI 回复
export const chatWithAI = async ({ question }) => {
    return axios.post('http://localhost:8080/api/ai/chat', { question }, {
        withCredentials: true // 如果后端需要 cookie/session
    })
}

export function getDailyAnalysis(date) {
    const store = useUserStore()
    const params = {}
    if (date) params.date = date
    return request({
        url: '/api/ai/daily-analysis',
        method: 'get',
        params,
        headers: {
            'X-Username': store.username || ''
        }
    })
}

export function getTrendAnalysis(params) {
    // params: { period: '7d'|'30d'|'3m', userId?, username? }
    return axios.get('http://localhost:8080/api/ai/trend', { params, withCredentials: true })
}
