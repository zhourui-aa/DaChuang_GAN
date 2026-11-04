import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import App from './App.vue';
import router from './router';
import './styles/index.scss';

const app = createApp(App);

// 图标同时注册两种命名：
//  - ele-Xxx：对齐「智学云 / vue-next-admin」约定（如 <ele-Search />）
//  - Xxx    ：兼容直接写组件名（如 <Search />）与 <component :is="'Search'" />
for (const [key, comp] of Object.entries(ElementPlusIconsVue)) {
  app.component('ele-' + key, comp as any);
  app.component(key, comp as any);
}

app.use(createPinia());
app.use(router);
app.use(ElementPlus);
app.mount('#app');
