import axios from 'axios'
import { Message } from 'element-ui'
// 开发环境走 Vite 的 /api 代理。打包进 Spring Boot 后接口就在当前域名根路径，不能再加 /api。
const http = axios.create({
  baseURL: import.meta.env.DEV ? '/api' : '',
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
}, (error) => {
  // 已经收到 HTTP 响应时，后端是启动着的，不能一律提示网络异常
  const data = error.response && error.response.data
  if (data) {
    const msg = data.msg || data.error || '请求失败'
    Message.error(msg)
    return Promise.reject(new Error(msg))
  }
  Message.error('网络异常，请确认后端已启动')
  return Promise.reject(new Error('网络异常'))
})

export default http
