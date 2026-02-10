import request from '@/utils/request'

export function addCartItem(data) {
  return request({
    url: '/api/cart/items',
    method: 'post',
    data
  })
}

export function listCartItems() {
  return request({
    url: '/api/cart/items',
    method: 'get'
  })
}

export function updateCartItemQuantity(id, data) {
  return request({
    url: `/api/cart/items/${id}/quantity`,
    method: 'put',
    data
  })
}

export function updateCartItemSelected(id, data) {
  return request({
    url: `/api/cart/items/${id}/selected`,
    method: 'put',
    data
  })
}

export function deleteCartItem(id) {
  return request({
    url: `/api/cart/items/${id}`,
    method: 'delete'
  })
}

export function getCartSummary() {
  return request({
    url: '/api/cart/summary',
    method: 'get'
  })
}

