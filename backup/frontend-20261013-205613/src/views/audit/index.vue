<template>
  <div class="audit layout-pd">
    <div class="page-head">
      <h2>审计日志</h2>
      <p>所有对检测、报告、申诉的敏感访问均在此留痕，用于越权排查与学期末复盘。</p>
    </div>

    <el-alert v-if="isTeacher" type="info" show-icon :closable="false" class="mb15"
              title="按最小必要原则，任课教师仅可查看本人操作记录；教研室 / 学院 / 管理员可查看全部。" />

    <el-card shadow="hover">
      <div class="search-bar mb15">
        <el-select v-model="action" placeholder="全部动作" style="width: 200px" clearable @change="reload">
          <el-option label="查看检测详情" value="VIEW_DETECT" />
          <el-option label="导出报告" value="EXPORT_REPORT" />
          <el-option label="复核申诉" value="REVIEW_APPEAL" />
        </el-select>
        <el-input v-model="keyword" placeholder="按角色/动作关键字" style="width: 220px" class="ml10" clearable @keyup.enter="reload" />
        <el-button type="primary" class="ml10" @click="reload"><el-icon class="mr5"><Search /></el-icon>查询</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column prop="logId" label="ID" width="80" />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column label="角色" width="110">
          <template #default="scope"><el-tag size="small" effect="plain">{{ roleLabel(scope.row.operatorRole) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="动作" width="150">
          <template #default="scope"><el-tag size="small" :type="actionTag(scope.row.action)">{{ actionLabel(scope.row.action) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="targetSubmission" label="目标提交" width="110">
          <template #default="scope">{{ scope.row.targetSubmission ?? '—' }}</template>
        </el-table-column>
        <el-table-column prop="ip" label="来源 IP" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="180" />
      </el-table>
      <el-pagination class="mt15" :pager-count="5" :page-sizes="[10, 20, 50]" background
        v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="onSizeChange" @current-change="load" />
      <el-empty v-if="!loading && !rows.length" description="暂无审计记录" />
    </el-card>
  </div>
</template>

<script setup lang="ts" name="audit">
import { onMounted, ref, computed } from 'vue';
import { auditListApi } from '@/api';
import { useAuthStore } from '@/store/auth';

const auth = useAuthStore();
const isTeacher = computed(() => auth.role === 'TEACHER');

const rows = ref<any[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const action = ref('');
const keyword = ref('');
const loading = ref(false);

const ROLE: Record<string, string> = { TEACHER: '任课教师', CHAIR: '教研室', COLLEGE: '学院', ADMIN: '管理员', ANONYMOUS: '未登录' };
const roleLabel = (r: string) => ROLE[r] || r;

const ACTION: Record<string, string> = { VIEW_DETECT: '查看检测详情', EXPORT_REPORT: '导出报告', REVIEW_APPEAL: '复核申诉' };
const actionLabel = (a: string) => ACTION[a] || a;
const actionTag = (a: string) => (a === 'EXPORT_REPORT' ? 'warning' : a === 'REVIEW_APPEAL' ? 'success' : 'info');

const load = async () => {
  loading.value = true;
  try {
    const data: any = await auditListApi(action.value, keyword.value, pageNum.value, pageSize.value);
    rows.value = data.records;
    total.value = data.total;
  } finally { loading.value = false; }
};

const reload = () => { pageNum.value = 1; load(); };
const onSizeChange = (val: number) => { pageSize.value = val; pageNum.value = 1; load(); };

onMounted(load);
</script>

<style scoped lang="scss">
.page-head { margin-bottom: 15px; h2 { margin: 0; font-size: 18px; } p { margin: 6px 0 0; font-size: 13px; color: var(--el-text-color-secondary); } }
.search-bar { display: flex; align-items: center; flex-wrap: wrap; }
</style>
