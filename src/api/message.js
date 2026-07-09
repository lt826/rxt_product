import request from './index'

// 发送消息
export function sendMessageAPI(data) {
  return request({
    url: '/api/v1/message/send',
    method: 'post',
    data
  })
}

// 获取与某用户的对话历史
export function getConversationAPI(toUser) {
  return request({
    url: '/api/v1/message/conversation',
    method: 'get',
    params: { toUser }
  })
}

// 获取未读消息统计（按发送方分组）
export function getUnreadSummaryAPI() {
  return request({
    url: '/api/v1/message/unread-summary',
    method: 'get'
  })
}

// 标记来自某人的消息为已读
export function markAsReadAPI(fromUser) {
  return request({
    url: `/api/v1/message/read/${fromUser}`,
    method: 'put'
  })
}
