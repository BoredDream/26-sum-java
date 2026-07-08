<template>
  <div class="my-attendance-page">
    <page-header title="我的考勤" />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="mb-4" />

    <!-- 待签到任务 -->
    <el-card v-loading="tasksLoading" class="mb-4">
      <template #header><span>待签到任务</span></template>
      <el-empty v-if="pendingTasks.length === 0" description="暂无待签到任务" />
      <el-row v-else :gutter="16">
        <el-col v-for="task in pendingTasks" :key="task.taskId" :span="8" class="task-card-col">
          <el-card shadow="hover">
            <div class="task-card-title">
              {{ task.taskTitle }}
              <el-tag v-if="task.requireLocation === 1" type="warning" size="small" effect="plain">
                📍 定位签到
              </el-tag>
            </div>
            <div class="task-card-meta">
              <div>签到类型：{{ task.taskTypeName }}</div>
              <div>适用范围：{{ task.scopeDisplayName }}</div>
              <div v-if="task.locationName">签到地点：{{ task.locationName }}</div>
              <div>开始时间：{{ formatDateTime(task.startTime) }}</div>
              <div>结束时间：{{ formatDateTime(task.endTime) }}</div>
            </div>
            <el-button
              v-if="signedTaskIds.has(task.taskId)"
              type="info"
              disabled
              style="width: 100%"
            >
              已签到
            </el-button>
            <el-button
              v-else
              type="primary"
              :loading="signingId === task.taskId"
              style="width: 100%"
              @click="handleSign(task.taskId)"
            >
              立即签到
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 考勤记录 -->
    <el-card v-loading="loading">
      <template #header><span>考勤记录</span></template>
      <el-empty v-if="!loading && records.length === 0 && !error" description="暂无考勤记录" />
      <el-table v-else :data="records" border :row-class-name="getRowClass">
        <el-table-column prop="taskTitle" label="签到任务" show-overflow-tooltip />
        <el-table-column label="签到时间" width="170">
          <template #default="scope">{{
            formatDateTime((scope.row as AttendanceRecordVO).signTime)
          }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="scope">
            <status-tag
              category="attendanceSign"
              :value="(scope.row as AttendanceRecordVO).signStatus"
            />
          </template>
        </el-table-column>
        <el-table-column label="补签" width="90">
          <template #default="scope">
            <el-tag v-if="(scope.row as AttendanceRecordVO).isMakeup === 1" type="info" size="small"
              >是</el-tag
            >
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>

      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadRecords"
          @current-change="loadRecords"
        />
      </div>
    </el-card>

    <!-- 签到确认 -->
    <el-dialog v-model="signVisible" title="签到确认" width="560px" @opened="initSignMap" @closed="destroySignMap">
      <div v-if="currentSignTask?.requireLocation === 1" class="location-info">
        <div class="sign-map-box">
          <div id="sign-location-map" class="sign-location-map" :class="{ hidden: !mapReady }"></div>
          <div v-if="!mapReady" class="map-placeholder">
            <div v-if="taskLocationMissing">签到任务未设置签到点，请联系教师重新发布</div>
            <div v-else-if="locating">正在获取当前位置...</div>
            <div v-else-if="locateFailed" class="map-placeholder-action">
              <span>位置获取失败，请允许浏览器定位后重试</span>
              <el-button type="primary" size="small" @click="startLocate">重新定位</el-button>
            </div>
            <div v-else-if="!amapReady">地图服务未加载，请检查高德 Key 或网络连接</div>
            <div v-else>等待定位结果...</div>
          </div>
        </div>

        <div class="location-item">
          <span class="location-label">签到地点：</span>
          <span>{{ currentSignTask.locationName || '未命名地点' }}</span>
        </div>
        <div class="location-item">
          <span class="location-label">允许半径：</span>
          <span>{{ currentSignTask.locationRadius || 500 }} 米</span>
        </div>
        <div class="location-item">
          <span class="location-label">我的位置：</span>
          <span>{{ currentPositionText }}</span>
        </div>
        <div class="location-item">
          <span class="location-label">距签到点：</span>
          <span>{{ distanceText }}</span>
        </div>
        <div class="location-item">
          <span class="location-label">范围判断：</span>
          <span :class="distanceStatusClass">{{ edgeText }}</span>
        </div>
        <div class="location-status">
          <el-tag :type="rangeStatusType" size="small">{{ rangeStatusText }}</el-tag>
        </div>
      </div>
      <el-form :model="signForm" label-width="80px">
        <el-form-item label="备注">
          <el-input v-model="signForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="signVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="signLoading"
          :disabled="signConfirmDisabled"
          @click="confirmSign"
        >确认签到</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as attendanceApi from '@/api/attendance'
import type { AttendanceRecordVO, AttendanceTaskVO } from '@/types/attendance'
import { formatDateTime } from '@/utils/format'

declare const AMap: any

const loading = ref(false)
const tasksLoading = ref(false)
const error = ref('')
const records = ref<AttendanceRecordVO[]>([])
const tasks = ref<AttendanceTaskVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const pendingTasks = computed(() => tasks.value.filter((t) => t.status === 1))

const signedTaskIds = ref<Set<number>>(new Set())

async function loadSignedTaskIds() {
  try {
    const ids = await attendanceApi.getSignedTaskIds()
    signedTaskIds.value = new Set(ids)
  } catch {
    // 静默失败
  }
}

function getRowClass({ row }: { row: AttendanceRecordVO }) {
  return row.signStatus === 0 ? 'row-unsigned' : ''
}

async function loadRecords() {
  loading.value = true
  error.value = ''
  try {
    const res = await attendanceApi.queryAttendanceRecordPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })
    records.value = res.records
    total.value = res.total
  } catch (err: any) {
    error.value = err?.message || '加载考勤记录失败'
  } finally {
    loading.value = false
  }
}

async function loadTasks() {
  tasksLoading.value = true
  try {
    const res = await attendanceApi.queryAttendanceTaskPage({ pageNum: 1, pageSize: 100 })
    tasks.value = res.records
  } catch (err: any) {
    error.value = err?.message || '加载签到任务失败'
  } finally {
    tasksLoading.value = false
  }
}

// 签到
const signVisible = ref(false)
const signLoading = ref(false)
const signingId = ref(0)
const currentTaskId = ref(0)
const currentSignTask = ref<AttendanceTaskVO | null>(null)
const locating = ref(false)
const locateFailed = ref(false)
const mapReady = ref(false)
const signForm = reactive({
  remark: '',
  signLng: undefined as number | undefined,
  signLat: undefined as number | undefined,
})

const amapReady = computed(() => typeof window !== 'undefined' && !!(window as any).AMap)

const taskLocationMissing = computed(() => {
  const task = currentSignTask.value
  return !!task && task.requireLocation === 1 && (task.locationLng == null || task.locationLat == null)
})

// 到圆心的距离
const signDistance = computed(() => {
  const task = currentSignTask.value
  if (!task || task.requireLocation !== 1) return -1
  if (taskLocationMissing.value) return -1
  if (signForm.signLng == null || signForm.signLat == null) return -1
  const centerLng = task.locationLng
  const centerLat = task.locationLat
  if (centerLng == null || centerLat == null) return -1
  return haversine(signForm.signLat, signForm.signLng, centerLat, centerLng)
})

// 到签到范围边缘的距离：>0 表示在圈外xxx米，<=0 表示在圈内
const distanceToEdge = computed(() => {
  if (signDistance.value < 0) return -1
  const radius = currentSignTask.value?.locationRadius || 500
  return signDistance.value - radius
})

const distanceStatusClass = computed(() => {
  if (signDistance.value < 0) return ''
  return distanceToEdge.value <= 0 ? 'distance-ok' : 'distance-far'
})

const currentPositionText = computed(() => {
  if (signForm.signLng == null || signForm.signLat == null) {
    return locateFailed.value ? '定位失败' : '定位中'
  }
  return `${signForm.signLng.toFixed(6)}, ${signForm.signLat.toFixed(6)}`
})

const distanceText = computed(() => {
  if (taskLocationMissing.value) return '无法计算'
  return signDistance.value >= 0 ? `${signDistance.value} 米` : '待计算'
})

const edgeText = computed(() => {
  if (taskLocationMissing.value) return '签到点未设置'
  if (signDistance.value < 0) return '待计算'
  if (distanceToEdge.value <= 0) {
    return `范围内，距边缘 ${Math.abs(distanceToEdge.value)} 米`
  }
  return `超出范围 ${distanceToEdge.value} 米`
})

const rangeStatusType = computed<'success' | 'danger' | 'info'>(() => {
  if (signDistance.value < 0) return 'info'
  return distanceToEdge.value <= 0 ? 'success' : 'danger'
})

const rangeStatusText = computed(() => {
  if (taskLocationMissing.value) return '签到点未设置'
  if (signDistance.value < 0) return '等待定位'
  return distanceToEdge.value <= 0 ? '在签到范围内' : '不在签到范围内'
})

const signConfirmDisabled = computed(() => {
  if (signLoading.value) return true
  if (currentSignTask.value?.requireLocation !== 1) return false
  return taskLocationMissing.value || signForm.signLng == null || signForm.signLat == null
})

function haversine(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const R = 6371000
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLng = (lng2 - lng1) * Math.PI / 180
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
    + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180)
    * Math.sin(dLng / 2) * Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return Math.round(R * c)
}

function handleSign(taskId: number) {
  currentTaskId.value = taskId
  signForm.remark = ''
  signForm.signLng = undefined
  signForm.signLat = undefined
  locating.value = false
  locateFailed.value = false
  mapReady.value = false

  currentSignTask.value = tasks.value.find((t) => t.taskId === taskId) || null

  // 先打开弹窗，再获取位置
  if (currentSignTask.value?.requireLocation === 1) {
    signVisible.value = true
    if (!taskLocationMissing.value) {
      locating.value = true
      startLocate()
    }
  } else {
    signVisible.value = true
  }
}

function startLocate() {
  locating.value = true
  locateFailed.value = false
  mapReady.value = false
  // 优先使用浏览器原生 Geolocation API（不依赖高德 SDK 加载状态）
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        signForm.signLng = pos.coords.longitude
        signForm.signLat = pos.coords.latitude
        locating.value = false
        locateFailed.value = false
      },
      () => {
        // 浏览器定位失败，尝试高德
        tryAmapLocate()
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
    )
  } else {
    tryAmapLocate()
  }
}

function tryAmapLocate() {
  if (typeof AMap === 'undefined') {
    locating.value = false
    locateFailed.value = true
    ElMessage.warning('定位失败，请检查浏览器位置权限设置')
    return
  }
  AMap.plugin('AMap.Geolocation', () => {
    const geo = new AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
    })
    geo.getCurrentPosition((status: string, result: any) => {
      locating.value = false
      if (status === 'complete' && result.position) {
        signForm.signLng = result.position.lng
        signForm.signLat = result.position.lat
        locateFailed.value = false
      } else {
        locateFailed.value = true
        ElMessage.warning('定位失败，请确保已开启GPS并授权位置权限')
      }
    })
  })
}

async function confirmSign() {
  if (currentSignTask.value?.requireLocation === 1 && taskLocationMissing.value) {
    ElMessage.error('该签到任务未设置签到点，请联系教师重新发布')
    return
  }
  // 如果任务要求定位但定位失败，阻止签到
  if (currentSignTask.value?.requireLocation === 1 && (signForm.signLng == null || signForm.signLat == null)) {
    ElMessage.warning('正在获取位置信息，请稍候...')
    return
  }
  signLoading.value = true
  signingId.value = currentTaskId.value
  try {
    const params: any = {
      taskId: currentTaskId.value,
      remark: signForm.remark,
    }
    if (signForm.signLng != null) {
      params.signLng = signForm.signLng
    }
    if (signForm.signLat != null) {
      params.signLat = signForm.signLat
    }
    await attendanceApi.signAttendance(params)
    ElMessage.success('签到成功')
    signedTaskIds.value.add(currentTaskId.value)
    signVisible.value = false
    loadRecords()
    loadTasks()
  } catch (err: any) {
    ElMessage.error(err?.message || '签到失败')
  } finally {
    signLoading.value = false
    signingId.value = 0
  }
}

// 签到地图
let signMap: any = null

function initSignMap() {
  if (typeof AMap === 'undefined') {
    mapReady.value = false
    return
  }
  const task = currentSignTask.value
  if (!task || task.requireLocation !== 1) return
  if (signForm.signLng == null || task.locationLng == null || task.locationLat == null) return

  const container = document.getElementById('sign-location-map')
  if (!container || signMap) return

  const centerLng = task.locationLng
  const centerLat = task.locationLat
  const studentLng = signForm.signLng!
  const studentLat = signForm.signLat!
  const radius = task.locationRadius || 500

  signMap = new AMap.Map('sign-location-map', {
    zoom: 14,
    center: [centerLng, centerLat],
    resizeEnable: true,
  })
  mapReady.value = true

  // 根据两个点调整视野
  signMap.setFitView(null, false, [60, 60, 60, 260])

  // 绘制签到范围圈（蓝色半透明）
  new AMap.Circle({
    center: [centerLng, centerLat],
    radius: radius,
    strokeColor: '#1890ff',
    strokeWeight: 2,
    strokeOpacity: 0.6,
    fillColor: '#1890ff',
    fillOpacity: 0.12,
    map: signMap,
  })

  // 签到中心点标记（蓝色）
  new AMap.Marker({
    position: [centerLng, centerLat],
    title: task.locationName || '签到点',
    icon: new AMap.Icon({
      size: new AMap.Size(24, 34),
      image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
      imageSize: new AMap.Size(24, 34),
    }),
    label: {
      content: '<div style="background:#1890ff;color:#fff;padding:2px 6px;border-radius:3px;font-size:11px;white-space:nowrap">' + (task.locationName || '签到点') + '</div>',
      offset: new AMap.Pixel(0, -36),
    },
    map: signMap,
  })

  // 学生当前位置标记（红色）
  new AMap.Marker({
    position: [studentLng, studentLat],
    title: '我的位置',
    icon: new AMap.Icon({
      size: new AMap.Size(24, 34),
      image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
      imageSize: new AMap.Size(24, 34),
    }),
    label: {
      content: '<div style="background:#f56c6c;color:#fff;padding:2px 6px;border-radius:3px;font-size:11px;white-space:nowrap">我的位置</div>',
      offset: new AMap.Pixel(0, -36),
    },
    map: signMap,
  })

  // 自动适配视野
  setTimeout(() => {
    signMap?.setFitView(null, false, [80, 80, 80, 280])
  }, 300)
}

function destroySignMap() {
  if (signMap) {
    signMap.destroy()
    signMap = null
  }
  mapReady.value = false
}

// 定位数据到达后重新初始化地图
watch(() => signForm.signLng, (newVal) => {
  if (newVal != null && signVisible.value) {
    nextTick(() => initSignMap())
  }
})

onMounted(() => {
  loadRecords()
  loadTasks()
  loadSignedTaskIds()
})
</script>

<style scoped lang="scss">
.my-attendance-page {
  .mb-4 {
    margin-bottom: 16px;
  }

  .task-card-col {
    margin-bottom: 16px;
  }

  .task-card-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
  }

  .task-card-meta {
    font-size: 13px;
    color: #606266;
    line-height: 1.8;
    margin-bottom: 16px;
  }

  .location-info {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;

    .sign-map-box {
      position: relative;
      width: 100%;
      height: 260px;
      border: 1px solid #dcdfe6;
      border-radius: 6px;
      overflow: hidden;
      background: #eef2f7;
      margin-bottom: 12px;
    }

    .sign-location-map {
      width: 100%;
      height: 100%;

      &.hidden {
        visibility: hidden;
      }
    }

    .map-placeholder {
      position: absolute;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      text-align: center;
      color: #606266;
      font-size: 14px;
      padding: 24px;

      .map-placeholder-action {
        display: inline-flex;
        align-items: center;
        gap: 12px;
        flex-wrap: wrap;
        justify-content: center;
      }
    }

    .location-item {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      font-size: 14px;

      .location-label {
        color: #909399;
        width: 90px;
        flex-shrink: 0;
      }

      .distance-ok {
        color: #67c23a;
        font-weight: 600;
      }

      .distance-far {
        color: #f56c6c;
        font-weight: 600;
      }
    }

    .location-status {
      margin-top: 4px;
    }

    .locating-hint {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #409eff;
      font-size: 14px;
      padding: 8px 0;
    }

    .locate-failed {
      color: #e6a23c;
      font-size: 13px;
      padding: 8px 0;
      line-height: 2;
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  :deep(.row-unsigned) {
    background-color: #fef0f0 !important;
    td {
      background-color: #fef0f0 !important;
    }
  }
}
</style>
