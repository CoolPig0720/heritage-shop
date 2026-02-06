import request from '@/utils/request'

export function getHeritageCategoryTree() {
  return request({
    url: '/api/heritage/categories/tree',
    method: 'get'
  })
}

export function pageHeritageProjects(params) {
  return request({
    url: '/api/heritage/projects',
    method: 'get',
    params
  })
}

export function getHeritageProjectDetail(id) {
  return request({
    url: `/api/heritage/projects/${id}`,
    method: 'get'
  })
}

export function getHeritageInheritorDetail(id) {
  return request({
    url: `/api/heritage/inheritors/${id}`,
    method: 'get'
  })
}

export function getAdminHeritageCategoryTree() {
  return request({
    url: '/api/admin/heritage/categories/tree',
    method: 'get'
  })
}

export function createAdminHeritageCategory(data) {
  return request({
    url: '/api/admin/heritage/categories',
    method: 'post',
    data
  })
}

export function pageAdminHeritageProjects(params) {
  return request({
    url: '/api/admin/heritage/projects',
    method: 'get',
    params
  })
}

export function getAdminHeritageProjectDetail(id) {
  return request({
    url: `/api/admin/heritage/projects/${id}`,
    method: 'get'
  })
}

export function createAdminHeritageProject(data) {
  return request({
    url: '/api/admin/heritage/projects',
    method: 'post',
    data
  })
}

export function updateAdminHeritageProject(id, data) {
  return request({
    url: `/api/admin/heritage/projects/${id}`,
    method: 'put',
    data
  })
}

export function deleteAdminHeritageProject(id) {
  return request({
    url: `/api/admin/heritage/projects/${id}`,
    method: 'delete'
  })
}

export function updateAdminHeritageProjectInheritors(id, data) {
  return request({
    url: `/api/admin/heritage/projects/${id}/inheritors`,
    method: 'put',
    data
  })
}

export function createAdminHeritageProjectMedia(projectId, data) {
  return request({
    url: `/api/admin/heritage/projects/${projectId}/medias`,
    method: 'post',
    data
  })
}

export function updateAdminHeritageProjectMedia(mediaId, data) {
  return request({
    url: `/api/admin/heritage/projects/medias/${mediaId}`,
    method: 'put',
    data
  })
}

export function deleteAdminHeritageProjectMedia(mediaId) {
  return request({
    url: `/api/admin/heritage/projects/medias/${mediaId}`,
    method: 'delete'
  })
}

export function pageAdminHeritageInheritors(params) {
  return request({
    url: '/api/admin/heritage/inheritors',
    method: 'get',
    params
  })
}

export function getAdminHeritageInheritorDetail(id) {
  return request({
    url: `/api/admin/heritage/inheritors/${id}`,
    method: 'get'
  })
}

export function createAdminHeritageInheritor(data) {
  return request({
    url: '/api/admin/heritage/inheritors',
    method: 'post',
    data
  })
}

export function updateAdminHeritageInheritor(id, data) {
  return request({
    url: `/api/admin/heritage/inheritors/${id}`,
    method: 'put',
    data
  })
}

export function deleteAdminHeritageInheritor(id) {
  return request({
    url: `/api/admin/heritage/inheritors/${id}`,
    method: 'delete'
  })
}
