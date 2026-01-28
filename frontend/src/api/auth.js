import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data
  })
}

export function getProfile() {
  return request({
    url: '/api/user/profile',
    method: 'get'
  })
}

export function updateUser(id, data) {
  return request({
    url: `/api/user/${id}/update`,
    method: 'put',
    data
  })
}

export function updateProfile(data, id) {
  return request({
    url: id ? `/api/user/profile?id=${id}` : '/api/user/profile',
    method: 'put',
    data
  })
}

export function updatePassword(data) {
  return request({
    url: '/api/user/password',
    method: 'put',
    data
  })
}

export function getUserList(params) {
  return request({
    url: '/api/user/list',
    method: 'get',
    params
  })
}

export function deleteUser(id) {
  return request({
    url: `/api/user/${id}`,
    method: 'delete'
  })
}

export function updateUserStatus(id, data) {
  return request({
    url: `/api/user/${id}/status`,
    method: 'put',
    data
  })
}

export function verifyCertificate(data) {
  return request({
    url: '/api/certificate',
    method: 'post',
    data
  })
}

export function getCertificateInfo() {
  return request({
    url: '/api/certificate',
    method: 'get'
  })
}

export function updateCertificate(data) {
  return request({
    url: '/api/certificate',
    method: 'put',
    data
  })
}
