<template>
  <svg :width="width" :height="height" :viewBox="`0 0 ${width} ${height}`" class="mini-gauge">
    <path :d="arc" fill="none" stroke="#eef0f4" :stroke-width="stroke" stroke-linecap="round" />
    <path :d="arc" fill="none" :stroke="color" :stroke-width="stroke" stroke-linecap="round"
          :stroke-dasharray="len" :stroke-dashoffset="offset" style="transition: stroke-dashoffset .6s ease" />
  </svg>
</template>

<script setup lang="ts" name="MiniGauge">
import { computed } from 'vue';

/** 迷你半圆仪表（对齐参考图指标砖里的小仪表）。 */
const props = withDefaults(defineProps<{ value: number; color?: string; width?: number; stroke?: number }>(), {
  color: '#2e6be6', width: 120, stroke: 8,
});
const R = computed(() => (props.width - props.stroke) / 2);
const height = computed(() => R.value + props.stroke);
const arc = computed(() => {
  const pad = props.stroke / 2 + 1;
  const y = R.value + props.stroke / 2;
  return `M ${pad} ${y} A ${R.value} ${R.value} 0 0 1 ${props.width - pad} ${y}`;
});
const len = computed(() => Math.PI * R.value);
const offset = computed(() => len.value * (1 - Math.max(0, Math.min(100, props.value)) / 100));
</script>

<style scoped lang="scss">
.mini-gauge { display: block; }
</style>
