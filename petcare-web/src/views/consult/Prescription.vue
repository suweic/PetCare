<template>
  <div class="prescription-page">
    <NavBar title="电子处方" show-back @back="$router.back()" />

    <div v-if="rx" class="rx-content">
      <!-- 处方头部 -->
      <div class="rx-card">
        <div class="rx-card-header">
          <span class="rx-icon">📋</span>
          <div>
            <div class="rx-card-title">电子处方笺</div>
            <div class="rx-card-sub">处方编号 #{{ rx.id }}</div>
          </div>
        </div>

        <!-- 诊断结果 -->
        <div class="rx-section" v-if="rx.diagnosis">
          <div class="rx-section-title">🔍 诊断结果</div>
          <div class="rx-section-body">{{ rx.diagnosis }}</div>
        </div>

        <!-- 药品清单 -->
        <div class="rx-section" v-if="rx.items?.length">
          <div class="rx-section-title">💊 药品清单</div>
          <div class="rx-med-list">
            <div class="rx-med-card" v-for="(item, idx) in rx.items" :key="idx">
              <div class="med-header flex-between">
                <span class="med-index">{{ idx + 1 }}</span>
                <span class="med-name">{{ item.medicineName }}</span>
                <el-tag v-if="item.quantity" size="small" type="warning">×{{ item.quantity }}</el-tag>
              </div>
              <div class="med-spec" v-if="item.specification">规格：{{ item.specification }}</div>
              <div class="med-usage">
                <div class="usage-item">
                  <span class="usage-label">用量</span>
                  <span>{{ item.dosage }}</span>
                </div>
                <div class="usage-item">
                  <span class="usage-label">频次</span>
                  <span>{{ item.frequency }}</span>
                </div>
                <div class="usage-item">
                  <span class="usage-label">疗程</span>
                  <span>{{ item.duration }}</span>
                </div>
              </div>
              <div class="med-remarks" v-if="item.remarks">
                <span class="usage-label">备注</span>
                <span>{{ item.remarks }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 医嘱建议 -->
        <div class="rx-section" v-if="rx.advice">
          <div class="rx-section-title">📝 医嘱建议</div>
          <div class="rx-section-body advice-text">{{ rx.advice }}</div>
        </div>

        <!-- 处方信息 -->
        <div class="rx-meta">
          <div class="meta-item">
            <span class="meta-label">开具时间</span>
            <span>{{ formatDate(rx.createTime) }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">处方状态</span>
            <el-tag :type="rx.status === 1 ? 'success' : 'info'" size="small">
              {{ rx.status === 1 ? '已开具' : rx.status === 2 ? '已审核' : '已拒绝' }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 底部提示 -->
      <div class="rx-disclaimer">
        <p>⚠️ 本处方仅限宠物使用，请遵医嘱用药</p>
        <p>如有不适请及时联系医生或到院就诊</p>
      </div>
    </div>

    <div v-else class="rx-loading" v-loading="true">
      <el-skeleton :rows="6" animated />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '@/layouts/NavBar.vue'
import { getPrescription } from '@/api/consultation'
import type { PrescriptionDTO } from '@/types'

const route = useRoute()
const rx = ref<PrescriptionDTO | null>(null)

onMounted(async () => {
  const consultationId = Number(route.params.consultationId)
  try {
    const { data } = await getPrescription(consultationId)
    if (data.code === 200) rx.value = data.data
  } catch { /* ignore */ }
})

function formatDate(d?: string) { return d ? d.slice(0, 16).replace('T', ' ') : '' }
</script>

<style scoped>
.prescription-page {
  min-height: 100vh;
  background: var(--bg);
  padding-bottom: 30px;
}

.rx-content { padding: 14px; }

/* 处方卡片 */
.rx-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 18px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.rx-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 2px dashed var(--border);
  margin-bottom: 16px;
}
.rx-icon { font-size: 36px; }
.rx-card-title { font-size: 18px; font-weight: 700; color: var(--text-primary); }
.rx-card-sub { font-size: 12px; color: var(--text-muted); margin-top: 2px; }

/* 每个分区 */
.rx-section {
  margin-bottom: 18px;
}
.rx-section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}
.rx-section-body {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  background: var(--bg);
  padding: 12px;
  border-radius: 8px;
}
.advice-text {
  background: #f1f8e9;
  color: #388e3c;
}

/* 药品卡片 */
.rx-med-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.rx-med-card {
  background: var(--bg);
  border-radius: 10px;
  padding: 12px 14px;
}
.med-header {
  margin-bottom: 6px;
}
.med-index {
  width: 22px; height: 22px; border-radius: 50%;
  background: var(--primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 600;
}
.med-name { font-size: 14px; font-weight: 500; flex: 1; margin-left: 8px; }
.med-spec { font-size: 12px; color: var(--text-muted); margin-bottom: 8px; }
.med-usage {
  display: flex; gap: 16px;
}
.usage-item { display: flex; gap: 4px; font-size: 13px; color: var(--text-regular); }
.usage-label { font-size: 11px; color: var(--text-muted); }
.med-remarks {
  display: flex; gap: 4px; font-size: 12px; color: var(--text-secondary); margin-top: 6px;
}

/* 处方信息 */
.rx-meta {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--border);
  display: flex; justify-content: space-between;
}
.meta-item { display: flex; gap: 6px; font-size: 12px; color: var(--text-muted); }
.meta-label { color: var(--text-regular); }

/* 免责 */
.rx-disclaimer {
  text-align: center; padding: 20px 16px 0;
  font-size: 11px; color: var(--text-muted); line-height: 1.8;
}

.rx-loading { padding: 20px; }
</style>
