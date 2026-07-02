<template>
  <div class="page-container">
    <div class="page-header flex-between">
      <span class="page-title">
        医生审核
        <el-badge v-if="total" :value="total" class="audit-badge" />
      </span>
      <el-button size="small" @click="fetchList">刷新</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索姓名/手机号" clearable style="width:220px" @keyup.enter="fetchList" />
      <el-select v-model="deptFilter" placeholder="科室筛选" clearable style="width:140px" @change="fetchList">
        <el-option v-for="d in departments" :key="d.id" :value="d.id" :label="d.name" />
      </el-select>
      <el-button type="primary" @click="fetchList">查询</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe highlight-current-row @row-click="viewDetail">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="departmentName" label="科室" width="90" />
        <el-table-column prop="title" label="职称" width="90" />
        <el-table-column prop="specialty" label="专长" min-width="150" show-overflow-tooltip />
        <el-table-column prop="hospital" label="医院" width="140" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click.stop="audit(row, 1)">通过</el-button>
            <el-button size="small" type="danger" @click.stop="audit(row, 2)">拒绝</el-button>
            <el-button size="small" text type="primary" @click.stop="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap" v-if="total > size">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchList"
        />
      </div>
    </div>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditDialog" :title="auditAction === 1 ? '通过审核' : '拒绝申请'" width="480px" :close-on-click-modal="false">
      <div class="audit-info">
        <el-descriptions v-if="currentDoctor" :column="2" border size="small">
          <el-descriptions-item label="医生">{{ currentDoctor.realName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentDoctor.phone }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ currentDoctor.departmentName }}</el-descriptions-item>
          <el-descriptions-item label="职称">{{ currentDoctor.title }}</el-descriptions-item>
          <el-descriptions-item label="专长" :span="2">{{ currentDoctor.specialty }}</el-descriptions-item>
          <el-descriptions-item label="医院">{{ currentDoctor.hospital }}</el-descriptions-item>
          <el-descriptions-item label="经验">{{ currentDoctor.experience }}年</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form label-position="top" class="mt-16">
        <el-form-item :label="auditAction === 1 ? '审核意见（选填）' : '拒绝原因'">
          <el-input
            v-model="auditComment"
            type="textarea"
            :rows="3"
            :placeholder="auditAction === 1 ? '审核通过意见...' : '请填写拒绝原因...'"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialog = false">取消</el-button>
        <el-button
          :type="auditAction === 1 ? 'success' : 'danger'"
          :loading="submitting"
          @click="submitAudit"
        >
          {{ auditAction === 1 ? '确认通过' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialog" title="医生详情" width="560px">
      <template v-if="currentDoctor">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ currentDoctor.realName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentDoctor.phone }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ currentDoctor.departmentName }}</el-descriptions-item>
          <el-descriptions-item label="职称">{{ currentDoctor.title }}</el-descriptions-item>
          <el-descriptions-item label="医院">{{ currentDoctor.hospital }}</el-descriptions-item>
          <el-descriptions-item label="学历">{{ currentDoctor.education || '-' }}</el-descriptions-item>
          <el-descriptions-item label="经验">{{ currentDoctor.experience }}年</el-descriptions-item>
          <el-descriptions-item label="评分">{{ currentDoctor.rating?.toFixed(1) }}</el-descriptions-item>
          <el-descriptions-item label="专长" :span="2">{{ currentDoctor.specialty }}</el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">{{ currentDoctor.createTime }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingDoctors, auditDoctor, getDoctorList } from '@/api/doctor'
import type { Doctor } from '@/types'

const list = ref<Doctor[]>([])
const loading = ref(false)
const keyword = ref('')
const deptFilter = ref<number | null>(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const auditDialog = ref(false)
const detailDialog = ref(false)
const auditAction = ref(1)
const auditComment = ref('')
const submitting = ref(false)
const currentDoctor = ref<Doctor | null>(null)

const departments = [
  { id: 1, name: '内科' }, { id: 2, name: '外科' }, { id: 3, name: '皮肤科' },
  { id: 4, name: '眼科' }, { id: 5, name: '口腔科' }, { id: 6, name: '营养科' },
]

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const { data } = await getPendingDoctors({ page: page.value, size: size.value })
    if (data.code === 200) {
      list.value = data.data.records
      total.value = data.data.total
    }
  } finally {
    loading.value = false
  }
}

function viewDetail(row: Doctor) {
  currentDoctor.value = row
  detailDialog.value = true
}

function audit(row: Doctor, status: number) {
  currentDoctor.value = row
  auditAction.value = status
  auditComment.value = ''
  auditDialog.value = true
}

async function submitAudit() {
  if (!currentDoctor.value) return
  submitting.value = true
  try {
    await auditDoctor(currentDoctor.value.id, {
      auditStatus: auditAction.value,
      auditComment: auditComment.value || undefined,
    })
    ElMessage.success(auditAction.value === 1 ? '审核通过' : '已拒绝')
    auditDialog.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.audit-badge { margin-left: 8px; }
.mt-16 { margin-top: 8px; }
.pagination-wrap { display: flex; justify-content: flex-end; padding-top: 16px; }
.audit-info { max-height: 200px; overflow-y: auto; }
</style>
