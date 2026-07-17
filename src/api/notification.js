import request from './index'

// 当前用户的系统通知列表（审批结果、联合贷款人邀请等）
export function getMyNotificationsAPI() {
  return request({ url: '/api/v1/notifications/list', method: 'get' })
}

// 未读通知数量
export function getUnreadNotificationCountAPI() {
  return request({ url: '/api/v1/notifications/unread-count', method: 'get' })
}

// 标记某条通知为已读
export function markNotificationReadAPI(id) {
  return request({ url: `/api/v1/notifications/${id}/read`, method: 'put' })
}

// 标记当前用户全部通知为已读（点击铃铛即消除红点）
export function markAllNotificationsReadAPI() {
  return request({ url: '/api/v1/notifications/read-all', method: 'put' })
}
