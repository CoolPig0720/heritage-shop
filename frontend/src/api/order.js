import request from '@/utils/request'

export function createOrder(data) {
  return request({
    url: '/api/orders',
    method: 'post',
    data
  })
}

export function pageOrders(params) {
  return request({
    url: '/api/orders',
    method: 'get',
    params
  })
}

export function getOrderDetail(id) {
  return request({
    url: `/api/orders/${id}`,
    method: 'get'
  })
}

export function cancelOrder(id) {
  return request({
    url: `/api/orders/${id}/cancel`,
    method: 'put'
  })
}

