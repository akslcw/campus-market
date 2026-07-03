<template>
  <div class="auth">
    <div class="auth-inner">
      <header class="auth-head">
        <p class="eyebrow">JOIN&nbsp;US</p>
        <h1 class="display auth-title">注册账号</h1>
        <p class="auth-sub">加入校园集市，让闲置流动起来</p>
        <hr class="rule-ink" style="margin-top: 16px" />
      </header>

      <el-form :model="form" label-position="top" class="auth-form" @submit.prevent @keyup.enter="onRegister">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="4-16 位，字母或数字" size="large" />
        </el-form-item>
        <el-form-item label="昵称（选填）">
          <el-input v-model="form.nickname" placeholder="不填则与用户名相同" size="large" maxlength="32" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="须含大小写字母和数字" size="large" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirm" type="password" show-password placeholder="再输入一次密码" size="large" />
        </el-form-item>

        <p class="pw-hint muted">密码须同时包含大写字母、小写字母和数字，例如 <b>Test1234</b></p>

        <button type="button" class="btn-ink btn-ink--brand auth-submit" :disabled="loading" @click.prevent="onRegister">
          {{ loading ? '注册中…' : '注 册' }}
        </button>
      </el-form>

      <div class="auth-foot">
        <span class="muted">已有账号？</span>
        <span class="link" @click="goLogin">去登录</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const route = useRoute()
const router = useRouter()

const form = reactive({ username: '', nickname: '', password: '', confirm: '' })
const loading = ref(false)

const pwOk = (p: string) => /[a-z]/.test(p) && /[A-Z]/.test(p) && /\d/.test(p)

async function onRegister() {
  if (loading.value) return
  const username = form.username.trim()
  const nickname = form.nickname.trim()
  if (!username || username.length < 3 || username.length > 20) { ElMessage.warning('用户名长度需为 3-20 位'); return }
  if (!pwOk(form.password)) { ElMessage.warning('密码须含大写字母、小写字母和数字'); return }
  if (form.password !== form.confirm) { ElMessage.warning('两次密码不一致'); return }
  loading.value = true
  try {
    await register({ username, password: form.password, nickname: nickname || undefined })
    ElMessage.success('注册成功，请登录')
    router.replace({ path: '/login', query: route.query })
  } catch { /* 拦截器已提示（如用户名已存在） */ } finally {
    loading.value = false
  }
}

function goLogin() { router.push({ path: '/login', query: route.query }) }
</script>

<style scoped>
.auth { min-height: 100vh; background: var(--paper); display: flex; align-items: center; justify-content: center; padding: 24px; }
.auth-inner { width: 100%; max-width: 380px; }
.auth-head { margin-bottom: 24px; }
.auth-title { font-size: 38px; margin: 10px 0 6px; }
.auth-sub { font-size: 14px; color: var(--ink-soft); }
.auth-form { margin-bottom: 16px; }
.pw-hint { font-size: 12px; margin: -4px 0 14px; }
.pw-hint b { color: var(--olive); font-family: var(--font-serif); }
.auth-submit { width: 100%; height: 50px; font-size: 16px; letter-spacing: 0.3em; }
.auth-foot { text-align: center; font-size: 14px; }
.link { color: var(--brand); font-weight: 600; cursor: pointer; margin-left: 4px; }
.link:hover { color: var(--brand-ink); }
</style>
