<template>
  <div ref="refEl" style="height: 320px"></div>
</template>

<script setup lang="ts" name="FeatureRadar">
import { onBeforeUnmount, onMounted, ref, watch, nextTick } from 'vue';
import * as echarts from 'echarts';

/**
 * 特征雷达图：6 维特征 → 风险分（0-100）。
 */
const props = defineProps<{ features: any[] }>();
const refEl = ref();
let chart: echarts.ECharts | null = null;

const render = () => {
  if (!refEl.value || !props.features || !props.features.length) return;
  if (!chart) chart = echarts.init(refEl.value);
  chart.setOption({
    tooltip: {},
    radar: {
      indicator: props.features.map((f) => ({ name: f.featureName, max: 100 })),
      radius: '62%',
      splitLine: { lineStyle: { color: '#f1f2f3' } },
      splitArea: { areaStyle: { color: ['#fff', '#fafbfc'] } },
      axisName: { color: '#909399', fontSize: 12 },
    },
    series: [{
      type: 'radar',
      data: [{
        value: props.features.map((f) => Math.round((f.featureScore || 0) * 100)),
        name: '风险分',
        areaStyle: { color: 'rgba(64,158,255,.25)' },
        lineStyle: { color: '#409eff' },
        itemStyle: { color: '#409eff' },
      }],
    }],
  });
};

onMounted(() => nextTick(render));
watch(() => props.features, () => nextTick(render), { deep: true });
const onResize = () => chart?.resize();
window.addEventListener('resize', onResize);
onBeforeUnmount(() => { window.removeEventListener('resize', onResize); chart?.dispose(); });
</script>
