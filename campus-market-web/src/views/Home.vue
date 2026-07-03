<template>
  <div class="home">
    <!-- 杂志刊头 -->
    <header class="masthead">
      <div class="masthead-top">
        <span class="eyebrow">CAMPUS&nbsp;MARKET&nbsp;·&nbsp;第 02 期</span>
        <span class="masthead-date">{{ today }}</span>
      </div>
      <h1 class="display masthead-title">校园集市</h1>
      <p class="masthead-sub">二手好物 · 让闲置在校园里流动起来</p>
      <hr class="rule-ink masthead-rule" />
    </header>

    <!-- 搜索 -->
    <div class="search-row">
      <el-input
        v-model="keyword"
        placeholder="搜点什么…  iPhone / 教材 / 自行车"
        clearable
        @keyup.enter="onSearch"
        @clear="onSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <button class="btn-ink search-btn" @click="onSearch">搜索</button>
    </div>

    <!-- 分类筛选（横滑） -->
    <nav class="cat-bar">
      <button class="cat-chip" :class="{ on: activeCat === null }" @click="selectCat(null)">全部</button>
      <button
        v-for="c in categories"
        :key="c.id"
        class="cat-chip"
        :class="{ on: activeCat === c.id }"
        @click="selectCat(c.id)"
      >{{ c.name }}</button>
    </nav>

    <!-- 排序 + 计数 -->
    <div class="sort-row">
      <span class="count-text">共 <b>{{ total }}</b> 件在售</span>
      <div class="sort-tabs">
        <button :class="{ on: sort === 'newest' }" @click="changeSort('newest')">最新</button>
        <span class="sort-sep">/</span>
        <button :class="{ on: sort === 'price_asc' }" @click="changeSort('price_asc')">价低</button>
        <span class="sort-sep">/</span>
        <button :class="{ on: sort === 'price_desc' }" @click="changeSort('price_desc')">价高</button>
      </div>
    </div>

    <hr class="hairline" />

    <!-- 商品网格 -->
    <div v-loading="loading" class="goods-wrap">
      <div v-if="!loading && list.length === 0" class="empty">
        <el-empty description="还没有商品，去发布第一件吧" />
      </div>

      <div class="goods-grid">
        <article
          v-for="(g, i) in list"
          :key="g.id"
          class="goods-card paper-card reveal"
          :class="`reveal-delay-${(i % 3) + 1}`"
          @click="goDetail(g.id)"
        >
          <div class="cover">
            <img v-if="g.coverImage" :src="g.coverImage" :alt="g.title" />
            <div v-else class="cover-empty">暂无图片</div>
            <span v-if="g.status === 'SOLD'" class="cover-flag">已售</span>
          </div>
          <div class="card-body">
            <h3 class="serif card-title clamp-2">{{ g.title }}</h3>
            <div class="card-meta">
              <span class="tag tag--mute">{{ g.categoryName }}</span>
              <span class="views">{{ g.viewCount }} 浏览</span>
            </div>
            <div class="card-price">
              <span class="price"><span class="unit">¥</span>{{ formatPrice(g.price) }}</span>
              <span v-if="g.originalPrice" class="price-original">¥{{ formatPrice(g.originalPrice) }}</span>
            </div>
          </div>
        </article>
      </div>

      <!-- 分页 -->
      <div v-if="pages > 1" class="pager">
        <button
          class="pager-btn"
          :disabled="current <= 1"
          @click="onPageChange(current - 1)"
        >上一页</button>
        <span class="pager-info">第 {{ current }} / {{ pages }} 页</span>
        <button
          class="pager-btn"
          :disabled="current >= pages"
          @click="onPageChange(current + 1)"
        >下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useReveal } from '@/composables/useReveal'
import { getGoodsList, type GoodsVO } from '@/api/goods'
import { getCategories, type CategoryVO } from '@/api/category'

const router = useRouter()
const { refresh: refreshReveal } = useReveal()

const today = new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })

const keyword = ref('')
const activeCat = ref<number | null>(null)
const sort = ref<'newest' | 'price_asc' | 'price_desc'>('newest')
const categories = ref<CategoryVO[]>([])

const list = ref<GoodsVO[]>([])
const total = ref(0)
const pages = ref(0)
const current = ref(1)
const size = ref(10)
const loading = ref(false)

const formatPrice = (p: number) => (Number.isInteger(p) ? String(p) : p.toFixed(2))

async function loadCategories() {
  try {
    categories.value = await getCategories()
  } catch { /* 静默，分类失败不阻断列表 */ }
}

async function loadGoods() {
  loading.value = true
  try {
    const res = await getGoodsList({
      keyword: keyword.value || undefined,
      categoryId: activeCat.value ?? undefined,
      sort: sort.value,
      page: current.value,
      size: size.value,
    })
    list.value = res.records
    total.value = res.total
    pages.value = res.pages
    current.value = res.current
    await nextTick()
    refreshReveal()
  } finally {
    loading.value = false
  }
}

function onSearch() { current.value = 1; loadGoods() }
function selectCat(id: number | null) { activeCat.value = id; current.value = 1; loadGoods() }
function changeSort(s: typeof sort.value) { sort.value = s; current.value = 1; loadGoods() }
function onPageChange(p: number) { current.value = p; loadGoods(); window.scrollTo({ top: 0, behavior: 'smooth' }) }
function goDetail(id: number) { router.push(`/goods/${id}`) }

onMounted(() => { loadCategories(); loadGoods() })
</script>

<style scoped>
.home { padding: 0 18px 24px; }

/* —— 刊头 —— */
.masthead { padding: 22px 0 0; }
.masthead-top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.masthead-date { font-size: 12px; color: var(--ink-mute); letter-spacing: 0.05em; }
.masthead-title { font-size: 44px; margin: 10px 0 4px; }
.masthead-sub { font-size: 14px; color: var(--ink-soft); }
.masthead-rule { margin: 14px 0 18px; }

/* —— 搜索 —— */
.search-row { display: flex; gap: 10px; margin-bottom: 16px; }
.search-row :deep(.el-input) { flex: 1; }
.search-row :deep(.el-input__wrapper) { height: 44px; }
.search-btn { height: 44px; padding: 0 22px; flex-shrink: 0; }

/* —— 分类 —— */
.cat-bar {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
  margin-bottom: 14px;
  scrollbar-width: none;
}
.cat-bar::-webkit-scrollbar { display: none; }
.cat-chip {
  flex-shrink: 0;
  padding: 6px 14px;
  font-size: 14px;
  color: var(--ink-soft);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--card);
  transition: all 0.18s var(--ease);
}
.cat-chip:hover { border-color: var(--ink); }
.cat-chip.on {
  background: var(--ink);
  color: var(--paper);
  border-color: var(--ink);
  font-weight: 600;
}

/* —— 排序 —— */
.sort-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.count-text { font-size: 13px; color: var(--ink-mute); }
.count-text b { color: var(--brand); font-family: var(--font-serif); }
.sort-tabs { display: flex; align-items: center; gap: 6px; font-size: 14px; }
.sort-tabs button { color: var(--ink-mute); }
.sort-tabs button.on { color: var(--ink); font-weight: 700; }
.sort-sep { color: var(--line); }

/* —— 商品网格 —— */
.goods-wrap { min-height: 200px; padding-top: 16px; }
.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}
.goods-card { overflow: hidden; cursor: pointer; }
.cover {
  position: relative;
  aspect-ratio: 1 / 1;
  background: var(--paper-2);
  overflow: hidden;
}
.cover img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.4s var(--ease); }
.goods-card:hover .cover img { transform: scale(1.04); }
.cover-empty {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  color: var(--ink-mute); font-size: 13px;
}
.cover-flag {
  position: absolute; top: 8px; left: 8px;
  background: var(--ink); color: var(--paper);
  font-size: 12px; padding: 2px 8px; border-radius: var(--radius);
}
.card-body { padding: 10px 12px 14px; }
.card-title { font-size: 15px; font-weight: 700; line-height: 1.4; min-height: 42px; }
.card-meta {
  display: flex; align-items: center; justify-content: space-between;
  margin: 8px 0;
}
.views { font-size: 12px; color: var(--ink-mute); }
.card-price { display: flex; align-items: baseline; gap: 8px; }
.card-price .price { font-size: 20px; }

.empty { padding: 40px 0; }
.pager {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
  padding-bottom: calc(96px + env(safe-area-inset-bottom));
}
.pager-btn {
  min-width: 86px;
  height: 40px;
  padding: 0 14px;
  border: 1px solid var(--ink);
  border-radius: var(--radius);
  background: var(--ink);
  color: var(--paper);
  font-size: 14px;
  font-weight: 600;
}
.pager-btn:disabled {
  cursor: not-allowed;
  color: var(--ink-mute);
  background: var(--paper-2);
  border-color: var(--line);
}
.pager-info {
  min-width: 82px;
  text-align: center;
  color: var(--ink-soft);
  font-size: 13px;
}
</style>
