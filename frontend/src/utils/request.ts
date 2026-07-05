import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '@/types/api'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  withCredentials: true,
})

request.interceptors.response.use(
  (response: AxiosResponse<Result<unknown>>) => {
    const { data } = response
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      if (data.code === 401) {
        window.location.href = '/login'
      }
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return response
  },
  (error: AxiosError<Result<unknown>>) => {
    const status = error.response?.status
    const data = error.response?.data
    if (status === 401) {
      window.location.href = '/login'
    }
    const msg = data?.message || error.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(error)
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
    .post<Result<T>>(url, formData, {
      ...config,
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((res) => res.data.data)
}

export default request
