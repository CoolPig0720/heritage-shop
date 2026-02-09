import request from '@/utils/request'

export function imageToImage(formData) {
  return request({
    url: '/api/customize/ai-image/image-to-image',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function textToImage(data) {
  return request({
    url: '/api/customize/ai-image/text-to-image',
    method: 'post',
    data
  })
}

export function pageAiImageRecords(params) {
  return request({
    url: '/api/customize/ai-image/records',
    method: 'get',
    params
  })
}
