<template>
  <div class="workbench layout-pd">
    <div class="page-head">
      <h2>教师工作台</h2>
      <p>上传课程作业 → 本地特征引擎检测 → 查看可解释证据。所有检测在本地完成，作业原文不出校园。</p>
    </div>

    <!-- 指标砖（对齐参考图顶部的一排指标） -->
    <el-row :gutter="16" class="mb15">
      <el-col :xs="12" :sm="6" v-for="t in tiles" :key="t.label">
        <el-card shadow="hover">
          <div class="tile-stat">
            <div class="ts-icon" :style="{ background: t.bg, color: t.color }">
              <el-icon><component :is="t.icon" /></el-icon>
            </div>
            <div class="flex-auto">
              <div class="ts-label">{{ t.label }}</div>
              <div class="ts-value" :style="{ color: t.color }">{{ t.value }}</div>
              <div class="ts-bar"><i :style="{ width: t.pct + '%', background: t.color }"></i></div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="mb15">
      <div class="search-bar">
        <el-select v-model="courseId" placeholder="选择课程" style="width: 220px" @change="onCourseChange">
          <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
        </el-select>
        <el-select v-model="assignmentId" placeholder="选择作业" style="width: 280px" class="ml10" @change="loadList">
          <el-option v-for="a in assignments" :key="a.assignmentId" :label="a.title" :value="a.assignmentId" />
        </el-select>
        <el-radio-group v-model="riskLevel" class="ml10" @change="loadList">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="低关注">低关注</el-radio-button>
          <el-radio-button value="中关注">中关注</el-radio-button>
          <el-radio-button value="高关注">高关注</el-radio-button>
        </el-radio-group>
        <el-button type="success" class="ml10" @click="openUpload"><el-icon class="mr5"><FolderAdd /></el-icon>批量上传</el-button>
        <el-button class="ml10" :loading="exporting" @click="doExport"><el-icon class="mr5"><Download /></el-icon>导出 Excel</el-button>
        <span class="tip">仅显示所选作业下的提交；列表已对学号/姓名脱敏</span>
      </div>
    </el-card>

    <el-card shadow="hover">
      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="提交人" width="150">
          <template #default="scope">
            <div>{{ scope.row.nameMask }}</div>
            <div class="font12 color-info">{{ scope.row.studentNoMask }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="assignmentTitle" label="作业" min-width="220" show-overflow-tooltip />
        <el-table-column prop="charCount" label="字数" width="90" />
        <el-table-column label="AI 疑似度" width="190">
          <template #default="scope">
            <span v-if="scope.row.riskScore === null" class="color-info font13">待检测</span>
            <el-progress v-else :percentage="Math.round(scope.row.riskScore * 100)" :color="colorOf(scope.row.riskLevel)" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column label="关注等级" width="110">
          <template #default="scope">
            <el-tag v-if="!scope.row.riskLevel" type="info">待检测</el-tag>
            <el-tag v-else :type="tagOf(scope.row.riskLevel)">{{ scope.row.riskLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" text type="primary" @click="goDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="mt15" :pager-count="5" :page-sizes="[10, 20, 30]" background
        v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="onSizeChange" @current-change="loadList" />
      <el-empty v-if="!loading && !rows.length" description="该作业下暂无提交，点「批量上传」导入" />
    </el-card>

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadVisible" title="上传作业" width="640px" draggable>
      <el-form label-width="90px">
        <el-form-item label="检测作业">
          <el-select v-model="uploadForm.assignmentId" class="w100" placeholder="请选择作业">
            <el-option v-for="a in assignments" :key="a.assignmentId" :label="a.title" :value="a.assignmentId" />
          </el-select>
        </el-form-item>
        <el-form-item label="上传方式">
          <el-radio-group v-model="uploadForm.mode">
            <el-radio label="single">单份（指定学生）</el-radio>
            <el-radio label="batch">整班（文件名含学号自动匹配）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="uploadForm.mode === 'single'" label="学生">
          <el-select v-model="uploadForm.studentId" class="w100" placeholder="请选择学生">
            <el-option v-for="s in students" :key="s.studentId" :label="`${s.nameMask} · ${s.studentNo}`" :value="s.studentId" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件">
          <el-upload
            v-model:file-list="uploadFiles" drag :auto-upload="false"
            :multiple="uploadForm.mode === 'batch'"
            :limit="uploadForm.mode === 'batch' ? 50 : 1"
            accept=".docx,.txt,.pdf" style="width: 100%">
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽文件到此处，或 <em>点击选择</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 .docx / .txt / .pdf，单文件 ≤ 20MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadVisible = false">取 消</el-button>
        <el-button type="primary" :loading="uploading" @click="doUpload">上 传 并 检 测</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="workbench">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { assignmentsApi, coursesApi, exportExcelApi, listApi, studentsApi, submitApi, submitBatchApi } from '@/api';

const router = useRouter();
const courses = ref<any[]>([]);
const assignments = ref<any[]>([]);
const students = ref<any[]>([]);
const courseId = ref<number>();
const assignmentId = ref<number>();
const riskLevel = ref('');
const rows = ref<any[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const stats = ref({ total: 0, high: 0, mid: 0, low: 0 });

const uploadVisible = ref(false);
const uploading = ref(false);
const exporting = ref(false);
const uploadFiles = ref<any[]>([]);
const uploadForm = reactive({ assignmentId: undefined as any, mode: 'batch', studentId: undefined as any });

const tiles = computed(() => {
  const t = stats.value.total || 1;
  const pct = (n: number) => Math.round((n / t) * 100);
  return [
    { label: '提交总数', value: stats.value.total, pct: 100, icon: 'Document', color: '#2e6be6', bg: '#eaf0fd' },
    { label: '高关注', value: stats.value.high, pct: pct(stats.value.high), icon: 'Warning', color: '#f87171', bg: '#fdf2f2' },
    { label: '中关注', value: stats.value.mid, pct: pct(stats.value.mid), icon: 'Bell', color: '#f59e0b', bg: '#fdf6ec' },
    { label: '低关注', value: stats.value.low, pct: pct(stats.value.low), icon: 'CircleCheck', color: '#22c55e', bg: '#effaf3' },
  ];
});

const tagOf = (l: string) => (l === '高关注' ? 'danger' : l === '中关注' ? 'warning' : 'success');
const colorOf = (l: string) => (l === '高关注' ? '#f56c6c' : l === '中关注' ? '#e6a23c' : '#67c23a');

onMounted(async () => {
  courses.value = (await coursesApi()) as any;
  if (courses.value.length) {
    courseId.value = courses.value[0].courseId;
    await onCourseChange(courseId.value);
  } else {
    // 兜底：课程列表为空时仍加载全部作业，避免页面空白且无提示
    assignments.value = (await assignmentsApi()) as any;
    if (assignments.value.length) {
      assignmentId.value = assignments.value[0].assignmentId;
      loadList();
    }
  }
  students.value = (await studentsApi()) as any;
});

const onCourseChange = async (cid: number) => {
  assignments.value = (await assignmentsApi(cid)) as any;
  if (assignments.value.length) {
    assignmentId.value = assignments.value[0].assignmentId;
    loadList();
  } else {
    rows.value = [];
    total.value = 0;
  }
};

const loadList = async () => {
  if (!assignmentId.value) return;
  loading.value = true;
  try {
    const data: any = await listApi(assignmentId.value, riskLevel.value, pageNum.value, pageSize.value);
    rows.value = data.records;
    total.value = data.total;
    await loadStats();
  } finally {
    loading.value = false;
  }
};

const loadStats = async () => {
  if (!assignmentId.value) return;
  const all: any = await listApi(assignmentId.value, '', 1, 500);
  const recs = all.records || [];
  stats.value = {
    total: all.total,
    high: recs.filter((r: any) => r.riskLevel === '高关注').length,
    mid: recs.filter((r: any) => r.riskLevel === '中关注').length,
    low: recs.filter((r: any) => r.riskLevel === '低关注').length,
  };
};

const onSizeChange = (val: number) => {
  pageSize.value = val;
  pageNum.value = 1;
  loadList();
};

const goDetail = (row: any) => {
  if (row.riskScore === null) {
    ElMessage.info('该提交尚未完成检测');
    return;
  }
  router.push(`/detect/${row.submissionId}`);
};

const openUpload = () => {
  uploadForm.assignmentId = assignmentId.value;
  uploadForm.mode = 'batch';
  uploadForm.studentId = undefined;
  uploadFiles.value = [];
  uploadVisible.value = true;
};

const doExport = async () => {
  if (!assignmentId.value) return ElMessage.warning('请先选择作业');
  exporting.value = true;
  try {
    const blob: any = await exportExcelApi(assignmentId.value);
    const a = document.createElement('a');
    const url = URL.createObjectURL(blob);
    a.href = url;
    const title = assignments.value.find((x) => x.assignmentId === assignmentId.value)?.title || '作业';
    a.download = `${title}_检测结果.xlsx`;
    a.click();
    URL.revokeObjectURL(url);
    ElMessage.success('导出完成（已脱敏）');
  } finally {
    exporting.value = false;
  }
};

const doUpload = async () => {
  if (!uploadForm.assignmentId) return ElMessage.warning('请选择作业');
  const files = uploadFiles.value.map((f) => f.raw).filter(Boolean);
  if (!files.length) return ElMessage.warning('请选择文件');
  const fd = new FormData();
  fd.append('assignmentId', String(uploadForm.assignmentId));
  uploading.value = true;
  try {
    if (uploadForm.mode === 'batch') {
      files.forEach((f) => fd.append('files', f));
      const res: any = await submitBatchApi(fd);
      ElMessage.success(`批量提交完成：成功 ${res.success} / 失败 ${res.failed}`);
      if (res.failed) {
        res.items.filter((i: any) => !i.success).slice(0, 3).forEach((i: any) =>
          ElMessage.warning(`${i.fileName}：${i.message}`));
      }
    } else {
      if (!uploadForm.studentId) { uploading.value = false; return ElMessage.warning('请选择学生'); }
      fd.append('file', files[0]);
      fd.append('studentId', String(uploadForm.studentId));
      await submitApi(fd);
      ElMessage.success('提交成功，正在异步检测');
    }
    uploadVisible.value = false;
    assignmentId.value = uploadForm.assignmentId;
    setTimeout(loadList, 2500);
  } finally {
    uploading.value = false;
  }
};
</script>

<style scoped lang="scss">
.search-bar { display: flex; align-items: center; flex-wrap: wrap; gap: 0; }
.tile-stat { display: flex; align-items: center; gap: 14px;
  .ts-icon { width: 44px; height: 44px; border-radius: 14px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 20px; }
  .ts-label { font-size: 12px; color: var(--ink-2); font-weight: 600; }
  .ts-value { font-size: 27px; font-weight: 800; letter-spacing: -0.03em; line-height: 1.15; }
  .ts-bar { height: 5px; border-radius: 999px; background: #eef0f4; overflow: hidden; margin-top: 8px;
    i { display: block; height: 100%; border-radius: 999px; transition: width .6s ease; } }
}
.tip { margin-left: 10px; color: var(--el-text-color-secondary); font-size: 13px; }
</style>
