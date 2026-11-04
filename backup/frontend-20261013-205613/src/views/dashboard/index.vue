<template>
  <div class="dashboard layout-pd">
    <div class="page-head">
      <h2>课程统计看板</h2>
      <p>关注等级构成与近期检测趋势。所有统计基于已完成的本地特征检测。</p>
    </div>

    <el-row :gutter="15" class="mb15">
      <el-col :xs="12" :sm="6" :md="6" :lg="6" v-for="(c, i) in cards" :key="i">
        <el-card shadow="hover">
          <div class="stat">
            <div class="stat-icon" :style="{ background: c.bg }"><el-icon :style="{ color: c.color }"><component :is="c.icon" /></el-icon></div>
            <div>
              <div class="font13 color-info">{{ c.label }}</div>
              <div class="stat-num" :style="{ color: c.color }">{{ c.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="15">
      <el-col :xs="24" :sm="10" :md="10" :lg="8">
        <el-card shadow="hover"><div ref="pieRef" style="height: 320px"></div></el-card>
      </el-col>
      <el-col :xs="24" :sm="14" :md="14" :lg="16">
        <el-card shadow="hover">
          <template #header><span style="font-weight: 600">近期检测记录</span></template>
          <el-table :data="data.recent" style="width: 100%">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="assignmentTitle" label="作业" min-width="220" show-overflow-tooltip />
            <el-table-column prop="nameMask" label="提交人" width="100" />
            <el-table-column label="疑似度" width="180">
              <template #default="scope">
                <el-progress :percentage="Math.round((scope.row.riskScore || 0) * 100)" :color="colorOf(scope.row.riskLevel)" :stroke-width="8" />
              </template>
            </el-table-column>
            <el-table-column label="等级" width="100">
              <template #default="scope"><el-tag :type="tagOf(scope.row.riskLevel)">{{ scope.row.riskLevel }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="170" />
          </el-table>
          <el-empty v-if="!data.recent?.length" description="暂无检测记录" :image-size="70" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="dashboard">
import { computed, onMounted, ref, nextTick } from 'vue';
import * as echarts from 'echarts';
import { dashboardApi } from '@/api';

const data = ref<any>({});
const pieRef = ref();
let pie: echarts.ECharts | null = null;

const cards = computed(() => [
  { label: '已检测作业', value: data.value.detected || 0, icon: 'Document', color: 'var(--el-color-primary)', bg: 'var(--next-color-primary-lighter)' },
  { label: '疑似（中/高关注）', value: data.value.suspected || 0, icon: 'Warning', color: 'var(--el-color-danger)', bg: 'var(--el-color-danger-light-9)' },
  { label: '复核认定人工', value: data.value.confirmed || 0, icon: 'CircleCheck', color: 'var(--el-color-success)', bg: 'var(--el-color-success-light-9)' },
  { label: '申诉工单', value: data.value.appeals || 0, icon: 'ChatDotSquare', color: 'var(--el-color-warning)', bg: 'var(--el-color-warning-light-9)' },
]);

const tagOf = (l: string) => (l === '高关注' ? 'danger' : l === '中关注' ? 'warning' : 'success');
const colorOf = (l: string) => (l === '高关注' ? '#f56c6c' : l === '中关注' ? '#e6a23c' : '#67c23a');

const renderPie = () => {
  if (!pieRef.value) return;
  if (!pie) pie = echarts.init(pieRef.value);
  const levels = data.value.levelCounts || [];
  pie.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    title: { text: '关注等级构成', left: 'center', top: 8, textStyle: { fontSize: 14, color: '#606266' } },
    series: [{
      type: 'pie', radius: ['44%', '66%'], center: ['50%', '48%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false }, labelLine: { show: false },
      data: levels.map((l: any) => ({
        name: l.level,
        value: l.cnt,
        itemStyle: { color: l.level === '高关注' ? '#f56c6c' : l.level === '中关注' ? '#e6a23c' : l.level === '证据不足' ? '#909399' : '#67c23a' },
      })),
    }],
  });
};

onMounted(async () => {
  data.value = (await dashboardApi()) as any;
  await nextTick();
  renderPie();
  window.addEventListener('resize', () => pie?.resize());
});
</script>

<style scoped lang="scss">
.page-head { margin-bottom: 15px; h2 { margin: 0; font-size: 18px; } p { margin: 6px 0 0; font-size: 13px; color: var(--el-text-color-secondary); } }
.stat { display: flex; align-items: center; gap: 14px;
  .stat-icon { width: 48px; height: 48px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 22px; }
  .stat-num { font-size: 26px; font-weight: 600; letter-spacing: -0.5px; }
}
</style>
