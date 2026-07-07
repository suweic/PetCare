<template>
  <div class="pet-edit-page">
    <!-- 顶部栏 -->
    <div class="edit-header">
      <div class="header-left" @click="$router.back()">
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </div>
      <span class="header-title">{{ isEdit ? '编辑宠物' : '添加宠物' }}</span>
      <div class="header-right" />
    </div>

    <!-- 表单 -->
    <div class="form-container">
      <!-- 头像上传 -->
      <div class="avatar-section">
        <div class="avatar-upload" @click="triggerUpload">
          <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" />
          <div v-else class="avatar-placeholder">
            <el-icon :size="32" color="#bdbdbd"><Camera /></el-icon>
            <span>上传头像</span>
          </div>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            class="file-input"
            @change="handleFileChange"
          />
        </div>
        <p class="avatar-tip">点击上传宠物照片</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
      >
        <!-- 姓名 -->
        <el-form-item label="宠物姓名" prop="name">
          <el-input v-model="form.name" placeholder="给你的宠物起个名字" maxlength="20" clearable />
        </el-form-item>

        <!-- 物种 + 品种 -->
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="物种" prop="species">
              <el-select v-model="form.species" placeholder="选择物种">
                <el-option :value="1" label="🐱 猫" />
                <el-option :value="2" label="🐶 狗" />
                <el-option :value="3" label="🐹 其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品种">
              <el-input v-model="form.breed" placeholder="品种（选填）" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 性别 -->
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender" class="gender-group">
            <el-radio-button :value="1">♂ 公</el-radio-button>
            <el-radio-button :value="2">♀ 母</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 出生日期 + 体重 -->
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker
                v-model="form.birthday"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                :disabled-date="disableFuture"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重 (kg)">
              <el-input
                v-model="form.weight"
                placeholder="如 3.5"
                type="number"
                :min="0"
                :step="0.1"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 绝育 -->
        <el-form-item label="是否绝育">
          <el-switch
            v-model="form.sterilized"
            :active-value="1"
            :inactive-value="0"
            active-text="已绝育"
            inactive-text="未绝育"
          />
        </el-form-item>

        <!-- 病史 -->
        <el-form-item label="病史记录">
          <el-input
            v-model="form.medicalHistory"
            type="textarea"
            :rows="3"
            placeholder="如慢性病、手术史等（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 过敏信息 -->
        <el-form-item label="过敏信息">
          <el-input
            v-model="form.allergyInfo"
            type="textarea"
            :rows="2"
            placeholder="如对某种药物/食物过敏（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 提交 -->
        <el-form-item class="submit-item">
          <el-button type="primary" class="submit-btn" :loading="loading" round @click="handleSubmit">
            {{ isEdit ? '保存修改' : '添加宠物' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Camera } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { usePetStore } from '@/stores/pet'
import { getPetDetail, uploadPetAvatar } from '@/api/pet'
import { safeAvatar } from '@/utils/avatar'

const route = useRoute()
const router = useRouter()
const store = usePetStore()

const isEdit = computed(() => !!route.params.id)
const formRef = ref<FormInstance>()
const loading = ref(false)
const uploading = ref(false)

const fileInputRef = ref<HTMLInputElement>()
const avatarUrl = ref('')
const avatarFile = ref<File | null>(null)

const form = reactive({
  name: '',
  species: null as number | null,
  breed: '',
  gender: null as number | null,
  birthday: '',
  weight: '' as string | number,
  medicalHistory: '',
  allergyInfo: '',
  sterilized: 0,
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入宠物姓名', trigger: 'blur' },
    { max: 20, message: '不能超过20个字', trigger: 'blur' },
  ],
  species: [{ required: true, message: '请选择物种', trigger: 'change' }],
}

onMounted(async () => {
  if (isEdit.value) {
    const id = Number(route.params.id)
    try {
      const { data } = await getPetDetail(id)
      if (data.code === 200) {
        Object.assign(form, {
          name: data.data.name,
          species: data.data.species,
          breed: data.data.breed || '',
          gender: data.data.gender,
          birthday: data.data.birthDate || '',
          weight: data.data.weight || '',
          medicalHistory: data.data.medicalHistory || '',
          allergyInfo: data.data.allergyInfo || '',
          sterilized: data.data.sterilized || 0,
        })
        if (data.data.avatar) {
          avatarUrl.value = safeAvatar(data.data.avatar)
        }
      }
    } catch { /* ignore */ }
  }
})

function disableFuture(date: Date): boolean {
  return date.getTime() > Date.now()
}

// ----- 头像上传 -----
function triggerUpload() {
  fileInputRef.value?.click()
}

async function handleFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return

  // 前端预览
  const reader = new FileReader()
  reader.onload = () => {
    avatarUrl.value = reader.result as string
  }
  reader.onerror = () => { ElMessage.warning('图片读取失败，请重试') }
  reader.readAsDataURL(file)

  // 上传到服务器
  uploading.value = true
  try {
    const { data } = await uploadPetAvatar(file)
    if (data.code === 200) {
      avatarUrl.value = data.data.url
      avatarFile.value = file
      ElMessage.success('头像上传成功')
    }
  } catch {
    ElMessage.warning('头像上传失败，使用本地预览')
  } finally {
    uploading.value = false
  }
}

// ----- 提交 -----
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const payload = {
      name: form.name,
      species: form.species!,
      breed: form.breed,
      gender: form.gender ?? undefined,
      birthday: form.birthday,
      weight: form.weight ? Number(form.weight) : undefined,
      medicalHistory: form.medicalHistory,
      allergyInfo: form.allergyInfo,
      sterilized: form.sterilized,
      avatar: avatarUrl.value || undefined,
    }
    if (isEdit.value) {
      await store.editPet(Number(route.params.id), payload)
    } else {
      await store.addPet(payload)
    }
    ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
    router.back()
  } catch {
    // 错误已在 request 拦截器中通过 ElMessage 提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.pet-edit-page {
  min-height: 100vh;
  background: var(--bg);
  padding-bottom: 40px;
}

/* 顶部栏 */
.edit-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid var(--border);
}
.header-left, .header-right {
  width: 36px;
  display: flex;
  align-items: center;
  cursor: pointer;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
}

/* 表单容器 */
.form-container {
  padding: 0 16px;
}

/* 头像上传 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0 16px;
}
.avatar-upload {
  position: relative;
  cursor: pointer;
}
.avatar-img {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.avatar-placeholder {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: #f5f5f5;
  border: 2px dashed #ddd;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  font-size: 10px;
  color: var(--text-muted);
}
.file-input { display: none; }
.avatar-tip {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 8px;
}

/* Element Plus 表单项移动端适配 */
:deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  padding-bottom: 4px;
}
:deep(.el-select) {
  width: 100%;
}
:deep(.el-date-editor) {
  width: 100%;
}

/* 性别按钮组 */
.gender-group {
  width: 100%;
}
:deep(.gender-group .el-radio-button__inner) {
  width: 50vw;
  max-width: 200px;
}

/* 提交按钮 */
.submit-item {
  margin-top: 8px;
}
.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
}
</style>
