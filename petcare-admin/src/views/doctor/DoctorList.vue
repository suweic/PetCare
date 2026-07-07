<template>
  <div class="page-container">
    <div class="page-header flex-between">
      <span class="page-title">医生管理</span>
      <span class="page-sub text-muted">共 {{ total }} 名医生</span>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索姓名/手机号" clearable style="width:200px" @keyup.enter="fetchList">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="deptFilter" placeholder="科室" clearable style="width:120px" @change="fetchList">
        <el-option v-for="d in departments" :key="d.id" :value="d.id" :label="d.name" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width:110px" @change="fetchList">
        <el-option :value="0" label="待审核" />
        <el-option :value="1" label="正常" />
        <el-option :value="2" label="禁用" />
        <el-option :value="3" label="已拒绝" />
      </el-select>
      <el-button type="primary" @click="fetchList">查询</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="departmentName" label="科室" width="90" />
        <el-table-column prop="title" label="职称" width="90" />
        <el-table-column prop="hospital" label="医院" min-width="140" show-overflow-tooltip />
        <el-table-column prop="rating" label="评分" width="70" align="center">
          <template #default="{ row }">⭐ {{ row.rating?.toFixed(1) || '-' }}</template>
        </el-table-column>
        <el-table-column prop="consultationCount" label="问诊数" width="75" align="center" />
        <el-table-column label="状态" width="75" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="viewDetail(row)">详情</el-button>
            <template v-if="row.status === 1">
              <el-button size="small" text type="warning" @click="toggleStatus(row, 2)">禁用</el-button>
            </template>
            <template v-if="row.status === 2">
              <el-button size="small" text type="success" @click="toggleStatus(row, 1)">启用</el-button>
            </template>
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
    <el-dialog v-model="detailDialog" title="医生详情" width="560px">
      <template v-if="currentDoctor">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="姓名">{{ currentDoctor.realName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentDoctor.phone }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ currentDoctor.departmentName }}</el-descriptions-item>
          <el-descriptions-item label="职称">{{ currentDoctor.title }}</el-descriptions-item>
          <el-descriptions-item label="医院">{{ currentDoctor.hospital }}</el-descriptions-item>
          <el-descriptions-item label="经验">{{ currentDoctor.experience }}年</el-descriptions-item>
          <el-descriptions-item label="评分">⭐ {{ currentDoctor.rating?.toFixed(1) }}</el-descriptions-item>
          <el-descriptions-item label="问诊数">{{ currentDoctor.consultationCount }}</el-descriptions-item>
          <el-descriptions-item label="专长" :span="2">{{ currentDoctor.specialty || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="statusTag(currentDoctor.status)" size="small">{{ statusText(currentDoctor.status) }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ currentDoctor.createTime }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getDoctorList, toggleDoctorStatus } from '@/api/doctor'
import type { Doctor } from '@/types'

const list = ref<Doctor[]>([])
const loading = ref(false)
const keyword = ref('')
const deptFilter = ref<number | null>(null)
const statusFilter = ref<number | null>(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const detailDialog = ref(false)
const currentDoctor = ref<Doctor | null>(null)

const departments = ref<{ id: number; name: string }[]>([])

onMounted(() => { fetchDepartments(); fetchList() })

async function fetchDepartments() {
  try {
    const { getDepartments } = await import('@/api/department')
    const { data } = await getDepartments()
    if (data.code === 200) {
      departments.value = data.data || []
    }
  } catch {
    console.warn('科室列表加载失败')
  }
}

function onSizeChange(s: number) { size.value = s; fetchList() }

async function fetchList() {
  loading.value = true
  try {
    const { data } = await getDoctorList({
      keyword: keyword.value || undefined,
      deptId: deptFilter.value ?? undefined,
      status: statusFilter.value ?? undefined,
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

function statusTag(s: number) {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[s] || 'info'
}
function statusText(s: number) {
  const map: Record<number, string> = { 0: '待审核', 1: '正常', 2: '禁用', 3: '已拒绝' }
  return map[s] || '未知'
}

function viewDetail(row: Doctor) {
  currentDoctor.value = row
  detailDialog.value = true
}

async function toggleStatus(row: Doctor, status: number) {
  const action = status === 2 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}医生「${row.realName}」？`, '提示', { type: 'warning' })
    await toggleDoctorStatus(row.id, status)
    ElMessage.success(`${action}成功`)
    fetchList()
  } catch { /* cancel */ }
}
</script>

<style scoped>
.page-sub { font-size: 13px; }
.pagination-wrap { display: flex; justify-content: flex-end; padding-top: 16px; }
</style>
