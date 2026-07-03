<template>
  <div id="app">
    <router-view v-slot="{ Component }">
      <transition name="page-fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>

    <!-- 底部导航 -->
    <nav class="tab-bar" v-if="showTabBar">
      <div
        class="tab-item"
        v-for="tab in tabs"
        :key="tab.key"
        :class="{ active: isActive(tab), center: tab.center }"
        @click="goTo(tab)"
      >
        <template v-if="tab.center">
          <div class="publish-btn">
            <el-icon size="24"><Plus /></el-icon>
          </div>
          <span class="tab-label">发布</span>
        </template>
        <template v-else>
          <el-icon size="20">
            <component :is="iconMap[tab.icon as keyof typeof iconMap]" />
          </el-icon>
          <span class="tab-label">{{ tab.label }}</span>
        </template>
      </div>
    </nav>

    <!-- 登录提示抽屉 -->
    <transition name="drawer-slide">
      <div class="login-drawer-overlay" v-if="showLoginDrawer" @click.self="showLoginDrawer = false">
        <div class="login-drawer paper-card">
          <div class="drawer-handle"></div>
          <p class="eyebrow">CAMPUS&nbsp;MARKET</p>
          <h3 class="display drawer-title">{{ loginDrawerMessage }}</h3>
          <p class="drawer-sub">登录后即可发布闲置、管理你的商品。</p>
          <hr class="hairline" />
          <button class="btn-ink btn-ink--brand drawer-btn" @click="goLoginFromDrawer">去登录</button>
          <button class="drawer-close-btn" @click="showLoginDrawer = false">先随便看看</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, UserFilled, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const iconMap = { HomeFilled, UserFilled }

const tabs = [
  { path: '/home', label: '首页', icon: 'HomeFilled', key: 'home' },
  { path: '/publish', label: '', icon: '', center: true, key: 'publish' },
  { path: '/profile', label: '我的', icon: 'UserFilled', key: 'profile' },
]

const isAuthPage = computed(() => ['/login', '/register'].includes(route.path))
const isDetailPage = computed(() => route.path.startsWith('/goods/'))
const showTabBar = computed(() => !isAuthPage.value && !isDetailPage.value)

const showLoginDrawer = ref(false)
const loginDrawerMessage = ref('')

const triggerLogin = (message: string) => {
  loginDrawerMessage.value = message
  showLoginDrawer.value = true
}

const goLoginFromDrawer = () => {
  showLoginDrawer.value = false
  router.push({ path: '/login', query: { redirect: route.fullPath } })
}

const isActive = (tab: typeof tabs[number]) => {
  if (tab.path === '/home') return route.path === '/home' || route.path === '/'
  return route.path === tab.path
}

const goTo = (tab: typeof tabs[number]) => {
  if (tab.path === '/publish' && !userStore.token) {
    triggerLogin('登录后即可发布')
    return
  }
  router.push(tab.path)
}
</script>

<style>
#app {
  max-width: 520px;
  margin: 0 auto;
  min-height: 100vh;
  position: relative;
  padding-bottom: 72px;
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.22s var(--ease), transform 0.22s var(--ease);
}
.page-fade-enter-from { opacity: 0; transform: translateY(8px); }
.page-fade-leave-to { opacity: 0; }

/* —— 底部导航：纸感、细线顶边 —— */
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 520px;
  height: 64px;
  background: var(--paper);
  border-top: 1px solid var(--line);
  display: flex;
  justify-content: space-around;
  align-items: center;
  z-index: 1000;
  padding-bottom: env(safe-area-inset-bottom);
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--ink-mute);
  padding: 4px 18px;
  gap: 3px;
  transition: color 0.15s var(--ease), transform 0.15s var(--ease);
}
.tab-item:active { transform: scale(0.94); }
.tab-item.active { color: var(--ink); }
.tab-item.active .tab-label { font-weight: 700; }

.tab-label { font-size: 11px; letter-spacing: 0.05em; }

.tab-item.center .tab-label { color: var(--brand); font-weight: 700; }
.publish-btn {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: var(--brand);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-top: -20px;
  box-shadow: 0 6px 18px rgba(232, 70, 43, 0.32);
  transition: transform 0.2s var(--ease);
}
.tab-item.center:active .publish-btn { transform: scale(0.92); }

/* —— 登录抽屉 —— */
.login-drawer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(26, 26, 26, 0.4);
  z-index: 2000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.login-drawer {
  width: 100%;
  max-width: 520px;
  background: var(--card);
  border-radius: 16px 16px 0 0;
  padding: 14px 28px 40px;
  text-align: center;
  animation: drawer-up 0.32s var(--ease);
}
@keyframes drawer-up { from { transform: translateY(100%); } to { transform: translateY(0); } }
.drawer-handle { width: 38px; height: 4px; background: var(--line); border-radius: 2px; margin: 0 auto 18px; }
.login-drawer .eyebrow { margin-bottom: 8px; }
.drawer-title { font-size: 26px; margin-bottom: 8px; }
.drawer-sub { font-size: 14px; color: var(--ink-soft); margin-bottom: 18px; }
.login-drawer .hairline { margin: 0 0 18px; }
.drawer-btn { width: 100%; margin-bottom: 12px; }
.drawer-close-btn { font-size: 14px; color: var(--ink-mute); }

html.is-auth-page #app { max-width: 100%; padding-bottom: 0; }

@media (min-width: 1024px) {
  #app { max-width: 640px; }
  .tab-bar { max-width: 640px; }
  .login-drawer { max-width: 640px; }
}
</style>
