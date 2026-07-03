<script setup lang="ts">
import {
  unref,
  reactive,
  computed,
  nextTick,
  onBeforeMount
} from "vue";
import { emitter } from "@/utils/mitt";
import LayPanel from "../lay-panel/index.vue";
import { useDataThemeChange } from "@/layout/hooks/useDataThemeChange";
import { useGlobal } from "@pureadmin/utils";

const { $storage } = useGlobal<GlobalPropertiesApi>();

const {
  dataTheme,
  layoutTheme,
  toggleClass,
  dataThemeChange
} = useDataThemeChange();

/** 保持horizontal布局 */
if (unref(layoutTheme)) {
  document.documentElement.setAttribute("data-theme", "light");
  document.body.setAttribute("layout", "horizontal");
}

const settings = reactive({
  greyVal: $storage.configure.grey,
  darkVal: $storage.layout?.darkMode ?? false,
  tabsVal: $storage.configure.hideTabs,
  showLogo: $storage.configure.showLogo,
  hideFooter: $storage.configure.hideFooter
});

function storageConfigureChange<T>(key: string, val: T): void {
  const storageConfigure = $storage.configure;
  storageConfigure[key] = val;
  $storage.configure = storageConfigure;
}

const greyChange = (value): void => {
  const htmlEl = document.querySelector("html");
  toggleClass(settings.greyVal, "html-grey", htmlEl);
  storageConfigureChange("grey", value);
};

const darkChange = (value: boolean): void => {
  dataTheme.value = value;
  dataThemeChange(value ? "dark" : "light");
  if ($storage.layout) {
    $storage.layout.darkMode = value;
    $storage.layout.overallStyle = value ? "dark" : "light";
  }
};

const tagsChange = () => {
  storageConfigureChange("hideTabs", settings.tabsVal);
  emitter.emit("tagViewsChange", settings.tabsVal as unknown as string);
};

const hideFooterChange = () => {
  storageConfigureChange("hideFooter", settings.hideFooter);
};

function logoChange() {
  storageConfigureChange("showLogo", settings.showLogo);
  emitter.emit("logoChange", settings.showLogo);
}

onBeforeMount(() => {
  nextTick(() => {
    settings.greyVal && document.querySelector("html")?.classList.add("html-grey");
    settings.tabsVal && tagsChange();
    settings.hideFooter && hideFooterChange();
  });
});

const pClass = computed(() => ["mb-[12px]!", "font-medium", "text-sm", "dark:text-white"]);
</script>

<template>
  <LayPanel>
    <div class="p-5">
      <p :class="pClass">界面显示</p>
      <ul class="setting">
        <li>
          <span>灰色模式</span>
          <el-switch v-model="settings.greyVal" inline-prompt active-text="开" inactive-text="关" @change="greyChange" />
        </li>
        <li>
          <span>夜间模式</span>
          <el-switch v-model="settings.darkVal" inline-prompt active-text="开" inactive-text="关" @change="darkChange" />
        </li>
        <li>
          <span>隐藏标签页</span>
          <el-switch v-model="settings.tabsVal" inline-prompt active-text="开" inactive-text="关" @change="tagsChange" />
        </li>
        <li>
          <span>隐藏页脚</span>
          <el-switch v-model="settings.hideFooter" inline-prompt active-text="开" inactive-text="关" @change="hideFooterChange" />
        </li>
        <li>
          <span>Logo</span>
          <el-switch v-model="settings.showLogo" :active-value="true" :inactive-value="false" inline-prompt active-text="开" inactive-text="关" @change="logoChange" />
        </li>
      </ul>
    </div>
  </LayPanel>
</template>

<style lang="scss" scoped>
:deep(.el-switch__core) {
  --el-switch-off-color: var(--pure-switch-off-color);
  min-width: 36px;
  height: 18px;
}
:deep(.el-switch__core .el-switch__action) { height: 14px; }

.setting {
  li {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 3px 0;
    font-size: 14px;
  }
}
</style>