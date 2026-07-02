<template>
  <div class="doctor-detail-page">
    <NavBar title="医生详情" show-back @back="$router.back()" />

    <div v-if="detail" class="detail-content">
      <!-- 医生信息卡片 -->
      <div class="info-card">
        <div class="info-top">
          <el-avatar :size="68" :src="detail.avatar" />
          <div class="info-main">
            <div class="info-name">
              {{ detail.realName }}
              <el-tag size="small" type="success">{{ detail.title }}</el-tag>
            </div>
            <div class="info-dept">{{ detail.departmentName }} · {{ detail.hospital }}</div>
            <div class="info-meta">
              <span>⭐ {{ detail.rating?.toFixed(1) }}</span>
              <span>{{ detail.consultationCount }} 次问诊</span>
              <span>{{ detail.experience }} 年经验</span>
            </div>
          </div>
        </div>
        <div class="info-tags">
          <el-tag v-if="detail.specialty" size="small" type="warning">{{ detail.specialty }}</el-tag>
          <el-tag v-if="detail.education" size="small" type="info">{{ detail.education }}</el-tag>
        </div>
        <div class="info-desc" v-if="detail.introduction">{{ detail.introduction }}</div>
      </div>

      <!-- 擅长领域 -->
      <div class="section" v-if="detail.specialty">
        <div class="section-title">擅长领域</div>
        <p class="specialty-text">{{ detail.specialty }}</p>
      </div>

      <!-- 患者评价 -->
      <div class="section">
        <div class="section-title">患者评价</div>
        <div v-if="detail.recentEvaluations?.length">
          <div v-for="e in detail.recentEvaluations" :key="e.id" class="eval-item">
            <div class="eval-header flex-between">
              <div class="eval-user">
                <el-avatar :size="30" :src="e.userAvatar" />
                <div>
                  <div class="eval-user-name">{{ e.userName }}</div>
                  <div class="eval-time">{{ e.createTime?.slice(0, 10) }}</div>
                </div>
              </div>
              <el-rate :model-value="e.rating" disabled size="small" />
            </div>
            <div class="eval-content" v-if="e.content">{{ e.content }}</div>
            <div class="eval-reply" v-if="e.reply">
              <span class="reply-label">👨‍⚕️ 医生回复：</span>{{ e.reply }}
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无评价" :image-size="60" />
      </div>
    </div>

    <!-- 加载/错误状态 -->
    <div v-else class="loading-state">
      <template v-if="loadError">
        <el-result icon="error" title="加载失败" sub-title="网络或服务异常，请稍后重试">
          <template #extra>
            <el-button type="primary" @click="window.location.reload()">
              重新加载
            </el-button>
          </template>
        </el-result>
      </template>
      <el-skeleton v-else :rows="4" animated />
    </div>

    <!-- 底部操作栏 -->
    <div class="bottom-bar safe-bottom" v-if="detail">
      <div class="fee-info">
        <span class="fee-label">图文问诊</span>
        <span class="fee-price">¥{{ detail.consultationFee }}</span>
      </div>
      <el-button type="primary" size="large" round class="consult-btn" @click="openConsultDialog">
        图文问诊
      </el-button>
    </div>

    <!-- 发起问诊弹窗 -->
    <el-dialog
      v-model="consultDialog"
      title="发起问诊"
      width="90%"
      :close-on-click-modal="false"
      @open="loadPets"
    >
      <el-form :model="consultForm" :rules="consultRules" ref="consultFormRef" label-position="top">
        <el-form-item label="选择宠物" prop="petId">
          <el-select v-model="consultForm.petId" placeholder="请选择需要问诊的宠物" style="width:100%">
            <el-option
              v-for="p in pets"
              :key="p.id"
              :value="p.id"
              :label="`${p.name}（${p.speciesName}${p.breed ? ' · ' + p.breed : ''}）`"
            />
          </el-select>
          <div class="form-tip" v-if="pets.length === 0">
            暂无宠物，请先<router-link to="/pet/add">添加宠物</router-link>
          </div>
        </el-form-item>
        <el-form-item label="主要问题（主诉）" prop="chiefComplaint">
          <el-input
            v-model="consultForm.chiefComplaint"
            type="textarea"
            :rows="3"
            placeholder="请描述宠物出现了什么问题，持续多久了…"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="症状描述（选填）">
          <el-input
            v-model="consultForm.symptoms"
            type="textarea"
            :rows="2"
            placeholder="如有其他症状请补充说明"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="consultDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleStartConsult">
          确认问诊（¥{{ detail?.consultationFee }}）
        </el-button>
      </template>
    </el-dialog>

    <!-- 支付弹窗 -->
    <el-dialog v-model="payDialog" title="确认支付" width="85%">
      <div class="pay-info">
        <div class="pay-item flex-between">
          <span>医生</span><span>{{ detail?.realName }}</span>
        </div>
        <div class="pay-item flex-between">
          <span>问诊方式</span><span>图文问诊</span>
        </div>
        <div class="pay-item flex-between">
          <span>费用</span><span class="pay-price">¥{{ detail?.consultationFee }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="payDialog = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="handlePay">
          立即支付
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import NavBar from '@/layouts/NavBar.vue'
import { getDoctorDetail } from '@/api/doctor'
import { getPetList } from '@/api/pet'
import { createConsultation } from '@/api/consultation'
import type { DoctorDetail, Pet } from '@/types'

const route = useRoute()
const router = useRouter()

const detail = ref<DoctorDetail | null>(null)
const loadError = ref(false)
const doctorId = Number(route.params.id)

onMounted(async () => {
  try {
    const { data } = await getDoctorDetail(doctorId)
    if (data.code === 200) detail.value = data.data
  } catch {
    loadError.value = true
  }
})

// ----- 发起问诊弹窗 -----
const consultDialog = ref(false)
const consultFormRef = ref<FormInstance>()
const pets = ref<Pet[]>([])
const submitting = ref(false)

const consultForm = reactive({
  petId: null as number | null,
  chiefComplaint: '',
  symptoms: '',
})

const consultRules: FormRules = {
  petId: [{ required: true, message: '请选择宠物', trigger: 'change' }],
  chiefComplaint: [{ required: true, message: '请填写主要问题', trigger: 'blur' }],
}

async function loadPets() {
  if (pets.value.length) return
  try {
    const { data } = await getPetList()
    if (data.code === 200) pets.value = data.data.records
  } catch { /* ignore */ }
}

function openConsultDialog() {
  consultForm.petId = null
  consultForm.chiefComplaint = ''
  consultForm.symptoms = ''
  consultDialog.value = true
}

// ----- 支付弹窗 -----
const payDialog = ref(false)
const paying = ref(false)
const newConsultationId = ref<number | null>(null)

async function handleStartConsult() {
  const valid = await consultFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const { data } = await createConsultation({
      petId: consultForm.petId!,
      departmentId: detail.value?.departmentId,
      type: 1, // 图文问诊
      chiefComplaint: consultForm.chiefComplaint,
      symptoms: consultForm.symptoms || undefined,
    })
    if (data.code === 200) {
      consultDialog.value = false
      newConsultationId.value = data.data.id
      payDialog.value = true
    }
  } finally {
    submitting.value = false
  }
}

async function handlePay() {
  paying.value = true
  try {
    // TODO: 调用支付API（微信/支付宝/余额），此处模拟支付成功
    await new Promise((r) => setTimeout(r, 800))
    ElMessage.success('支付成功，进入问诊')
    payDialog.value = false
    router.push(`/consult/chat/${newConsultationId.value}`)
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.doctor-detail-page { min-height: 100vh; background: var(--bg); padding-bottom: 80px; }

.info-card { background: #fff; padding: 20px 16px; margin-bottom: 10px; }
.info-top { display: flex; gap: 14px; }
.info-main { flex: 1; }
.info-name { font-size: 17px; font-weight: 600; display: flex; align-items: center; gap: 8px; }
.info-dept { font-size: 13px; color: var(--text-secondary); margin: 4px 0; }
.info-meta { font-size: 12px; color: var(--text-secondary); display: flex; gap: 12px; margin-top: 2px; }
.info-tags { display: flex; gap: 6px; margin-top: 12px; }
.info-desc { font-size: 13px; color: var(--text-primary); margin-top: 12px; line-height: 1.7; }

.section { background: #fff; padding: 16px; margin-bottom: 10px; }
.section-title { font-size: 15px; font-weight: 600; margin-bottom: 12px; }
.specialty-text { font-size: 13px; color: var(--text-secondary); line-height: 1.6; }

.eval-item { padding: 12px 0; border-bottom: 1px solid var(--border); }
.eval-item:last-child { border-bottom: none; }
.eval-user { display: flex; align-items: center; gap: 10px; }
.eval-user-name { font-size: 13px; font-weight: 500; }
.eval-time { font-size: 11px; color: var(--text-muted); }
.eval-content { font-size: 13px; color: var(--text-secondary); margin: 8px 0; line-height: 1.5; }
.eval-reply {
  font-size: 12px; color: #388e3c; background: #f1f8e9; padding: 8px 10px;
  border-radius: 8px; margin-top: 6px; line-height: 1.5;
}
.reply-label { font-weight: 500; }

.loading-state { padding: 20px; }

/* 底部栏 */
.bottom-bar {
  position: fixed; bottom: 0; left: 50%; transform: translateX(-50%);
  width: 100%; max-width: 480px; display: flex; align-items: center;
  justify-content: space-between; padding: 10px 16px;
  background: #fff; border-top: 1px solid var(--border); z-index: 100;
}
.fee-label { font-size: 14px; color: var(--text-regular); display: block; }
.fee-price { font-size: 20px; color: var(--primary); font-weight: 700; }
.consult-btn { width: 160px; }

/* 支付 */
.pay-info { padding: 4px 0 12px; }
.pay-item { padding: 10px 0; border-bottom: 1px solid var(--border); font-size: 14px; }
.pay-price { color: var(--primary); font-weight: 600; font-size: 18px; }

.form-tip { font-size: 12px; color: var(--text-muted); margin-top: 4px; }
.form-tip a { color: var(--primary); }
</style>
