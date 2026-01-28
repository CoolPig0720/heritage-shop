import request from '@/utils/request'

export function getRecommendProducts(params) {
  return request({
    url: '/api/shop/recommend',
    method: 'get',
    params
  })
}

export function getProductDetail(id) {
  return request({
    url: `/api/shop/products/${id}`,
    method: 'get'
  })
}

