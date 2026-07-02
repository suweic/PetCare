<template>
  <div class="page-container">
    <div class="page-header flex-between">
      <span class="page-title">用户管理</span>
      <span class="page-sub text-muted">共 {{ total }} 名用户</span>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索手机号/昵称/姓名" clearable style="width:220px" @keyup.enter="fetchList">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="typeFilter" placeholder="用户类型" clearable style="width:120px" @change="fetchList">
        <el-option :value="1" label="普通用户" />
        <el-option :value="2" label="医生" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width:100px" @change="fetchList">
        <el-option :value="0" label="禁用" />
        <el-option :value="1" label="正常" />
      </el-select>
      <el-button type="primary" @click="fetchList">查询</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.userType === 2 ? 'warning' : ''" size="small" effect="plain">
              {{ row.userType === 2 ? '医生' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="75" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="viewDetail(row)">详情</el-button>
            <template v-if="row.status === 1">
              <el-button size="small" text type="warning" @click="toggle(row, 0)">禁用</el-button>
            </template>
            <template v-if="row.status === 0">
              <el-button size="small" text type="success" @click="toggle(row, 1)">启用</el-button>
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
    <el-dialog v-model="detailDialog" title="用户详情" width="480px">
      <template v-if="currentUser">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentUser.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ currentUser.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="currentUser.userType === 2 ? 'warning' : ''" size="small" effect="plain">
              {{ currentUser.userType === 2 ? '医生' : '普通用户' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentUser.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间" :span="2">{{ currentUser.createTime }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getUserList, toggleUserStatus } from '@/api/user'
import type { UserInfo } from '@/types'

const list = ref<UserInfo[]>([])
const loading = ref(false)
const keyword = ref('')
const typeFilter = ref<number | null>(null)
const statusFilter = ref<number | null>(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const detailDialog = ref(false)
const currentUser = ref<UserInfo | null>(null)

onMounted(() => fetchList())

function onSizeChange(s: number) { size.value = s; fetchList() }

async function fetchList() {
  loading.value = true
  try {
    const { data } = await getUserList({
      keyword: keyword.value || undefined,
      status: statusFilter.value ?? undefined,
      page: page.value,
      size: size.value,
    })
    if (data.code === 200) {
      list.value = data.data.records
      total.value = data.data.total
    }
  } finally {
    loading.value = false
  }
}

function viewDetail(row: UserInfo) {
  currentUser.value = row
  detailDialog.value = true
}

async function toggle(row: UserInfo, status: number) {
  const action = status === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}用户「${row.nickname || row.phone}」？`, '提示', { type: 'warning' })
    await toggleUserStatus(row.id, status)
    ElMessage.success(`${action}成功`)
    fetchList()
  } catch { /* cancel */ }
}
</script>

<style scoped>
.page-sub { font-size: 13px; }
.pagination-wrap { display: flex; justify-content: flex-end; padding-top: 16px; }
</style>
