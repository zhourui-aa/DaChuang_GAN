<template>
  <div class="layout">
    <!-- 侧栏 -->
    <aside class="layout-aside">
      <div class="logo">
        <div class="logo-mark">鉴</div>
        <div class="logo-name">学鉴</div>
      </div>
      <nav class="menu">
        <button v-for="m in menus" :key="m.path" class="menu-item"
                :class="{ 'is-active': route.path.startsWith(m.path), 'is-locked': m.locked }"
                @click="router.push(m.path)">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span>
          <el-icon v-if="m.locked" class="lock" title="当前角色只读/受限"><Lock /></el-icon>
        </button>
      </nav>
      <div class="aside-foot">
        <span class="dot"></span>本地检测引擎 · 已就绪<br />
        <span style="opacity:.75">数据不出校园 · P0</span>
      </div>
    </aside>

    <!-- 主区 -->
    <div class="layout-main">
      <header class="layout-header">
        <el-icon><Fold /></el-icon>
        <div class="crumb">教师端 / <b>{{ route.meta.title || '' }}</b></div>
        <div class="head-right">
          <el-tag size="small" type="info" effect="plain">{{ roleLabel }}</el-tag>
          <span class="user">
            <el-avatar :size="28" style="background: var(--el-color-primary)">{{ initial }}</el-avatar>
            {{ auth.displayName }}
          </span>
          <el-button text @click="onLogout"><el-icon class="mr5"><SwitchButton /></el-icon>退出</el-button>
        </div>
      </header>
      <main class="layout-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts" name="layout">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const menus = computed(() =>
  [
    { path: '/workbench', title: '教师工作台', icon: 'HomeFilled', roles: [] as string[] },
    { path: '/appeal', title: '申诉处理台', icon: 'ChatDotSquare', roles: [] },
    { path: '/dashboard', title: '课程统计看板', icon: 'DataLine', roles: [] },
    { path: '/config', title: '模型与阈值', icon: 'Setting', roles: ['CHAIR', 'ADMIN'] },
    { path: '/audit', title: '审计日志', icon: 'Tickets', roles: [] },
  ].map((m) => ({ ...m, locked: m.roles.length > 0 && !auth.hasRole(...m.roles) }))
);

const ROLE: Record<string, string> = {
  TEACHER: '任课教师', CHAIR: '教研室', COLLEGE: '学院', ADMIN: '管理员',
};
const roleLabel = computed(() => ROLE[auth.role] || auth.role);
const initial = computed(() => (auth.displayName || '师').charAt(0));

const onLogout = () => {
  auth.logout();
  router.push('/login');
};
</script>

<style scoped lang="scss">
.layout { display: flex; height: 100vh; overflow: hidden; }

.layout-aside {
  width: 220px; flex: 0 0 220px; background: var(--next-bg-menuBar);
  box-shadow: 0 2px 16px rgba(0, 21, 41, 0.06);
  display: flex; flex-direction: column; z-index: 10;
  .logo {
    height: 58px; flex: 0 0 58px; display: flex; align-items: center;
    justify-content: center; gap: 9px;
    .logo-mark {
      width: 26px; height: 26px; border-radius: 5px; background: var(--el-color-primary);
      color: #fff; display: grid; place-items: center; font-size: 15px; font-weight: 700;
    }
    .logo-name { font-size: 16px; font-weight: 700; color: var(--next-bg-menuBarTitle); letter-spacing: 1px; }
  }
  .menu { flex: 1; overflow-y: auto; padding: 12px 0; }
  .menu-item {
    display: flex; align-items: center; gap: 10px; height: 44px; padding: 0 14px;
    width: calc(100% - 20px); margin: 2px 10px; border-radius: 10px; border: 0;
    background: transparent; color: var(--next-bg-menuBarColor); font-size: 14px;
    cursor: pointer; text-align: left; transition: background 0.3s ease, color 0.3s ease;
    &:hover { background: var(--next-bg-menuBarActiveColor); }
    &.is-active { background: var(--next-bg-menuBarActiveColor); color: var(--next-bg-menuBarTitle); font-weight: 500; }
    &.is-locked { opacity: 0.65; }
    .lock { margin-left: auto; font-size: 12px; opacity: 0.7; }
    .el-icon { font-size: 16px; }
  }
  .aside-foot {
    padding: 12px 20px; border-top: 1px solid rgba(255, 255, 255, 0.08);
    font-size: 12px; color: var(--next-bg-menuBarColor); opacity: 0.8; line-height: 1.8;
    .dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; background: #67c23a; margin-right: 6px; }
  }
}

.layout-main { flex: 1; min-width: 0; display: flex; flex-direction: column; overflow: hidden; }
.layout-header {
  height: 56px; flex: 0 0 56px; background: #fff; border: 0;
  box-shadow: 0 1px 8px rgba(0, 21, 41, 0.045);
  display: flex; align-items: center; padding: 0 18px; gap: 12px; z-index: 9;
  .crumb { font-size: 13px; color: var(--el-text-color-secondary); b { color: var(--el-text-color-primary); font-weight: 500; } }
  .head-right { margin-left: auto; display: flex; align-items: center; gap: 14px; }
  .user { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--el-text-color-regular); }
}
.layout-content { flex: 1; overflow-y: auto; background: var(--next-bg-main-color); }
</style>
