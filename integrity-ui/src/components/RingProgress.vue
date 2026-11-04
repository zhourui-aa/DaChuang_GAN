<template>
  <div class="ring" :style="{ width: size + 'px', height: size + 'px' }">
    <svg :width="size" :height="size" :viewBox="`0 0 ${size} ${size}`">
      <circle :cx="c" :cy="c" :r="r" fill="none" stroke="#eef0f4" :stroke-width="stroke" />
      <circle :cx="c" :cy="c" :r="r" fill="none" :stroke="color" :stroke-width="stroke"
              stroke-linecap="round" :stroke-dasharray="circ" :stroke-dashoffset="offset"
              :transform="`rotate(-90 ${c} ${c})`" style="transition: stroke-dashoffset .6s ease" />
    </svg>
    <div class="ring-center">
      <div class="ring-val" :style="{ color }">{{ Math.round(value) }}<small>%</small></div>
      <div v-if="label" class="ring-label">{{ label }}</div>
    </div>
  </div>
</template>

<script setup lang="ts" name="RingProgress">
import { computed } from 'vue';

/** 圆环百分比（对齐参考图右侧那种 56% / 80% 圆环）。 */
const props = withDefaults(defineProps<{
  value: number; color?: string; size?: number; stroke?: number; label?: string;
}>(), { color: '#2e6be6', size: 104, stroke: 10 });

const c = computed(() => props.size / 2);
const r = computed(() => props.size / 2 - props.stroke / 2 - 1);
const circ = computed(() => 2 * Math.PI * r.value);
const offset = computed(() => circ.value * (1 - Math.max(0, Math.min(100, props.value)) / 100));
</script>

<style scoped lang="scss">
.ring { position: relative; display: inline-block; }
.ring-center {
  position: absolute; inset: 0; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  .ring-val { font-size: 22px; font-weight: 800; letter-spacing: -0.03em; line-height: 1;
    small { font-size: 11px; font-weight: 700; margin-left: 1px; } }
  .ring-label { font-size: 11px; color: var(--ink-2); font-weight: 600; margin-top: 3px; }
}
</style>
