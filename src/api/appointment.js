import request from './index'

// 创建预约
export function createAppointmentAPI(data) {
  return request({
    url: '/api/v1/appointment',
    method: 'post',
    data
  })
}

// 获取农户的预约列表
export function getFarmerAppointmentsAPI(farmerName) {
  return request({
    url: `/api/v1/appointment/farmer/${farmerName}`,
    method: 'get'
  })
}

// 获取专家的预约列表
export function getExpertAppointmentsAPI(expertName) {
  return request({
    url: `/api/v1/appointment/expert/${expertName}`,
    method: 'get'
  })
}

// 专家处理预约
export function handleAppointmentAPI(id, expertName, approved, reply) {
  return request({
    url: `/api/v1/appointment/${id}/handle`,
    method: 'post',
    params: { expertName, approved, reply }
  })
}

// 取消预约
export function cancelAppointmentAPI(id, farmerName) {
  return request({
    url: `/api/v1/appointment/${id}/cancel`,
    method: 'post',
    params: { farmerName }
  })
}

// 完成预约
export function completeAppointmentAPI(id, expertName) {
  return request({
    url: `/api/v1/appointment/${id}/complete`,
    method: 'post',
    params: { expertName }
  })
}
