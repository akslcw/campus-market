<script setup lang="ts">
import BackTopIcon from "@/assets/svg/back_top.svg?component";
</script>

<template>
  <section class="app-main">
    <el-scrollbar>
      <el-backtop
        title="回到顶部"
        target=".app-main .el-scrollbar__wrap"
      >
        <BackTopIcon />
      </el-backtop>
      <router-view v-slot="{ Component, route }">
        <transition name="fade-transform" mode="out-in">
          <component
            :is="Component"
            :key="route.fullPath"
            class="main-content"
          />
        </transition>
      </router-view>
    </el-scrollbar>
  </section>
</template>

<style scoped>
.app-main {
  position: relative;
  width: 100%;
  height: 100vh;
  padding-top: 48px;
  overflow-x: hidden;
}

.main-content {
  margin: 24px;
}
</style>
