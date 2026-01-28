import request from '@/utils/request'

export function createProduct(data) {
  return request({
    url: '/api/products',
    method: 'post',
    data
  })
}

export function updateProduct(id, data) {
  return request({
    url: `/api/products/${id}`,
    method: 'put',
    data
  })
}

export function updateProductStatus(id, data) {
  return request({
    url: `/api/products/${id}/status`,
    method: 'put',
    data
  })
}

export function deleteProduct(id) {
  return request({
    url: `/api/products/${id}`,
    method: 'delete'
  })
}

export function pageProducts(params) {
  return request({
    url: '/api/products',
    method: 'get',
    params
  })
}

export function listProductImages(productId) {
  return request({
    url: `/api/products/${productId}/images`,
    method: 'get'
  })
}

export function addProductImages(productId, data) {
  return request({
    url: `/api/products/${productId}/images`,
    method: 'post',
    data
  })
}

export function updateProductImage(imageId, data) {
  return request({
    url: `/api/products/images/${imageId}`,
    method: 'put',
    data
  })
}

export function deleteProductImage(imageId) {
  return request({
    url: `/api/products/images/${imageId}`,
    method: 'delete'
  })
}
