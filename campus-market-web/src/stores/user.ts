import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userId = ref<number | null>(parseInt(localStorage.getItem('userId') || '0') || null)
    const username = ref(localStorage.getItem('username') || '')
    const nickname = ref(localStorage.getItem('nickname') || '')
    const role = ref(localStorage.getItem('role') || '')

    function setLogin(data: {
        token: string
        userId: number
        username: string
        nickname: string
        role: string
    }) {
        token.value = data.token
        userId.value = data.userId
        username.value = data.username
        nickname.value = data.nickname
        role.value = data.role
        localStorage.setItem('token', data.token)
        localStorage.setItem('userId', String(data.userId))
        localStorage.setItem('username', data.username)
        localStorage.setItem('nickname', data.nickname)
        localStorage.setItem('role', data.role)
    }

    function logout() {
        token.value = ''
        userId.value = null
        username.value = ''
        nickname.value = ''
        role.value = ''
        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('nickname')
        localStorage.removeItem('role')
    }

    return { token, userId, username, nickname, role, setLogin, logout }
})