<template>
  <div class="dashboard-page page-container">
    <div class="page-header flex-between">
      <span class="page-title">📊 数据看板</span>
      <el-button :icon="Refresh" size="small" @click="refreshAll">刷新数据</el-button>
    </div>

    <!-- 4 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6">
        <div class="stat-card card-blue">
          <div class="stat-left">
            <div class="stat-value">
              <span>{{ displayUsers }}</span>
            </div>
            <div class="stat-label">总用户数</div>
            <div class="stat-sub">今日新增 +{{ stats.todayNewUsers ?? 0 }}</div>
          </div>
          <div class="stat-icon-box icon-blue">
            <el-icon :size="32"><User /></el-icon>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card card-green">
          <div class="stat-left">
            <div class="stat-value">
              <span>{{ displayDoctors }}</span>
            </div>
            <div class="stat-label">注册医生</div>
            <div class="stat-sub">待审核 {{ stats.pendingDoctors ?? 0 }} 人</div>
          </div>
          <div class="stat-icon-box icon-green">
            <el-icon :size="32"><UserFilled /></el-icon>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card card-orange">
          <div class="stat-left">
            <div class="stat-value">
              <span>{{ displayConsultations }}</span>
            </div>
            <div class="stat-label">总问诊数</div>
            <div class="stat-sub">今日 {{ stats.todayConsultations ?? 0 }} 次</div>
          </div>
          <div class="stat-icon-box icon-orange">
            <el-icon :size="32"><ChatDotRound /></el-icon>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card card-red">
          <div class="stat-left">
            <div class="stat-value">
              <span>{{ displayRevenue }}</span>
            </div>
            <div class="stat-label">总营收 (元)</div>
            <div class="stat-sub">本月 ¥{{ formatMoney(stats.revenue ?? 0) }}</div>
          </div>
          <div class="stat-icon-box icon-red">
            <el-icon :size="32"><Money /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="16">
        <div class="chart-card">
          <div class="chart-header flex-between">
            <span class="chart-title">📈 问诊趋势（近30天）</span>
          </div>
          <div ref="trendChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">🍩 问诊类型占比</span>
          </div>
          <div ref="typeChartRef" class="chart-box chart-box-sm"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">📊 科室问诊分布</span>
          </div>
          <div ref="deptChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-header">
            <span class="chart-title">🕐 最近操作日志</span>
          </div>
          <div class="log-list">
            <div v-for="(log, i) in recentLogs" :key="i" class="log-item">
              <el-tag :type="log.type || 'info'" size="small">{{ log.tag }}</el-tag>
              <span class="log-msg">{{ log.msg }}</span>
              <span class="log-time">{{ log.time }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Refresh, User, UserFilled, ChatDotRound, Money } from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/dashboard'
import type { DashboardStats } from '@/types'

// ----- 统计数据 -----
const stats = reactive<Partial<DashboardStats>>({
  totalUsers: 0, totalDoctors: 0, totalConsultations: 0,
  todayConsultations: 0, pendingDoctors: 0, revenue: 0, todayNewUsers: 0,
})

const displayUsers = ref(0)
const displayDoctors = ref(0)
const displayConsultations = ref(0)
const displayRevenue = ref(0)

// 追踪活跃的动画计时器，防止快速刷新时重叠
const activeTimers = new Map<string, ReturnType<typeof setInterval>>()

function animateValue(key: 'users' | 'doctors' | 'consultations' | 'revenue', target: number) {
  // 取消该 key 已有动画
  const existing = activeTimers.get(key)
  if (existing) clearInterval(existing)

  const map = {
    users: { ref: displayUsers, get: () => displayUsers.value, set: (v: number) => { displayUsers.value = v } },
    doctors: { ref: displayDoctors, get: () => displayDoctors.value, set: (v: number) => { displayDoctors.value = v } },
    consultations: { ref: displayConsultations, get: () => displayConsultations.value, set: (v: number) => { displayConsultations.value = v } },
    revenue: { ref: displayRevenue, get: () => displayRevenue.value, set: (v: number) => { displayRevenue.value = v } },
  }
  const entry = map[key]
  const start = entry.get()
  const diff = target - start
  if (diff === 0) return
  const duration = 800
  const steps = 30
  const increment = diff / steps
  let step = 0
  const timer = setInterval(() => {
    step++
    entry.set(Math.round(start + increment * step))
    if (step >= steps) {
      entry.set(target)
      clearInterval(timer)
      activeTimers.delete(key)
    }
  }, duration / steps)
  activeTimers.set(key, timer)
}

// ----- 图表 -----
const trendChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()
const deptChartRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let typeChart: echarts.ECharts | null = null
let deptChart: echarts.ECharts | null = null

// 模拟日志
const recentLogs = [
  { type: 'success', tag: '审核', msg: '管理员审核通过了医生「张伟」的申请', time: '3分钟前' },
  { type: 'warning', tag: '问诊', msg: '问诊 #1288 已超时自动关闭', time: '15分钟前' },
  { type: '', tag: '注册', msg: '新用户 138****6789 完成注册', time: '28分钟前' },
  { type: 'danger', tag: '禁用', msg: '管理员禁用了用户「测试账号」', time: '1小时前' },
  { type: 'success', tag: '支付', msg: '问诊 #1285 支付成功 ¥68.00', time: '2小时前' },
  { type: '', tag: '系统', msg: '系统配置更新：问诊超时时间调整为 30 分钟', time: '3小时前' },
]

onMounted(async () => {
  await loadStats()
  await nextTick()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  trendChart?.dispose()
  typeChart?.dispose()
  deptChart?.dispose()
  window.removeEventListener('resize', handleResize)
})

function handleResize() {
  trendChart?.resize()
  typeChart?.resize()
  deptChart?.resize()
}

async function loadStats() {
  try {
    const { data } = await getDashboardStats()
    if (data.code === 200 && data.data) {
      Object.assign(stats, data.data)
      animateValue('users', stats.totalUsers ?? 0)
      animateValue('doctors', stats.totalDoctors ?? 0)
      animateValue('consultations', stats.totalConsultations ?? 0)
      animateValue('revenue', stats.revenue ?? 0)
      refreshCharts()
    } else {
      ElMessage.warning('获取统计数据失败')
    }
  } catch {
    ElMessage.error('获取统计数据失败，请检查网络连接')
  }
}

async function refreshAll() {
  await loadStats()
}

// ----- ECharts -----
function initCharts() {
  if (trendChartRef.value) trendChart = echarts.init(trendChartRef.value)
  if (typeChartRef.value) typeChart = echarts.init(typeChartRef.value)
  if (deptChartRef.value) deptChart = echarts.init(deptChartRef.value)
  refreshCharts()
}

function refreshCharts() {
  trendChart?.setOption(buildTrendOption(), true)
  typeChart?.setOption(buildTypeOption(), true)
  deptChart?.setOption(buildDeptOption(), true)
}

function buildTrendOption() {
  const days = Array.from({ length: 30 }, (_, i) => `${i + 1}`)
  const mockData = Array.from({ length: 30 }, () => Math.floor(Math.random() * 80 + 30))

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e4e7ed',
      textStyle: { color: '#303133', fontSize: 12 },
      boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    },
    grid: { left: '2%', right: '3%', top: '8%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: days,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#e4e7ed' } },
      axisLabel: { color: '#909399', fontSize: 10, interval: 4 },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
      axisLabel: { color: '#909399', fontSize: 10 },
    },
    series: [{
      data: stats.trendData?.map((p) => p.count) || mockData,
      type: 'line',
      smooth: true,
      symbol: 'none',
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.3)' },
          { offset: 1, color: 'rgba(64,158,255,0.02)' },
        ]),
      },
      lineStyle: { color: '#409eff', width: 2 },
      itemStyle: { color: '#409eff' },
    }],
  }
}

function buildTypeOption() {
  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#e4e7ed',
      textStyle: { color: '#303133' },
      formatter: '{b}: {c} 次 ({d}%)',
    },
    legend: { bottom: 0, textStyle: { fontSize: 11 } },
    series: [{
      type: 'pie',
      radius: ['55%', '78%'],
      center: ['50%', '43%'],
      avoidLabelOverlap: false,
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
      },
      data: [
        { value: 2200, name: '图文问诊', itemStyle: { color: '#409eff' } },
        { value: 800, name: '视频问诊', itemStyle: { color: '#67c23a' } },
        { value: 560, name: '语音问诊', itemStyle: { color: '#e6a23c' } },
      ],
    }],
  }
}

function buildDeptOption() {
  const rawData = stats.deptDistribution || [
    { name: '内科', value: 820 },
    { name: '外科', value: 680 },
    { name: '皮肤科', value: 540 },
    { name: '眼科', value: 320 },
    { name: '口腔科', value: 280 },
    { name: '营养科', value: 350 },
    { name: '行为学', value: 180 },
    { name: '急诊科', value: 390 },
  ]

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: '#fff',
      borderColor: '#e4e7ed',
      textStyle: { color: '#303133' },
    },
    grid: { left: '2%', right: '4%', top: '5%', bottom: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: rawData.map((d) => d.name),
      axisLabel: { rotate: 35, fontSize: 10, color: '#909399' },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
      axisLabel: { color: '#909399' },
    },
    series: [{
      data: rawData.map((d) => d.value),
      type: 'bar',
      barWidth: '55%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#67c23a' },
          { offset: 1, color: '#b3e19d' },
        ]),
      },
      emphasis: {
        itemStyle: { color: '#529b2e' },
      },
    }],
  }
}

function formatMoney(v: number) { return v.toLocaleString() }
</script>

<style scoped>
.dashboard-page { min-height: 100%; }

/* 卡片 */
.stat-cards { margin-bottom: 16px; }
.stat-card {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; padding: 22px 20px; border-radius: 8px;
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer; overflow: hidden; position: relative;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.08); }
.stat-card::after {
  content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 3px;
}
.card-blue::after { background: #409eff; }
.card-green::after { background: #67c23a; }
.card-orange::after { background: #e6a23c; }
.card-red::after { background: #f56c6c; }

.stat-value { font-size: 30px; font-weight: 700; color: var(--text-primary); line-height: 1.1; }
.stat-label { font-size: 13px; color: var(--text-secondary); margin-top: 4px; }
.stat-sub { font-size: 11px; color: var(--text-muted); margin-top: 2px; }
.stat-icon-box {
  width: 60px; height: 60px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
}
.icon-blue { background: #ecf5ff; color: #409eff; }
.icon-green { background: #f0f9eb; color: #67c23a; }
.icon-orange { background: #fdf6ec; color: #e6a23c; }
.icon-red { background: #fef0f0; color: #f56c6c; }

/* 图表 */
.chart-row { margin-bottom: 16px; }
.chart-card { background: #fff; padding: 16px 20px; border-radius: 8px; }
.chart-header { margin-bottom: 8px; }
.chart-title { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.chart-box { width: 100%; height: 300px; }
.chart-box-sm { height: 280px; }

/* 日志 */
.log-list { max-height: 280px; overflow-y: auto; }
.log-item {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 0; border-bottom: 1px solid #f5f5f5; font-size: 12px;
}
.log-item:last-child { border-bottom: none; }
.log-msg { flex: 1; color: var(--text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.log-time { color: var(--text-muted); font-size: 11px; flex-shrink: 0; }
</style>
