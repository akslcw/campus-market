<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  approveGoods,
  getGoodsDetail,
  getPendingGoods,
  rejectGoods,
  type GoodsDetail,
  type GoodsSummary
} from "@/api/admin";

defineOptions({ name: "AdminAudit" });

const loading = ref(false);
const actionId = ref<number | null>(null);
const rows = ref<GoodsSummary[]>([]);
const keyword = ref("");
const pagination = reactive({ current: 1, size: 10, total: 0 });
const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref<GoodsDetail | null>(null);
const rejectVisible = ref(false);
const rejectTarget = ref<GoodsSummary | null>(null);
const rejectReason = ref("");

const filteredRows = computed(() => {
  const value = keyword.value.trim().toLowerCase();
  if (!value) return rows.value;
  return rows.value.filter(item =>
    [item.title, item.categoryName, item.sellerName]
      .filter(Boolean)
      .some(text => text.toLowerCase().includes(value))
  );
});

async function loadPending() {
  loading.value = true;
  try {
    const response = await getPendingGoods({
      page: pagination.current,
      size: pagination.size
    });
    rows.value = response.data.records ?? [];
    pagination.total = response.data.total ?? 0;
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.msg || "待审核商品加载失败");
  } finally {
    loading.value = false;
  }
}

async function openDetail(row: GoodsSummary) {
  detailVisible.value = true;
  detailLoading.value = true;
  detail.value = null;
  try {
    const response = await getGoodsDetail(row.id);
    detail.value = response.data;
  } catch (error: any) {
    detailVisible.value = false;
    ElMessage.error(error?.response?.data?.msg || "商品详情加载失败");
  } finally {
    detailLoading.value = false;
  }
}

async function handleApprove(row: GoodsSummary) {
  try {
    await ElMessageBox.confirm(
      `通过「${row.title}」后，商品会立即出现在用户端首页。`,
      "确认审核通过",
      {
        confirmButtonText: "通过并上架",
        cancelButtonText: "取消",
        type: "warning"
      }
    );
    actionId.value = row.id;
    await approveGoods(row.id);
    ElMessage.success("审核通过，商品已上架");
    detailVisible.value = false;
    await loadPending();
  } catch (error: any) {
    if (error !== "cancel" && error !== "close") {
      ElMessage.error(error?.response?.data?.msg || "审核操作失败");
    }
  } finally {
    actionId.value = null;
  }
}

function openReject(row: GoodsSummary) {
  rejectTarget.value = row;
  rejectReason.value = "";
  rejectVisible.value = true;
}

async function submitReject() {
  if (!rejectTarget.value) return;
  const reason = rejectReason.value.trim();
  if (!reason) {
    ElMessage.warning("请填写拒绝原因");
    return;
  }
  actionId.value = rejectTarget.value.id;
  try {
    await rejectGoods(rejectTarget.value.id, reason);
    ElMessage.success("商品已拒绝，原因将展示给发布者");
    rejectVisible.value = false;
    detailVisible.value = false;
    await loadPending();
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.msg || "拒绝操作失败");
  } finally {
    actionId.value = null;
  }
}

function changePage(page: number) {
  pagination.current = page;
  loadPending();
}

function changeSize(size: number) {
  pagination.size = size;
  pagination.current = 1;
  loadPending();
}

function formatTime(value?: string) {
  return value ? value.replace("T", " ").slice(0, 16) : "-";
}

function formatPrice(value?: number) {
  return Number(value ?? 0).toFixed(2);
}

function asGoods(value: unknown) {
  return value as GoodsSummary;
}

onMounted(loadPending);
</script>

<template>
  <main class="archive-page">
    <header class="archive-head">
      <div>
        <p class="archive-kicker">CONTENT REVIEW / 内容审查</p>
        <h1>商品审核台</h1>
        <p class="archive-summary">
          审阅 AI 检测后的待审核商品。通过即上架，拒绝必须记录原因。
        </p>
      </div>
      <div class="head-actions">
        <div class="pending-count">
          <span>待处理</span>
          <strong>{{ pagination.total }}</strong>
        </div>
        <el-button
          :icon="'ri:refresh-line'"
          :loading="loading"
          @click="loadPending"
        >
          刷新
        </el-button>
      </div>
    </header>

    <section class="workbar" aria-label="审核筛选">
      <el-input
        v-model="keyword"
        clearable
        :prefix-icon="'ep:search'"
        placeholder="按标题、分类或发布者筛选当前页"
      />
      <span>第 {{ pagination.current }} 页 · 每页 {{ pagination.size }} 条</span>
    </section>

    <section class="ledger">
      <el-table
        v-loading="loading"
        :data="filteredRows"
        row-key="id"
        empty-text="当前没有待审核商品"
        @row-dblclick="openDetail"
      >
        <el-table-column prop="id" label="档案号" width="88">
          <template #default="{ row }">#{{ row.id }}</template>
        </el-table-column>
        <el-table-column label="商品" min-width="300">
          <template #default="{ row }">
            <button class="goods-cell" @click="openDetail(asGoods(row))">
              <img v-if="row.coverImage" :src="row.coverImage" :alt="row.title" />
              <span v-else class="goods-placeholder">无图</span>
              <span>
                <strong>{{ row.title }}</strong>
                <small>{{ row.categoryName }} · {{ row.sellerName }}</small>
              </span>
            </button>
          </template>
        </el-table-column>
        <el-table-column label="售价" width="130">
          <template #default="{ row }">
            <span class="money">¥{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default>
            <span class="status-mark status-mark--pending">待审核</span>
          </template>
        </el-table-column>
        <el-table-column label="处理" width="220" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button
                type="success"
                :loading="actionId === row.id"
                @click="handleApprove(asGoods(row))"
              >
                通过
              </el-button>
              <el-button
                type="danger"
                plain
                :disabled="actionId === row.id"
                @click="openReject(asGoods(row))"
              >
                拒绝
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <footer class="archive-footer">
      <el-pagination
        background
        :current-page="pagination.current"
        :page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @current-change="changePage"
        @size-change="changeSize"
      />
    </footer>

    <el-drawer
      v-model="detailVisible"
      title="商品审核档案"
      size="560px"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-sheet">
        <template v-if="detail">
          <div class="detail-id">GOODS FILE #{{ detail.id }}</div>
          <h2>{{ detail.title }}</h2>
          <div class="detail-images">
            <el-image
              v-for="image in detail.images"
              :key="image"
              :src="image"
              :preview-src-list="detail.images"
              preview-teleported
              fit="cover"
            />
          </div>
          <dl class="detail-grid">
            <div><dt>售价</dt><dd>¥{{ formatPrice(detail.price) }}</dd></div>
            <div><dt>分类</dt><dd>{{ detail.categoryName }}</dd></div>
            <div><dt>发布者</dt><dd>{{ detail.sellerName }}</dd></div>
            <div><dt>联系方式</dt><dd>{{ detail.sellerContact || "未填写" }}</dd></div>
            <div class="detail-wide">
              <dt>商品描述</dt>
              <dd>{{ detail.description }}</dd>
            </div>
          </dl>
          <div class="drawer-actions">
            <el-button type="success" @click="handleApprove(detail)">
              通过并上架
            </el-button>
            <el-button type="danger" plain @click="openReject(detail)">
              拒绝商品
            </el-button>
          </div>
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="rejectVisible"
      title="登记拒绝原因"
      width="520px"
      :close-on-click-modal="false"
    >
      <p class="dialog-note">
        {{ rejectTarget?.title }}
      </p>
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="5"
        maxlength="200"
        show-word-limit
        placeholder="说明违规点或需要修改的内容"
      />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button
          type="danger"
          :loading="actionId === rejectTarget?.id"
          @click="submitReject"
        >
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </main>
</template>

<style scoped>
.archive-page {
  min-width: 920px;
  color: #202622;
}

.archive-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 32px;
  padding: 12px 4px 24px;
  border-bottom: 2px solid #252b27;
}

.archive-kicker {
  margin: 0 0 8px;
  color: #8f2f2f;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

h1 {
  margin: 0;
  font-family: "Noto Serif SC", "Songti SC", serif;
  font-size: 30px;
  letter-spacing: 0;
}

.archive-summary {
  margin: 8px 0 0;
  color: #667068;
  font-size: 13px;
}

.head-actions,
.row-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pending-count {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding-right: 18px;
  border-right: 1px solid #c7cbc5;
}

.pending-count span {
  color: #667068;
  font-size: 12px;
}

.pending-count strong {
  color: #9d2f2f;
  font-family: Georgia, serif;
  font-size: 30px;
}

.workbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 0;
  color: #737b75;
  font-size: 12px;
}

.workbar .el-input {
  width: 380px;
}

.ledger {
  overflow: hidden;
  border: 1px solid #cfd3cd;
  border-radius: 6px;
  background: #fffef9;
}

.goods-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  text-align: left;
}

.goods-cell img,
.goods-placeholder {
  width: 54px;
  height: 54px;
  flex: 0 0 54px;
  border: 1px solid #d7d9d3;
  border-radius: 4px;
  object-fit: cover;
}

.goods-placeholder {
  display: grid;
  place-items: center;
  color: #8a918b;
  background: #f0f1ec;
  font-size: 11px;
}

.goods-cell span:last-child {
  display: grid;
  min-width: 0;
  gap: 5px;
}

.goods-cell strong {
  overflow: hidden;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-cell small {
  color: #788079;
}

.money {
  color: #9d2f2f;
  font-family: Georgia, serif;
  font-weight: 700;
}

.status-mark {
  display: inline-block;
  padding: 3px 8px;
  border: 1px solid;
  border-radius: 3px;
  font-size: 12px;
  font-weight: 600;
}

.status-mark--pending {
  color: #775b16;
  border-color: #c9a84a;
  background: #fff8dc;
}

.archive-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 18px;
}

.detail-sheet {
  min-height: 320px;
}

.detail-id {
  color: #8f2f2f;
  font-family: Consolas, monospace;
  font-size: 12px;
}

.detail-sheet h2 {
  margin: 8px 0 20px;
  font-family: "Noto Serif SC", "Songti SC", serif;
  font-size: 24px;
}

.detail-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 22px;
}

.detail-images .el-image {
  aspect-ratio: 1;
  border: 1px solid #d7d9d3;
  border-radius: 4px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  margin: 0;
  border-top: 1px solid #d7d9d3;
}

.detail-grid > div {
  padding: 14px 0;
  border-bottom: 1px solid #d7d9d3;
}

.detail-grid dt {
  margin-bottom: 5px;
  color: #788079;
  font-size: 12px;
}

.detail-grid dd {
  margin: 0;
  line-height: 1.7;
}

.detail-wide {
  grid-column: 1 / -1;
}

.drawer-actions {
  display: flex;
  gap: 10px;
  padding-top: 22px;
}

.drawer-actions .el-button {
  flex: 1;
}

.dialog-note {
  margin: 0 0 14px;
  color: #5f685f;
}
</style>
