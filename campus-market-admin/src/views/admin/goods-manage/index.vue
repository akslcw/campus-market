<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { getAllGoods, takeDownGoods, type AllGoods } from "@/api/admin";

defineOptions({
  name: "GoodsManage"
});

const loading = ref(false);
const goodsList = ref<AllGoods[]>([]);
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 });
const statusFilter = ref<number | undefined>(undefined);
const searchKeyword = ref("");
const detailDrawerVisible = ref(false);
const detailGoods = ref<AllGoods | null>(null);
const detailImageIndex = ref(0);

const fetchData = async () => {
  loading.value = true;
  try {
    const params: { page: number; size: number; status?: number; keyword?: string } = { page: pagination.currentPage, size: pagination.pageSize };
    if (statusFilter.value !== undefined) params.status = statusFilter.value;
    if (searchKeyword.value.trim()) params.keyword = searchKeyword.value.trim();
    const res = await getAllGoods(params);
    if (res?.code === 200) { goodsList.value = res.data.records || []; pagination.total = res.data.total || 0; }
    else { goodsList.value = []; pagination.total = 0; }
  } catch { ElMessage.warning("获取商品列表失败"); goodsList.value = []; pagination.total = 0; }
  finally { loading.value = false; }
};

const handlePageChange = (page: number) => { pagination.currentPage = page; fetchData(); };
const handleSizeChange = (size: number) => { pagination.pageSize = size; pagination.currentPage = 1; fetchData(); };
const handleStatusChange = () => { pagination.currentPage = 1; fetchData(); };

const handleTakeDown = (goods: AllGoods) => {
  ElMessageBox.confirm(
    `确认下架商品「${goods.title}」吗？下架后该商品将不再公开展示。`,
    "下架确认",
    { confirmButtonText: "确认下架", cancelButtonText: "取消", type: "warning", confirmButtonClass: "el-button--danger" }
  ).then(async () => {
    try {
      const res = await takeDownGoods({ goodsId: goods.id });
      if (res?.code === 200) { ElMessage.success(res.msg || "已下架"); fetchData(); }
      else ElMessage.error(res?.msg || "操作失败");
    } catch { ElMessage.error("操作失败"); }
  }).catch(() => {});
};

const openDetail = (goods: AllGoods) => { detailGoods.value = goods; detailImageIndex.value = 0; detailDrawerVisible.value = true; };

const formatTime = (time: string) => time ? time.replace("T", " ").substring(0, 19) : "-";
const formatPrice = (price: number) => `¥${price.toFixed(2)}`;
const getImageSrc = (url: string) => url || "https://picsum.photos/200/200?random=placeholder";

const statusTagType = (status: number): "warning" | "success" | "danger" | "info" => {
  switch (status) { case 0: return "warning"; case 1: return "success"; case 2: return "danger"; default: return "info"; }
};
const statusText = (status: number) => {
  switch (status) { case 0: return "待审核"; case 1: return "已上架"; case 2: return "已拒绝"; default: return "未知"; }
};

onMounted(() => { fetchData(); });
</script>

<template>
  <div class="page-container">
    <div class="page-top">
      <div class="page-title-row">
        <div class="title-left">
          <span class="title-icon" style="background: linear-gradient(135deg, #00cec9, #81ecec)">
            <IconifyIconOnline icon="ri:shopping-bag-3-line" width="22" />
          </span>
          <div>
            <h2 class="page-title">商品管理</h2>
            <p class="page-desc">查看平台所有商品，支持按下架和状态筛选</p>
          </div>
        </div>
        <el-button :icon="'ri:refresh-line'" round @click="fetchData">刷新</el-button>
      </div>

      <div class="search-row">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品标题、发布者、分类..."
          clearable
          :prefix-icon="'ep:search'"
          class="search-input"
          @keyup.enter="fetchData"
          @clear="fetchData"
        />
        <el-select v-model="statusFilter" placeholder="审核状态" clearable class="filter-select" @change="handleStatusChange">
          <el-option label="全部" :value="undefined" />
          <el-option label="待审核" :value="0" />
          <el-option label="已上架" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
      </div>
    </div>

    <div class="stats-strip" style="background: linear-gradient(135deg, #e6fffa, #dff9f3); border-color: #a3e4d7">
      <div class="strip-item">
        <span class="strip-dot" style="background:#00cec9" />
        <span class="strip-label" style="color:#0e8c7f">商品总数</span>
        <span class="strip-num" style="color:#0e8c7f">{{ pagination.total }}</span>
      </div>
    </div>

    <div class="table-card">
      <el-table v-loading="loading" :data="goodsList" border stripe empty-text="暂无商品数据" highlight-current-row @row-click="openDetail">
        <el-table-column prop="id" label="ID" width="65" align="center" />
        <el-table-column label="图片" width="90" align="center">
          <template #default="{ row }">
            <el-image :src="getImageSrc(row.images?.[0])" :preview-src-list="row.images" preview-teleported fit="cover" class="thumb-img" lazy />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="商品标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="link-title" @click.stop="openDetail(row)">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" type="info" round>{{ row.categoryName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="110" align="center" sortable>
          <template #default="{ row }">
            <span class="price-text">{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发布者" width="110" align="center">
          <template #default="{ row }">
            <span>{{ row.nickname || row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small" effect="dark" round>{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="170" align="center" sortable>
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="danger" size="small" :icon="'ri:delete-bin-line'" @click.stop="handleTakeDown(row)">下架</el-button>
            <el-tooltip v-else-if="row.status === 0" content="请前往待审核商品页面处理" placement="left">
              <el-button type="warning" size="small" disabled>审核</el-button>
            </el-tooltip>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[5, 10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="商品详情" size="520px" destroy-on-close>
      <template v-if="detailGoods">
        <div class="drawer-body">
          <div class="drawer-images">
            <el-image v-if="detailGoods.images?.length" :src="getImageSrc(detailGoods.images[detailImageIndex])" :preview-src-list="detailGoods.images" preview-teleported fit="contain" class="drawer-main-img" />
            <div v-if="detailGoods.images?.length > 1" class="drawer-thumbs">
              <div v-for="(img, idx) in detailGoods.images" :key="idx" :class="['drawer-thumb', { active: detailImageIndex === idx }]" @click="detailImageIndex = idx">
                <el-image :src="getImageSrc(img)" fit="cover" class="drawer-thumb-img" />
              </div>
            </div>
          </div>
          <div class="drawer-info">
            <h3 class="drawer-title">{{ detailGoods.title }}</h3>
            <div class="drawer-price-row">
              <span class="drawer-price">{{ formatPrice(detailGoods.price) }}</span>
              <el-tag size="small" effect="plain" type="info" round>{{ detailGoods.categoryName }}</el-tag>
              <el-tag :type="statusTagType(detailGoods.status)" size="small" effect="dark" round>{{ statusText(detailGoods.status) }}</el-tag>
            </div>
          </div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="商品ID">{{ detailGoods.id }}</el-descriptions-item>
            <el-descriptions-item label="商品描述">{{ detailGoods.description || "暂无描述" }}</el-descriptions-item>
            <el-descriptions-item label="发布者">{{ detailGoods.nickname }}（{{ detailGoods.username }}）</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ formatTime(detailGoods.createdAt) }}</el-descriptions-item>
          </el-descriptions>
          <div class="drawer-actions">
            <el-button v-if="detailGoods.status === 0" type="warning" size="large" disabled>请前往待审核商品页面处理</el-button>
            <el-button v-else-if="detailGoods.status === 1" type="danger" size="large" :icon="'ri:delete-bin-line'" @click="detailDrawerVisible = false; handleTakeDown(detailGoods!)">下架商品</el-button>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.page-container { padding: 0 4px; }

.page-top {
  background: #fff;
  border-radius: 16px;
  padding: 24px 28px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(120,70,50,.06);
  border: 1px solid #f0ddd5;
  border-top: 3px solid #ff8a65;
}

.page-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.title-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.title-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 6px 16px rgba(0,0,0,0.15);
}

.page-title { font-size: 18px; font-weight: 600; color: #4a3728; margin: 0; }
.page-desc { font-size: 12px; color: #8d6e63; margin: 4px 0 0; }

.search-row { display: flex; gap: 12px; align-items: center; }
.search-input { width: 300px; }
.filter-select { width: 130px; }

.stats-strip {
  display: flex;
  gap: 24px;
  padding: 14px 22px;
  border-radius: 14px;
  margin-bottom: 16px;
  border: 1px solid;
}

.strip-item { display: flex; align-items: center; gap: 8px; }
.strip-dot { width: 8px; height: 8px; border-radius: 50%; }
.strip-label { font-size: 13px; }
.strip-num { font-size: 20px; font-weight: 700; }

.table-card {
  background: #fff;
  border-radius: 16px;
  padding: 4px;
  box-shadow: 0 2px 12px rgba(120,70,50,.06);
  border: 1px solid #f0ddd5;
  overflow: hidden;
  margin-bottom: 16px;
}

.thumb-img { width: 60px; height: 60px; border-radius: 8px; cursor: pointer; }
.thumb-img :deep(img) { border-radius: 8px; }

.link-title { color: #409eff; cursor: pointer; }
.link-title:hover { color: #337ecc; text-decoration: underline; }

.price-text { color: #f56c6c; font-weight: 600; font-size: 14px; }
.text-muted { color: #bcaaa4; font-size: 13px; }

.pagination-bar { display: flex; justify-content: flex-end; }

/* 抽屉 */
.drawer-body { display: flex; flex-direction: column; gap: 20px; }
.drawer-images { display: flex; flex-direction: column; gap: 10px; }
.drawer-main-img { width: 100%; height: 300px; border-radius: 12px; background: #f5f7fa; }
.drawer-main-img :deep(img) { border-radius: 12px; object-fit: contain; }
.drawer-thumbs { display: flex; gap: 8px; overflow-x: auto; }
.drawer-thumb { width: 64px; height: 64px; border-radius: 8px; overflow: hidden; cursor: pointer; border: 2px solid transparent; flex-shrink: 0; }
.drawer-thumb.active { border-color: #409eff; }
.drawer-thumb-img { width: 100%; height: 100%; }
.drawer-thumb-img :deep(img) { border-radius: 6px; }
.drawer-info { display: flex; flex-direction: column; gap: 10px; }
.drawer-title { font-size: 20px; font-weight: 600; color: #4a3728; margin: 0; line-height: 1.4; }
.drawer-price-row { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.drawer-price { font-size: 26px; font-weight: 700; color: #f56c6c; }
.drawer-actions { display: flex; gap: 12px; margin-top: 8px; padding-top: 16px; border-top: 1px solid #f0ddd5; }
.drawer-actions .el-button { flex: 1; }
</style>