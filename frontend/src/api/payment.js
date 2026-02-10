import request from '@/utils/request'

export function createPayment(data) {
  return request({
    url: '/api/payments',
    method: 'post',
    data
  })
}

export function getPayment(id) {
  return request({
    url: `/api/payments/${id}`,
    method: 'get'
  })
}

export function mockPay(id, data) {
  return request({
    url: `/api/payments/${id}/mock-pay`,
    method: 'post',
    data
  })
}

