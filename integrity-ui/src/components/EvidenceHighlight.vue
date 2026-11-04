<template>
  <div class="raw-text">
    <template v-for="(seg, i) in segments" :key="i">
      <span v-if="seg.ev" class="ev-hl" :class="{ warn: seg.ev.featureCode === 'WRITING_TRAJECTORY', on: activeId === seg.ev.evidenceId }"
            @click="$emit('select', seg.ev)">{{ seg.text }}</span>
      <span v-else>{{ seg.text }}</span>
    </template>
  </div>
</template>

<script setup lang="ts" name="EvidenceHighlight">
import { computed } from 'vue';

/**
 * 证据高亮：把后端返回的 begin_pos/end_pos 渲染到 raw_text 上。
 *
 * <p>前端只做渲染，不重算任何特征；坐标必须与后端 submissions.raw_text 同一坐标系。
 */
const props = withDefaults(defineProps<{ rawText?: string; evidences?: any[]; activeId?: number }>(), {
  rawText: '',
  evidences: () => [],
});
defineEmits(['select']);

const segments = computed(() => {
  const txt = props.rawText || '';
  const evs = [...(props.evidences || [])]
    .filter((e) => e.beginPos >= 0 && e.endPos > e.beginPos && e.endPos <= txt.length)
    .sort((a, b) => a.beginPos - b.beginPos);
  const out: any[] = [];
  let cur = 0;
  for (const e of evs) {
    if (e.beginPos < cur) continue; // 跳过与已渲染区间重叠的证据
    if (e.beginPos > cur) out.push({ text: txt.slice(cur, e.beginPos), ev: null });
    out.push({ text: txt.slice(e.beginPos, e.endPos), ev: e });
    cur = e.endPos;
  }
  if (cur < txt.length) out.push({ text: txt.slice(cur), ev: null });
  return out;
});
</script>
