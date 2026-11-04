<template>
  <div class="ev-card" :class="{ warn: warn }" @click="$emit('locate', evidence)">
    <div class="ev-h">
      <el-icon><WarningFilled /></el-icon>
      {{ featureName }}
      <span class="font12 color-info" style="margin-left:auto">[{{ evidence.beginPos }}, {{ evidence.endPos }})</span>
    </div>
    <div class="ev-q">「{{ evidence.snippet }}」</div>
    <div class="ev-k">判定依据</div>
    <div class="ev-t">{{ evidence.reason }}</div>
  </div>
</template>

<script setup lang="ts" name="EvidenceCard">
import { computed } from 'vue';

const props = defineProps<{ evidence: any }>();
defineEmits(['locate']);

const NAMES: Record<string, string> = {
  PERPLEXITY: '文本困惑度', PARAPHRASE_TRACE: '改写痕迹', WRITING_TRAJECTORY: '行文轨迹',
  BURSTINESS: '句长突发度', LEXICAL_DIVERSITY: '词汇丰富度', STRUCT_PATTERN: '结构规律性',
};
const featureName = computed(() => NAMES[props.evidence.featureCode] || props.evidence.featureCode);
const warn = computed(() => props.evidence.featureCode === 'WRITING_TRAJECTORY');
</script>

<style scoped lang="scss">
.ev-card {
  border: 1px solid var(--el-color-danger-light-7); background: var(--el-color-danger-light-9);
  border-radius: var(--r-block); padding: 14px; cursor: pointer; transition: box-shadow .2s ease;
  &:hover { box-shadow: var(--sh-soft); }
  &.warn { border-color: var(--el-color-warning-light-7); background: var(--el-color-warning-light-9); }
  .ev-h { display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 600; color: var(--el-color-danger); margin-bottom: 8px; }
  &.warn .ev-h { color: var(--el-color-warning); }
  .ev-q { font-size: 13px; line-height: 1.8; padding-left: 10px; border-left: 2px solid currentColor; margin-bottom: 10px; }
  .ev-k { font-size: 12px; color: var(--el-text-color-secondary); margin-bottom: 3px; }
  .ev-t { font-size: 13px; line-height: 1.75; color: var(--el-text-color-regular); }
}
</style>
