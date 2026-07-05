<template>
  <div class="attendance-statistics-page">
    <page-header title="考勤统计" />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <el-form :model="queryForm" inline class="query-form">
      <el-form-item label="签到任务">
        <el-select v-model="queryForm.taskId" placeholder="全部任务" clearable style="width: 220px">
          <el-option
            v-for="task in taskOptions"
            :key="task.taskId"
            :label="task.taskTitle"
            :value="task.taskId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="班级">
        <el-input v-model="queryForm.className" placeholder="选填" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 260px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSearch">统计</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="16" class="stat-cards">
      <el-col :span="4">
        <el-statistic title="总人数" :value="statistics?.totalCount || 0" />
      </el-col>
      <el-col :span="4">
        <el-statistic title="正常" :value="statistics?.normalCount || 0">
          <template #suffix>人</template>
        </el-statistic>
      </el-col>
      <el-col :span="4">
        <el-statistic title="迟到" :value="statistics?.lateCount || 0">
          <template #suffix>人</template>
        </el-statistic>
      </el-col>
      <el-col :span="4">
        <el-statistic title="缺勤" :value="statistics?.absentCount || 0">
          <template #suffix>人</template>
        </el-statistic>
      </el-col>
      <el-col :span="4">
        <el-statistic title="补签" :value="statistics?.makeupCount || 0">
          <template #suffix>人</template>
        </el-statistic>
      </el-col>
      <el-col :span="4">
        <el-statistic title="出勤率" :value="attendanceRateText" />
      </el-col>
    </el-row>

    <el-card v-loading="loading" class="chart-card">
      <template #header><span>考勤分布</span></template>
      <div ref="chartRef" class="chart-container"></div>
      <el-empty v-if="!loading && !hasData" description="暂无统计数据" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import * as attendanceApi from '@/api/attendance'
import type {
  AttendanceStatisticsVO,
  AttendanceStatisticsQuery,
  AttendanceTaskVO,
} from '@/types/attendance'

const loading = ref(false)
const error = ref('')
const statistics = ref<AttendanceStatisticsVO | null>(null)
const taskOptions = ref<AttendanceTaskVO[]>([])
const dateRange = ref<[string, string] | null>(null)

const queryForm = reactive({
  taskId: undefined as number | undefined,
  className: '',
})

const attendanceRateText = computed(() => {
  const rate = statistics.value?.attendanceRate
  return rate !== undefined ? `${(Number(rate) * 100).toFixed(2)}%` : '-'
})

const hasData = computed(() => {
  if (!statistics.value) return false
  const { normalCount, lateCount, absentCount, makeupCount } = statistics.value
  return (normalCount || 0) + (lateCount || 0) + (absentCount || 0) + (makeupCount || 0) > 0
})

// ECharts
const chartRef = ref<HTMLDivElement | null>(null)
let chartInstance: echarts.ECharts | null = null

function initChart() {
  if (!chartRef.value) return
  if (chartInstance) {
    chartInstance.dispose()
  }
  chartInstance = echarts.init(chartRef.value)
  updateChart()
  window.addEventListener('resize', handleResize)
}

function updateChart() {
  if (!chartInstance) return
  if (!hasData.value) {
    chartInstance.clear()
    return
  }
  const { normalCount, lateCount, absentCount, makeupCount } = statistics.value!
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    legend: {
      bottom: '5%',
      left: 'center',
    },
    series: [
      {
        name: '考勤分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: true,
          formatter: '{b}: {c}',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
          },
        },
        data: [
          { value: normalCount || 0, name: '正常', itemStyle: { color: '#67c23a' } },
          { value: lateCount || 0, name: '迟到', itemStyle: { color: '#e6a23c' } },
          { value: absentCount || 0, name: '缺勤', itemStyle: { color: '#f56c6c' } },
          { value: makeupCount || 0, name: '补签', itemStyle: { color: '#909399' } },
        ],
      },
    ],
  }
  chartInstance.setOption(option)
}

function handleResize() {
  chartInstance?.resize()
}

async function loadStatistics() {
  loading.value = true
  error.value = ''
  try {
    const query: AttendanceStatisticsQuery = {
      taskId: queryForm.taskId,
      className: queryForm.className || undefined,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
    }
    statistics.value = await attendanceApi.getAttendanceStatistics(query)
    await nextTick()
    if (!chartInstance) {
      initChart()
    } else {
      updateChart()
    }
  } catch (err: any) {
    error.value = err?.message || '加载考勤统计失败'
    statistics.value = null
  } finally {
    loading.value = false
  }
}

async function loadTaskOptions() {
  try {
    const res = await attendanceApi.queryAttendanceTaskPage({ pageNum: 1, pageSize: 100 })
    taskOptions.value = res.records
  } catch {
    // 非核心
  }
}

function handleSearch() {
  loadStatistics()
}

function resetQuery() {
  queryForm.taskId = undefined
  queryForm.className = ''
  dateRange.value = null
  loadStatistics()
}

watch(
  () => statistics.value,
  () => {
    nextTick(() => updateChart())
  }
)

onMounted(() => {
  loadTaskOptions()
  loadStatistics()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped lang="scss">
.attendance-statistics-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .query-form {
    margin-bottom: 16px;
  }

  .stat-cards {
    margin-bottom: 16px;
  }

  .chart-card {
    .chart-container {
      width: 100%;
      height: 400px;
    }
  }
}
</style>
