import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      let message = res.message || '请求失败'
      const url = response?.config?.url || ''
      if (message === '系统异常，请联系管理员' && typeof url === 'string') {
        if (/^\/api\/products\/\d+\/images/.test(url)) {
          message = '获取商品图片失败：请确认后端已更新并重启'
        }
      }
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    return res
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
