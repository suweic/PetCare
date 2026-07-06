<template>
  <MainLayout>
    <div class="consult-page">
      <NavBar title="问诊记录" />

      <!-- 状态 Tabs: 全部 / 进行中 / 已完成 -->
      <div class="status-tabs">
        <span
          v-for="t in tabs"
          :key="t.value ?? 'all'"
          class="tab-item"
          :class="{ active: activeStatus === t.value }"
          @click="switchTab(t.value)"
        >{{ t.label }}</span>
      </div>

      <!-- 问诊列表 -->
      <div class="consult-list" v-loading="loading">
        <!-- 错误兜底：接口失败时给出重试入口，避免控制台一片红 -->
        <div v-if="errorMsg" class="error-state">
          <el-empty :description="errorMsg">
            <el-button type="primary" @click="fetchList">重新加载</el-button>
          </el-empty>
        </div>

        <div
          v-for="c in store.list"
          :key="c.id"
          class="consult-card"
        >
          <div class="card-header flex-between">
            <div class="card-header-left">
              <span class="consult-type">{{ typeIcon(c.type) }} {{ typeName(c.type) }}</span>
              <span class="card-id">#{{ c.id }}</span>
            </div>
            <el-tag :type="statusType(c.status)" size="small">{{ statusName(c.status) }}</el-tag>
          </div>

          <div class="card-body">
            <div class="card-field" v-if="c.chiefComplaint">
              <span class="field-label">主诉</span>
              <span class="field-value">{{ c.chiefComplaint }}</span>
            </div>
            <div class="card-field" v-if="c.symptoms">
              <span class="field-label">症状</span>
              <span class="field-value">{{ c.symptoms }}</span>
            </div>
          </div>

          <div class="card-footer flex-between">
            <span class="card-time">{{ formatDate(c.createTime) }}</span>
            <div class="card-actions">
              <!-- 进行中 → 继续问诊 -->
              <el-button
                v-if="c.status === 1"
                size="small"
                type="primary"
                @click="enterChat(c)"
              >继续问诊</el-button>
              <!-- 已完成 → 查看处方 + 评价 -->
              <template v-if="c.status === 2">
                <el-button size="small" text @click="viewPrescription(c)">
                  <el-icon><Document /></el-icon> 处方
                </el-button>
                <el-button size="small" text type="warning" @click="evaluateConsult(c)">
                  <el-icon><Star /></el-icon> 评价
                </el-button>
              </template>
              <!-- 待接单 → 取消 -->
              <el-button
                v-if="c.status === 0"
                size="small"
                text
                type="danger"
                @click="cancelConsult(c)"
              >取消</el-button>
            </div>
          </div>
        </div>

        <el-empty v-if="!loading && store.list.length === 0" description="暂无问诊记录">
          <el-button type="primary" @click="$router.push('/doctor-list')">去找医生问诊</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 评价对话框 -->
    <el-dialog v-model="evalDialog" title="评价问诊" width="88%">
      <div class="eval-content">
        <div class="eval-rating-row">
          <span>评分</span>
          <el-rate v-model="evalRating" :max="5" show-score />
        </div>
        <el-input
          v-model="evalText"
          type="textarea"
          :rows="3"
          placeholder="分享你的就诊体验（可选）"
          maxlength="200"
          show-word-limit
        />
        <el-checkbox v-model="evalAnonymous" style="margin-top:10px" size="small">匿名评价</el-checkbox>
      </div>
      <template #footer>
        <el-button @click="evalDialog = false">取消</el-button>
        <el-button type="primary" :loading="evalSubmitting" @click="submitEval">提交</el-button>
      </template>
    </el-dialog>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Star } from '@element-plus/icons-vue'
import MainLayout from '@/layouts/MainLayout.vue'
import NavBar from '@/layouts/NavBar.vue'
import { useConsultationStore } from '@/stores/consultation'
import type { Consultation } from '@/types'

const router = useRouter()
const store = useConsultationStore()
const loading = ref(false)
const activeStatus = ref<number | null>(null)
const errorMsg = ref('')

const tabs = [
  { label: '全部', value: null },
  { label: '进行中', value: 1 },
  { label: '已完成', value: 2 },
]

onMounted(() => fetchList())

function switchTab(v: number | null) {
  activeStatus.value = v
  fetchList()
}

async function fetchList() {
  loading.value = true
  errorMsg.value = ''
  try {
    await store.fetchList({ status: activeStatus.value ?? undefined })
  } catch (e: any) {
    errorMsg.value = e?.message || '加载问诊记录失败，请稍后重试'
    console.error('[ConsultList] fetchList failed:', e)
  } finally {
    loading.value = false
  }
}

// ----- 操作 -----
function enterChat(c: Consultation) {
  router.push(`/consult/chat/${c.id}`)
}

function viewPrescription(c: Consultation) {
  router.push(`/prescription/${c.id}`)
}

async function cancelConsult(c: Consultation) {
  try {
    await ElMessageBox.confirm('确定取消此问诊？', '提示', { type: 'warning' })
    await store.cancel(c.id)
    ElMessage.success('已取消')
    fetchList()
  } catch { /* cancel */ }
}

// ----- 评价 -----
const evalDialog = ref(false)
const evalRating = ref(5)
const evalText = ref('')
const evalAnonymous = ref(false)
const evalSubmitting = ref(false)
const evalTargetId = ref(0)

function evaluateConsult(c: Consultation) {
  evalTargetId.value = c.id
  evalRating.value = 5
  evalText.value = ''
  evalAnonymous.value = false
  evalDialog.value = true
}

async function submitEval() {
  evalSubmitting.value = true
  try {
    await store.submitEvaluation({
      consultationId: evalTargetId.value,
      rating: evalRating.value,
      content: evalText.value || undefined,
      isAnonymous: evalAnonymous.value ? 1 : 0,
    })
    ElMessage.success('评价成功')
    evalDialog.value = false
    fetchList()
  } finally {
    evalSubmitting.value = false
  }
}

// ----- 辅助 -----
function typeIcon(t: number) { return { 1: '📝', 2: '📹', 3: '🎤' }[t] || '' }
function typeName(t: number) { return { 1: '图文问诊', 2: '视频问诊', 3: '语音问诊' }[t] || '问诊' }
function statusName(s: number) {
  return { 0: '待接单', 1: '进行中', 2: '已完成', 3: '已取消', 4: '已拒绝', 5: '超时' }[s] || '未知'
}
function statusType(s: number): 'warning' | 'success' | 'info' | 'danger' {
  if (s === 0 || s === 1) return 'warning'
  if (s === 2) return 'success'
  return 'info'
}
function formatDate(d?: string) { return d ? d.slice(0, 16).replace('T', ' ') : '' }
</script>

<style scoped>
.consult-page { min-height: 100vh; padding-bottom: 60px; }

.status-tabs {
  display: flex;
  background: #fff;
  padding: 0 16px;
  border-bottom: 1px solid var(--border);
}
.tab-item {
  padding: 12px 16px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.tab-item.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
  font-weight: 500;
}

.consult-list { padding: 10px 0; }
.consult-card {
  background: #fff;
  margin: 0 12px 10px;
  padding: 14px 16px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.card-header { margin-bottom: 10px; }
.card-header-left { display: flex; align-items: center; gap: 8px; }
.consult-type { font-size: 14px; font-weight: 500; }
.card-id { font-size: 11px; color: var(--text-muted); }

.card-body { margin-bottom: 10px; }
.card-field {
  display: flex; align-items: baseline; gap: 8px;
  font-size: 13px; margin-bottom: 4px; line-height: 1.5;
}
.field-label {
  font-size: 11px; color: var(--text-muted); flex-shrink: 0;
  background: var(--bg); padding: 1px 6px; border-radius: 4px;
}
.field-value {
  color: var(--text-secondary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.card-footer { margin-top: 8px; }
.card-time { font-size: 11px; color: var(--text-muted); }
.card-actions { display: flex; gap: 4px; }

/* 评价弹窗 */
.eval-content { padding: 4px 0; }
.eval-rating-row {
  display: flex; align-items: center; gap: 12px; margin-bottom: 12px;
  font-size: 14px;
}

/* 错误态 */
.error-state { padding: 40px 16px; }
</style>
