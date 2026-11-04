<template>
  <div class="layout">
    <!-- 侧栏（浅色，对齐参考图） -->
    <aside class="layout-aside">
      <div class="logo">
        <div class="logo-mark">鉴</div>
        <div class="logo-name">学鉴</div>
      </div>
      <nav class="menu">
        <button v-for="m in menus" :key="m.path" class="menu-item"
                :class="{ 'is-active': activePath.startsWith(m.path), 'is-locked': m.locked }"
                @click="router.push(m.path)">
          <span class="mi-icon"><el-icon><component :is="m.icon" /></el-icon></span>
          <span class="mi-text">{{ m.title }}</span>
          <el-icon v-if="m.locked" class="lock" title="当前角色只读/受限"><Lock /></el-icon>
        </button>
      </nav>
      <div class="aside-foot">
        <span class="dot"></span>本地检测引擎 · 已就绪<br />
        <span style="opacity:.7">数据不出校园 · P0</span>
      </div>
    </aside>

    <!-- 主区 -->
    <div class="layout-main">
      <header class="layout-header">
        <div class="crumb">
          <span class="crumb-root">教师端</span>
          <span class="crumb-sep">/</span>
          <b>{{ route.meta.title || '' }}</b>
        </div>
        <div class="head-right">
          <span class="role-pill">{{ roleLabel }}</span>
          <div class="user">
            <span class="avatar">{{ initial }}</span>
            <span class="uname">{{ auth.displayName }}</span>
          </div>
          <el-button text circle @click="onLogout" title="退出登录"><el-icon><SwitchButton /></el-icon></el-button>
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

const activePath = computed(() => (route.meta.activeMenu as string) || route.path);

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

/* ---------- 侧栏 ---------- */
.layout-aside {
  width: 236px; flex: 0 0 236px; background: #fff;
  box-shadow: 0 0 24px rgba(20, 30, 60, 0.05);
  display: flex; flex-direction: column; z-index: 10;
  .logo {
    height: 68px; flex: 0 0 68px; display: flex; align-items: center; gap: 10px; padding: 0 22px;
    .logo-mark {
      width: 34px; height: 34px; border-radius: 11px; background: var(--el-color-primary);
      color: #fff; display: grid; place-items: center; font-size: 17px; font-weight: 800;
      box-shadow: 0 6px 16px rgba(46, 107, 230, 0.32);
    }
    .logo-name { font-size: 18px; font-weight: 800; letter-spacing: -0.02em; color: var(--ink); }
  }
  .menu { flex: 1; overflow-y: auto; padding: 6px 14px; }
  .menu-item {
    display: flex; align-items: center; gap: 11px; height: 46px; padding: 0 12px; width: 100%;
    margin-bottom: 2px; border-radius: 12px; border: 0; background: transparent; cursor: pointer;
    color: var(--ink-2); font-size: 13.5px; font-weight: 600; text-align: left;
    transition: background 0.2s ease, color 0.2s ease;
    &:hover { background: #f7f8fa; color: var(--ink); }
    &.is-active {
      background: var(--el-color-primary-light-9); color: var(--el-color-primary);
      .mi-icon { background: var(--el-color-primary); color: #fff; }
    }
    &.is-locked { opacity: 0.6; }
    .mi-icon {
      width: 28px; height: 28px; flex: 0 0 28px; border-radius: 9px; display: grid; place-items: center;
      background: #f2f4f8; color: var(--ink-2); transition: background 0.2s ease, color 0.2s ease;
      font-size: 15px;
    }
    .lock { margin-left: auto; font-size: 13px; opacity: 0.7; }
  }
  .aside-foot {
    padding: 14px 22px; border-top: 1px solid var(--next-border-color-light);
    font-size: 11.5px; color: var(--ink-2); line-height: 1.9;
    .dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; background: var(--ok); margin-right: 6px; }
  }
}

/* ---------- 主区 ---------- */
.layout-main { flex: 1; min-width: 0; display: flex; flex-direction: column; overflow: hidden; }
.layout-header {
  height: 64px; flex: 0 0 64px; background: #fff; border: 0;
  box-shadow: 0 1px 0 var(--next-border-color-light);
  display: flex; align-items: center; padding: 0 22px; gap: 12px; z-index: 9;
  .crumb { font-size: 12.5px; color: var(--ink-2); display: flex; align-items: center; gap: 6px; }
  .crumb-root { color: var(--ink-2); }
  .crumb-sep { color: #ccd2dd; }
  .crumb b { color: var(--ink); font-weight: 700; font-size: 14px; }
  .head-right { margin-left: auto; display: flex; align-items: center; gap: 12px; }
  .role-pill {
    background: var(--el-color-primary-light-9); color: var(--el-color-primary);
    border-radius: 999px; padding: 4px 11px; font-size: 11.5px; font-weight: 700;
  }
  .user { display: flex; align-items: center; gap: 8px; }
  .avatar {
    width: 30px; height: 30px; border-radius: 50%; background: var(--el-color-primary); color: #fff;
    display: grid; place-items: center; font-size: 12px; font-weight: 700;
  }
  .uname { font-size: 13px; font-weight: 600; color: var(--ink); }
}
.layout-content { flex: 1; overflow-y: auto; background: var(--next-bg-main-color); }
</style>
