import axios from 'axios';
import { ElMessage } from 'element-plus';

/**
 * axios 封装：
 * - 自动带 JWT
 * - 统一解包 Result.data
 * - code !== 200 弹错并 reject；HTTP 401 清 token 跳登录
 */
const service = axios.create({ baseURL: '/api', timeout: 60000 });

service.interceptors.request.use((config) => {
  const token = localStorage.getItem('integrity_token');
  if (token) {
    config.headers = config.headers || {};
    (config.headers as any).Authorization = 'Bearer ' + token;
  }
  return config;
});

service.interceptors.response.use(
  (res) => {
    const body = res.data;
    if (body && typeof body.code !== 'undefined') {
      if (body.code === 200) return body.data;
      ElMessage.error(body.msg || '请求失败');
      return Promise.reject(new Error(body.msg));
    }
    return body;
  },
  (err) => {
    const status = err.response?.status;
    if (status === 401) {
      localStorage.removeItem('integrity_token');
      ElMessage.error('登录已过期，请重新登录');
      if (location.hash !== '#/login') location.hash = '#/login';
    } else {
      ElMessage.error(err.response?.data?.msg || err.message || '网络错误');
    }
    return Promise.reject(err);
  }
);

export default service;
