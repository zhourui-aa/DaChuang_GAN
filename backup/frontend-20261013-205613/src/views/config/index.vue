<template>
  <div class="config layout-pd">
    <div class="page-head">
      <h2>模型与阈值</h2>
      <p>判定阈值支持回看与调整。调整<b>不修改原版本</b>，而是派生新版本并留痕，历史报告按原版本复算。</p>
    </div>

    <el-alert v-if="!canAdjust" type="info" show-icon :closable="false" class="mb15"
              title="当前角色为只读：阈值调整与影响面预览需「教研室 / 管理员」权限，你仍可查看当前配置与调整记录。" />

    <el-row :gutter="15">
      <!-- 当前生效配置 -->
      <el-col :xs="24" :sm="12" :md="12" :lg="10">
        <el-card shadow="hover" class="mb15">
          <template #header><span style="font-weight: 600">当前生效配置</span>
            <el-tag v-if="active.version" type="success" size="small" style="margin-left: 8px">{{ active.version }}</el-tag>
          </template>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="检测引擎">{{ active.modelVersion }}</el-descriptions-item>
            <el-descriptions-item label="短文本门槛">{{ active.minChars }} 字（低于不计综合分）</el-descriptions-item>
            <el-descriptions-item label="存疑阈值"><b class="color-primary">{{ pct(active.mid) }}</b>（超过需人工复核）</el-descriptions-item>
            <el-descriptions-item label="高危阈值"><b class="color-danger">{{ pct(active.high) }}</b>（超过建议约谈）</el-descriptions-item>
          </el-descriptions>
          <div class="mt15 font13 color-info">特征维度（{{ active.features?.length || 0 }} 项）</div>
          <el-table :data="active.features" size="small" class="mt5">
            <el-table-column prop="name" label="特征" />
            <el-table-column prop="code" label="编码" width="150" />
            <el-table-column label="方向" width="80">
              <template #default="scope"><el-tag size="small" :type="scope.row.direction === 'inverse' ? 'warning' : 'info'">{{ scope.row.direction === 'inverse' ? '越低越疑' : '越高越疑' }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="weight" label="权重" width="70" />
          </el-table>
        </el-card>
      </el-col>

      <!-- 调整阈值 -->
      <el-col :xs="24" :sm="12" :md="12" :lg="14">
        <el-card shadow="hover" class="mb15">
          <template #header><span style="font-weight: 600">调整阈值</span><span class="tip">拖动后可先预览影响面，再保存为新版本</span></template>
          <div class="slider-row">
            <div class="k"><span>存疑阈值（中关注）</span><b class="color-primary">{{ form.mid }}%</b></div>
            <el-slider v-model="form.mid" :min="30" :max="85" :step="1" :disabled="!canAdjust" />
          </div>
          <div class="slider-row">
            <div class="k"><span>高危阈值（高关注）</span><b class="color-danger">{{ form.high }}%</b></div>
            <el-slider v-model="form.high" :min="50" :max="98" :step="1" :disabled="!canAdjust" />
          </div>
          <el-form label-width="80px" size="small">
            <el-form-item label="调整说明">
              <el-input v-model="form.note" placeholder="如：本学期误报偏高，上调存疑阈值" />
            </el-form-item>
          </el-form>
          <div class="flex" style="gap: 10px">
            <el-button :disabled="!canAdjust" @click="doPreview" :loading="previewing"><el-icon class="mr5"><View /></el-icon>预览影响面</el-button>
            <el-button type="primary" :disabled="!canAdjust" @click="doAdjust" :loading="adjusting"><el-icon class="mr5"><Check /></el-icon>保存为新版本</el-button>
          </div>

          <div v-if="preview" class="preview mt15">
            <el-divider />
            <div class="font13 mb10">影响面预览（共 {{ preview.total }} 份，等级翻转 {{ preview.changed.length }} 份）</div>
            <div class="flex" style="gap: 8px; flex-wrap: wrap">
              <el-tag v-for="(v, k) in preview.levelCounts" :key="k" :type="tagOf(k)">{{ k }}: {{ v }}</el-tag>
              <el-tag v-if="!Object.keys(preview.levelCounts).length" type="info">无数据</el-tag>
            </div>
            <el-table v-if="preview.changed.length" :data="preview.changed" size="small" class="mt10">
              <el-table-column prop="submissionId" label="提交号" width="90" />
              <el-table-column label="原等级" width="100"><template #default="scope"><el-tag size="small" :type="tagOf(scope.row.oldLevel)">{{ scope.row.oldLevel }}</el-tag></template></el-table-column>
              <el-table-column label="新等级" width="100"><template #default="scope"><el-tag size="small" :type="tagOf(scope.row.newLevel)">{{ scope.row.newLevel }}</el-tag></template></el-table-column>
              <el-table-column label="新疑似度"><template #default="scope">{{ Math.round((scope.row.newScore || 0) * 100) }}%</template></el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 调整记录 -->
    <el-card shadow="hover">
      <template #header><span style="font-weight: 600">调整记录</span><span class="tip">生效时间与操作人全程留痕</span></template>
      <el-table :data="logs" style="width: 100%">
        <el-table-column prop="version" label="新版本" width="90" />
        <el-table-column prop="baseVersion" label="基准版本" width="90" />
        <el-table-column label="阈值变化" min-width="220">
          <template #default="scope">
            <span class="font12 color-info">{{ thr(scope.row.oldThreshold) }}</span>
            <el-icon class="mr5" style="margin: 0 6px"><Right /></el-icon>
            <span class="font12 color-primary">{{ thr(scope.row.newThreshold) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="110" />
        <el-table-column prop="note" label="说明" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="生效时间" width="170" />
      </el-table>
      <el-empty v-if="!logs.length" description="暂无调整记录" :image-size="70" />
    </el-card>
  </div>
</template>

<script setup lang="ts" name="config">
import { onMounted, reactive, ref, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { configActiveApi, configAdjustApi, configLogsApi, configPreviewApi } from '@/api';
import { useAuthStore } from '@/store/auth';

const auth = useAuthStore();
const canAdjust = computed(() => auth.hasRole('CHAIR', 'ADMIN'));

const active = ref<any>({});
const logs = ref<any[]>([]);
const preview = ref<any>(null);
const previewing = ref(false);
const adjusting = ref(false);
const form = reactive({ mid: 60, high: 80, note: '' });

const pct = (v: number) => (v === undefined || v === null ? '—' : Math.round(v * 100) + '%');
const tagOf = (l: string) => (l === '高关注' ? 'danger' : l === '中关注' ? 'warning' : l === '证据不足' ? 'info' : 'success');

const thr = (json: string) => {
  try { const o = JSON.parse(json); return `存疑 ${Math.round(o.mid * 100)}% / 高危 ${Math.round(o.high * 100)}%`; }
  catch { return '—'; }
};

const load = async () => {
  active.value = await configActiveApi();
  logs.value = (await configLogsApi()) as any;
  form.mid = Math.round((active.value.mid || 0.6) * 100);
  form.high = Math.round((active.value.high || 0.8) * 100);
  preview.value = null;
};

const payload = () => ({ mid: form.mid / 100, high: form.high / 100, note: form.note });

const doPreview = async () => {
  previewing.value = true;
  try { preview.value = await configPreviewApi(payload()); }
  finally { previewing.value = false; }
};

const doAdjust = async () => {
  if (form.mid >= form.high) return ElMessage.warning('存疑阈值必须小于高危阈值');
  await ElMessageBox.confirm('将派生一个新版本并立即生效，历史报告仍按原版本可回看，确定吗？', '确认调整', { type: 'warning' });
  adjusting.value = true;
  try {
    const v: any = await configAdjustApi(payload());
    ElMessage.success('已生成新版本 ' + v.version);
    await load();
  } finally { adjusting.value = false; }
};

onMounted(load);
</script>

<style scoped lang="scss">
.page-head { margin-bottom: 15px; h2 { margin: 0; font-size: 18px; } p { margin: 6px 0 0; font-size: 13px; color: var(--el-text-color-secondary); } }
.tip { margin-left: 8px; color: var(--el-text-color-secondary); font-size: 12px; }
.slider-row { margin-bottom: 10px; .k { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 4px; } }
.preview { background: var(--next-bg-main-color); border-radius: var(--r-block); padding: 12px 14px; }
</style>
