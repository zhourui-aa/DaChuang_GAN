<template>
  <div class="doc-map">
    <div class="dm-legend">
      <span class="lg"><i class="dot red"></i>命中证据</span>
      <span class="lg"><i class="dot amber"></i>轻微</span>
      <span class="lg"><i class="dot grey"></i>无证据</span>
      <span class="lg right">共 {{ (evidences || []).length }} 处热点</span>
    </div>

    <div class="dm-spine">
      <div v-for="(p, i) in paras" :key="i" class="dm-row">
        <!-- 段序 -->
        <div class="dm-idx" :class="{ hot: p.evs.length }">{{ i + 1 }}</div>

        <!-- 段落本体 -->
        <div class="dm-body" :class="{ hot: p.evs.length, active: p.evs.some(e => e.evidenceId === activeId) }"
             @click="p.evs[0] && $emit('select', p.evs[0])">
          <div class="dm-text">{{ p.preview }}</div>
          <div class="dm-meta">
            <div class="dm-bar"><i :style="{ width: p.riskPct + '%', background: barColor(p.riskPct) }"></i></div>
            <span class="dm-risk" :style="{ color: barColor(p.riskPct) }">{{ p.riskPct }}%</span>
          </div>
        </div>

        <!-- 热点引脚 -->
        <div class="dm-pins">
          <button v-for="e in p.evs" :key="e.evidenceId" class="dm-pin"
                  :class="{ on: e.evidenceId === activeId, warn: e.featureCode === 'WRITING_TRAJECTORY' }"
                  :title="e.featureCode"
                  @click.stop="$emit('select', e)">
            {{ e.no }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="DocumentMap">
import { computed } from 'vue';

/**
 * 正文结构图（hero 部件）。
 *
 * <p>把论文正文抽象成「段落骨架 + 证据热点」，对应参考图里带热点标记的人体结构：
 * 每个段落是一段脊柱，命中证据的地方挂一个编号引脚，点击联动右侧证据卡。
 */
const props = withDefaults(defineProps<{ rawText?: string; evidences?: any[]; activeId?: number }>(), {
  rawText: '',
  evidences: () => [],
});
defineEmits(['select']);

const paras = computed(() => {
  const text = props.rawText || '';
  const evs = [...(props.evidences || [])];
  const evsByNo = evs.map((e, i) => ({ ...e, no: i + 1 }));

  // 按空行切段，并记录每段在原文中的区间
  const out: any[] = [];
  const re = /[^\n]+(?:\n(?!\n)[^\n]+)*/g;
  let m: RegExpExecArray | null;
  while ((m = re.exec(text)) !== null) {
    const begin = m.index;
    const end = begin + m[0].length;
    const hit = evsByNo.filter((e) => e.beginPos < end && e.endPos > begin);
    const risk = hit.length ? Math.min(100, hit.reduce((s, e) => s + 30, 10)) : 0;
    out.push({
      preview: m[0].replace(/\s+/g, ' ').slice(0, 42) + (m[0].length > 42 ? '…' : ''),
      evs: hit,
      riskPct: risk,
    });
  }
  return out;
});

const barColor = (p: number) => (p >= 60 ? '#f87171' : p >= 30 ? '#f59e0b' : p > 0 ? '#fbbf24' : '#22c55e');
</script>

<style scoped lang="scss">
.doc-map { display: flex; flex-direction: column; gap: 10px; }

.dm-legend {
  display: flex; align-items: center; gap: 14px; font-size: 11.5px; color: var(--ink-2);
  .lg { display: flex; align-items: center; gap: 5px; }
  .lg.right { margin-left: auto; }
  .dot { width: 7px; height: 7px; border-radius: 50%; display: inline-block; }
  .dot.red { background: #f87171; } .dot.amber { background: #f59e0b; } .dot.grey { background: #d8dce5; }
}

.dm-spine { display: flex; flex-direction: column; gap: 8px; max-height: 420px; overflow-y: auto; padding-right: 4px; }

.dm-row { display: flex; align-items: center; gap: 10px; }

.dm-idx {
  width: 26px; height: 26px; flex: 0 0 26px; border-radius: 9px; display: grid; place-items: center;
  background: #f2f4f8; color: var(--ink-2); font-size: 11.5px; font-weight: 700;
  &.hot { background: #fdf2f2; color: #d05959; }
}

.dm-body {
  flex: 1; min-width: 0; background: #f7f8fa; border-radius: var(--r-block); padding: 10px 13px;
  cursor: default; transition: background 0.2s ease, box-shadow 0.2s ease;
  &.hot { background: #fdf2f2; cursor: pointer; }
  &.active { box-shadow: 0 0 0 2px var(--el-color-primary-light-7); }
  &:hover.hot { box-shadow: var(--sh-soft); }
  .dm-text { font-size: 12.5px; color: var(--el-text-color-regular); line-height: 1.6; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .dm-meta { display: flex; align-items: center; gap: 8px; margin-top: 7px; }
  .dm-bar { flex: 1; height: 5px; border-radius: 999px; background: #eef0f4; overflow: hidden;
    i { display: block; height: 100%; border-radius: 999px; transition: width 0.5s ease; } }
  .dm-risk { font-size: 11px; font-weight: 700; font-family: Manrope, monospace; }
}

.dm-pins { width: 30px; flex: 0 0 30px; display: flex; flex-direction: column; gap: 5px; align-items: center; }
.dm-pin {
  width: 24px; height: 24px; border-radius: 50%; border: 0; cursor: pointer;
  background: var(--el-color-primary); color: #fff; font-size: 11px; font-weight: 800;
  box-shadow: 0 3px 10px rgba(46, 107, 230, 0.35);
  transition: transform 0.15s ease, background 0.15s ease;
  &.warn { background: #f59e0b; box-shadow: 0 3px 10px rgba(245, 158, 11, 0.35); }
  &:hover { transform: scale(1.14); }
  &.on { outline: 3px solid var(--el-color-primary-light-8); }
}
</style>
