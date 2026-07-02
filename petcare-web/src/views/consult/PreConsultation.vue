<template>
  <MainLayout>
    <div class="pre-consult-page">
      <NavBar title="AI预问诊" />

      <!-- 症状输入表单 -->
      <div class="form-section">
        <div class="section-title">
          <span class="title-icon">🐾</span> 请描述宠物的症状
        </div>
        <div class="section-desc">
          AI将根据您描述的症状，自动推荐最合适的科室和医生
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <!-- 物种 -->
          <el-form-item label="宠物物种" prop="species">
            <el-select v-model="form.species" placeholder="请选择物种" clearable>
              <el-option :value="1" label="🐱 猫" />
              <el-option :value="2" label="🐶 狗" />
              <el-option :value="3" label="🐰 其他" />
            </el-select>
          </el-form-item>

          <!-- 品种 -->
          <el-form-item label="品种">
            <el-input v-model="form.breed" placeholder="如：英短、金毛、布偶..." maxlength="50" clearable />
          </el-form-item>

          <!-- 年龄 -->
          <el-form-item label="年龄">
            <div class="age-row">
              <el-input-number v-model="form.ageYears" :min="0" :max="30" placeholder="岁" controls-position="right" />
              <span class="age-unit">岁</span>
              <el-input-number v-model="form.ageMonths" :min="0" :max="11" placeholder="月" controls-position="right" />
              <span class="age-unit">个月</span>
            </div>
          </el-form-item>

          <!-- 症状描述（必填） -->
          <el-form-item label="症状描述" prop="symptoms">
            <el-input
              v-model="form.symptoms"
              type="textarea"
              :rows="4"
              placeholder="请详细描述宠物的症状，例如：猫咪最近3天食欲不振、频繁呕吐、精神萎靡..."
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>

          <!-- 持续时间 -->
          <el-form-item label="症状持续时间">
            <el-input v-model="form.symptomDuration" placeholder="如：3天、1周、2小时..." maxlength="50" clearable />
          </el-form-item>

          <!-- 补充信息 -->
          <el-form-item label="补充信息">
            <el-input
              v-model="form.additionalInfo"
              type="textarea"
              :rows="2"
              placeholder="其他补充信息（饮食变化、行为异常、用药史等）"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            :loading="analyzing"
            :disabled="analyzing"
            class="submit-btn"
            @click="handleSubmit"
          >
            <el-icon v-if="!analyzing"><MagicStick /></el-icon>
            {{ analyzing ? 'AI正在分析中...' : '开始AI预问诊分析' }}
          </el-button>
        </el-form>
      </div>

      <!-- 分析结果 -->
      <div v-if="result" class="result-section">
        <!-- 科室推荐 -->
        <div class="result-card dept-card">
          <div class="card-icon">🏥</div>
          <div class="card-content">
            <div class="card-label">AI推荐科室</div>
            <div class="card-value">{{ result.departmentName }}</div>
            <div class="card-desc" v-if="result.departmentDescription">
              {{ result.departmentDescription }}
            </div>
          </div>
          <el-tag type="primary" size="small" effect="plain">AI推荐</el-tag>
        </div>

        <!-- AI分析 -->
        <div class="result-card analysis-card">
          <div class="card-icon">💡</div>
          <div class="card-content">
            <div class="card-label">AI症状分析</div>
            <div class="card-desc analysis-text">{{ result.aiAnalysis }}</div>
          </div>
        </div>

        <!-- 推荐医生 -->
        <div class="doctors-section" v-if="result.recommendedDoctors && result.recommendedDoctors.length > 0">
          <div class="section-title">
            <span class="title-icon">👨‍⚕️</span> 推荐医生（{{ result.recommendedDoctors.length }}位）
          </div>

          <div
            v-for="doc in result.recommendedDoctors"
            :key="doc.doctorId"
            class="result-card doctor-card"
            @click="goDoctorDetail(doc.doctorId)"
          >
            <div class="doctor-avatar">
              <el-avatar :size="48">
                <el-icon :size="24"><UserFilled /></el-icon>
              </el-avatar>
            </div>
            <div class="doctor-info">
              <div class="doctor-name-row">
                <span class="doctor-name">{{ doc.doctorName }}</span>
                <span class="doctor-title">{{ doc.title }}</span>
              </div>
              <div class="doctor-meta">
                <span v-if="doc.hospital">{{ doc.hospital }}</span>
                <span v-if="doc.specialty"> · {{ doc.specialty }}</span>
              </div>
              <div class="doctor-stats">
                <el-rate :model-value="doc.rating" disabled show-score text-color="#ff9900" size="small" />
                <span class="fee" v-if="doc.consultationFee">¥{{ doc.consultationFee }}</span>
              </div>
              <div class="match-reason">
                <el-icon color="#67c23a"><CircleCheckFilled /></el-icon>
                {{ doc.matchReason }}
              </div>
            </div>
            <div class="doctor-arrow">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>

        <!-- AI护理建议 -->
        <div class="result-card advice-card" v-if="result.generalAdvice">
          <div class="card-icon">📋</div>
          <div class="card-content">
            <div class="card-label">就医前注意事项</div>
            <div class="card-desc">{{ result.generalAdvice }}</div>
          </div>
        </div>

        <!-- 模型信息 -->
        <div class="model-info" v-if="result.llmModel">
          分析引擎：{{ result.llmModel }}
        </div>
      </div>

      <!-- 初始引导（未分析时） -->
      <div v-if="!result && !analyzing" class="guide-section">
        <div class="guide-card">
          <div class="guide-icon">🤖</div>
          <div class="guide-title">AI智能分诊</div>
          <div class="guide-desc">
            只需描述宠物的症状，AI将自动分析并推荐最合适的科室和医生，帮您快速找到对的医生。
          </div>
          <div class="guide-steps">
            <div class="step">
              <span class="step-num">1</span>
              <span>描述症状</span>
            </div>
            <div class="step-arrow">→</div>
            <div class="step">
              <span class="step-num">2</span>
              <span>AI智能分析</span>
            </div>
            <div class="step-arrow">→</div>
            <div class="step">
              <span class="step-num">3</span>
              <span>推荐科室医生</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部占位 -->
      <div style="height: 80px" />
    </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { MagicStick, UserFilled, ArrowRight, CircleCheckFilled } from '@element-plus/icons-vue'
import MainLayout from '@/layouts/MainLayout.vue'
import NavBar from '@/layouts/NavBar.vue'
import { analyzeSymptoms } from '@/api/preConsultation'
import type { PreConsultationResult } from '@/types'

const router = useRouter()

// ----- 表单 -----
const formRef = ref<FormInstance>()
const analyzing = ref(false)
const form = reactive({
  species: null as number | null,
  breed: '',
  ageYears: null as number | null,
  ageMonths: null as number | null,
  symptoms: '',
  symptomDuration: '',
  additionalInfo: '',
})

const rules: FormRules = {
  symptoms: [
    { required: true, message: '请描述宠物的症状', trigger: 'blur' },
    { min: 2, max: 2000, message: '症状描述2-2000字符', trigger: 'blur' },
  ],
}

// ----- 结果 -----
const result = ref<PreConsultationResult | null>(null)

// ----- 提交分析 -----
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  analyzing.value = true
  result.value = null

  try {
    const res = await analyzeSymptoms({
      species: form.species ?? undefined,
      breed: form.breed || undefined,
      ageYears: form.ageYears ?? undefined,
      ageMonths: form.ageMonths ?? undefined,
      symptoms: form.symptoms,
      symptomDuration: form.symptomDuration || undefined,
      additionalInfo: form.additionalInfo || undefined,
    })
    result.value = res.data.data
    ElMessage.success('AI分析完成')

    // 滚动到结果区域
    setTimeout(() => {
      document.querySelector('.result-section')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }, 100)
  } catch {
    ElMessage.error('分析失败，请稍后重试')
  } finally {
    analyzing.value = false
  }
}

// ----- 查看医生详情 -----
function goDoctorDetail(doctorId: number) {
  router.push(`/consult/doctor/${doctorId}`)
}
</script>

<style scoped>
.pre-consult-page {
  min-height: 100vh;
  background: var(--bg, #f5f5f5);
}

/* ===== 表单区域 ===== */
.form-section {
  background: #fff;
  margin: 10px 12px;
  padding: 20px 16px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text, #333);
  margin-bottom: 4px;
}
.title-icon { margin-right: 4px; }

.section-desc {
  font-size: 12px;
  color: var(--text-secondary, #999);
  margin-bottom: 16px;
  line-height: 1.6;
}

.age-row {
  display: flex;
  align-items: center;
  gap: 4px;
}
.age-row .el-input-number {
  width: 100px;
}
.age-unit {
  font-size: 13px;
  color: var(--text-secondary, #666);
  margin-right: 8px;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
  height: 44px;
  font-size: 15px;
  border-radius: 8px;
}

/* ===== 结果区域 ===== */
.result-section {
  padding: 0 12px 12px;
}

.result-card {
  background: #fff;
  margin-bottom: 10px;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.card-icon {
  font-size: 28px;
  flex-shrink: 0;
  line-height: 1;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-label {
  font-size: 12px;
  color: var(--text-muted, #aaa);
  margin-bottom: 2px;
}

.card-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--text, #333);
}

.card-desc {
  font-size: 13px;
  color: var(--text-secondary, #666);
  line-height: 1.6;
  margin-top: 4px;
}

.analysis-text {
  color: var(--text, #333);
}

/* 医生卡片 */
.doctor-card {
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.doctor-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.doctor-card:active {
  background: #fafafa;
}

.doctor-avatar {
  flex-shrink: 0;
  padding-top: 2px;
}

.doctor-info {
  flex: 1;
  min-width: 0;
}

.doctor-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}
.doctor-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text, #333);
}
.doctor-title {
  font-size: 11px;
  color: var(--primary, #409eff);
  background: rgba(64,158,255,0.1);
  padding: 1px 6px;
  border-radius: 4px;
}

.doctor-meta {
  font-size: 12px;
  color: var(--text-secondary, #666);
  margin-bottom: 4px;
}

.doctor-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.fee {
  font-size: 13px;
  font-weight: 600;
  color: #e6a23c;
}

.match-reason {
  font-size: 12px;
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  background: rgba(103,194,58,0.06);
  padding: 4px 8px;
  border-radius: 6px;
}

.doctor-arrow {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  color: var(--text-muted, #ccc);
  padding-top: 14px;
}

/* 建议卡片 */
.advice-card {
  border-left: 3px solid #e6a23c;
}

/* 模型信息 */
.model-info {
  text-align: center;
  font-size: 11px;
  color: var(--text-muted, #ccc);
  padding: 8px 0 4px;
}

/* ===== 引导区域 ===== */
.guide-section {
  padding: 20px 12px;
}

.guide-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px 20px;
  text-align: center;
  color: #fff;
}

.guide-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.guide-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
}

.guide-desc {
  font-size: 13px;
  opacity: 0.9;
  line-height: 1.6;
  margin-bottom: 16px;
}

.guide-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.step {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  background: rgba(255,255,255,0.15);
  padding: 6px 12px;
  border-radius: 20px;
}

.step-num {
  background: rgba(255,255,255,0.3);
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
}

.step-arrow {
  font-size: 16px;
  opacity: 0.6;
}
</style>
