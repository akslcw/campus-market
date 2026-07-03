<template>
  <div class="publish">
    <!-- 顶栏 -->
    <div class="topbar">
      <button class="icon-btn" @click="router.back()"><el-icon size="20"><ArrowLeft /></el-icon></button>
      <span class="topbar-title">发布闲置</span>
      <span style="width: 40px"></span>
    </div>

    <div class="pub-head">
      <p class="eyebrow">SELL&nbsp;YOUR&nbsp;STUFF</p>
      <h1 class="display pub-title">把闲置挂上集市</h1>
      <hr class="hairline" style="margin-top: 14px" />
    </div>

    <el-form :model="form" label-position="top" class="pub-form" @submit.prevent>
      <!-- 标题 -->
      <el-form-item label="商品标题">
        <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="一句话说清楚你卖什么" />
      </el-form-item>

      <!-- 分类 + 成色 -->
      <div class="two-col">
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="成色">
          <el-select v-model="condition" placeholder="选择成色" style="width: 100%">
            <el-option v-for="c in conditionOptions" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
      </div>

      <!-- 价格 -->
      <div class="two-col">
        <el-form-item label="售价 (元)">
          <el-input v-model.number="form.price" type="number" placeholder="0.00">
            <template #prefix>¥</template>
          </el-input>
        </el-form-item>
        <el-form-item label="原价 (选填)">
          <el-input v-model.number="form.originalPrice" type="number" placeholder="选填">
            <template #prefix>¥</template>
          </el-input>
        </el-form-item>
      </div>

      <!-- 描述 + AI -->
      <el-form-item>
        <template #label>
          <div class="desc-label">
            <span>商品描述</span>
            <button type="button" class="ai-btn" :disabled="aiLoading || !form.description.trim()" @click.prevent="onOptimize">
              <el-icon class="ai-icon" :class="{ spin: aiLoading }"><MagicStick /></el-icon>
              {{ aiLoading ? 'AI 优化中…' : 'AI 一键优化' }}
            </button>
          </div>
        </template>
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="6"
          maxlength="2000"
          show-word-limit
          placeholder="先简单写下成色、使用情况、出售原因，点「AI 一键优化」帮你润色成规范文案。"
        />
        <p class="ai-hint">
          <span v-if="aiUsed" class="tag tag--olive">✓ 已由 AI 优化</span>
          AI 会整理成「成色 / 功能 / 出售原因 / 交易方式」四要素。
        </p>
      </el-form-item>

      <!-- 图片 -->
      <el-form-item label="商品图片（1-5 张）">
        <div class="uploader">
          <div v-for="(img, i) in form.images" :key="i" class="img-cell">
            <img :src="img" />
            <button type="button" class="img-del" @click.prevent="removeImage(i)"><el-icon><Close /></el-icon></button>
          </div>
          <label v-if="form.images.length < 5" class="img-add" :class="{ busy: uploading }">
            <input type="file" accept="image/*" hidden @change="onPickImage" :disabled="uploading" />
            <el-icon size="24"><Plus /></el-icon>
            <span>{{ uploading ? '上传中' : '添加' }}</span>
          </label>
        </div>
      </el-form-item>

      <!-- 联系方式 -->
      <el-form-item label="联系方式（选填）">
        <el-input v-model="form.contact" placeholder="微信 / QQ / 手机，不填则用资料里的" />
      </el-form-item>

      <button type="button" class="btn-ink btn-ink--brand submit-btn" :disabled="submitting" @click.prevent="onSubmit">
        {{ submitting ? '发布中…' : '发布商品' }}
      </button>
      <p class="submit-note muted">发布后将自动进行 AI 违规检测，通过后等待管理员审核。</p>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Close, MagicStick } from '@element-plus/icons-vue'
import { getCategories, type CategoryVO } from '@/api/category'
import { optimizeDescription } from '@/api/ai'
import { uploadImage } from '@/api/upload'
import { publishGoods } from '@/api/goods'

const router = useRouter()

const categories = ref<CategoryVO[]>([])
const conditionOptions = ['全新', '九五新', '九成新', '八成新', '七成新', '有使用痕迹']
const condition = ref('')
const rawText = ref('')

const form = reactive({
  title: '',
  description: '',
  price: undefined as number | undefined,
  originalPrice: undefined as number | undefined,
  categoryId: undefined as number | undefined,
  images: [] as string[],
  contact: '',
})

const aiLoading = ref(false)
const aiUsed = ref(false)
const uploading = ref(false)
const submitting = ref(false)

const categoryName = computed(() => categories.value.find(c => c.id === form.categoryId)?.name || '')

// 把描述框内容同步给 rawText（AI 用），保证按钮基于当前文本
function syncRaw() { rawText.value = form.description }

async function onOptimize() {
  if (aiLoading.value) return
  syncRaw()
  if (!form.description.trim()) { ElMessage.warning('先写点描述再优化'); return }
  aiLoading.value = true
  try {
    const optimized = await optimizeDescription({
      title: form.title || '二手商品',
      description: form.description,
    })
    form.description = optimized
    aiUsed.value = true
    ElMessage.success('AI 已为你润色描述')
  } catch {
    // request 拦截器已提示，这里保底
    ElMessage.error('AI 优化失败，请稍后再试')
  } finally {
    aiLoading.value = false
  }
}

async function onPickImage(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { ElMessage.warning('图片不能超过 5MB'); input.value = ''; return }
  uploading.value = true
  try {
    const url = await uploadImage(file)
    form.images.push(url)
  } catch {
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

function removeImage(i: number) { form.images.splice(i, 1) }

function validate(): string | null {
  if (!form.title.trim() || form.title.trim().length < 2) return '标题至少 2 个字'
  if (!form.categoryId) return '请选择分类'
  if (!form.price || form.price <= 0) return '请填写有效售价'
  if (form.price > 99999.99) return '售价不能超过 99999.99'
  if (!form.description.trim()) return '请填写商品描述'
  if (form.images.length < 1) return '请至少上传 1 张图片'
  return null
}

async function onSubmit() {
  if (submitting.value) return
  const err = validate()
  if (err) { ElMessage.warning(err); return }
  submitting.value = true
  try {
    await publishGoods({
      title: form.title.trim(),
      description: condition.value ? `【${condition.value}】${form.description.trim()}` : form.description.trim(),
      rawDescription: rawText.value || undefined,
      price: form.price!,
      originalPrice: form.originalPrice || undefined,
      categoryId: form.categoryId!,
      categoryName: categoryName.value,
      images: form.images,
      contact: form.contact || undefined,
    })
    await ElMessageBox.alert('商品已提交，AI 检测通过，正在等待管理员审核。', '发布成功', {
      confirmButtonText: '查看我的发布',
      type: 'success',
    })
    router.push('/profile')
  } catch (e: any) {
    const msg: string = e?.message || ''
    if (msg.includes('违规')) {
      ElMessageBox.alert(msg.replace('内容违规: ', ''), '内容未通过 AI 检测', {
        confirmButtonText: '我去修改',
        type: 'warning',
      })
    }
    // 其它错误 request 拦截器已提示
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try { categories.value = await getCategories() } catch { /* 静默 */ }
})
</script>

<style scoped>
.publish { padding-bottom: 40px; }

.topbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px;
  position: sticky; top: 0; z-index: 50;
  background: var(--paper); border-bottom: 1px solid var(--line);
}
.topbar-title { font-size: 15px; font-weight: 600; }
.icon-btn {
  width: 40px; height: 40px;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid var(--line); border-radius: var(--radius); background: var(--card);
}

.pub-head { padding: 20px 18px 0; }
.pub-title { font-size: 30px; }

.pub-form { padding: 18px; }
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }

/* AI 按钮 */
.desc-label { display: flex; align-items: center; justify-content: space-between; width: 100%; }
.ai-btn {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 13px; font-weight: 600;
  color: var(--brand);
  padding: 4px 12px;
  border: 1px solid var(--brand); border-radius: var(--radius);
  background: var(--brand-soft);
  transition: all 0.18s var(--ease);
}
.ai-btn:hover:not(:disabled) { background: var(--brand); color: #fff; }
.ai-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.ai-icon.spin { animation: spin 0.9s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.ai-hint { font-size: 12px; color: var(--ink-mute); margin-top: 8px; display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }

/* 上传 */
.uploader { display: flex; flex-wrap: wrap; gap: 10px; }
.img-cell, .img-add {
  width: 88px; height: 88px;
  border-radius: var(--radius); overflow: hidden; position: relative;
}
.img-cell img { width: 100%; height: 100%; object-fit: cover; }
.img-del {
  position: absolute; top: 2px; right: 2px;
  width: 20px; height: 20px; border-radius: 50%;
  background: rgba(26,26,26,0.7); color: #fff;
  display: flex; align-items: center; justify-content: center; font-size: 12px;
}
.img-add {
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px;
  border: 1px dashed var(--line); color: var(--ink-mute); cursor: pointer;
  font-size: 12px; background: var(--paper-2);
  transition: border-color 0.18s var(--ease);
}
.img-add:hover { border-color: var(--brand); color: var(--brand); }
.img-add.busy { opacity: 0.6; pointer-events: none; }

.submit-btn { width: 100%; margin-top: 8px; }
.submit-note { font-size: 12px; text-align: center; margin-top: 10px; }
</style>
