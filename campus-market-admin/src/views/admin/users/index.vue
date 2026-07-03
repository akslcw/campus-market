<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  banUser,
  getUsers,
  unbanUser,
  type PlatformUser
} from "@/api/admin";

defineOptions({ name: "AdminUsers" });

const loading = ref(false);
const actionId = ref<number | null>(null);
const rows = ref<PlatformUser[]>([]);
const keyword = ref("");
const statusFilter = ref<"ALL" | "ACTIVE" | "BANNED">("ALL");
const pagination = reactive({ current: 1, size: 50, total: 0 });

const filteredRows = computed(() => {
  const value = keyword.value.trim().toLowerCase();
  return rows.value.filter(user => {
    const matchesKeyword =
      !value ||
      [user.username, user.nickname, user.contact]
        .filter(Boolean)
        .some(text => text.toLowerCase().includes(value));
    const matchesStatus =
      statusFilter.value === "ALL" ||
      (statusFilter.value === "ACTIVE" && user.status === 0) ||
      (statusFilter.value === "BANNED" && user.status === 1);
    return matchesKeyword && matchesStatus;
  });
});

async function loadUsers() {
  loading.value = true;
  try {
    const response = await getUsers({
      page: pagination.current,
      size: pagination.size
    });
    rows.value = response.data.records ?? [];
    pagination.total = response.data.total ?? 0;
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.msg || "用户列表加载失败");
  } finally {
    loading.value = false;
  }
}

async function toggleBan(user: PlatformUser) {
  const isBanned = user.status === 1;
  const action = isBanned ? "解封" : "封禁";
  try {
    await ElMessageBox.confirm(
      isBanned
        ? `解封「${user.nickname || user.username}」后，该账号可重新登录。`
        : `封禁「${user.nickname || user.username}」后，该账号将无法登录。`,
      `确认${action}`,
      {
        confirmButtonText: action,
        cancelButtonText: "取消",
        type: isBanned ? "success" : "warning"
      }
    );
    actionId.value = user.id;
    if (isBanned) await unbanUser(user.id);
    else await banUser(user.id);
    ElMessage.success(`账号已${action}`);
    await loadUsers();
  } catch (error: any) {
    if (error !== "cancel" && error !== "close") {
      ElMessage.error(error?.response?.data?.msg || `${action}失败`);
    }
  } finally {
    actionId.value = null;
  }
}

function changePage(page: number) {
  pagination.current = page;
  loadUsers();
}

function changeSize(size: number) {
  pagination.size = size;
  pagination.current = 1;
  loadUsers();
}

function formatTime(value?: string) {
  return value ? value.replace("T", " ").slice(0, 16) : "-";
}

function asUser(value: unknown) {
  return value as PlatformUser;
}

onMounted(loadUsers);
</script>

<template>
  <main class="archive-page">
    <header class="archive-head">
      <div>
        <p class="archive-kicker">ACCOUNT REGISTRY / 账号名册</p>
        <h1>用户管理</h1>
        <p class="archive-summary">
          管理平台注册用户。封禁后账号立即失去登录权限，解封后恢复。
        </p>
      </div>
      <div class="head-actions">
        <div class="user-count">
          <span>登记用户</span>
          <strong>{{ pagination.total }}</strong>
        </div>
        <el-button
          :icon="'ri:refresh-line'"
          :loading="loading"
          @click="loadUsers"
        >
          刷新
        </el-button>
      </div>
    </header>

    <section class="workbar" aria-label="用户筛选">
      <el-input
        v-model="keyword"
        clearable
        :prefix-icon="'ep:search'"
        placeholder="搜索用户名、昵称或联系方式"
      />
      <el-segmented
        v-model="statusFilter"
        :options="[
          { label: '全部', value: 'ALL' },
          { label: '正常', value: 'ACTIVE' },
          { label: '已封禁', value: 'BANNED' }
        ]"
      />
    </section>

    <section class="ledger">
      <el-table
        v-loading="loading"
        :data="filteredRows"
        row-key="id"
        empty-text="没有符合条件的用户"
      >
        <el-table-column prop="id" label="编号" width="80">
          <template #default="{ row }">#{{ row.id }}</template>
        </el-table-column>
        <el-table-column label="用户" min-width="250">
          <template #default="{ row }">
            <div class="identity">
              <el-avatar :src="row.avatar || undefined">
                {{ (row.nickname || row.username).slice(0, 1) }}
              </el-avatar>
              <span>
                <strong>{{ row.nickname || row.username }}</strong>
                <small>@{{ row.username }}</small>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="联系方式" min-width="180">
          <template #default="{ row }">{{ row.contact || "未填写" }}</template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="账号状态" width="120">
          <template #default="{ row }">
            <span
              class="status-mark"
              :class="
                row.status === 1
                  ? 'status-mark--banned'
                  : 'status-mark--active'
              "
            >
              {{ row.status === 1 ? "已封禁" : "正常" }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button
              :type="row.status === 1 ? 'success' : 'danger'"
              :plain="row.status === 0"
              :loading="actionId === row.id"
              @click="toggleBan(asUser(row))"
            >
              {{ row.status === 1 ? "解封" : "封禁" }}
            </el-button>
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
  </main>
</template>

<style scoped>
.archive-page {
  min-width: 900px;
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
  color: #285b52;
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

.head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-count {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding-right: 18px;
  border-right: 1px solid #c7cbc5;
}

.user-count span {
  color: #667068;
  font-size: 12px;
}

.user-count strong {
  color: #285b52;
  font-family: Georgia, serif;
  font-size: 30px;
}

.workbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 0;
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

.identity {
  display: flex;
  align-items: center;
  gap: 12px;
}

.identity span {
  display: grid;
  gap: 4px;
}

.identity strong {
  font-weight: 600;
}

.identity small {
  color: #788079;
}

.status-mark {
  display: inline-block;
  padding: 3px 8px;
  border: 1px solid;
  border-radius: 3px;
  font-size: 12px;
  font-weight: 600;
}

.status-mark--active {
  color: #285b52;
  border-color: #7ca096;
  background: #edf6f1;
}

.status-mark--banned {
  color: #912f2f;
  border-color: #c98383;
  background: #fff0ee;
}

.archive-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 18px;
}
</style>
