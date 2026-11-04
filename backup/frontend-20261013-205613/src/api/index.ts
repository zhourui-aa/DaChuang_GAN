import request from './request';

/* ---------------- 认证 ---------------- */
export const loginApi = (data: { username: string; password: string }) =>
  request.post('/auth/login', data);
export const meApi = () => request.get('/auth/me');

/* ---------------- 基础数据 ---------------- */
export const coursesApi = () => request.get('/assignment/courses');
export const assignmentsApi = (courseId?: number) =>
  request.get('/assignment/assignments', { params: { courseId } });
export const studentsApi = (classId?: number) =>
  request.get('/assignment/students', { params: { classId } });

/* ---------------- 提交 ---------------- */
export const listApi = (assignmentId: number, riskLevel: string, page = 1, size = 10) =>
  request.get('/assignment/list', { params: { assignmentId, riskLevel, page, size } });
export const submitApi = (form: FormData) =>
  request.post('/assignment/submit', form, { headers: { 'Content-Type': 'multipart/form-data' } });
export const submitBatchApi = (form: FormData) =>
  request.post('/assignment/submit-batch', form, { headers: { 'Content-Type': 'multipart/form-data' } });

/* ---------------- 检测 ---------------- */
export const detectRunApi = (submissionId: number) => request.post('/detect/run', { submissionId });
export const detectResultApi = (submissionId: number) => request.get(`/detect/result/${submissionId}`);
export const replayApi = (configVersion: string, assignmentId?: number) =>
  request.post('/detect/replay', null, { params: { configVersion, assignmentId } });

/* ---------------- 证据 ---------------- */
export const evidenceApi = (submissionId: number) => request.get(`/evidence/${submissionId}`);

/* ---------------- 报告 / 看板 ---------------- */
export const dashboardApi = () => request.get('/report/dashboard');

/* ---------------- 申诉 ---------------- */
export const appealCreateApi = (data: any) => request.post('/appeal/create', data);
export const appealReviewApi = (data: any) => request.post('/appeal/review', data);
export const appealListApi = (status: string, page = 1, size = 10) =>
  request.get('/appeal/list', { params: { status, page, size } });
export const appealDetailApi = (appealId: number) => request.get(`/appeal/${appealId}`);

/* ---------------- 模型与阈值 ---------------- */
export const configActiveApi = () => request.get('/config/active');
export const configListApi = () => request.get('/config/list');
export const configLogsApi = () => request.get('/config/logs');
export const configAdjustApi = (data: any) => request.post('/config/adjust', data);
export const configPreviewApi = (data: any) => request.post('/config/preview', data);

/* ---------------- 审计日志 ---------------- */
export const auditListApi = (action: string, keyword: string, page = 1, size = 10) =>
  request.get('/audit/list', { params: { action, keyword, page, size } });

/* ---------------- 报告导出（二进制） ---------------- */
export const exportExcelApi = (assignmentId: number) =>
  request.get('/report/export', { params: { assignmentId }, responseType: 'blob' });
