<template>
  <div class="pet-list-page">
    <!-- 顶部操作栏 -->
    <div class="pet-header">
      <div class="header-left" @click="$router.back()">
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </div>
      <span class="header-title">宠物档案</span>
      <div class="header-right" @click="$router.push('/pet/add')">
        <el-icon :size="22"><Plus /></el-icon>
      </div>
    </div>

    <!-- 宠物卡片网格 -->
    <div class="pet-grid" v-loading="store.loading">
      <div
        v-for="pet in store.pets"
        :key="pet.id"
        class="pet-card"
        @click="$router.push(`/pet/edit/${pet.id}`)"
      >
        <!-- 操作菜单 -->
        <div class="card-actions" @click.stop>
          <el-dropdown trigger="click">
            <el-icon :size="18" class="more-icon"><MoreFilled /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="editPet(pet)">
                  <el-icon><Edit /></el-icon> 编辑
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleDelete(pet)">
                  <el-icon><Delete /></el-icon> 删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 头像 -->
        <div class="pet-avatar">
          <img
            :src="pet.avatar || defaultAvatar"
            :alt="pet.name"
            @error="onImgError"
          />
        </div>

        <!-- 信息 -->
        <div class="pet-info">
          <div class="pet-name">{{ pet.name }}</div>
          <div class="pet-tags">
            <span class="pet-tag species-tag">{{ pet.speciesName }}</span>
            <span v-if="pet.breed" class="pet-tag breed-tag">{{ pet.breed }}</span>
          </div>
          <div class="pet-detail" v-if="pet.genderName || pet.birthDate">
            <span v-if="pet.genderName">{{ pet.genderName }}</span>
            <span v-if="pet.genderName && pet.birthDate"> · </span>
            <span v-if="pet.birthDate">{{ formatAge(pet.birthDate) }}</span>
          </div>
        </div>

        <!-- 底部状态 -->
        <div class="pet-footer">
          <span v-if="pet.weight" class="pet-weight">{{ pet.weight }}kg</span>
          <span v-if="pet.sterilized" class="pet-sterilized">已绝育</span>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!store.loading && store.pets.length === 0" class="empty-state">
      <span class="empty-icon">🐕</span>
      <p>还没有宠物档案</p>
      <p class="text-muted">添加你的毛孩子，开始健康管理</p>
      <el-button type="primary" round @click="$router.push('/pet/add')">
        <el-icon><Plus /></el-icon> 添加宠物
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Plus, MoreFilled, Edit, Delete,
} from '@element-plus/icons-vue'
import { usePetStore } from '@/stores/pet'
import type { Pet } from '@/types'

const router = useRouter()
const store = usePetStore()

const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iODAiIHZpZXdCb3g9IjAgMCA4MCA4MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iODAiIGhlaWdodD0iODAiIHJ4PSIxMiIgZmlsbD0iI2U4ZjVlOSIvPjx0ZXh0IHg9IjQwIiB5PSI1MiIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZm9udC1zaXplPSIzNiI+8J+QvjwvdGV4dD48L3N2Zz4='

onMounted(() => store.fetchPets())

function editPet(pet: Pet) {
  router.push(`/pet/edit/${pet.id}`)
}

async function handleDelete(pet: Pet) {
  try {
    await ElMessageBox.confirm(
      `确定删除「${pet.name}」的档案吗？删除后不可恢复。`,
      '确认删除',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
    await store.removePet(pet.id)
    ElMessage.success('已删除')
  } catch { /* cancel */ }
}

function formatAge(dateStr: string): string {
  if (!dateStr) return ''
  const birth = new Date(dateStr)
  const now = new Date()
  const months = (now.getFullYear() - birth.getFullYear()) * 12 + now.getMonth() - birth.getMonth()
  if (months < 12) return `${Math.max(1, months)}个月`
  const years = Math.floor(months / 12)
  const m = months % 12
  return m > 0 ? `${years}岁${m}个月` : `${years}岁`
}

function onImgError(e: Event) {
  const img = e.target as HTMLImageElement
  img.src = defaultAvatar
}
</script>

<style scoped>
.pet-list-page {
  min-height: 100vh;
  background: var(--bg);
  padding-bottom: 20px;
}

/* 顶部栏 */
.pet-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid var(--border);
}
.header-left, .header-right {
  width: 36px;
  display: flex;
  align-items: center;
  cursor: pointer;
}
.header-right { justify-content: flex-end; }
.header-title {
  font-size: 16px;
  font-weight: 600;
}

/* 卡片网格 */
.pet-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 14px 12px;
}
.pet-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px 12px 12px;
  position: relative;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: transform 0.15s;
}
.pet-card:active { transform: scale(0.97); }

/* 右上角操作 */
.card-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 2;
}
.more-icon {
  color: var(--text-muted);
  padding: 4px;
}

/* 头像 */
.pet-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 8px;
  background: #f5f5f5;
}
.pet-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 信息 */
.pet-info {
  text-align: center;
}
.pet-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.pet-tags {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-bottom: 2px;
}
.pet-tag {
  font-size: 10px;
  padding: 1px 8px;
  border-radius: 8px;
}
.species-tag {
  background: #e8f5e9;
  color: #388e3c;
}
.breed-tag {
  background: #f5f5f5;
  color: var(--text-secondary);
}
.pet-detail {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
}

/* 底部 */
.pet-footer {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
  font-size: 10px;
  color: var(--text-secondary);
}
.pet-sterilized {
  background: #e3f2fd;
  color: #1565c0;
  padding: 1px 6px;
  border-radius: 6px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}
.empty-icon {
  font-size: 56px;
  display: block;
  margin-bottom: 12px;
}
.empty-state p {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}
.empty-state .text-muted {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 20px;
}
</style>
