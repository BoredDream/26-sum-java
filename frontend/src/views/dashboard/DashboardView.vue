<template>
  <div class="dashboard-page">
    <page-header title="仪表盘" />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-empty v-if="!loading && !error && !hasData" description="暂无统计数据" />

    <el-row v-loading="loading" :gutter="16">
      <el-col
        v-for="item in statItems"
        :key="item.key"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="8"
        class="mb-4"
      >
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <div
              class="stat-icon"
              :style="{ backgroundColor: item.color + '1a', color: item.color }"
            >
              <el-icon :size="28">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics[item.key as keyof DashboardStatisticsVO] ?? 0 }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { User, UserFilled, Document, CopyDocument, Tickets, Bell } from '@element-plus/icons-vue'
import * as infoApi from '@/api/info'
import type { DashboardStatisticsVO } from '@/types/info'

const loading = ref(false)
const error = ref('')
const statistics = ref<DashboardStatisticsVO>({})

const statItems = [
  { key: 'studentCount', label: '学生人数', icon: User, color: '#409eff' },
  { key: 'teacherCount', label: '教师人数', icon: UserFilled, color: '#67c23a' },
  { key: 'noticeCount', label: '公告数量', icon: Bell, color: '#e6a23c' },
  { key: 'backupCount', label: '备份数量', icon: CopyDocument, color: '#909399' },
  { key: 'logCount', label: '日志数量', icon: Tickets, color: '#f56c6c' },
  { key: 'topicCount', label: '题目数量', icon: Document, color: '#9254de' },
]

const hasData = computed(() => {
  return statItems.some(
    (item) => statistics.value[item.key as keyof DashboardStatisticsVO] !== undefined
  )
})

async function loadStatistics() {
  loading.value = true
  error.value = ''
  try {
    statistics.value = await infoApi.getDashboardStatistics()
  } catch (err: any) {
    error.value = err?.message || '加载统计数据失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadStatistics)
</script>

<style scoped lang="scss">
.dashboard-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .stat-card {
    :deep(.el-card__body) {
      padding: 20px;
    }
  }

  .stat-body {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .stat-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 56px;
    height: 56px;
    border-radius: 50%;
  }

  .stat-info {
    flex: 1;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    line-height: 1.2;
  }

  .stat-label {
    margin-top: 4px;
    font-size: 14px;
    color: #909399;
  }
}
</style>
