<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { deviceDetection, useResizeObserver } from "@pureadmin/utils";
import { useAppStoreHook } from "@/store/modules/app";
import LayNavbar from "./components/lay-navbar/index.vue";
import LayContent from "./components/lay-content/index.vue";
import NavVertical from "./components/lay-sidebar/NavVertical.vue";

const appWrapperRef = ref();
const isMobile = deviceDetection();
const pureApp = useAppStoreHook();
const sidebar = computed(() => pureApp.sidebar);
const device = computed(() => pureApp.device);

const classes = computed(() => ({
  hideSidebar: !sidebar.value.opened,
  openSidebar: sidebar.value.opened,
  withoutAnimation: sidebar.value.withoutAnimation,
  mobile: device.value === "mobile"
}));

function toggle(deviceName: string, opened: boolean) {
  pureApp.toggleDevice(deviceName);
  pureApp.toggleSideBar(opened, "resize");
}

let isAutoCloseSidebar = true;

useResizeObserver(appWrapperRef, entries => {
  if (isMobile) return;
  const [{ inlineSize: width, blockSize: height }] =
    entries[0].borderBoxSize;
  pureApp.setViewportSize({ width, height });

  if (width > 0 && width <= 760) {
    toggle("mobile", false);
    isAutoCloseSidebar = true;
  } else if (width <= 990) {
    if (isAutoCloseSidebar) {
      toggle("desktop", false);
      isAutoCloseSidebar = false;
    }
  } else if (!sidebar.value.isClickCollapse) {
    toggle("desktop", true);
    isAutoCloseSidebar = true;
  }
});

onMounted(() => {
  document.documentElement.classList.remove("dark");
  if (isMobile) toggle("mobile", false);
});
</script>

<template>
  <div ref="appWrapperRef" :class="['app-wrapper', classes]">
    <div
      v-show="device === 'mobile' && sidebar.opened"
      class="app-mask"
      @click="pureApp.toggleSideBar()"
    />
    <NavVertical />
    <div class="main-container">
      <div class="fixed-header">
        <LayNavbar />
      </div>
      <LayContent />
    </div>
  </div>
</template>

<style scoped>
.app-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.app-wrapper::after {
  display: table;
  clear: both;
  content: "";
}

.app-wrapper.mobile.openSidebar {
  position: fixed;
  top: 0;
}

.app-mask {
  position: absolute;
  top: 0;
  z-index: 2001;
  width: 100%;
  height: 100%;
  background: #000;
  opacity: 0.3;
}
</style>
