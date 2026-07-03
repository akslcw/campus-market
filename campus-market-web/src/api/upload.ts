import request from '@/utils/request'

/** 上传商品图片，返回可访问的相对 URL（如 /static/goods/.../x.png） */
export const uploadImage = (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<unknown, string>('/upload/image', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    })
}
