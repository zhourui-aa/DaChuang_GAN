<template>
  <div class="dashboard layout-pd">
    <div class="page-head">
      <h2>课程统计看板</h2>
      <p>关注等级构成与近期检测情况。所有统计基于已完成的本地特征检测。</p>
    </div>

    <!-- 指标砖 -->
    <el-row :gutter="16" class="mb15">
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

    <!-- Bento：记录表 + 风险构成 -->
    <el-row :gutter="16">
      <el-col :xs="24" :md="16" :lg="16">
        <el-card shadow="hover" class="h100">
          <template #header><div class="card-head"><span class="ch-title">近期检测记录</span><span class="ch-sub">最近 {{ data.recent?.length || 0 }} 条</span></div></template>
          <el-table :data="data.recent" style="width: 100%" @row-click="(row:any) => router.push('/detect/' + row.submissionId)">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="assignmentTitle" label="作业" min-width="200" show-overflow-tooltip />
            <el-table-column prop="nameMask" label="提交人" width="90" />
            <el-table-column label="疑似度" width="180">
              <template #default="scope">
                <el-progress :percentage="Math.round((scope.row.riskScore || 0) * 100)" :color="colorOf(scope.row.riskLevel)" :stroke-width="8" />
              </template>
            </el-table-column>
            <el-table-column label="等级" width="100">
              <template #default="scope"><el-tag :type="tagOf(scope.row.riskLevel)">{{ scope.row.riskLevel }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="165" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="scope">
                <el-button size="small" text type="primary" @click.stop="router.push('/detect/' + scope.row.submissionId)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!data.recent?.length" description="暂无检测记录" :image-size="70" />
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8" :lg="8">
        <el-card shadow="hover" class="mb15">
          <template #header><div class="card-head"><span class="ch-title">疑似率</span><span class="ch-sub">中/高关注占比</span></div></template>
          <div class="ring-hero">
            <RingProgress :value="suspectRate" :color="rateColor" :size="132" label="疑似率" />
          </div>
          <div class="tiles3">
            <div class="t3"><div class="t3-l">已检测</div><div class="t3-v">{{ data.detected || 0 }}</div></div>
            <div class="t3"><div class="t3-l">疑似</div><div class="t3-v" style="color:#f59e0b">{{ data.suspected || 0 }}</div></div>
            <div class="t3"><div class="t3-l">申诉</div><div class="t3-v" style="color:#2e6be6">{{ data.appeals || 0 }}</div></div>
          </div>
        </el-card>

        <el-card shadow="hover">
          <template #header><div class="card-head"><span class="ch-title">关注等级构成</span></div></template>
          <BarMeter v-for="l in levelRows" :key="l.level" :label="l.level" :value="l.pct"
                    :color="colorOf(l.level)" :hint="l.cnt + ' 份'" />
          <el-empty v-if="!levelRows.length" description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts" name="dashboard">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { dashboardApi } from '@/api';
import RingProgress from '@/components/RingProgress.vue';
import BarMeter from '@/components/BarMeter.vue';

const router = useRouter();
const data = ref<any>({});
const cards = computed(() => [
  { label: '已检测作业', value: data.value.detected || 0, icon: 'Document', color: '#2e6be6', bg: '#eaf0fd' },
  { label: '疑似（中/高关注）', value: data.value.suspected || 0, icon: 'Warning', color: '#f87171', bg: '#fdf2f2' },
  { label: '复核认定人工', value: data.value.confirmed || 0, icon: 'CircleCheck', color: '#22c55e', bg: '#effaf3' },
  { label: '申诉工单', value: data.value.appeals || 0, icon: 'ChatDotSquare', color: '#f59e0b', bg: '#fdf6ec' },
]);

const tagOf = (l: string) => (l === '高关注' ? 'danger' : l === '中关注' ? 'warning' : 'success');
const colorOf = (l: string) => (l === '高关注' ? '#f87171' : l === '中关注' ? '#f59e0b' : l === '证据不足' ? '#8a94a6' : '#22c55e');

const suspectRate = computed(() => {
  const d = data.value.detected || 0;
  return d ? Math.round(((data.value.suspected || 0) / d) * 100) : 0;
});
const rateColor = computed(() => (suspectRate.value >= 30 ? '#f87171' : suspectRate.value >= 15 ? '#f59e0b' : '#22c55e'));

const levelRows = computed(() => {
  const total = data.value.detected || 0;
  return (data.value.levelCounts || []).map((l: any) => ({
    level: l.level,
    cnt: l.cnt,
    pct: total ? Math.round((l.cnt / total) * 100) : 0,
  }));
});

onMounted(async () => {
  data.value = (await dashboardApi()) as any;
});
</script>

<style scoped lang="scss">
.stat { display: flex; align-items: center; gap: 15px;
  .stat-icon { width: 46px; height: 46px; border-radius: 14px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 21px; }
  .stat-num { font-size: 30px; font-weight: 800; letter-spacing: -0.03em; line-height: 1.15; }
  .font13 { font-weight: 600; }
}
.card-head { display: flex; align-items: center; }
.card-head .ch-title { font-size: 15px; font-weight: 800; color: var(--ink); }
.card-head .ch-sub { font-size: 12px; color: var(--ink-2); margin-left: auto; }

.ring-hero { display: flex; justify-content: center; padding: 6px 0 14px; }
.tiles3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.t3 { background: #f7f8fa; border-radius: 12px; padding: 10px 8px; text-align: center;
  .t3-l { font-size: 11px; color: var(--ink-2); font-weight: 600; }
  .t3-v { font-size: 20px; font-weight: 800; letter-spacing: -0.02em; color: var(--ink); margin-top: 2px; } }
</style>
