<template>
  <div class="auth">
    <div class="auth-inner">
      <header class="auth-head">
        <p class="eyebrow">CAMPUS&nbsp;MARKET</p>
        <h1 class="display auth-title">校园集市</h1>
        <p class="auth-sub">登录后发布闲置、管理你的商品</p>
        <hr class="rule-ink" style="margin-top: 16px" />
      </header>

      <el-form :model="form" label-position="top" class="auth-form" @submit.prevent @keyup.enter="onLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" size="large" />
        </el-form-item>

        <button type="button" class="btn-ink btn-ink--brand auth-submit" :disabled="loading" @click.prevent="onLogin">
          {{ loading ? '登录中…' : '登 录' }}
        </button>
      </el-form>

      <div class="auth-foot">
        <span class="muted">还没有账号？</span>
        <span class="link" @click="goRegister">立即注册</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const form = reactive({ username: '', password: '' })
const loading = ref(false)

async function onLogin() {
  if (loading.value) return
  const username = form.username.trim()
  if (!username || !form.password) { ElMessage.warning('请输入用户名和密码'); return }
  loading.value = true
  try {
    const vo = await login({ username, password: form.password })
    userStore.setLogin(vo)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/home'
    router.replace(redirect)
  } catch { /* 拦截器已提示 */ } finally {
    loading.value = false
  }
}

function goRegister() {
  router.push({ path: '/register', query: route.query })
}
</script>

<style scoped>
.auth { min-height: 100vh; background: var(--paper); display: flex; align-items: center; justify-content: center; padding: 24px; }
.auth-inner { width: 100%; max-width: 380px; }
.auth-head { margin-bottom: 28px; }
.auth-title { font-size: 40px; margin: 10px 0 6px; }
.auth-sub { font-size: 14px; color: var(--ink-soft); }
.auth-form { margin-bottom: 18px; }
.auth-submit { width: 100%; height: 50px; font-size: 16px; letter-spacing: 0.3em; margin-top: 6px; }
.auth-foot { text-align: center; font-size: 14px; }
.link { color: var(--brand); font-weight: 600; cursor: pointer; margin-left: 4px; }
.link:hover { color: var(--brand-ink); }
</style>
