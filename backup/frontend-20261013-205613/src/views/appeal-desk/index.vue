<template>
  <div class="appeal-desk layout-pd">
    <div class="page-head">
      <h2>申诉处理台</h2>
      <p>复核结论将回写工单并留痕；若标记「阈值需调整」，该案例进入配置迭代候选池。</p>
    </div>

    <el-card shadow="hover">
      <template #header>
        <div class="flex" style="align-items: center">
          <span style="font-weight: 600">申诉工单</span>
          <el-radio-group v-model="status" class="ml15" @change="load">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="待受理">待受理</el-radio-button>
            <el-radio-button label="已认定">已认定</el-radio-button>
            <el-radio-button label="已驳回">已驳回</el-radio-button>
            <el-radio-button label="已归档">已归档</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="学生 / 作业" min-width="260">
          <template #default="scope">
            <div style="font-weight: 500">{{ scope.row.nameMask }} · {{ scope.row.assignmentTitle }}</div>
            <div class="font12 color-info">{{ scope.row.statement }}</div>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.riskLevel" :type="tagOf(scope.row.riskLevel)">{{ scope.row.riskLevel }}</el-tag>
            <span v-else class="color-info">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="scope"><el-tag :type="statusTag(scope.row.status)">{{ scope.row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="170" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="scope">
            <el-button size="small" text type="primary" @click="open(scope.row)">复核</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="mt15" :pager-count="5" :page-sizes="[10, 20, 30]" background
        v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="onSizeChange" @current-change="load" />
      <el-empty v-if="!loading && !rows.length" description="暂无申诉工单" />
    </el-card>

    <el-dialog v-model="visible" title="申诉复核" width="760px" draggable destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="2" size="small" border class="mb15">
          <el-descriptions-item label="作业">{{ detail.assignmentTitle }}</el-descriptions-item>
          <el-descriptions-item label="提交人">{{ detail.nameMask }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="statusTag(detail.status)">{{ detail.status }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="发起时间">{{ detail.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="学生陈述" :span="2">{{ detail.statement }}</el-descriptions-item>
          <el-descriptions-item label="佐证材料" :span="2">
            <span v-if="detail.attachKeys?.length">{{ detail.attachKeys.join('、') }}</span>
            <span v-else class="color-info">无</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="flex" style="gap: 20px; align-items: flex-start">
          <el-card shadow="never" class="flex-auto" style="background: var(--next-bg-main-color)">
            <div style="font-weight: 600; margin-bottom: 10px">处理流程</div>
            <AppealTimeline :timeline="detail.timeline || []" />
          </el-card>
          <el-card shadow="never" class="flex-auto" style="background: var(--next-bg-main-color)">
            <div style="font-weight: 600; margin-bottom: 10px">复核依据</div>
            <div class="mb10"><el-button size="small" text type="primary" @click="gotoDetail">查看检测详情与证据 →</el-button></div>
            <el-divider />
            <el-form label-position="top" size="small">
              <el-form-item label="复核结论">
                <el-select v-model="form.reviewConclusion" class="w100" placeholder="请选择">
                  <el-option label="认定为人工撰写" value="认定为人工撰写" />
                  <el-option label="证据不足" value="证据不足" />
                  <el-option label="维持关注" value="维持关注" />
                </el-select>
              </el-form-item>
              <el-form-item label="复核说明">
                <el-input v-model="form.reviewNote" type="textarea" :rows="3" placeholder="须写清依据哪条证据、如何排除" />
              </el-form-item>
              <el-form-item>
                <el-checkbox v-model="form.thresholdFeedback" label="阈值需调整" border size="small" />
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </template>
      <template #footer>
        <span v-if="!canReview" class="font12 color-warning mr10">当前角色无申诉认定权限（需教研室 / 学院 / 管理员）</span>
        <el-button @click="visible = false">关 闭</el-button>
        <el-button type="primary" :disabled="!canReview || (detail && detail.status === '已归档')" @click="doReview">提 交 复 核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="appealDesk">
import { onMounted, reactive, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { appealDetailApi, appealListApi, appealReviewApi } from '@/api';
import AppealTimeline from '@/components/AppealTimeline.vue';
import { useAuthStore } from '@/store/auth';

const router = useRouter();
const auth = useAuthStore();
const canReview = computed(() => auth.hasRole('CHAIR', 'COLLEGE', 'ADMIN'));
const rows = ref<any[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const status = ref('');
const loading = ref(false);
const visible = ref(false);
const detail = ref<any>(null);
const form = reactive({ reviewConclusion: '', reviewNote: '', thresholdFeedback: false });

const tagOf = (l: string) => (l === '高关注' ? 'danger' : l === '中关注' ? 'warning' : 'success');
const statusTag = (s: string) =>
  s === '已认定' ? 'success' : s === '已驳回' ? 'danger' : s === '已归档' ? 'info' : 'warning';

const load = async () => {
  loading.value = true;
  try {
    const data: any = await appealListApi(status.value, pageNum.value, pageSize.value);
    rows.value = data.records;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
};

const onSizeChange = (val: number) => { pageSize.value = val; pageNum.value = 1; load(); };

const open = async (row: any) => {
  detail.value = await appealDetailApi(row.appealId);
  form.reviewConclusion = '';
  form.reviewNote = '';
  form.thresholdFeedback = false;
  visible.value = true;
};

const gotoDetail = () => {
  if (detail.value?.submissionId) router.push(`/detect/${detail.value.submissionId}`);
};

const doReview = async () => {
  if (!form.reviewConclusion) return ElMessage.warning('请选择复核结论');
  await appealReviewApi({
    appealId: detail.value.appealId,
    reviewConclusion: form.reviewConclusion,
    reviewNote: form.reviewNote,
    thresholdFeedback: form.thresholdFeedback ? '阈值需调整' : null,
  });
  ElMessage.success('复核已提交');
  visible.value = false;
  load();
};

onMounted(load);
</script>

<style scoped lang="scss">
.page-head { margin-bottom: 15px; h2 { margin: 0; font-size: 18px; } p { margin: 6px 0 0; font-size: 13px; color: var(--el-text-color-secondary); } }
</style>
