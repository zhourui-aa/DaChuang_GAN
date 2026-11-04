<template>
  <div class="ev-card" :class="{ warn: warn }" @click="$emit('locate', evidence)">
    <div class="ev-h">
      <span class="ev-dot"></span>
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
  background: var(--danger-bg); border-radius: var(--r-block); padding: 14px 16px; cursor: pointer;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  &:hover { box-shadow: var(--sh-soft); transform: translateY(-1px); }
  &.warn { background: var(--warn-bg); }
  .ev-h {
    display: flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 700;
    color: #d05959; margin-bottom: 10px;
    .ev-dot { width: 8px; height: 8px; border-radius: 50%; background: currentColor; }
  }
  &.warn .ev-h { color: #c98a1a; }
  .ev-q {
    font-size: 13px; line-height: 1.85; padding-left: 11px; border-left: 2px solid currentColor;
    margin-bottom: 12px; color: var(--ink);
  }
  .ev-k { font-size: 11px; color: var(--ink-2); font-weight: 700; letter-spacing: 0.06em; margin-bottom: 4px; }
  .ev-t { font-size: 12.5px; line-height: 1.8; color: var(--el-text-color-regular); }
}
</style>
