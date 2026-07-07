<template>
  <div class="page-container">
    <div class="page-header flex-between">
      <span class="page-title">问诊管理</span>
      <span class="page-sub text-muted">共 {{ total }} 条记录</span>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索用户/医生/宠物名" clearable style="width:200px" @keyup.enter="fetchList">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width:120px" @change="fetchList">
        <el-option :value="0" label="待接单" />
        <el-option :value="1" label="进行中" />
        <el-option :value="2" label="已完成" />
        <el-option :value="3" label="已取消" />
        <el-option :value="4" label="已拒绝" />
        <el-option :value="5" label="超时" />
      </el-select>
      <el-select v-model="typeFilter" placeholder="类型" clearable style="width:120px" @change="fetchList">
        <el-option :value="1" label="图文问诊" />
        <el-option :value="2" label="视频问诊" />
        <el-option :value="3" label="语音问诊" />
      </el-select>
      <el-button type="primary" @click="fetchList">查询</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="userName" label="用户" width="100" />
        <el-table-column label="医生" width="100">
          <template #default="{ row }">{{ row.doctorName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="petName" label="宠物" width="80" />
        <el-table-column prop="departmentName" label="科室" width="80" />
        <el-table-column label="类型" width="85" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chiefComplaint" label="主诉" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap" v-if="total > size">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          @size-change="onSizeChange"
          @current-change="fetchList"
        />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialog" title="问诊详情" width="600px">
      <template v-if="currentRow">
        <!-- 基本信息 -->
        <div class="detail-section">
          <div class="detail-section-title">基本信息</div>
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="问诊ID">{{ currentRow.id }}</el-descriptions-item>
            <el-descriptions-item label="用户">{{ currentRow.userName }}</el-descriptions-item>
            <el-descriptions-item label="医生">{{ currentRow.doctorName || '未分配' }}</el-descriptions-item>
            <el-descriptions-item label="宠物">{{ currentRow.petName }}</el-descriptions-item>
            <el-descriptions-item label="科室">{{ currentRow.departmentName }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ typeText(currentRow.type) }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusTag(currentRow.status)" size="small">{{ statusText(currentRow.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">{{ currentRow.createTime }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 问诊内容 -->
        <div class="detail-section" v-if="currentRow.chiefComplaint || currentRow.symptoms">
          <div class="detail-section-title">问诊内容</div>
          <div class="detail-field" v-if="currentRow.chiefComplaint">
            <span class="field-label">主诉</span>
            <div class="field-value">{{ currentRow.chiefComplaint }}</div>
          </div>
          <div class="detail-field" v-if="currentRow.symptoms">
            <span class="field-label">症状描述</span>
            <div class="field-value">{{ currentRow.symptoms }}</div>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getConsultationList, getConsultationDetail } from '@/api/consultation'
import type { Consultation } from '@/types'

const list = ref<Consultation[]>([])
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref<number | null>(null)
const typeFilter = ref<number | null>(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const detailDialog = ref(false)
const currentRow = ref<Consultation | null>(null)

onMounted(() => fetchList())

function onSizeChange(s: number) { size.value = s; fetchList() }

async function fetchList() {
  loading.value = true
  try {
    const { data } = await getConsultationList({
      keyword: keyword.value || undefined,
      status: statusFilter.value ?? undefined,
      type: typeFilter.value ?? undefined,
      page: page.value,
      size: size.value,
    })
    if (data.code === 200) {
      list.value = data.data.records
      total.value = data.data.total
    }
  } catch {
    // 错误已在 request 拦截器中通过 ElMessage 提示
  } finally {
    loading.value = false
  }
}

async function viewDetail(row: Consultation) {
  try {
    const { data } = await getConsultationDetail(row.id)
    if (data.code === 200) currentRow.value = data.data
  } catch {
    currentRow.value = row
  }
  detailDialog.value = true
}

function typeText(t: number) {
  return { 1: '图文', 2: '视频', 3: '语音' }[t] || '未知'
}
function statusText(s: number) {
  return { 0: '待接单', 1: '进行中', 2: '已完成', 3: '已取消', 4: '已拒绝', 5: '超时' }[s] || '未知'
}
function statusTag(s: number): '' | 'success' | 'warning' | 'info' | 'danger' {
  if (s === 1) return 'warning'
  if (s === 2) return 'success'
  if (s === 3 || s === 4 || s === 5) return 'danger'
  return 'info'
}
</script>

<style scoped>
.page-sub { font-size: 13px; }
.pagination-wrap { display: flex; justify-content: flex-end; padding-top: 16px; }

.detail-section { margin-bottom: 16px; }
.detail-section-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; color: var(--text-primary); }
.detail-field {
  background: var(--bg); padding: 10px 12px; border-radius: 6px; margin-bottom: 8px;
}
.field-label { font-size: 12px; color: var(--text-muted); margin-bottom: 4px; display: block; }
.field-value { font-size: 13px; color: var(--text-regular); line-height: 1.6; }
</style>
