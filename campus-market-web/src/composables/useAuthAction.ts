import { inject } from 'vue'
import { useUserStore } from '@/stores/user'

export function useAuthAction() {
  const userStore = useUserStore()
  const triggerLogin = inject<(msg: string) => void>('triggerLogin', () => {})

  const requireLogin = (message: string, action?: () => void): boolean => {
    if (userStore.token) {
      action?.()
      return true
    }
    triggerLogin(message)
    return false
  }

  return { requireLogin, isLoggedIn: () => !!userStore.token }
}
