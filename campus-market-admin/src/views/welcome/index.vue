<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStoreHook } from "@/store/modules/user";

defineOptions({ name: "Welcome" });

const router = useRouter();
const userName = ref(useUserStoreHook().nickname || useUserStoreHook().username || "管理员");

const currentTime = ref("");
const updateTime = () => {
  const now = new Date();
  const h = now.getHours();
  const prefix = h < 6 ? "夜深了" : h < 9 ? "早上好" : h < 12 ? "上午好" : h < 14 ? "中午好" : h < 18 ? "下午好" : h < 22 ? "晚上好" : "夜深了";
  currentTime.value = `${prefix}，${userName.value}`;
};

const stats = [
  { label: "待审核商品", value: 5, icon: "ri:file-list-3-line", gradient: "linear-gradient(135deg, #e8836b, #f5a091)", path: "/admin/goods-audit" },
  { label: "已上架商品", value: 128, icon: "ri:shopping-bag-3-line", gradient: "linear-gradient(135deg, #ff8a65, #ffab40)", path: "" },
  { label: "今日访问", value: 356, icon: "ri:bar-chart-grouped-line", gradient: "linear-gradient(135deg, #ff6f61, #ff8a80)", path: "" },
  { label: "注册用户", value: 1024, icon: "ri:user-heart-line", gradient: "linear-gradient(135deg, #a1887f, #bcaaa4)", path: "/admin/users" }
];

const quickActions = [
  { label: "商品审核", icon: "ri:file-list-3-line", gradient: "linear-gradient(135deg, #e8836b, #f5a091)", desc: "审核待发布商品", path: "/admin/goods-audit" },
  { label: "用户管理", icon: "ri:group-line", gradient: "linear-gradient(135deg, #a1887f, #bcaaa4)", desc: "管理系统用户", path: "/admin/users" }
];

const recentItems = ref([
  { id: 1, title: "iPhone 14 Pro 256GB 九成新", user: "张三", time: "10分钟前", status: "待审核", type: "warning" },
  { id: 2, title: "考研数学全套资料 2025版", user: "李四", time: "30分钟前", status: "待审核", type: "warning" },
  { id: 3, title: "机械键盘 Cherry MX 青轴", user: "王五", time: "1小时前", status: "已通过", type: "success" },
  { id: 4, title: "高仿LV包 99新", user: "小李", time: "2小时前", status: "已拒绝", type: "danger" },
  { id: 5, title: "宿舍用小冰箱 50L", user: "王五", time: "3小时前", status: "已通过", type: "success" }
]);

const goTo = (path: string) => { if (path) router.push(path); };
onMounted(() => { updateTime(); });
</script>

<template>
  <div class="dashboard">
    <div class="hero-banner">
      <div class="hero-content">
        <div class="hero-left">
          <div class="hero-avatar">
            <span class="hero-avatar-text">{{ userName.charAt(0) }}</span>
          </div>
          <div class="hero-text">
            <h1 class="hero-greeting">{{ currentTime }}</h1>
            <p class="hero-sub">校园二手交易平台 · 管理后台</p>
          </div>
        </div>
        <div class="hero-right">
          <div class="hero-date-box">
            <span class="hero-date-num">{{ new Date().getDate() }}</span>
            <div class="hero-date-meta">
              <span>{{ new Date().toLocaleDateString("zh-CN", { month: "long" }) }}</span>
              <span>{{ new Date().toLocaleDateString("zh-CN", { weekday: "long" }) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="stats-row">
      <div v-for="(stat, idx) in stats" :key="idx" class="stat-card" :class="{ clickable: !!stat.path }" :style="{ background: stat.gradient }" @click="goTo(stat.path)">
        <div class="stat-card-inner">
          <div class="stat-card-icon">
            <IconifyIconOnline :icon="stat.icon" width="32" color="#fff" />
          </div>
          <div class="stat-card-body">
            <span class="stat-card-num">{{ stat.value.toLocaleString() }}</span>
            <span class="stat-card-label">{{ stat.label }}</span>
          </div>
        </div>
        <div class="stat-card-glow" />
      </div>
    </div>

    <div class="bottom-grid">
      <div class="panel">
        <div class="panel-head"><span class="panel-title">快捷操作</span></div>
        <div class="panel-body">
          <div class="action-list">
            <div v-for="(act, idx) in quickActions" :key="idx" class="action-row" @click="goTo(act.path)">
              <div class="action-left">
                <div class="action-icon" :style="{ background: act.gradient }">
                  <IconifyIconOnline :icon="act.icon" width="22" color="#fff" />
                </div>
                <div class="action-info">
                  <span class="action-name">{{ act.label }}</span>
                  <span class="action-desc">{{ act.desc }}</span>
                </div>
              </div>
              <IconifyIconOnline icon="ri:arrow-right-s-line" width="20" color="#bcaaa4" />
            </div>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">
          <span class="panel-title">最新审核动态</span>
          <span class="panel-extra" @click="goTo('/admin/goods-audit')">查看全部</span>
        </div>
        <div class="panel-body">
          <div class="feed-list">
            <div v-for="item in recentItems" :key="item.id" class="feed-item">
              <div class="feed-left">
                <div class="feed-dot" :class="item.type" />
                <div class="feed-info">
                  <div class="feed-title">{{ item.title }}</div>
                  <div class="feed-meta">{{ item.user }} · {{ item.time }}</div>
                </div>
              </div>
              <el-tag :type="item.type as any" size="small" round effect="light">{{ item.status }}</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard { padding: 0 4px; max-width: 1400px; }

.hero-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #fef9f0 0%, #fff5f0 30%, #fff0ec 60%, #fbeae5 100%);
  border-radius: 20px;
  padding: 36px 40px;
  margin-bottom: 22px;
  border: 1px solid #f0ddd5;
  box-shadow: 0 2px 12px rgba(120, 70, 50, 0.06);
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.hero-left { display: flex; align-items: center; gap: 20px; }

.hero-avatar {
  width: 56px; height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #e8836b, #f5a091);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 8px 24px rgba(232, 131, 107, 0.3);
}

.hero-avatar-text { color: #fff; font-size: 24px; font-weight: 800; }
.hero-text { display: flex; flex-direction: column; gap: 4px; }

.hero-greeting {
  font-size: 24px; font-weight: 700; color: #4a3728; margin: 0; line-height: 1.3;
  animation: fadeSlideIn 0.6s ease-out;
}

.hero-sub {
  font-size: 14px; color: #8d6e63; margin: 0;
  animation: fadeSlideIn 0.6s ease-out 0.15s both;
}

.hero-date-box {
  display: flex; align-items: center; gap: 14px;
  background: #fff; border-radius: 16px; padding: 14px 22px;
  border: 1px solid #f0ddd5;
}

.hero-date-num { font-size: 42px; font-weight: 800; color: #e8836b; line-height: 1; }
.hero-date-meta { display: flex; flex-direction: column; gap: 2px; color: #8d6e63; font-size: 14px; }

@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; margin-bottom: 22px; }

.stat-card {
  border-radius: 18px; padding: 26px 24px; position: relative; overflow: hidden;
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
  animation: cardEnter 0.5s ease-out both;
}
.stat-card:nth-child(1) { animation-delay: 0.1s; }
.stat-card:nth-child(2) { animation-delay: 0.2s; }
.stat-card:nth-child(3) { animation-delay: 0.3s; }
.stat-card:nth-child(4) { animation-delay: 0.4s; }

@keyframes cardEnter {
  from { opacity: 0; transform: translateY(20px) scale(0.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.stat-card.clickable { cursor: pointer; }
.stat-card.clickable:hover { transform: translateY(-6px) scale(1.03); transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1); }

.stat-card-glow {
  position: absolute; width: 160px; height: 160px;
  background: rgba(255,255,255,0.1); border-radius: 50%;
  top: -40px; right: -40px;
}

.stat-card-inner { position: relative; z-index: 1; display: flex; flex-direction: column; gap: 16px; }

.stat-card-icon {
  width: 54px; height: 54px; border-radius: 14px;
  background: rgba(255,255,255,0.2); backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
}

.stat-card-body { display: flex; flex-direction: column; }
.stat-card-num { font-size: 36px; font-weight: 800; color: #fff; line-height: 1.1; }
.stat-card-label { font-size: 14px; color: rgba(255,255,255,0.9); margin-top: 4px; font-weight: 500; }

.bottom-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; }

.panel {
  background: #fff; border-radius: 18px;
  box-shadow: 0 2px 12px rgba(120, 70, 50, 0.06);
  border: 1px solid #f0ddd5; overflow: hidden;
  animation: cardEnter 0.5s ease-out 0.5s both;
}

.panel-head { display: flex; align-items: center; justify-content: space-between; padding: 22px 26px 0; }
.panel-title { font-size: 16px; font-weight: 700; color: #4a3728; }
.panel-extra { font-size: 13px; color: #e8836b; cursor: pointer; font-weight: 500; }
.panel-extra:hover { color: #d4725a; }
.panel-body { padding: 18px 26px 26px; }

.action-list { display: flex; flex-direction: column; gap: 4px; }
.action-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; border-radius: 12px; cursor: pointer;
  transition: all 0.25s;
}
.action-row:hover { background: #fff5f0; transform: translateX(6px); }
.action-left { display: flex; align-items: center; gap: 16px; }
.action-icon {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; box-shadow: 0 4px 14px rgba(0,0,0,0.12);
  transition: transform 0.25s;
}
.action-row:hover .action-icon { transform: scale(1.1); }
.action-info { display: flex; flex-direction: column; }
.action-name { font-size: 15px; font-weight: 600; color: #4a3728; }
.action-desc { font-size: 12px; color: #8d6e63; margin-top: 2px; }

.feed-list { display: flex; flex-direction: column; }
.feed-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 0; border-bottom: 1px solid #f5ebe5;
  transition: background 0.2s;
}
.feed-item:last-child { border-bottom: none; padding-bottom: 0; }
.feed-item:hover { background: #fef9f0; border-radius: 8px; padding-left: 8px; padding-right: 8px; }
.feed-left { display: flex; align-items: center; gap: 14px; flex: 1; min-width: 0; }
.feed-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; box-shadow: 0 0 8px rgba(0,0,0,0.1); }
.feed-dot.warning { background: #ffab40; }
.feed-dot.success { background: #66bb6a; }
.feed-dot.danger { background: #e8836b; }
.feed-info { flex: 1; min-width: 0; }
.feed-title { font-size: 14px; color: #4a3728; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-weight: 500; }
.feed-meta { font-size: 12px; color: #bcaaa4; margin-top: 4px; }

@media (max-width: 1200px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .bottom-grid { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .hero-banner { padding: 28px; }
  .hero-content { flex-direction: column; align-items: flex-start; gap: 18px; }
  .stats-row { grid-template-columns: 1fr; }
}
</style>
