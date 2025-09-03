import request from '@/utils/request'

// 获取餐次状态
export function getMealStatus() {
  return request.get('/meal/threeStatus')
}

// 获取今日统计数据
export function getTodayStats(today) {
//   return request.get('/meal/todayData')
  return request.get(`/meal/todayData?date=${today}`);
}

export function getRecentMeals() {
  return request({
    url: '/meal/recent',
    method: 'get'
  })
}