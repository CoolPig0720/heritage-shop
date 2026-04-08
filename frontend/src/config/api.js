// API 基础配置
export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";

// 上传地址
export const UPLOAD_URL = `${API_BASE_URL}/api/file/upload`;

// 工具函数：处理图片 URL
export const getImageUrl = (url) => {
  if (!url) return "";
  return url.startsWith("http") ? url : `${API_BASE_URL}${url}`;
};

// 工具函数：处理头像 URL
export const getAvatarUrl = (avatar) => {
  if (!avatar) return "";
  return avatar.startsWith("http") ? avatar : `${API_BASE_URL}${avatar}`;
};
