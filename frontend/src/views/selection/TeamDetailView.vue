<template>
  <div class="team-detail-page">
    <page-header title="团队详情">
      <template #extra>
        <el-button @click="$router.back()">返回</el-button>
      </template>
    </page-header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>{{ team?.teamName }}</span>
          <el-tag v-if="team" size="small">{{ team.status === 'SELECTED' ? '已选题' : '组建中' }}</el-tag>
        </div>
      </template>

      <el-descriptions v-if="team" :column="2" border>
        <el-descriptions-item label="团队编号">{{ team.id }}</el-descriptions-item>
        <el-descriptions-item label="队长ID">{{ team.leaderId }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{
          formatDateTime(team.createTime)
        }}</el-descriptions-item>
        <el-descriptions-item label="团队介绍" :span="2">{{
          team.introduction || '-'
        }}</el-descriptions-item>
      </el-descriptions>

      <h4 v-if="team" class="section-title">成员列表</h4>
      <el-table v-if="team" :data="team.members" border>
        <el-table-column prop="studentId" label="学生ID" />
        <el-table-column label="角色">
          <template #default="{ row }">
            {{ row.memberRole === 'LEADER' ? '队长' : '成员' }}
          </template>
        </el-table-column>
        <el-table-column prop="workContent" label="分工" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import * as selectionApi from '@/api/selection'
import type { TeamVO } from '@/types/selection'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const loading = ref(false)
const error = ref('')
const team = ref<TeamVO | null>(null)

async function loadTeam() {
  const teamId = Number(route.params.teamId)
  if (!teamId) {
    error.value = '团队编号无效'
    return
  }
  loading.value = true
  error.value = ''
  try {
    team.value = await selectionApi.getTeam(teamId)
  } catch (err: any) {
    error.value = err?.message || '加载团队信息失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadTeam)
</script>

<style scoped lang="scss">
.team-detail-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .section-title {
    margin: 20px 0 12px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}
</style>
