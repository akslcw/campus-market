import request from '@/utils/request'

export interface AIParams {
    title: string
    description: string
}

export interface AICheckVO {
    violation: boolean
    reason: string
}

/** AI 一键优化描述：返回规范的四要素文案（成色/功能/原因/交易方式） */
export const optimizeDescription = (data: AIParams) =>
    request.post<unknown, string>('/ai/optimize', data)

/** AI 违规检测：违禁/仿冒/欺诈等。后端发布时也会内置调用 */
export const checkContent = (data: AIParams) =>
    request.post<unknown, AICheckVO>('/ai/check', data)
