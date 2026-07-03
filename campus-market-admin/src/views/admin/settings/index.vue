<script setup lang="ts">
import { ref, reactive, onMounted, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  updateAdminProfile, changePassword, uploadAvatar,
  getSystemConfig, updateSystemConfig, type SystemConfig
} from "@/api/admin";
import { emitter } from "@/utils/mitt";
import { useDataThemeChange } from "@/layout/hooks/useDataThemeChange";
import { useGlobal, storageLocal } from "@pureadmin/utils";
import { useUserStoreHook } from "@/store/modules/user";
import { userKey } from "@/utils/auth";

defineOptions({ name: "SystemSettings" });

const activeTab = ref("profile");

// 账号信息
const profileLoading = ref(false);
const profileForm = reactive({ nickname: "", email: "", phone: "" });
const avatarUrl = ref("");

const loadProfile = () => {
  const userStore = useUserStoreHook();
  profileForm.nickname = userStore.nickname || "管理员";
  // 从 auth 存储读
  const authData = storageLocal().getItem<any>(userKey);
  profileForm.email = authData?.email || "admin@campus.com";
  profileForm.phone = authData?.phone || "13800000001";
  avatarUrl.value = userStore.avatar || authData?.avatar || "https://avatars.githubusercontent.com/u/44761321";
};

const handleSaveProfile = async () => {
  if (!profileForm.nickname.trim()) { ElMessage.warning("昵称不能为空"); return; }
  profileLoading.value = true;
  try {
    const res = await updateAdminProfile({ nickname: profileForm.nickname, email: profileForm.email, phone: profileForm.phone });
    if (res.code === 200) {
      ElMessage.success("个人信息更新成功");
      useUserStoreHook().SET_NICKNAME(profileForm.nickname);
      // 同步写入 auth 存储，保留所有现有字段
      const existingAuth = storageLocal().getItem<any>(userKey);
      if (existingAuth) {
        existingAuth.nickname = profileForm.nickname;
        existingAuth.email = profileForm.email;
        existingAuth.phone = profileForm.phone;
        storageLocal().setItem(userKey, existingAuth);
      }
    } else ElMessage.error(res.msg || "更新失败");
  } catch { ElMessage.error("网络错误，请重试"); }
  finally { profileLoading.value = false; }
};

// 头像
const avatarUploading = ref(false);
const avatarInputRef = ref<HTMLInputElement>();

const handleAvatarClick = () => { avatarInputRef.value?.click(); };

const handleAvatarChange = async (e: Event) => {
  const input = e.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;
  const allowedTypes = ["image/jpeg", "image/png", "image/gif", "image/webp"];
  if (!allowedTypes.includes(file.type)) { ElMessage.warning("仅支持 JPG、PNG、GIF、WebP 格式"); return; }
  if (file.size > 5 * 1024 * 1024) { ElMessage.warning("图片大小不能超过 5MB"); return; }
  avatarUploading.value = true;
  try {
    const formData = new FormData(); formData.append("file", file);
    const res = await uploadAvatar(formData);
    if (res.code === 200 && res.data?.url) {
      const newAvatar = res.data.url;
      // 1. 更新设置页头像
      avatarUrl.value = newAvatar;
      // 2. 更新 pinia store（顶栏头像立即变化）
      useUserStoreHook().SET_AVATAR(newAvatar);
      // 3. 同步写入 auth 存储（刷新后保持），保留所有现有字段
      const existingAuth = storageLocal().getItem<any>(userKey);
      if (existingAuth) {
        existingAuth.avatar = newAvatar;
        storageLocal().setItem(userKey, existingAuth);
      }
      // 4. 清除 input 值，支持重新选择同一文件
      input.value = "";
      ElMessage.success("头像更新成功");
    } else ElMessage.error(res.msg || "上传失败");
  } catch { ElMessage.error("网络错误，请重试"); }
  finally { avatarUploading.value = false; }
};

// 修改密码
const passwordLoading = ref(false);
const passwordForm = reactive({ oldPassword: "", newPassword: "", confirmPassword: "" });

const handleChangePassword = async () => {
  if (!passwordForm.oldPassword) { ElMessage.warning("请输入原密码"); return; }
  if (!passwordForm.newPassword) { ElMessage.warning("请输入新密码"); return; }
  if (passwordForm.newPassword.length < 6) { ElMessage.warning("新密码长度至少6位"); return; }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) { ElMessage.warning("两次输入的新密码不一致"); return; }
  await ElMessageBox.confirm("确定要修改密码吗？", "修改密码", { confirmButtonText: "确定", cancelButtonText: "取消", type: "warning" });
  passwordLoading.value = true;
  try {
    const res = await changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword });
    if (res.code === 200) { ElMessage.success("密码修改成功，请重新登录"); passwordForm.oldPassword = ""; passwordForm.newPassword = ""; passwordForm.confirmPassword = ""; }
    else ElMessage.error(res.msg || "密码修改失败");
  } catch (err: any) { if (err !== "cancel") ElMessage.error("网络错误，请重试"); }
  finally { passwordLoading.value = false; }
};

// 系统配置
const configLoading = ref(false);
const configSaving = ref(false);
const configForm = reactive<SystemConfig>({ siteName: "", siteDescription: "", announcement: "", autoAudit: false, maxGoodsPerUser: 10, logo: "" });

const loadSysConfig = async () => {
  configLoading.value = true;
  try {
    const res = await getSystemConfig();
    if (res.code === 200 && res.data) Object.assign(configForm, res.data);
  } catch { ElMessage.error("加载系统配置失败"); }
  finally { configLoading.value = false; }
};

const handleSaveConfig = async () => {
  if (!configForm.siteName.trim()) { ElMessage.warning("站点名称不能为空"); return; }
  if (configForm.maxGoodsPerUser < 1 || configForm.maxGoodsPerUser > 50) { ElMessage.warning("每人最大发布数量范围为 1-50"); return; }
  await ElMessageBox.confirm("确定要保存系统配置吗？修改后立即生效。", "保存配置", { confirmButtonText: "确定", cancelButtonText: "取消", type: "warning" });
  configSaving.value = true;
  try {
    const res = await updateSystemConfig(configForm);
    if (res.code === 200) ElMessage.success("系统配置保存成功");
    else ElMessage.error(res.msg || "保存失败");
  } catch (err: any) { if (err !== "cancel") ElMessage.error("网络错误，请重试"); }
  finally { configSaving.value = false; }
};

// ====== 界面配置 ======
const { $storage } = useGlobal<GlobalPropertiesApi>();
const { dataTheme, toggleClass, dataThemeChange } = useDataThemeChange();

const uiSettings = reactive({
  greyVal: $storage.configure.grey,
  darkVal: $storage.layout?.darkMode ?? false,
  tabsVal: $storage.configure.hideTabs,
  showLogo: $storage.configure.showLogo,
  hideFooter: $storage.configure.hideFooter
});

function uiStorageChange<T>(key: string, val: T): void {
  const storageConfigure = $storage.configure;
  storageConfigure[key] = val;
  $storage.configure = storageConfigure;
}

const handleGreyChange = (value: boolean) => {
  toggleClass(value, "html-grey", document.querySelector("html"));
  uiStorageChange("grey", value);
};
const handleDarkChange = (value: boolean) => {
  dataTheme.value = value;
  dataThemeChange(value ? "dark" : "light");
  if ($storage.layout) {
    $storage.layout.darkMode = value;
    $storage.layout.overallStyle = value ? "dark" : "light";
  }
};

onMounted(() => { loadProfile(); });
watch(activeTab, (tab) => { if (tab === "config") loadSysConfig(); });
</script>

<template>
  <div class="page-container">
    <div class="page-top">
      <div class="title-left">
        <span class="title-icon" style="background: linear-gradient(135deg, #8d6e63, #bcaaa4)">
          <IconifyIconOnline icon="ri:settings-3-line" width="22" />
        </span>
        <div>
          <h2 class="page-title">系统设置</h2>
          <p class="page-desc">管理账号信息、密码、头像、平台配置及界面风格</p>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-tabs v-model="activeTab" class="settings-tabs">

        <!-- Tab 1: 账号信息 -->
        <el-tab-pane label="账号信息" name="profile">
          <div class="tab-content">
            <div class="avatar-section">
              <div class="avatar-wrapper" @click="handleAvatarClick">
                <el-avatar :size="96" :src="avatarUrl" class="setting-avatar" />
                <div class="avatar-overlay">
                  <span v-if="avatarUploading">上传中...</span>
                  <span v-else>更换头像</span>
                </div>
              </div>
              <input ref="avatarInputRef" type="file" accept="image/*" style="display: none" @change="handleAvatarChange" />
              <p class="avatar-hint">点击头像更换，支持 JPG/PNG/GIF/WebP，不超过 5MB</p>
            </div>
            <el-divider />
            <el-form :model="profileForm" label-width="90px" label-position="left" class="setting-form">
              <el-form-item label="用户名">
                <el-input value="admin" disabled />
                <span class="form-tip">用户名不可修改</span>
              </el-form-item>
              <el-form-item label="昵称">
                <el-input v-model="profileForm.nickname" placeholder="请输入昵称" maxlength="20" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号" maxlength="11" />
              </el-form-item>
              <el-form-item label="角色">
                <el-tag type="danger" effect="dark" round>超级管理员</el-tag>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="profileLoading" @click="handleSaveProfile">保存修改</el-button>
                <el-button @click="loadProfile">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- Tab 2: 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <div class="tab-content">
            <div class="section-head">
              <h4 class="section-title">修改登录密码</h4>
              <p class="section-desc">建议使用 6-20 位字母、数字和符号的组合密码，定期更换可提升安全性。</p>
            </div>
            <el-form :model="passwordForm" label-width="90px" label-position="left" class="setting-form">
              <el-form-item label="原密码">
                <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password maxlength="20" />
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码（至少6位）" show-password maxlength="20" />
              </el-form-item>
              <el-form-item label="确认密码">
                <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password maxlength="20" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- Tab 3: 系统配置 -->
        <el-tab-pane label="系统配置" name="config">
          <div class="tab-content" v-loading="configLoading">
            <div class="section-head">
              <h4 class="section-title">平台全局配置</h4>
              <p class="section-desc">以下配置将影响整个平台的运行行为，修改后立即生效。</p>
            </div>
            <el-form :model="configForm" label-width="140px" label-position="left" class="setting-form">
              <el-form-item label="站点名称">
                <el-input v-model="configForm.siteName" placeholder="请输入站点名称" maxlength="30" />
              </el-form-item>
              <el-form-item label="站点描述">
                <el-input v-model="configForm.siteDescription" type="textarea" :rows="2" placeholder="请输入站点描述" maxlength="200" show-word-limit />
              </el-form-item>
              <el-form-item label="站点 Logo URL">
                <el-input v-model="configForm.logo" placeholder="请输入 Logo 图片地址" />
                <div v-if="configForm.logo" class="logo-preview">
                  <el-image :src="configForm.logo" style="width: 48px; height: 48px; border-radius: 10px" fit="cover" />
                </div>
              </el-form-item>
              <el-form-item label="首页公告">
                <el-input v-model="configForm.announcement" type="textarea" :rows="3" placeholder="请输入首页公告内容" maxlength="500" show-word-limit />
              </el-form-item>
              <el-divider />
              <el-form-item label="商品自动审核">
                <el-switch v-model="configForm.autoAudit" active-text="开启" inactive-text="关闭" />
                <span class="form-tip">{{ configForm.autoAudit ? '用户发布商品后自动上架，无需管理员审核' : '用户发布商品后需管理员审核通过才可上架' }}</span>
              </el-form-item>
              <el-form-item label="每人最大发布数">
                <el-input-number v-model="configForm.maxGoodsPerUser" :min="1" :max="50" :step="1" />
                <span class="form-tip">每个用户最多可同时上架的商品数量</span>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="configSaving" @click="handleSaveConfig">保存配置</el-button>
                <el-button @click="loadSysConfig">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- Tab 4: 界面配置 -->
        <el-tab-pane label="界面配置" name="ui">
          <div class="tab-content">
            <div class="section-head">
              <h4 class="section-title">界面外观</h4>
              <p class="section-desc">自定义平台界面风格，修改后立即生效。</p>
            </div>

            <el-form label-width="100px" label-position="left" class="setting-form">
              <el-form-item label="灰色模式">
                <el-switch v-model="uiSettings.greyVal" active-text="开" inactive-text="关" @change="handleGreyChange" />
                <span class="form-tip">开启后页面变为灰白色调</span>
              </el-form-item>

              <el-form-item label="夜间模式">
                <el-switch v-model="uiSettings.darkVal" active-text="开" inactive-text="关" @change="handleDarkChange" />
                <span class="form-tip">开启后切换为深色暗色界面</span>
              </el-form-item>

              <el-form-item label="显示页签">
                <el-switch v-model="uiSettings.tabsVal" :active-value="false" :inactive-value="true" active-text="开" inactive-text="关" />
                <span class="form-tip">是否在顶部显示标签页导航</span>
              </el-form-item>

              <el-form-item label="显示页脚">
                <el-switch v-model="uiSettings.hideFooter" :active-value="false" :inactive-value="true" active-text="开" inactive-text="关" />
                <span class="form-tip">是否在页面底部显示版权信息</span>
              </el-form-item>

              <el-form-item label="显示 Logo">
                <el-switch v-model="uiSettings.showLogo" :active-value="true" :inactive-value="false" active-text="开" inactive-text="关" />
                <span class="form-tip">是否在顶栏显示平台 Logo</span>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- Tab 5: 关于系统 -->
        <el-tab-pane label="关于系统" name="about">
          <div class="tab-content">
            <div class="about-section">
              <div class="about-logo">
                <el-avatar :size="80" :src="configForm.logo || 'https://picsum.photos/100/100?random=99'" />
              </div>
              <h3 class="about-title">校园二手交易平台</h3>
              <p class="about-version">v1.0.0</p>
              <el-divider />
              <div class="about-info">
                <div class="about-item"><span class="a-label">前端框架</span><span class="a-value">Vue 3 + TypeScript</span></div>
                <div class="about-item"><span class="a-label">UI 组件库</span><span class="a-value">Element Plus</span></div>
                <div class="about-item"><span class="a-label">后台模板</span><span class="a-value">Pure Admin Thin</span></div>
                <div class="about-item"><span class="a-label">构建工具</span><span class="a-value">Vite 7</span></div>
                <div class="about-item"><span class="a-label">包管理器</span><span class="a-value">pnpm</span></div>
                <div class="about-item"><span class="a-label">开发环境</span><span class="a-value">Windows 11</span></div>
              </div>
              <el-divider />
              <div class="about-desc">
                <p>校园二手交易平台是一款专为大学生设计的一站式二手物品交易平台。平台支持商品发布、智能分类、在线沟通、审核管理等功能，致力于为在校学生提供安全、便捷、实惠的二手交易服务。</p>
                <p class="copyright">Copyright &copy; 2026 校园二手交易平台. All rights reserved.</p>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
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
  border-top: 3px solid #8d6e63;
}

.title-left { display: flex; align-items: center; gap: 14px; }
.title-icon {
  width: 46px; height: 46px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; box-shadow: 0 6px 16px rgba(0,0,0,0.15);
}
.page-title { font-size: 18px; font-weight: 600; color: #4a3728; margin: 0; }
.page-desc { font-size: 12px; color: #8d6e63; margin: 4px 0 0; }

.table-card {
  background: #fff; border-radius: 16px; padding: 8px 22px 22px;
  box-shadow: 0 2px 12px rgba(120,70,50,.06);
  border: 1px solid #f0ddd5; overflow: hidden;
}

.settings-tabs { min-height: 480px; }
.tab-content { padding: 12px 0; max-width: 720px; }

.section-head { margin-bottom: 20px; }
.section-title { font-size: 15px; font-weight: 600; color: #4a3728; margin: 0 0 4px; }
.section-desc { font-size: 13px; color: #8d6e63; margin: 0; }

/* 头像 */
.avatar-section { margin-bottom: 4px; }
.avatar-wrapper { position: relative; display: inline-block; cursor: pointer; border-radius: 50%; overflow: hidden; margin-top: 8px; }
.setting-avatar { border: 3px solid #f0ddd5; }
.avatar-overlay { position: absolute; inset: 0; background: rgba(0,0,0,.45); color: #fff; font-size: 13px; display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity .2s; border-radius: 50%; }
.avatar-wrapper:hover .avatar-overlay { opacity: 1; }
.avatar-hint { font-size: 12px; color: #bcaaa4; margin-top: 10px; }

.setting-form { margin-top: 16px; }
.form-tip { font-size: 12px; color: #909399; margin-left: 12px; }
.logo-preview { margin-top: 8px; }

/* 关于 */
.about-section { text-align: center; padding: 20px 0; }
.about-logo { margin-bottom: 16px; }
.about-title { font-size: 22px; font-weight: 700; color: #4a3728; margin: 0 0 6px; }
.about-version { font-size: 14px; color: #8d6e63; margin: 0; }
.about-info { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; text-align: left; padding: 0 20px; max-width: 400px; margin: 0 auto; }
.about-item { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; font-size: 13px; }
.a-label { color: #8d6e63; }
.a-value { color: #4a3728; font-weight: 500; }
.about-desc { max-width: 400px; margin: 0 auto; font-size: 13px; color: #8d6e63; line-height: 1.8; }
.copyright { color: #bcaaa4; margin-top: 16px; }
</style>