import request from '@/utils/request'

/** 商品分类（GET /api/categories） */
export interface CategoryVO {
    id: number
    name: string
    sortOrder: number
}

/** 获取分类列表（游客可访问，按 sortOrder 升序） */
export const getCategories = () =>
    request.get<unknown, CategoryVO[]>('/categories')
