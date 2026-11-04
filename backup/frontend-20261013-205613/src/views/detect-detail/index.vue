<template>
  <div class="detail layout-pd" v-loading="loading">
    <!-- 返回（从工作台或申诉台进来，原路返回） -->
    <div class="mb10">
      <el-button text @click="goBack">
        <el-icon class="mr5"><ArrowLeft /></el-icon>返回
      </el-button>
      <span class="font12 color-info">工作台 → 检测详情</span>
    </div>

    <!-- ① 仅供参考声明条（置顶，不可折叠） -->
    <el-alert
      title="本结果由本地特征分析生成，仅供教师复核参考，不作为处理依据。"
      type="warning" :closable="false" show-icon class="mb15" />

    <div class="page-head flex" style="align-items:center">
      <div class="flex-auto">
        <h2>检测详情</h2>
        <p>{{ data.assignmentTitle || '—' }} · 提交人 {{ data.nameMask }}（{{ data.studentNoMask }}） · {{ data.charCount }} 字</p>
      </div>
      <el-button class="mr10" @click="rerun"><el-icon class="mr5"><Refresh /></el-icon>重新检测</el-button>
      <el-button type="primary" @click="appealVisible = true"><el-icon class="mr5"><ChatDotRound /></el-icon>发起申诉复核</el-button>
    </div>

    <!-- ② 置信度 + 关注等级 -->
    <el-card shadow="hover" class="mb15">
      <el-row :gutter="20" align="middle">
        <el-col :xs="24" :sm="8" :md="8" :lg="7">
          <div class="text-center">
            <el-progress type="dashboard" :percentage="scorePct" :color="riskColor" :width="140">
              <template #default>
                <div class="font30" style="font-weight: 600">{{ scorePct }}%</div>
                <div class="font13 color-info">AI 生成疑似度</div>
              </template>
            </el-progress>
          </div>
        </el-col>
        <el-col :xs="24" :sm="16" :md="16" :lg="17">
          <div class="flex mb10" style="align-items:center;gap:10px">
            <span class="color-info font13">综合判定：</span>
            <el-tag :type="riskTag" size="large">{{ result.riskLevel || '待检测' }}</el-tag>
            <ConfidenceBadge v-if="result.confidence !== undefined && result.confidence !== null" :confidence="result.confidence" />
          </div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="检测任务">{{ result.taskId || '—' }}</el-descriptions-item>
            <el-descriptions-item label="任务状态">{{ result.status || '—' }}</el-descriptions-item>
            <el-descriptions-item label="配置版本">{{ result.configVersion || '—' }}</el-descriptions-item>
            <el-descriptions-item label="是否降级">{{ result.degraded ? '是（纯统计特征，未含模型语义）' : '否' }}</el-descriptions-item>
          </el-descriptions>
          <p class="font13 color-info mt10" style="line-height:1.8">{{ result.summary }}</p>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="15">
      <!-- 正文与证据定位 -->
      <el-col :xs="24" :sm="14" :md="14" :lg="15">
        <el-card shadow="hover">
          <template #header><span style="font-weight: 600">正文与证据定位</span><span class="tip">点击彩色片段查看依据</span></template>
          <EvidenceHighlight :raw-text="data.rawText" :evidences="data.evidences" :active-id="activeId" @select="onSelect" />
        </el-card>
      </el-col>

      <!-- 特征 + 证据 -->
      <el-col :xs="24" :sm="10" :md="10" :lg="9">
        <!-- ③ 特征雷达 + 明细 -->
        <el-card shadow="hover" class="mb15">
          <template #header><span style="font-weight: 600">特征雷达</span><span class="tip">{{ data.features?.length || 0 }} 维</span></template>
          <FeatureRadar v-if="data.features?.length" :features="data.features" />
          <el-empty v-else description="暂无特征" :image-size="70" />
        </el-card>

        <el-card shadow="hover" class="mb15">
          <template #header><span style="font-weight: 600">特征明细</span></template>
          <div v-for="f in data.features" :key="f.featureCode" class="feature-row">
            <div class="flex" style="justify-content: space-between; align-items: center">
              <span style="font-weight: 500">{{ f.featureName }}</span>
              <span class="font13" :style="{ color: scoreColor(f.featureScore) }">{{ Math.round((f.featureScore || 0) * 100) }}%</span>
            </div>
            <el-progress :percentage="Math.round((f.featureScore || 0) * 100)" :color="scoreColor(f.featureScore)" :show-text="false" :stroke-width="6" class="mt5" />
            <div class="font12 color-info mt5">
              原始值 {{ f.featureValue }} · 权重 {{ f.weight }} · 来源 {{ f.source }}
            </div>
            <div class="font12 color-info mt5">{{ f.featureText }}</div>
          </div>
          <el-empty v-if="!data.features?.length" description="暂无特征" :image-size="70" />
        </el-card>

        <!-- ④ 证据区 -->
        <el-card shadow="hover" class="mb15">
          <template #header><span style="font-weight: 600">可溯源证据</span><span class="tip">{{ data.evidences?.length || 0 }} 条</span></template>
          <div class="ev-list">
            <EvidenceCard v-for="(e, i) in data.evidences" :key="i" :evidence="e"
                          :class="{ 'is-active': activeId === e.evidenceId }" @locate="onSelect" />
          </div>
          <el-empty v-if="!data.evidences?.length" description="本次检测未产出可定位证据" :image-size="70" />
        </el-card>

        <!-- ⑤ 复核建议 -->
        <el-card shadow="hover">
          <template #header><span style="font-weight: 600">复核建议</span></template>
          <p class="font13" style="line-height: 1.9; margin: 0">{{ result.reviewSuggestion }}</p>
          <el-divider style="margin: 12px 0" />
          <p class="font12 color-info" style="margin: 0">条文引用与相似案例为 P1 能力（RAG / ES），当前版本未启用。</p>
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
import FeatureRadar from '@/components/FeatureRadar.vue';
import EvidenceCard from '@/components/EvidenceCard.vue';
import ConfidenceBadge from '@/components/ConfidenceBadge.vue';

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
const riskColor = computed(() => (result.value.riskLevel === '高关注' ? '#f56c6c' : result.value.riskLevel === '中关注' ? '#e6a23c' : '#67c23a'));

const scoreColor = (s: number) => (s >= 0.6 ? '#f56c6c' : s >= 0.35 ? '#e6a23c' : '#67c23a');

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

/** 原路返回（从工作台进就回工作台，从申诉台进就回申诉台）；无历史时兜底到工作台。 */
const goBack = () => {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push('/workbench');
  }
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
.page-head { margin-bottom: 15px; h2 { margin: 0; font-size: 18px; } p { margin: 6px 0 0; font-size: 13px; color: var(--el-text-color-secondary); } }
.tip { margin-left: 8px; color: var(--el-text-color-secondary); font-size: 12px; }
.feature-row { padding: 10px 0; border-bottom: 1px solid var(--next-border-color-light); &:last-child { border-bottom: 0; } }
.ev-list { display: flex; flex-direction: column; gap: 12px; }
.ev-list :deep(.ev-card.is-active) { box-shadow: 0 0 0 2px var(--el-color-primary-light-5); }
</style>
