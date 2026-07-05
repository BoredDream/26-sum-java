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
        <el-menu-item v-for="menu in menus" :key="menu.path" :index="menu.path">
          <el-icon>
            <component :is="menu.icon" />
          </el-icon>
          <span>{{ menu.title }}</span>
        </el-menu-item>
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { menusByRole, getMenuByPath, roleTextMap } from '@/utils/permission'

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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
