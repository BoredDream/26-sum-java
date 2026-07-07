import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios'
import type { Result } from '@/types/api'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  withCredentials: true,
})

function redirectToLogin() {
  if (window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}

request.interceptors.response.use(
  (response: AxiosResponse<Result<unknown>>) => {
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }
    const { data } = response
    if (data.code !== 200) {
      if (data.code === 401) {
        redirectToLogin()
        return Promise.reject(new Error(data.message || '登录已过期，请重新登录'))
      }
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return response
  },
  (error: AxiosError<Result<unknown>>) => {
    const status = error.response?.status
    if (status === 401) {
      redirectToLogin()
    }
    const msg = error.response?.data?.message || error.message || '网络错误'
    return Promise.reject(new Error(msg))
  }
)

export function get<T>(url: string, config?: AxiosRequestConfig) {
  return request.get<Result<T>>(url, config).then((res) => res.data.data)
}

export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.post<Result<T>>(url, data, config).then((res) => res.data.data)
}

export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.put<Result<T>>(url, data, config).then((res) => res.data.data)
}

export function patch<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.patch<Result<T>>(url, data, config).then((res) => res.data.data)
}

export function del<T>(url: string, config?: AxiosRequestConfig) {
  return request.delete<Result<T>>(url, config).then((res) => res.data.data)
}

export function downloadFile(url: string, config?: AxiosRequestConfig) {
  return request.get(url, { ...config, responseType: 'blob' })
}

export function uploadFile<T>(url: string, formData: FormData, config?: AxiosRequestConfig) {
  return request
    .post<Result<T>>(url, formData, config)
    .then((res) => res.data.data)
}

export function putUploadFile<T>(url: string, formData: FormData, config?: AxiosRequestConfig) {
  return request
    .put<Result<T>>(url, formData, config)
    .then((res) => res.data.data)
}

export default request
