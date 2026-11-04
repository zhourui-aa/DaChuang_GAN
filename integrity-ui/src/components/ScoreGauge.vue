<template>
  <div ref="refEl" :style="{ height: height + 'px' }"></div>
</template>

<script setup lang="ts" name="ScoreGauge">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';

/**
 * 半圆仪表盘（对齐参考图的数据可视化风格：圆头、浅轨、中心大数字）。
 */
const props = withDefaults(defineProps<{ value: number; color?: string; height?: number; label?: string }>(), {
  color: '#2e6be6', height: 190, label: 'AI 生成疑似度',
});

const refEl = ref();
let chart: echarts.ECharts | null = null;

const render = () => {
  if (!refEl.value) return;
  if (!chart) chart = echarts.init(refEl.value);
  chart.setOption({
    series: [{
      type: 'gauge',
      startAngle: 180, endAngle: 0, min: 0, max: 100,
      center: ['50%', '76%'], radius: '104%',
      progress: { show: true, width: 16, roundCap: true, itemStyle: { color: props.color } },
      axisLine: { roundCap: true, lineStyle: { width: 16, color: [[1, '#eef0f4']] } },
      pointer: { show: false }, axisTick: { show: false }, splitLine: { show: false },
      axisLabel: { show: false }, anchor: { show: false },
      title: { show: false },
      detail: { show: false },
      data: [{ value: props.value }],
    }],
    graphic: [
      { type: 'text', left: 'center', top: '52%', style: {
          text: props.value + '%', fontSize: 40, fontWeight: 800, fill: '#1b2559', fontFamily: 'Manrope, PingFang SC' } },
      { type: 'text', left: 'center', top: '74%', style: {
          text: props.label, fontSize: 12, fill: '#8a94a6', fontFamily: 'Manrope, PingFang SC' } },
    ],
  });
};

onMounted(() => nextTick(render));
watch(() => [props.value, props.color], () => nextTick(render));
const onResize = () => chart?.resize();
window.addEventListener('resize', onResize);
onBeforeUnmount(() => { window.removeEventListener('resize', onResize); chart?.dispose(); });

</script>
