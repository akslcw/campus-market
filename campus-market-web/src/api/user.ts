import request from '@/utils/request'

/** 个人资料（GET /api/user/profile） */
export interface UserProfileVO {
    id: number
    username: string
    nickname: string
    avatar?: string | null
    contact?: string | null
    role: string
}

export interface UpdateProfileParams {
    nickname?: string
    avatar?: string
    contact?: string
}

/** 获取当前登录用户资料 */
export const getProfile = () =>
    request.get<unknown, UserProfileVO>('/user/profile')

/** 修改昵称 / 头像 / 联系方式 */
export const updateProfile = (data: UpdateProfileParams) =>
    request.put<unknown, void>('/user/profile', data)
