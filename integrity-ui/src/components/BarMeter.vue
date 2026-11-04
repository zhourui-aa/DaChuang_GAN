<template>
  <div class="bar-meter">
    <div class="bm-top">
      <span class="bm-label">{{ label }}</span>
      <span class="bm-value" :style="{ color }">{{ Math.round(value) }}%</span>
    </div>
    <div class="bm-track">
      <i :style="{ width: Math.max(0, Math.min(100, value)) + '%', background: color }"></i>
    </div>
    <div v-if="hint" class="bm-hint">{{ hint }}</div>
  </div>
</template>

<script setup lang="ts" name="BarMeter">
/** 横向进度条 + 百分比（对齐参考图 Health Risk Analysis 那列）。 */
withDefaults(defineProps<{ label: string; value: number; color?: string; hint?: string }>(), {
  color: '#2e6be6',
});
</script>

<style scoped lang="scss">
.bar-meter { margin-bottom: 14px; &:last-child { margin-bottom: 0; } }
.bm-top {
  display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 6px;
  .bm-label { font-size: 12.5px; font-weight: 600; color: var(--el-text-color-regular); }
  .bm-value { font-size: 13px; font-weight: 800; font-family: Manrope, monospace; }
}
.bm-track {
  height: 8px; border-radius: 999px; background: #eef0f4; overflow: hidden;
  i { display: block; height: 100%; border-radius: 999px; transition: width 0.6s ease; }
}
.bm-hint { font-size: 11.5px; color: var(--ink-2); margin-top: 5px; line-height: 1.6; }
</style>
