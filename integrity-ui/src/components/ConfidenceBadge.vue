<template>
  <el-tag :type="meta.type" effect="plain">{{ meta.text }}</el-tag>
</template>

<script setup lang="ts" name="ConfidenceBadge">
import { computed } from 'vue';

/** 置信度徽标：高/中/低 + 证据充分性说明。 */
const props = defineProps<{ confidence: number }>();
const meta = computed(() => {
  const c = props.confidence ?? 0;
  if (c >= 0.7) return { type: 'success', text: '置信度 高 · 证据较充分' };
  if (c >= 0.45) return { type: 'warning', text: '置信度 中 · 证据基本充分' };
  return { type: 'info', text: '置信度 低 · 证据不足，仅供参考' };
});
</script>
