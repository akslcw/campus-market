<template>
  <div class="profile">
    <!-- 资料卡 -->
    <header class="profile-head">
      <div class="profile-top">
        <span class="eyebrow">MY&nbsp;ACCOUNT</span>
        <button class="logout-link" @click="onLogout">退出登录</button>
      </div>
      <div class="user-row">
        <div class="avatar serif">{{ avatarChar }}</div>
        <div class="user-info">
          <div class="user-name-line">
            <span class="serif user-nick">{{ profile?.nickname || userStore.nickname }}</span>
            <span class="tag" :class="isAdmin ? 'tag--brand' : 'tag--mute'">{{ isAdmin ? '管理员' : '用户' }}</span>
          </div>
          <div class="muted user-account">@{{ profile?.username || userStore.username }}</div>
        </div>
        <button class="edit-btn" @click="openEdit"><el-icon><Edit /></el-icon></button>
      </div>
      <div v-if="profile?.contact" class="contact-line">
        <span class="muted">联系方式</span><span>{{ profile.contact }}</span>
      </div>

      <button v-if="isAdmin" class="btn-ghost admin-entry" @click="goAdmin">
        <el-icon><Setting /></el-icon> 进入后台管理
      </button>
    </header>

    <hr class="rule-ink" style="margin: 0 18px" />

    <!-- 我的发布 -->
    <section class="my-goods">
      <div class="section-title">
        <h2 class="serif">我的发布</h2>
        <span class="muted">{{ total }} 件</span>
      </div>

      <div v-loading="loading">
        <div v-if="!loading && list.length === 0" class="empty">
          <el-empty description="还没有发布商品">
            <button class="btn-ink btn-ink--brand" @click="router.push('/publish')">去发布</button>
          </el-empty>
        </div>

        <article v-for="g in list" :key="g.id" class="row paper-card">
          <div class="row-cover" @click="router.push(`/goods/${g.id}`)">
            <img v-if="g.coverImage" :src="g.coverImage" :alt="g.title" />
            <div v-else class="row-cover-empty">无图</div>
          </div>
          <div class="row-main" @click="router.push(`/goods/${g.id}`)">
            <h3 class="serif row-title clamp-1">{{ g.title }}</h3>
            <div class="row-price price"><span class="unit">¥</span>{{ formatPrice(g.price) }}</div>
            <div class="row-tags">
              <span class="tag" :class="auditClass(g.auditStatus || 'PENDING')">{{ auditText(g.auditStatus || 'PENDING') }}</span>
              <span class="tag tag--mute">{{ statusText(g.status) }}</span>
            </div>
          </div>
          <div class="row-actions">
            <button
              v-if="g.status === 'ON_SALE'"
              class="off-btn"
              @click.stop="onOffline(g)"
            >下架</button>
          </div>
        </article>

        <div v-if="pages > 1" class="pager">
          <el-pagination background layout="prev, pager, next" :total="total" :page-size="size" :current-page="current" @current-change="onPage" />
        </div>
      </div>
    </section>

    <!-- 编辑资料 -->
    <el-dialog v-model="editVisible" title="编辑资料" width="92%" style="max-width: 420px">
      <el-form :model="editForm" label-position="top">
        <el-form-item label="昵称"><el-input v-model="editForm.nickname" maxlength="32" /></el-form-item>
        <el-form-item label="联系方式"><el-input v-model="editForm.contact" placeholder="微信 / QQ / 手机" /></el-form-item>
      </el-form>
      <template #footer>
        <button class="btn-ghost" style="height: 40px" @click="editVisible = false">取消</button>
        <button class="btn-ink btn-ink--brand" style="height: 40px; margin-left: 10px" @click="saveEdit">保存</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getProfile, updateProfile, type UserProfileVO } from '@/api/user'
import { getMyGoods, offlineGoods, type GoodsVO } from '@/api/goods'
import { logout } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const profile = ref<UserProfileVO | null>(null)
const isAdmin = computed(() => (profile.value?.role || userStore.role) === 'ADMIN')
const avatarChar = computed(() => (profile.value?.nickname || userStore.nickname || '?').charAt(0).toUpperCase())

const list = ref<GoodsVO[]>([])
const total = ref(0)
const pages = ref(0)
const current = ref(1)
const size = ref(10)
const loading = ref(false)

const formatPrice = (p: number) => (Number.isInteger(p) ? String(p) : p.toFixed(2))
const statusText = (s: string) => ({ ON_SALE: '在售', SOLD: '已售', OFF_SHELF: '已下架' }[s] || s)
const auditText = (s: string) => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }[s] || s)
const auditClass = (s: string) => ({ PENDING: 'tag--mute', APPROVED: 'tag--olive', REJECTED: 'tag--brand' }[s] || 'tag--mute')

async function loadProfile() {
  try { profile.value = await getProfile() } catch { /* 静默 */ }
}

async function loadGoods() {
  loading.value = true
  try {
    const res = await getMyGoods({ page: current.value, size: size.value })
    list.value = res.records
    total.value = res.total
    pages.value = res.pages
    current.value = res.current
  } finally {
    loading.value = false
  }
}

function onPage(p: number) { current.value = p; loadGoods() }

async function onOffline(g: GoodsVO) {
  try {
    await ElMessageBox.confirm(`确定下架「${g.title}」吗？`, '下架商品', { type: 'warning' })
    await offlineGoods(g.id)
    ElMessage.success('已下架')
    loadGoods()
  } catch { /* 取消 */ }
}

// 编辑资料
const editVisible = ref(false)
const editForm = reactive({ nickname: '', contact: '' })
function openEdit() {
  editForm.nickname = profile.value?.nickname || ''
  editForm.contact = profile.value?.contact || ''
  editVisible.value = true
}
async function saveEdit() {
  try {
    await updateProfile({ nickname: editForm.nickname, contact: editForm.contact })
    ElMessage.success('已保存')
    editVisible.value = false
    loadProfile()
  } catch { /* 拦截器已提示 */ }
}

function goAdmin() { ElMessage.info('后台管理由管理端负责，敬请期待') }

async function onLogout() {
  try { await logout() } catch { /* 忽略 */ }
  userStore.logout()
  router.push('/home')
}

onMounted(() => { loadProfile(); loadGoods() })
</script>

<style scoped>
.profile { padding-bottom: 30px; }

.profile-head { padding: 22px 18px 18px; }
.profile-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.logout-link { font-size: 13px; color: var(--ink-mute); }
.logout-link:hover { color: var(--brand); }

.user-row { display: flex; align-items: center; gap: 14px; }
.avatar {
  width: 60px; height: 60px; border-radius: 50%;
  background: var(--ink); color: var(--paper);
  display: flex; align-items: center; justify-content: center;
  font-size: 26px; font-weight: 700; flex-shrink: 0;
}
.user-info { flex: 1; }
.user-name-line { display: flex; align-items: center; gap: 8px; }
.user-nick { font-size: 22px; font-weight: 700; }
.user-account { font-size: 13px; margin-top: 2px; }
.edit-btn {
  width: 40px; height: 40px;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid var(--line); border-radius: var(--radius); background: var(--card);
  color: var(--ink-soft);
}
.edit-btn:hover { border-color: var(--ink); color: var(--ink); }

.contact-line {
  display: flex; justify-content: space-between;
  margin-top: 14px; padding: 10px 14px;
  border: 1px dashed var(--line); border-radius: var(--radius); font-size: 14px;
}
.admin-entry { width: 100%; margin-top: 16px; }

.my-goods { padding: 18px; }
.section-title { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 14px; }
.section-title h2 { font-size: 20px; font-weight: 700; }

.row { display: flex; gap: 12px; padding: 12px; margin-bottom: 12px; }
.row-cover { width: 76px; height: 76px; border-radius: var(--radius); overflow: hidden; flex-shrink: 0; cursor: pointer; background: var(--paper-2); }
.row-cover img { width: 100%; height: 100%; object-fit: cover; }
.row-cover-empty { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 12px; color: var(--ink-mute); }
.row-main { flex: 1; min-width: 0; cursor: pointer; }
.row-title { font-size: 15px; font-weight: 600; }
.row-price { font-size: 18px; margin: 4px 0 8px; }
.row-tags { display: flex; gap: 6px; }
.row-actions { display: flex; align-items: flex-start; }
.off-btn {
  font-size: 13px; color: var(--ink-soft);
  border: 1px solid var(--line); border-radius: var(--radius);
  padding: 4px 12px;
}
.off-btn:hover { border-color: var(--brand); color: var(--brand); }

.empty { padding: 30px 0; }
.pager { display: flex; justify-content: center; margin-top: 18px; }
</style>
