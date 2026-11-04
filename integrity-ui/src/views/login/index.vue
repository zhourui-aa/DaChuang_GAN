<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-head">
        <div class="logo-mark">鉴</div>
        <h1>学 鉴</h1>
        <p>课程作业 AI 生成内容检测与学术诚信工具 · 教师端</p>
      </div>
      <el-card shadow="hover">
        <el-form :model="form" label-position="top" @submit.prevent="onSubmit">
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="请输入用户名" clearable @keyup.enter="onSubmit">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password @keyup.enter="onSubmit">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-button type="primary" class="w100" style="height: 38px" :loading="loading" @click="onSubmit">登 录</el-button>
        </el-form>
        <div class="text-center color-info font12 mt15">演示账号：lijianguo / 123456</div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts" name="login">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/store/auth';

const router = useRouter();
const auth = useAuthStore();
const loading = ref(false);
const form = reactive({ username: 'lijianguo', password: '123456' });

const onSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名与密码');
    return;
  }
  loading.value = true;
  try {
    await auth.login(form.username, form.password);
    ElMessage.success('登录成功');
    router.push('/workbench');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh; display: grid; place-items: center; background: var(--next-bg-main-color); padding: 24px;
}
.login-box { width: 420px; }
.login-head {
  text-align: center; margin-bottom: 22px;
  .logo-mark {
    width: 56px; height: 56px; border-radius: 18px; background: var(--el-color-primary); color: #fff;
    display: inline-grid; place-items: center; font-size: 28px; font-weight: 700; margin-bottom: 12px;
    box-shadow: 0 8px 20px rgba(64, 158, 255, 0.35);
  }
  h1 { margin: 0; font-size: 22px; letter-spacing: 2px; }
  p { margin: 8px 0 0; font-size: 13px; color: var(--el-text-color-secondary); }
}
</style>
