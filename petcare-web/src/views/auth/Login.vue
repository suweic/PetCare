<template>
  <div class="login-page flex-center">
    <div class="login-card">
      <div class="login-logo">
        <span class="logo-icon">🐾</span>
        <h1>PetCare</h1>
        <p>宠物在线问诊平台</p>
      </div>

      <!-- Tab 切换：密码登录 / 验证码登录 -->
      <div class="login-tabs">
        <span
          class="tab-item"
          :class="{ active: activeTab === 'password' }"
          @click="switchTab('password')"
        >密码登录</span>
        <span
          class="tab-item"
          :class="{ active: activeTab === 'code' }"
          @click="switchTab('code')"
        >验证码登录</span>
      </div>

      <!-- 密码登录表单 -->
      <el-form
        v-show="activeTab === 'password'"
        ref="pwdFormRef"
        :model="pwdForm"
        :rules="pwdRules"
        size="large"
      >
        <el-form-item prop="phone">
          <el-input v-model="pwdForm.phone" placeholder="请输入手机号" maxlength="11" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="pwdForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            @keyup.enter="handlePwdLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handlePwdLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 验证码登录表单 -->
      <el-form
        v-show="activeTab === 'code'"
        ref="codeFormRef"
        :model="codeForm"
        :rules="codeRules"
        size="large"
      >
        <el-form-item prop="phone">
          <el-input v-model="codeForm.phone" placeholder="请输入手机号" maxlength="11" clearable />
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-row">
            <el-input v-model="codeForm.code" placeholder="验证码" maxlength="6" class="code-input" />
            <el-button
              class="code-btn"
              :disabled="countdown > 0 || codeForm.phone.length < 11"
              @click="sendVerificationCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleCodeLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span @click="showRegister = true">注册账号</span>
      </div>

      <!-- 注册弹窗 -->
      <el-dialog v-model="showRegister" title="注册账号" width="88%" :close-on-click-modal="false">
        <el-form :model="regForm" :rules="regRules" ref="regFormRef" size="large">
          <el-form-item prop="phone">
            <el-input v-model="regForm.phone" placeholder="手机号" maxlength="11" />
          </el-form-item>
          <el-form-item prop="code">
            <div class="code-row">
              <el-input v-model="regForm.code" placeholder="验证码" maxlength="6" class="code-input" />
              <el-button class="code-btn" :disabled="regCd > 0 || regForm.phone.length < 11" @click="sendRegCode">
                {{ regCd > 0 ? `${regCd}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="regForm.password" type="password" placeholder="设置密码（至少6位）" show-password />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showRegister = false">取消</el-button>
          <el-button type="primary" :loading="loading" @click="handleRegister">注册</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { sendCode } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ---------- Tab 切换 ----------
const activeTab = ref<'password' | 'code'>('password')
function switchTab(tab: 'password' | 'code') {
  activeTab.value = tab
}

// ---------- 密码登录 ----------
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({ phone: '', password: '' })
const pwdRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// ---------- 验证码登录 ----------
const codeFormRef = ref<FormInstance>()
const codeForm = reactive({ phone: '', code: '' })
const countdown = ref(0)
const codeRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

// ---------- 通用 ----------
const loading = ref(false)

async function handlePwdLogin() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(pwdForm.phone, pwdForm.password)
    ElMessage.success('登录成功')
    router.replace((route.query.redirect as string) || '/home')
  } finally {
    loading.value = false
  }
}

async function sendVerificationCode() {
  const phone = codeForm.phone
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  await sendCode(phone)
  ElMessage.success('验证码已发送')
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

async function handleCodeLogin() {
  const valid = await codeFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.loginWithCode(codeForm.phone, codeForm.code)
    ElMessage.success('登录成功')
    router.replace((route.query.redirect as string) || '/home')
  } finally {
    loading.value = false
  }
}

// ---------- 注册 ----------
const showRegister = ref(false)
const regFormRef = ref<FormInstance>()
const regForm = reactive({ phone: '', password: '', code: '' })
const regCd = ref(0)
const regRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号' },
    { pattern: /^1[3-9]\d{9}$/, message: '格式错误' },
  ],
  password: [{ required: true, message: '请设置密码', min: 6, trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码' }],
}

async function sendRegCode() {
  if (!/^1[3-9]\d{9}$/.test(regForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  await sendCode(regForm.phone)
  ElMessage.success('验证码已发送')
  regCd.value = 60
  const timer = setInterval(() => {
    regCd.value--
    if (regCd.value <= 0) clearInterval(timer)
  }, 1000)
}

async function handleRegister() {
  const valid = await regFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.doRegister(regForm.phone, regForm.password, regForm.code)
    ElMessage.success('注册成功，请登录')
    showRegister.value = false
    // 重置注册表单，防止下次打开时残留上次数据
    regForm.phone = ''
    regForm.password = ''
    regForm.code = ''
    regFormRef.value?.resetFields()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
}
.login-card {
  width: 85%;
  max-width: 360px;
  padding: 32px 24px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: var(--shadow);
}
.login-logo {
  text-align: center;
  margin-bottom: 20px;
}
.logo-icon { font-size: 48px; }
.login-logo h1 {
  font-size: 24px;
  color: var(--primary);
  margin: 8px 0 2px;
}
.login-logo p {
  font-size: 13px;
  color: var(--text-muted);
}

/* Tab 切换 */
.login-tabs {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 20px;
}
.tab-item {
  font-size: 15px;
  color: var(--text-muted);
  cursor: pointer;
  padding-bottom: 6px;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.tab-item.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
  font-weight: 500;
}

.login-btn { width: 100%; }

/* 验证码行 */
.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.code-input { flex: 1; }
.code-btn {
  flex-shrink: 0;
  width: 110px;
  font-size: 12px;
}

.login-footer {
  text-align: center;
  font-size: 13px;
  color: var(--primary);
  cursor: pointer;
  margin-top: 4px;
}
</style>
