import request from '@/utils/request'

/** 商品列表项（GET /api/goods、GET /api/user/goods 的 records 元素） */
export interface GoodsVO {
    id: number
    title: string
    price: number
    originalPrice?: number
    categoryId: number
    categoryName: string
    status: string          // ON_SALE / SOLD / OFF_SHELF
    auditStatus?: string    // PENDING / APPROVED / REJECTED（我的发布列表使用）
    viewCount: number
    coverImage?: string | null
    sellerName: string
    createdAt: string
}

/** 商品详情（GET /api/goods/{id}） */
export interface GoodsDetailVO extends GoodsVO {
    description: string
    rawDescription?: string
    images: string[]
    auditStatus: string     // PENDING / APPROVED / REJECTED
    auditRemark?: string | null
    sellerId: number
    sellerContact?: string | null
    updatedAt: string
}

/** MyBatis-Plus 分页结构 */
export interface IPage<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}

export interface GoodsListParams {
    page?: number
    size?: number
    keyword?: string
    categoryId?: number
    status?: string
    sort?: 'newest' | 'price_asc' | 'price_desc'
}

export interface PublishGoodsParams {
    title: string
    description: string
    rawDescription?: string
    price: number
    originalPrice?: number
    categoryId: number
    categoryName: string
    images: string[]
    contact?: string
}

/** 商品列表（游客可访问） */
export const getGoodsList = (params?: GoodsListParams) =>
    request.get<unknown, IPage<GoodsVO>>('/goods', { params })

/** 商品详情（游客可访问，自动 +1 浏览量） */
export const getGoodsDetail = (id: number) =>
    request.get<unknown, GoodsDetailVO>(`/goods/${id}`)

/** 发布商品（需登录，后端自动触发 AI 违规检测） */
export const publishGoods = (data: PublishGoodsParams) =>
    request.post<unknown, GoodsDetailVO>('/goods', data)

/** 我的发布（需登录，返回全部状态的商品） */
export const getMyGoods = (params?: { status?: string; page?: number; size?: number }) =>
    request.get<unknown, IPage<GoodsVO>>('/user/goods', { params })

/** 标记已售 / 下架（仅本人，body: {status}） */
export const changeGoodsStatus = (id: number, status: 'SOLD' | 'OFF_SHELF') =>
    request.put<unknown, void>(`/goods/${id}/status`, { status })

/** 下架商品（仅本人） */
export const offlineGoods = (id: number) =>
    request.put<unknown, void>(`/goods/${id}/offline`)
