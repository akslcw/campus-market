import request from '@/utils/request'

export interface LoginParams {
    username: string
    password: string
}

export interface RegisterParams {
    username: string
    password: string
    nickname?: string
}

export interface LoginVO {
    token: string
    userId: number
    username: string
    nickname: string
    role: string
}

/** 登录：返回 token + 用户信息 */
export const login = (data: LoginParams) =>
    request.post<unknown, LoginVO>('/auth/login', data)

/** 注册：密码需含大小写字母与数字 */
export const register = (data: RegisterParams) =>
    request.post<unknown, void>('/auth/register', data)

/** 退出登录 */
export const logout = () =>
    request.post<unknown, void>('/auth/logout')
