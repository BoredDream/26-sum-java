<template>
  <div class="page-header">
    <h2 class="title">{{ title }}</h2>
    <div v-if="$slots.extra" class="extra">
      <slot name="extra" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
}>()
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  .title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
  }

  .extra {
    display: flex;
    gap: 12px;
  }
}
</style>
