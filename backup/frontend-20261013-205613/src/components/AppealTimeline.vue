<template>
  <el-timeline>
    <el-timeline-item v-for="(t, i) in timeline" :key="i" :type="typeOf(t.state)" :hollow="t.state === 'todo'">
      <div style="font-weight: 500">{{ t.title }}</div>
      <div class="font12 color-info">{{ t.detail }}</div>
    </el-timeline-item>
  </el-timeline>
</template>

<script setup lang="ts" name="AppealTimeline">
/** 申诉流程时间线（提交 → 受理 → 复核 → 结论 → 归档）。 */
defineProps<{ timeline: any[] }>();
const typeOf = (state: string) => (state === 'done' ? 'success' : state === 'now' ? 'primary' : 'info');
</script>
