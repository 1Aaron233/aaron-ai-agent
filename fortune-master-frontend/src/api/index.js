import axios from 'axios'
import { getToken } from '../auth'

const API_BASE_URL = import.meta.env.PROD ? '/api' : 'http://localhost:8123/api'

export const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

export const connectSSE = (url, params = {}, onMessage, onError) => {
  const queryParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    queryParams.set(key, value)
  })

  const token = getToken()
  if (token) {
    queryParams.set('accessToken', token)
  }

  const fullUrl = `${API_BASE_URL}${url}?${queryParams.toString()}`
  const eventSource = new EventSource(fullUrl)

  eventSource.onmessage = (event) => {
    onMessage?.(event.data)
  }

  eventSource.onerror = (error) => {
    onError?.(error)
    eventSource.close()
  }

  return eventSource
}

export const login = (payload) => http.post('/auth/login', payload)
export const getProfile = () => http.get('/auth/profile')
export const getMenus = () => http.get('/auth/menus')
export const getUsers = () => http.get('/admin/users')
export const getRoles = () => http.get('/admin/roles')
export const getPermissions = () => http.get('/admin/permissions')
export const createUser = (payload) => http.post('/admin/users', payload)
export const updateUser = (userId, payload) => http.put(`/admin/users/${userId}`, payload)
export const createRole = (payload) => http.post('/admin/roles', payload)
export const updateRole = (roleCode, payload) => http.put(`/admin/roles/${roleCode}`, payload)

export const chatWithFortuneApp = (message, chatId) =>
  connectSSE('/ai/fortune_app/chat/sse', { message, chatId })
