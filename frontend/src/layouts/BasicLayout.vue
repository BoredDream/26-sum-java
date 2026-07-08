<template>
  <el-container class="basic-layout">
    <el-aside width="220px" class="layout-aside">
      <div class="logo">综合实训选题系统</div>
      <el-menu
        :default-active="activePath"
        router
        class="layout-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        :collapse-transition="false"
      >
        <template v-for="menu in menus" :key="menu.path">
          <el-menu-item :index="menu.path">
            <el-icon>
              <component :is="menu.icon" />
            </el-icon>
            <template v-if="getBadgeCount(menu.path) > 0">
              <span class="badge-wrap">
                {{ menu.title }}
                <span class="badge-dot">{{ getBadgeCount(menu.path) > 99 ? '99+' : getBadgeCount(menu.path) }}</span>
              </span>
            </template>
            <span v-else>{{ menu.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container class="layout-main-container">
      <el-header class="layout-header">
        <div class="breadcrumb">{{ currentMenu?.title || '' }}</div>
        <div class="user-info">
          <span class="name">{{ auth.user?.name }}</span>
          <el-tag size="small" effect="plain">{{ roleText }}</el-tag>
          <el-button type="primary" text size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { menusByRole, getMenuByPath, roleTextMap } from '@/utils/permission'
import * as infoApi from '@/api/info'
import * as attendanceApi from '@/api/attendance'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const menus = computed(() => {
  if (!auth.role) return []
  return menusByRole[auth.role].filter((m) => !m.hidden)
})

const activePath = computed(() => route.path)
const currentMenu = computed(() => getMenuByPath(route.path))
const roleText = computed(() => (auth.role ? roleTextMap[auth.role] : ''))

const unreadNoticeCount = ref(0)

async function fetchUnreadCount() {
  if (!auth.isStudent) return
  try {
    unreadNoticeCount.value = await infoApi.getUnreadNoticeCount()
  } catch {
    unreadNoticeCount.value = 0
  }
}

const unsignedTaskCount = ref(0)
const unviewedMakeupCount = ref(0)

function getBadgeCount(path: string): number {
  if (path === '/notices') return unreadNoticeCount.value
  if (path === '/attendance/records/my') return unsignedTaskCount.value
  if (path === '/attendance/makeup/my') return unviewedMakeupCount.value
  return 0
}

async function fetchUnsignedTaskCount() {
  if (!auth.isStudent) return
  try {
    unsignedTaskCount.value = await attendanceApi.getUnsignedTaskCount()
  } catch {
    unsignedTaskCount.value = 0
  }
}

async function fetchUnviewedMakeupCount() {
  if (!auth.isStudent) return
  try {
    unviewedMakeupCount.value = await attendanceApi.getUnviewedMakeupCount()
  } catch {
    unviewedMakeupCount.value = 0
  }
}

async function fetchAttendanceCounts() {
  await Promise.all([fetchUnsignedTaskCount(), fetchUnviewedMakeupCount()])
}

onMounted(() => {
  fetchUnreadCount()
  fetchAttendanceCounts()
})

// 离开相关页面时刷新未读数
watch(
  () => route.path,
  (_newPath, oldPath) => {
    if (!auth.isStudent) return
    if (oldPath === '/notices') {
      fetchUnreadCount()
    }
    if (oldPath === '/attendance/records/my' || oldPath === '/attendance/makeup/my') {
      fetchAttendanceCounts()
    }
  }
)

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.basic-layout {
  height: 100vh;
  width: 100vw;
}

.layout-aside {
  background-color: #304156;
  color: #fff;

  .logo {
    height: 60px;
    line-height: 60px;
    text-align: center;
    font-size: 16px;
    font-weight: 600;
    border-bottom: 1px solid #1f2d3d;
  }
}

.layout-menu {
  border-right: none;
}

.layout-main-container {
  background-color: #f0f2f5;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 10;

  .breadcrumb {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .name {
      font-size: 14px;
      color: #606266;
    }
  }
}

.layout-main {
  padding: 20px;
  overflow-y: auto;
}

.badge-wrap {
  position: relative;
  display: inline-block;
}

.badge-dot {
  position: absolute;
  top: 10px;
  right: -16px;
  height: 16px;
  min-width: 16px;
  padding: 0 4px;
  font-size: 10px;
  line-height: 16px;
  text-align: center;
  color: #fff;
  background: #f56c6c;
  border-radius: 8px;
  box-sizing: border-box;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
