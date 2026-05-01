import { reactive } from 'vue'
import { getMenus, getProfile, login as loginRequest } from './api'

const TOKEN_KEY = 'fortune-rbac-token'

export const authState = reactive({
  token: '',
  user: null,
  menus: [],
  initialized: false
})

export const getToken = () => authState.token || localStorage.getItem(TOKEN_KEY) || ''

export const hasPermission = (permission) => {
  if (!permission) {
    return true
  }
  return authState.user?.permissions?.includes(permission) ?? false
}

export const restoreSession = () => {
  authState.token = localStorage.getItem(TOKEN_KEY) || ''
  authState.initialized = true
}

export const clearSession = () => {
  authState.token = ''
  authState.user = null
  authState.menus = []
  localStorage.removeItem(TOKEN_KEY)
}

export const fetchSession = async () => {
  if (!getToken()) {
    clearSession()
    return null
  }

  authState.token = getToken()
  const [profileRes, menuRes] = await Promise.all([getProfile(), getMenus()])
  authState.user = profileRes.data
  authState.menus = menuRes.data
  return authState.user
}

export const login = async (username, password) => {
  const result = await loginRequest({ username, password })
  if (result.code !== 200) {
    throw new Error(result.message || '登录失败')
  }
  authState.token = result.data.token
  localStorage.setItem(TOKEN_KEY, result.data.token)
  authState.user = result.data.user
  const menus = await getMenus()
  authState.menus = menus.data
  return result.data
}
