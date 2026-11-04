import { defineStore } from 'pinia';
import { loginApi } from '@/api';

/**
 * 登录态（Pinia）：token 入 localStorage，用户信息常驻内存。
 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('integrity_token') || '',
    user: JSON.parse(localStorage.getItem('integrity_user') || 'null') as any,
  }),
  getters: {
    isLoggedIn: (s) => !!s.token,
    displayName: (s) => s.user?.name || '',
    role: (s) => s.user?.role || '',
  },
  actions: {
    async login(username: string, password: string) {
      const data: any = await loginApi({ username, password });
      this.token = data.token;
      this.user = { userId: data.userId, username: data.username, name: data.name, role: data.role };
      localStorage.setItem('integrity_token', this.token);
      localStorage.setItem('integrity_user', JSON.stringify(this.user));
    },
    /** 是否属于给定角色之一（用于前端菜单/操作显隐；后端仍独立鉴权）。 */
    hasRole(...roles: string[]) {
      return !!this.user && roles.includes(this.user.role);
    },
    logout() {
      this.token = '';
      this.user = null;
      localStorage.removeItem('integrity_token');
      localStorage.removeItem('integrity_user');
    },
  },
});
