<template>
  <div class="detail layout-pd" v-loading="loading">
    <!-- 顶栏：返回 + 操作 -->
    <div class="topbar-mini">
      <el-button text @click="goBack"><el-icon class="mr5"><ArrowLeft /></el-icon>返回</el-button>
      <span class="font12 color-info">工作台 → 检测详情</span>
      <div class="flex-auto"></div>
      <el-button @click="rerun"><el-icon class="mr5"><Refresh /></el-icon>重新检测</el-button>
      <el-button type="primary" @click="appealVisible = true"><el-icon class="mr5"><ChatDotRound /></el-icon>发起申诉复核</el-button>
    </div>

    <el-alert title="本结果由本地特征分析生成，仅供教师复核参考，不作为处理依据。"
              type="warning" :closable="false" show-icon class="mb15" />

    <!-- ========== Bento 第一排：正文结构图(hero) + 评分卡 ========== -->
    <el-row :gutter="16">
      <el-col :xs="24" :md="14" :lg="14">
        <el-card shadow="hover" class="h100">
          <template #header>
            <div class="card-head">
              <span class="ch-title">正文结构图</span>
              <span class="ch-sub">{{ data.assignmentTitle }}</span>
            </div>
          </template>
          <DocumentMap :raw-text="data.rawText" :evidences="data.evidences" :active-id="activeId" @select="onSelect" />
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10" :lg="10">
        <el-card shadow="hover" class="h100">
          <template #header>
            <div class="card-head">
              <span class="ch-title">综合判定</span>
              <span class="ch-sub">{{ data.nameMask }} · {{ data.studentNoMask }}</span>
            </div>
          </template>
          <ScoreGauge :value="scorePct" :color="riskColor" :height="200" />
          <div class="verdict-row">
            <el-tag :type="riskTag" size="large">{{ result.riskLevel || '待检测' }}</el-tag>
          </div>
          <div class="conf-row">
            <RingProgress :value="confPct" :color="confColor" :size="92" label="置信度" />
            <div class="conf-text">
              <div class="ct-title">{{ confTitle }}</div>
              <div class="ct-desc">可用特征 {{ data.features?.length || 0 }} 项 · 可定位证据 {{ data.evidences?.length || 0 }} 条</div>
            </div>
          </div>
          <div class="tiles">
            <div class="tile"><div class="t-label">字数</div><div class="t-value">{{ data.charCount }}</div></div>
            <div class="tile"><div class="t-label">特征数</div><div class="t-value">{{ data.features?.length || 0 }}</div></div>
            <div class="tile"><div class="t-label">证据数</div><div class="t-value">{{ data.evidences?.length || 0 }}</div></div>
            <div class="tile"><div class="t-label">配置版本</div><div class="t-value sm">{{ result.configVersion || '—' }}</div></div>
          </div>
          <div class="panel-tint blue mt10 font12" style="line-height:1.8">{{ result.summary }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ========== Bento 第二排：正文高亮 + 特征/证据 ========== -->
    <el-row :gutter="16" class="mt15">
      <el-col :xs="24" :md="14" :lg="14">
        <el-card shadow="hover">
          <template #header>
            <div class="card-head">
              <span class="ch-title">正文与证据定位</span>
              <span class="ch-sub">点击彩色片段查看依据</span>
            </div>
          </template>
          <EvidenceHighlight :raw-text="data.rawText" :evidences="data.evidences" :active-id="activeId" @select="onSelect" />
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10" :lg="10">
        <el-card shadow="hover" class="mb15">
          <template #header><div class="card-head"><span class="ch-title">特征维度</span><span class="ch-sub">{{ data.features?.length || 0 }} 项</span></div></template>
          <BarMeter v-for="f in data.features" :key="f.featureCode"
                    :label="f.featureName" :value="Math.round((f.featureScore || 0) * 100)"
                    :color="scoreColor(f.featureScore)"
                    :hint="`原始值 ${f.featureValue} · 权重 ${f.weight} · ${f.featureText}`" />
          <el-empty v-if="!data.features?.length" description="暂无特征" :image-size="70" />
        </el-card>

        <el-card shadow="hover" class="mb15">
          <template #header><div class="card-head"><span class="ch-title">可溯源证据</span><span class="ch-sub">{{ data.evidences?.length || 0 }} 条</span></div></template>
          <div class="ev-list">
            <EvidenceCard v-for="(e, i) in data.evidences" :key="i" :evidence="e"
                          :class="{ 'is-active': activeId === e.evidenceId }" @locate="onSelect" />
          </div>
          <el-empty v-if="!data.evidences?.length" description="本次检测未产出可定位证据" :image-size="70" />
        </el-card>

        <el-card shadow="hover">
          <template #header><div class="card-head"><span class="ch-title">复核建议</span></div></template>
          <p class="font13" style="line-height:1.9; margin:0">{{ result.reviewSuggestion }}</p>
          <el-divider style="margin:12px 0" />
          <p class="font12 color-info" style="margin:0">条文引用与相似案例为 P1 能力（RAG / ES），当前版本未启用。</p>
        </el-card>
      </el-col>
    </el-row>

    <!-- 申诉对话框 -->
    <el-dialog v-model="appealVisible" title="发起申诉复核" width="640px" draggable>
      <el-form label-width="90px">
        <el-form-item label="关联作业"><el-input :model-value="data.assignmentTitle" readonly /></el-form-item>
        <el-form-item label="申诉理由">
          <el-input v-model="appealForm.statement" type="textarea" :rows="5" maxlength="300" show-word-limit
                    placeholder="请说明写作过程、资料来源，可附草稿、参考文献、写作时间线等佐证材料…" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appealVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitAppeal">提交申诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="detectDetail">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { appealCreateApi, detectRunApi, evidenceApi } from '@/api';
import EvidenceHighlight from '@/components/EvidenceHighlight.vue';
import EvidenceCard from '@/components/EvidenceCard.vue';
import ConfidenceBadge from '@/components/ConfidenceBadge.vue';
import ScoreGauge from '@/components/ScoreGauge.vue';
import DocumentMap from '@/components/DocumentMap.vue';
import RingProgress from '@/components/RingProgress.vue';
import BarMeter from '@/components/BarMeter.vue';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const data = ref<any>({});
const activeId = ref<number>();
const appealVisible = ref(false);
const appealForm = reactive({ statement: '' });

const result = computed(() => data.value.result || {});
const scorePct = computed(() => Math.round((result.value.riskScore || 0) * 100));
const riskTag = computed(() => (result.value.riskLevel === '高关注' ? 'danger' : result.value.riskLevel === '中关注' ? 'warning' : 'success'));
const riskColor = computed(() => (result.value.riskLevel === '高关注' ? '#f87171' : result.value.riskLevel === '中关注' ? '#f59e0b' : '#22c55e'));
const scoreColor = (s: number) => (s >= 0.6 ? '#f87171' : s >= 0.35 ? '#f59e0b' : '#22c55e');

const confPct = computed(() => Math.round((result.value.confidence || 0) * 100));
const confColor = computed(() => (confPct.value >= 70 ? '#22c55e' : confPct.value >= 45 ? '#f59e0b' : '#8a94a6'));
const confTitle = computed(() => (confPct.value >= 70 ? '证据较充分' : confPct.value >= 45 ? '证据基本充分' : '证据不足，仅供参考'));

const load = async () => {
  loading.value = true;
  try {
    data.value = (await evidenceApi(Number(route.params.id))) as any;
  } finally {
    loading.value = false;
  }
};

const onSelect = (ev: any) => { activeId.value = ev.evidenceId; };

const rerun = async () => {
  await detectRunApi(Number(route.params.id));
  ElMessage.success('已重新检测');
  load();
};

const goBack = () => {
  // 只在上一条记录也是本应用内页面时才回退；否则一律回工作台。
  // （window.history.length 会算上整个标签页的历史，直接 back() 可能跳出应用）
  const back = (router.options.history.state as any)?.back as string | undefined;
  if (back && back.startsWith('/') && !back.startsWith('/login')) router.back();
  else router.push('/workbench');
};

const submitAppeal = async () => {
  if (!appealForm.statement) return ElMessage.warning('请填写申诉理由');
  await appealCreateApi({ submissionId: Number(route.params.id), statement: appealForm.statement, attachKeys: [] });
  appealVisible.value = false;
  ElMessage.success('申诉已提交，请到申诉处理台跟进');
  router.push('/appeal');
};

onMounted(load);
</script>

<style scoped lang="scss">
.topbar-mini { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }

.card-head { display: flex; align-items: center; gap: 10px; }
.card-head .ch-title { font-size: 15px; font-weight: 800; color: var(--ink); letter-spacing: -0.01em; }
.card-head .ch-sub { font-size: 12px; color: var(--ink-2); margin-left: auto; }

.verdict-row { display: flex; align-items: center; justify-content: center; gap: 10px; margin-top: -8px; margin-bottom: 14px; }

/* 指标小砖（对齐参考图：小标签 + 大数字） */
.conf-row { display: flex; align-items: center; gap: 16px; background: #f7f8fa; border-radius: var(--r-block); padding: 14px 18px; margin-bottom: 10px; }
.conf-row .conf-text { .ct-title { font-size: 14px; font-weight: 800; color: var(--ink); } .ct-desc { font-size: 11.5px; color: var(--ink-2); margin-top: 4px; line-height: 1.6; } }

.tiles { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
.tile {
  background: #f7f8fa; border-radius: 12px; padding: 10px 8px; text-align: center;
  .t-label { font-size: 11px; color: var(--ink-2); font-weight: 600; }
  .t-value { font-size: 19px; font-weight: 800; color: var(--ink); letter-spacing: -0.02em; margin-top: 2px; }
  .t-value.sm { font-size: 13px; }
}

.feature-row {
  padding: 12px 14px; border-radius: var(--r-block); background: #f7f8fa; margin-bottom: 10px;
  &:last-child { margin-bottom: 0; }
}
.ev-list { display: flex; flex-direction: column; gap: 10px; }
.ev-list :deep(.ev-card.is-active) { box-shadow: 0 0 0 2px var(--el-color-primary-light-5); }
</style>
