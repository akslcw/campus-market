import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

const request: AxiosInstance = axios.create({
    baseURL: '/api',
    timeout: 15000,
})

// 请求拦截器:自动加 Token（sa-token 使用 raw token，不带 Bearer 前缀）
request.interceptors.request.use((config) => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = token
    }
    return config
})

// 响应拦截器:统一拆 Result
request.interceptors.response.use(
    (response: AxiosResponse) => {
        const res = response.data
        if (res.code === 200) {
            return res.data
        }
        if (res.code === 401) {
            const hadToken = !!localStorage.getItem('token')
            localStorage.removeItem('token')
            if (hadToken) {
                ElMessage.error('登录已过期，请重新登录')
                window.location.href = '/login'
            }
            return Promise.reject(new Error(res.msg))
        }
        if (res.apifoxError) {
            return Promise.reject(new Error(res.apifoxError.message))
        }
        ElMessage.error(res.msg || '请求失败')
        return Promise.reject(new Error(res.msg))
    },
    (error) => {
        // HTTP 层面的错误（4xx / 5xx）统一给出友好提示
        const status = error?.response?.status
        const msgMap: Record<number, string> = {
            400: '请求参数错误',
            401: '登录已过期,请重新登录',
            403: '没有访问权限',
            404: '请求的资源不存在',
            405: '请求方法不被允许',
            422: '请求参数校验失败',
            500: '服务器内部错误',
            502: '网关错误',
            503: '服务暂不可用',
            504: '网关超时',
        }
        if (status === 401) {
            const hadToken = !!localStorage.getItem('token')
            localStorage.removeItem('token')
            if (hadToken) {
                window.location.href = '/login'
            }
        }
        if (status) {
            ElMessage.error(msgMap[status] || `请求失败 (${status})`)
        } else if (error?.message?.includes('timeout')) {
            ElMessage.error('请求超时,请检查网络')
        } else if (error?.message?.includes('Network')) {
            ElMessage.error('网络异常,请检查连接')
        }
        return Promise.reject(error)
    }
)

export default request