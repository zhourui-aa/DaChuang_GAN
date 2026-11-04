<template>
  <div class="stat-tile">
    <div class="st-row">
      <span class="st-label">{{ label }}</span>
      <span v-if="delta" class="st-delta" :class="deltaType">{{ delta }}</span>
    </div>
    <div class="st-value" :style="{ color }">{{ value }}<small v-if="unit">{{ unit }}</small></div>
    <div class="st-bar">
      <i :style="{ width: Math.max(0, Math.min(100, percent)) + '%', background: color }"></i>
    </div>
    <div v-if="hint" class="st-hint">{{ hint }}</div>
  </div>
</template>

<script setup lang="ts" name="StatTile">
/** 指标砖（小标签 + 大数字 + 迷你进度条），对齐参考图大量出现的度量卡。 */
withDefaults(defineProps<{
  label: string; value: number | string; percent?: number; unit?: string;
  color?: string; delta?: string; deltaType?: 'up' | 'down'; hint?: string;
}>(), { percent: 0, color: '#2e6be6', deltaType: 'up' });
</script>

<style scoped lang="scss">
.stat-tile {
  background: #f7f8fa; border-radius: var(--r-block); padding: 14px 16px; height: 100%;
  .st-row { display: flex; align-items: center; gap: 6px; }
  .st-label { font-size: 11.5px; color: var(--ink-2); font-weight: 600; }
  .st-delta { font-size: 11px; font-weight: 700; padding: 1px 7px; border-radius: 999px; margin-left: auto;
    &.up { background: var(--danger-bg); color: #d05959; }
    &.down { background: var(--ok-bg); color: #16a34a; } }
  .st-value { font-size: 28px; font-weight: 800; letter-spacing: -0.03em; line-height: 1.2; margin-top: 2px;
    small { font-size: 13px; font-weight: 600; color: var(--ink-2); margin-left: 2px; } }
  .st-bar { height: 6px; border-radius: 999px; background: #e9ecf2; overflow: hidden; margin-top: 8px;
    i { display: block; height: 100%; border-radius: 999px; transition: width 0.6s ease; } }
  .st-hint { font-size: 11px; color: var(--ink-2); margin-top: 6px; }
}
</style>
