<script setup lang="ts">
import { useRoute } from "vue-router";
import { useNav } from "@/layout/hooks/useNav";
import { isAllEmpty } from "@pureadmin/utils";
import { usePermissionStoreHook } from "@/store/modules/permission";
import { computed, ref } from "vue";
import LaySidebarLogo from "./components/SidebarLogo.vue";
import LaySidebarItem from "./components/SidebarItem.vue";
import LaySidebarLeftCollapse from "./components/SidebarLeftCollapse.vue";
import LaySidebarCenterCollapse from "./components/SidebarCenterCollapse.vue";

const route = useRoute();
const isShow = ref(false);
const {
  device,
  pureApp,
  isCollapse,
  tooltipEffect,
  toggleSideBar
} = useNav();

const menuData = computed(() => usePermissionStoreHook().wholeMenus);
const loading = computed(() => menuData.value.length === 0);
const defaultActive = computed(() =>
  !isAllEmpty(route.meta?.activePath) ? route.meta.activePath : route.path
);
</script>

<template>
  <div
    v-loading="loading"
    class="sidebar-container has-logo"
    @mouseenter.prevent="isShow = true"
    @mouseleave.prevent="isShow = false"
  >
    <LaySidebarLogo :collapse="isCollapse" />
    <el-scrollbar
      wrap-class="scrollbar-wrapper"
      :class="[device === 'mobile' ? 'mobile' : 'pc']"
    >
      <el-menu
        unique-opened
        mode="vertical"
        popper-class="pure-scrollbar"
        class="outer-most select-none"
        :collapse="isCollapse"
        :collapse-transition="false"
        :popper-effect="tooltipEffect"
        :default-active="defaultActive"
      >
        <LaySidebarItem
          v-for="routes in menuData"
          :key="routes.path"
          :item="routes"
          :base-path="routes.path"
          class="outer-most select-none"
        />
      </el-menu>
    </el-scrollbar>
    <LaySidebarCenterCollapse
      v-if="device !== 'mobile' && (isShow || isCollapse)"
      :is-active="pureApp.sidebar.opened"
      @toggleClick="toggleSideBar"
    />
    <LaySidebarLeftCollapse
      v-if="device !== 'mobile'"
      :is-active="pureApp.sidebar.opened"
      @toggleClick="toggleSideBar"
    />
  </div>
</template>

<style scoped>
:deep(.el-loading-mask) {
  opacity: 0.45;
}
</style>
