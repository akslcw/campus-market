<script setup lang="ts">
import { emitter } from "@/utils/mitt";
import { useNav } from "@/layout/hooks/useNav";
import LaySearch from "../lay-search/index.vue";
import LayNotice from "../lay-notice/index.vue";
import { responsiveStorageNameSpace } from "@/config";
import { ref, computed, onMounted } from "vue";
import { storageLocal } from "@pureadmin/utils";
import { usePermissionStoreHook } from "@/store/modules/permission";
import LaySidebarFullScreen from "../lay-sidebar/components/SidebarFullScreen.vue";
import { useRouter } from "vue-router";

import LogoutCircleRLine from "~icons/ri/logout-circle-r-line";

const showLogo = ref(
  storageLocal().getItem<StorageConfigs>(
    `${responsiveStorageNameSpace()}configure`
  )?.showLogo ?? true
);

const router = useRouter();

const {
  route,
  title,
  logout,
  getLogo,
  username,
  userAvatar,
  backTopMenu,
  avatarsStyle
} = useNav();

const menus = computed(() => usePermissionStoreHook().wholeMenus);

const isActive = (item: any) => {
  const path = route.path;
  if (item.meta?.activePath) {
    return route.meta?.activePath === item.path;
  }
  return path === item.path || path.startsWith(item.path + "/");
};

const goTo = (item: any) => {
  if (item.children && item.children.length > 0 && item.children[0]?.path) {
    router.push(item.children[0].path);
  } else {
    router.push(item.path);
  }
};

onMounted(() => {
  emitter.on("logoChange", key => {
    showLogo.value = key;
  });
});
</script>

<template>
  <div class="horizontal-header">
    <div v-if="showLogo" class="horizontal-header-left" @click="backTopMenu">
      <img :src="getLogo()" alt="logo" />
      <span>{{ title }}</span>
    </div>

    <div class="capsule-nav" v-loading="menus.length === 0" element-loading-background="rgba(12, 53, 71, 0.9)">
      <div
        v-for="item in menus"
        :key="item.path"
        class="capsule-item"
        :class="{ active: isActive(item) }"
        @click="goTo(item)"
      >
        <IconifyIconOnline
          v-if="item.meta?.icon"
          :icon="item.meta.icon"
          width="18"
          class="capsule-icon"
        />
        <span class="capsule-label">{{ item.meta?.title || item.name }}</span>
      </div>
    </div>

    <div class="horizontal-header-right">
      <LaySearch id="header-search" />
      <LaySidebarFullScreen id="full-screen" />
      <LayNotice id="header-notice" />
      <el-dropdown trigger="click">
        <span class="el-dropdown-link navbar-bg-hover">
          <img :src="userAvatar" :style="avatarsStyle" />
          <p v-if="username">{{ username }}</p>
        </span>
        <template #dropdown>
          <el-dropdown-menu class="logout">
            <el-dropdown-item @click="logout">
              <IconifyIconOffline :icon="LogoutCircleRLine" style="margin: 5px" />
              退出系统
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.horizontal-header {
  display: flex;
  align-items: center;
  width: 100%;
  height: 56px;
  padding: 0 16px;
  background: linear-gradient(90deg, #fef9f0 0%, #fff5f0 40%, #fff0ec 70%, #fbeae5 100%);
  border-bottom: 1px solid #f0ddd5;
  position: relative;
  z-index: 1001;
}

.horizontal-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 180px;
  height: 100%;
  padding-left: 4px;
  cursor: pointer;
  flex-shrink: 0;

  img {
    display: inline-block;
    height: 32px;
    width: 32px;
    border-radius: 8px;
  }

  span {
    display: inline-block;
    height: 32px;
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 17px;
    font-weight: 700;
    line-height: 32px;
    color: #5d4037;
    white-space: nowrap;
    letter-spacing: 0.5px;
  }
}

.capsule-nav {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  height: 100%;
  padding: 0 12px;
  overflow: hidden;
  min-height: 56px;
}

.capsule-item {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 38px;
  padding: 0 18px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  white-space: nowrap;
  color: #8d6e63;
  background: transparent;

  &:hover {
    color: #5d4037;
    background: rgba(232, 131, 107, 0.1);
    transform: scale(1.04);
  }

  &.active {
    color: #fff;
    background: linear-gradient(135deg, #e8836b, #f5a091);
    box-shadow: 0 4px 14px rgba(232, 131, 107, 0.4);
    transform: scale(1.02);
  }
}

.capsule-icon { flex-shrink: 0; }

.capsule-label { font-size: 14px; font-weight: 500; }

.horizontal-header-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 2px;
  min-width: 280px;
  height: 100%;
  color: #6d4c41;
  flex-shrink: 0;

  .el-dropdown-link {
    display: flex;
    align-items: center;
    justify-content: space-around;
    height: 48px;
    padding: 10px;
    color: #6d4c41;
    cursor: pointer;

    p { font-size: 14px; }
    img { width: 22px; height: 22px; border-radius: 50%; }
  }

  .fullscreen-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 48px;
    cursor: pointer;
    color: #8d6e63;
    border-radius: 8px;
    &:hover { background: rgba(232, 131, 107, 0.08); }
  }
}

/* 夜间模式顶栏 */
html.dark .horizontal-header {
  background: linear-gradient(90deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  border-bottom-color: rgba(255, 255, 255, 0.08);

  .horizontal-header-left span { color: rgba(255, 255, 255, 0.9); }
  .capsule-item { color: rgba(255, 255, 255, 0.6); }
  .capsule-item:hover { color: #fff; background: rgba(255, 255, 255, 0.1); }
  .capsule-item.active { color: #fff; background: linear-gradient(135deg, #e8836b, #f5a091); }
  .horizontal-header-right { color: rgba(255, 255, 255, 0.7); }
  .horizontal-header-right .el-dropdown-link { color: rgba(255, 255, 255, 0.7); }
  .horizontal-header-right .fullscreen-icon { color: rgba(255, 255, 255, 0.6); }
}

.logout {
  width: 120px;
  ::v-deep(.el-dropdown-menu__item) {
    display: inline-flex;
    flex-wrap: wrap;
    min-width: 100%;
  }
}
</style>