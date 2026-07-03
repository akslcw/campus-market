<template>
  <div class="detail" v-loading="loading">
    <!-- 顶栏 -->
    <div class="topbar">
      <button class="icon-btn" @click="router.back()"><el-icon size="20"><ArrowLeft /></el-icon></button>
      <span class="topbar-title">商品详情</span>
      <span style="width: 40px"></span>
    </div>

    <template v-if="goods">
      <!-- 图片 -->
      <div class="gallery">
        <el-carousel v-if="goods.images && goods.images.length" height="360px" :autoplay="false" indicator-position="outside">
          <el-carousel-item v-for="(img, i) in goods.images" :key="i">
            <img :src="img" class="gallery-img" :alt="goods.title" />
          </el-carousel-item>
        </el-carousel>
        <div v-else class="gallery-empty">暂无图片</div>
      </div>

      <!-- 主信息 -->
      <section class="block">
        <div class="price-row">
          <span class="price big"><span class="unit">¥</span>{{ formatPrice(goods.price) }}</span>
          <span v-if="goods.originalPrice" class="price-original">原价 ¥{{ formatPrice(goods.originalPrice) }}</span>
          <span class="tag tag--mute status-tag">{{ statusText(goods.status) }}</span>
        </div>
        <h1 class="serif goods-title">{{ goods.title }}</h1>
        <div class="meta-line">
          <span class="tag">{{ goods.categoryName }}</span>
          <span class="muted">{{ goods.viewCount }} 次浏览</span>
        </div>
      </section>

      <hr class="hairline" />

      <!-- 描述 -->
      <section class="block">
        <p class="eyebrow">商品描述</p>
        <p class="desc">{{ goods.description }}</p>
      </section>

      <hr class="hairline" />

      <!-- 卖家 -->
      <section class="block">
        <p class="eyebrow">卖家信息</p>
        <div class="seller">
          <div class="seller-avatar serif">{{ (goods.sellerName || '?').charAt(0).toUpperCase() }}</div>
          <div class="seller-info">
            <div class="seller-name">{{ goods.sellerName }}</div>
            <div class="muted" style="font-size: 13px">{{ publishedAt }}</div>
          </div>
        </div>
        <div v-if="goods.sellerContact" class="contact-box">
          <span class="muted">联系方式</span>
          <span class="contact-val">{{ goods.sellerContact }}</span>
        </div>
      </section>

      <!-- 底部操作 -->
      <div class="action-bar">
        <button class="btn-ghost back-btn" @click="router.push('/home')">继续逛逛</button>
        <button class="btn-ink btn-ink--brand contact-btn" @click="onContact">
          {{ goods.sellerContact ? '联系卖家' : '查看卖家' }}
        </button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getGoodsDetail, type GoodsDetailVO } from '@/api/goods'

const route = useRoute()
const router = useRouter()

const goods = ref<GoodsDetailVO | null>(null)
const loading = ref(false)

const formatPrice = (p: number) => (Number.isInteger(p) ? String(p) : p.toFixed(2))
const statusText = (s: string) => ({ ON_SALE: '在售', SOLD: '已售', OFF_SHELF: '已下架' }[s] || s)
const publishedAt = computed(() =>
  goods.value ? new Date(goods.value.createdAt).toLocaleDateString('zh-CN') + ' 发布' : ''
)

async function load() {
  loading.value = true
  try {
    goods.value = await getGoodsDetail(Number(route.params.id))
  } finally {
    loading.value = false
  }
}

function onContact() {
  if (goods.value?.sellerContact) {
    ElMessageBox.alert(goods.value.sellerContact, `联系 ${goods.value.sellerName}`, {
      confirmButtonText: '知道了',
    })
  } else {
    ElMessage.info('该卖家未留下联系方式')
  }
}

onMounted(load)
</script>

<style scoped>
.detail { padding-bottom: 90px; }

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  position: sticky;
  top: 0;
  background: var(--paper);
  border-bottom: 1px solid var(--line);
  z-index: 50;
}
.topbar-title { font-size: 15px; font-weight: 600; }
.icon-btn {
  width: 40px; height: 40px;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid var(--line); border-radius: var(--radius);
  background: var(--card);
}

.gallery { background: var(--paper-2); }
.gallery-img { width: 100%; height: 360px; object-fit: cover; }
.gallery-empty {
  height: 360px; display: flex; align-items: center; justify-content: center;
  color: var(--ink-mute);
}

.block { padding: 18px; }
.price-row { display: flex; align-items: baseline; gap: 12px; flex-wrap: wrap; }
.price.big { font-size: 34px; }
.status-tag { margin-left: auto; align-self: center; }
.goods-title { font-size: 22px; font-weight: 700; line-height: 1.4; margin: 12px 0 10px; }
.meta-line { display: flex; align-items: center; gap: 12px; }
.meta-line .muted { font-size: 13px; }

.desc { font-size: 15px; line-height: 1.9; color: var(--ink-soft); white-space: pre-wrap; margin-top: 10px; }

.seller { display: flex; align-items: center; gap: 12px; margin-top: 12px; }
.seller-avatar {
  width: 46px; height: 46px; border-radius: 50%;
  background: var(--ink); color: var(--paper);
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; font-weight: 700;
}
.seller-name { font-size: 16px; font-weight: 600; }
.contact-box {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 14px; padding: 12px 14px;
  border: 1px dashed var(--line); border-radius: var(--radius);
  font-size: 14px;
}
.contact-val { font-weight: 600; color: var(--olive); }

.action-bar {
  position: fixed;
  bottom: 0; left: 50%;
  transform: translateX(-50%);
  width: 100%; max-width: 520px;
  display: flex; gap: 12px;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  background: var(--paper);
  border-top: 1px solid var(--line);
  z-index: 100;
}
.back-btn { flex: 1; }
.contact-btn { flex: 2; }

@media (min-width: 1024px) {
  .action-bar { max-width: 640px; }
}
</style>
