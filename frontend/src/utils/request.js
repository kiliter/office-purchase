import axios from 'axios'
import { Message } from 'element-ui'
const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

/** 已登录请求自动带上令牌。 */
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

/** 业务码不是 200 时直接提示，登录失效则回到登录页。 */
http.interceptors.response.use((response) => {
  const body = response.data
  if (body && body.code === 200) {
    return body
  }
  if (body && body.code === 401) {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    // 动态引入，避免 request 和 router 循环依赖
    import('../router').then(({ default: router }) => {
      if (router.currentRoute.path !== '/login') {
        router.push('/login')
      }
    })
  }
  Message.error((body && body.msg) || '请求失败')
  return Promise.reject(new Error((body && body.msg) || '请求失败'))
}, () => {
  Message.error('网络异常，请确认后端已启动')
  return Promise.reject(new Error('网络异常'))
})

export default http
