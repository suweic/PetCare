<template>
  <MainLayout>
    <div class="doctor-list-page">
      <NavBar title="找医生" />
      <!-- AI预问诊入口 -->
      <div class="ai-banner" @click="$router.push('/pre-consultation')">
        <div class="ai-banner-icon">🤖</div>
        <div class="ai-banner-text">
          <span class="ai-banner-title">不确定看哪个科室？</span>
          <span class="ai-banner-desc">AI帮您分析症状，精准匹配医生</span>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <!-- 搜索 -->
      <div class="search-area">
        <el-input v-model="keyword" placeholder="搜索医生姓名、科室" clearable @clear="fetchList" @keyup.enter="fetchList">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
      <!-- 科室筛选 -->
      <div class="dept-filter">
        <span
          v-for="d in departments"
          :key="d.id"
          class="filter-tag"
          :class="{ active: deptId === d.id }"
          @click="selectDept(d.id)"
        >{{ d.name }}</span>
      </div>
      <!-- 医生列表 -->
      <div class="doc-list" v-loading="loading">
        <div
          v-for="doc in list"
          :key="doc.id"
          class="doc-card"
          @click="$router.push(`/consult/doctor/${doc.id}`)"
        >
          <el-avatar :size="52" :src="doc.avatar" />
          <div class="doc-body">
            <div class="doc-name">
              {{ doc.realName }}
              <el-tag size="small" type="success">{{ doc.title }}</el-tag>
            </div>
            <div class="doc-dept">{{ doc.departmentName }} · {{ doc.hospital }}</div>
            <div class="doc-specialty">{{ doc.specialty }}</div>
            <div class="doc-meta">
              <span>⭐ {{ doc.rating?.toFixed(1) }}</span>
              <span>{{ doc.consultationCount }} 次问诊</span>
              <span class="doc-fee">¥{{ doc.consultationFee }}/次</span>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading && list.length === 0" description="暂无医生" />
      </div>
      <!-- 分页 -->
      <div class="pager" v-if="total > size">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          small
          @current-change="fetchList"
        />
      </div>
    </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Search, ArrowRight } from '@element-plus/icons-vue'
import MainLayout from '@/layouts/MainLayout.vue'
import NavBar from '@/layouts/NavBar.vue'
import { getDoctorList } from '@/api/doctor'
import type { DoctorListItem } from '@/types'

const route = useRoute()

const keyword = ref('')
const deptId = ref<number | null>(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const list = ref<DoctorListItem[]>([])
const loading = ref(false)

const departments = [
  { id: 1, name: '内科' }, { id: 2, name: '外科' }, { id: 3, name: '皮肤科' },
  { id: 4, name: '眼科' }, { id: 5, name: '口腔科' }, { id: 6, name: '营养科' },
  { id: 7, name: '行为学' }, { id: 8, name: '急诊科' },
]

onMounted(() => {
  if (route.query.deptId) deptId.value = Number(route.query.deptId)
  fetchList()
})

function selectDept(id: number) {
  deptId.value = deptId.value === id ? null : id
  page.value = 1
  fetchList()
}

async function fetchList() {
  loading.value = true
  try {
    const { data } = await getDoctorList({
      deptId: deptId.value ?? undefined,
      keyword: keyword.value || undefined,
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
</script>

<style scoped>
.doctor-list-page { min-height: 100vh; }

/* AI预问诊横幅 */
.ai-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 10px 16px;
  padding: 12px 14px;
  background: linear-gradient(135deg, #f0f0ff, #faf0ff);
  border: 1px solid #e0d6f5;
  border-radius: 10px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.ai-banner:hover { box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15); }
.ai-banner-icon { font-size: 28px; }
.ai-banner-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.ai-banner-title { font-size: 14px; font-weight: 600; color: #333; }
.ai-banner-desc { font-size: 12px; color: #888; }
.ai-banner .el-icon { color: #999; }

.search-area { padding: 10px 16px; background: #fff; }
.dept-filter {
  display: flex; flex-wrap: wrap; gap: 8px;
  padding: 10px 16px; background: #fff; border-top: 1px solid var(--border);
  overflow-x: auto;
}
.filter-tag {
  padding: 4px 14px; border-radius: 16px; font-size: 12px;
  background: var(--bg); color: var(--text-secondary); cursor: pointer; white-space: nowrap;
}
.filter-tag.active { background: var(--primary); color: #fff; }
.doc-list { padding: 10px 0 70px; }
.doc-card {
  display: flex; gap: 12px; padding: 14px 16px; background: #fff;
  margin-bottom: 1px; cursor: pointer;
}
.doc-body { flex: 1; min-width: 0; }
.doc-name { font-size: 15px; font-weight: 500; display: flex; align-items: center; gap: 6px; }
.doc-dept { font-size: 12px; color: var(--text-secondary); margin: 3px 0; }
.doc-specialty { font-size: 12px; color: var(--text-muted); margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.doc-meta { font-size: 11px; color: var(--text-secondary); display: flex; gap: 10px; }
.doc-fee { color: var(--primary); font-weight: 500; }
.pager { display: flex; justify-content: center; padding: 16px; }
</style>
