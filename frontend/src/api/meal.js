import request from '@/utils/request'

// 获取当前用户的饮食记录
export function getMealRecords() {
    return request({
        url: '/meal-records/query_me',
        method: 'get'
    })
}

// 添加饮食记录
export function addMealRecord(data) {
    return request({
        url: '/meal-records/create',
        method: 'post',
        data,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 更新饮食记录
export function updateMealRecord(id, data) {
    return request({
        url: `/meal-records/update/${id}`,
        method: 'put',
        data,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 删除饮食记录
export function deleteMealRecord(id) {
    return request({
        url: `/meal-records/delete/${id}`,
        method: 'delete'
    })
}

// 切换分享状态
export function toggleShareMealRecord(id, isShared) {
    return request({
        url: `/meal-records/share/${id}`,
        method: 'patch',
        data: {"isShared": isShared }
    })
}
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