<template>
  <div class="login-container" :key="componentKey">
    <div class="page-controls">
      <el-dropdown @command="handleThemeChange" trigger="click">
        <el-button
          circle
          :icon="
            themeMode === 'dark'
              ? Moon
              : themeMode === 'light'
                ? Sunny
                : Monitor
          "
          class="theme-btn"
        />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              command="light"
              :class="{ 'is-active': themeMode === 'light' }"
            >
              <el-icon><Sunny /></el-icon>
              亮色模式
            </el-dropdown-item>
            <el-dropdown-item
              command="dark"
              :class="{ 'is-active': themeMode === 'dark' }"
            >
              <el-icon><Moon /></el-icon>
              暗色模式
            </el-dropdown-item>
            <el-dropdown-item
              command="system"
              :class="{ 'is-active': themeMode === 'system' }"
            >
              <el-icon><Monitor /></el-icon>
              跟随系统
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-dropdown @command="handleSetLang">
        <span class="lang-switch">{{ langLabel }}</span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="zh">中文</el-dropdown-item>
            <el-dropdown-item command="en">EN</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div class="login-wrapper">
      <div class="login-left">
        <AnimatedCharacters
          :isTyping="isTyping"
          :isPasswordVisible="isPasswordVisible"
          :passwordLength="currentPasswordLength"
          :isLogin="isLogin"
          :isInputFocused="isInputFocused"
        />
      </div>

      <div class="login-right">
        <div class="login-card">
          <h2 class="login-card-title">{{ isLogin ? "登录" : "注册" }}</h2>

          <el-form
            v-if="isLogin"
            :model="loginForm"
            :rules="loginRules"
            ref="loginFormRef"
            class="login-form"
          >
            <el-form-item prop="account">
              <el-input
                v-model="loginForm.account"
                placeholder="请输入账号"
                prefix-icon="User"
                size="large"
                clearable
                @focus="handleInputFocused"
                @blur="handleInputBlurred"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                :type="loginPasswordVisible ? 'text' : 'password'"
                placeholder="请输入密码"
                prefix-icon="Lock"
                size="large"
                clearable
                @focus="handleInputFocused"
                @blur="handleInputBlurred"
              >
                <template #suffix>
                  <el-icon
                    class="password-toggle-icon"
                    @click="toggleLoginPasswordVisible"
                  >
                    <View v-if="!loginPasswordVisible" />
                    <Hide v-else />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="captcha">
              <div class="captcha-row">
                <el-input
                  v-model="loginForm.captcha"
                  placeholder="请输入验证码"
                  prefix-icon="Key"
                  size="large"
                  clearable
                />
                <div
                  class="captcha-image-box"
                  @click="refreshCaptcha"
                  title="点击刷新验证码"
                >
                  <canvas ref="captchaCanvas" width="200" height="60"></canvas>
                </div>
                <el-button
                  :icon="Refresh"
                  circle
                  @click="refreshCaptcha"
                  class="refresh-button"
                  title="刷新验证码"
                />
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                @click="handleLogin"
                :loading="loading"
                size="large"
                class="login-button"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>

          <el-form
            v-else
            :model="registerForm"
            :rules="registerRules"
            ref="registerFormRef"
            class="login-form"
          >
            <el-form-item prop="account">
              <el-input
                v-model="registerForm.account"
                placeholder="请输入账号（4-20位字母、数字或下划线）"
                prefix-icon="User"
                size="large"
                clearable
                @focus="handleInputFocused"
                @blur="handleInputBlurred"
              />
            </el-form-item>

            <el-form-item prop="name">
              <el-input
                v-model="registerForm.name"
                placeholder="请输入用户名（2-20个字符）"
                prefix-icon="UserFilled"
                size="large"
                clearable
                @focus="handleInputFocused"
                @blur="handleInputBlurred"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                :type="registerPasswordVisible ? 'text' : 'password'"
                placeholder="请输入密码（至少6位，包含字母和数字）"
                prefix-icon="Lock"
                size="large"
                clearable
                @focus="handleInputFocused"
                @blur="handleInputBlurred"
              >
                <template #suffix>
                  <el-icon
                    class="password-toggle-icon"
                    @click="toggleRegisterPasswordVisible"
                  >
                    <View v-if="!registerPasswordVisible" />
                    <Hide v-else />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                :type="registerPasswordVisible ? 'text' : 'password'"
                placeholder="请再次输入密码，确保两次输入一致"
                prefix-icon="Lock"
                size="large"
                clearable
                @focus="handleInputFocused"
                @blur="handleInputBlurred"
              >
                <template #suffix>
                  <el-icon
                    class="password-toggle-icon"
                    @click="toggleRegisterPasswordVisible"
                  >
                    <View v-if="!registerPasswordVisible" />
                    <Hide v-else />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="role">
              <el-select
                v-model="registerForm.role"
                placeholder="请选择用户类型"
                size="large"
                style="width: 100%"
              >
                <el-option label="普通用户" value="USER" />
                <el-option label="商家" value="MERCHANT" />
              </el-select>
            </el-form-item>

            <el-form-item prop="captcha">
              <div class="captcha-row">
                <el-input
                  v-model="registerForm.captcha"
                  placeholder="请输入验证码"
                  prefix-icon="Key"
                  size="large"
                  clearable
                />
                <div
                  class="captcha-image-box"
                  @click="refreshCaptcha"
                  title="点击刷新验证码"
                >
                  <canvas ref="captchaCanvas" width="200" height="60"></canvas>
                </div>
                <el-button
                  :icon="Refresh"
                  circle
                  @click="refreshCaptcha"
                  class="refresh-button"
                  title="刷新验证码"
                />
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                @click="handleRegister"
                :loading="loading"
                size="large"
                class="login-button"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-footer">
            <el-checkbox v-model="rememberPassword" class="remember-checkbox">
              记住密码
            </el-checkbox>
            <a
              href="javascript:void(0)"
              @click="toggleMode"
              class="footer-link"
            >
              {{ isLogin ? "立即注册" : "立即登录" }}
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  User,
  Lock,
  Key,
  Refresh,
  UserFilled,
  View,
  Hide,
  Moon,
  Sunny,
  Monitor,
} from "@element-plus/icons-vue";
import { login, register } from "@/api/auth";
import { useUserStore } from "@/stores/user";
import AnimatedCharacters from "@/components/AnimatedCharacters.vue";
import {
  getThemeMode,
  setThemeMode,
  isDark,
  initTheme,
  ThemeMode,
} from "@/utils/theme";
import { setLang } from "@/utils/lang";
import { i18n } from "@/i18n";
import {
  translatePageToEnglish,
  restorePageToChinese,
} from "@/utils/autoTranslate";

const router = useRouter();
const userStore = useUserStore();
const loginFormRef = ref(null);
const registerFormRef = ref(null);
const captchaCanvas = ref(null);
const loading = ref(false);
const isLogin = ref(true);

// 动画交互状态
const isInputFocused = ref(false);
const loginPasswordVisible = ref(false);
const registerPasswordVisible = ref(false);

// 当前密码长度（用于动画）
const currentPasswordLength = computed(() => {
  if (isLogin.value) {
    return loginForm.password.length;
  }
  return Math.max(
    registerForm.password.length,
    registerForm.confirmPassword.length,
  );
});

// 当前密码可见性（用于动画）
const isPasswordVisible = computed(() => {
  if (isLogin.value) {
    return loginPasswordVisible.value;
  }
  return registerPasswordVisible.value;
});

// 输入框聚焦处理
// 主题和语言
const themeMode = ref(getThemeMode());
const dark = ref(isDark());
const langLabel = computed(() =>
  i18n.global.locale.value === "zh" ? "中文" : "EN",
);
const componentKey = ref(0);

const handleThemeChange = (mode) => {
  setThemeMode(mode);
  themeMode.value = mode;
  dark.value = isDark();
};

const handleSetLang = (lang) => {
  setLang(lang);
  if (lang === "en") {
    // App.vue 的 watch 会自动触发翻译，这里只提示
    ElMessage.success("页面已自动翻译为英文");
  } else {
    // 切回中文：保存表单数据 → 强制重渲染组件 → 恢复表单数据 → 重新生成验证码
    const savedLogin = { ...loginForm };
    const savedRegister = { ...registerForm };
    const savedIsLogin = isLogin.value;
    componentKey.value++;
    nextTick(() => {
      Object.assign(loginForm, savedLogin);
      Object.assign(registerForm, savedRegister);
      isLogin.value = savedIsLogin;
      nextTick(() => {
        generateCaptcha();
      });
      ElMessage.info("已恢复为中文");
    });
  }
};

const handleInputFocused = () => {
  isInputFocused.value = true;
};

// 输入框失焦处理
const handleInputBlurred = () => {
  isInputFocused.value = false;
};

// 切换登录密码可见性
const toggleLoginPasswordVisible = () => {
  loginPasswordVisible.value = !loginPasswordVisible.value;
};

// 切换注册密码可见性
const toggleRegisterPasswordVisible = () => {
  registerPasswordVisible.value = !registerPasswordVisible.value;
};

const loginForm = reactive({
  account: "",
  password: "",
  captcha: "",
});

const rememberPassword = ref(false);

const registerForm = reactive({
  account: "",
  name: "",
  role: "USER",
  password: "",
  confirmPassword: "",
  captcha: "",
});

const captchaCode = ref("");

const validateConfirmPassword = (rule, value, callback) => {
  if (value === "") {
    callback(new Error("请再次输入密码"));
  } else if (value !== registerForm.password) {
    callback(new Error("两次输入密码不一致"));
  } else {
    callback();
  }
};

const loginRules = {
  account: [
    { required: true, message: "请输入账号", trigger: "blur" },
    {
      pattern: /^[a-zA-Z0-9_]{4,20}$/,
      message: "账号必须是4-20位字母、数字或下划线",
      trigger: "blur",
    },
  ],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  captcha: [{ required: true, message: "请输入验证码", trigger: "blur" }],
};

const registerRules = {
  account: [
    { required: true, message: "请输入账号", trigger: "blur" },
    {
      pattern: /^[a-zA-Z0-9_]{4,20}$/,
      message: "账号必须是4-20位字母、数字或下划线",
      trigger: "blur",
    },
  ],
  name: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 2, max: 20, message: "用户名长度为2-20个字符", trigger: "blur" },
  ],
  role: [{ required: true, message: "请选择用户类型", trigger: "change" }],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于6位", trigger: "blur" },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{6,}$/,
      message: "密码必须包含字母和数字",
      trigger: "blur",
    },
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: validateConfirmPassword, trigger: "blur" },
  ],
  captcha: [{ required: true, message: "请输入验证码", trigger: "blur" }],
};

const generateCaptcha = () => {
  const canvas = captchaCanvas.value;
  const ctx = canvas.getContext("2d");
  const chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
  let captcha = "";

  for (let i = 0; i < 4; i++) {
    const randomIndex = Math.floor(Math.random() * chars.length);
    captcha += chars[randomIndex];
  }

  captchaCode.value = captcha;

  ctx.fillStyle = "#f5f5f5";
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  ctx.font = "bold 45px Arial";
  ctx.textBaseline = "middle";

  for (let i = 0; i < captcha.length; i++) {
    ctx.save();
    const x = 15 + i * 44;
    const y = 32 + Math.random() * 10 - 5;
    const angle = Math.random() * 0.25 - 0.12;

    ctx.fillStyle = `rgb(${50 + Math.random() * 100}, ${50 + Math.random() * 100}, ${50 + Math.random() * 100})`;
    ctx.translate(x, y);
    ctx.rotate(angle);
    ctx.fillText(captcha[i], 0, 0);
    ctx.restore();
  }

  for (let i = 0; i < 20; i++) {
    ctx.strokeStyle = `rgba(${Math.random() * 255}, ${Math.random() * 255}, ${Math.random() * 255}, 1.0)`;
    ctx.beginPath();
    ctx.moveTo(Math.random() * canvas.width, Math.random() * canvas.height);
    ctx.lineTo(Math.random() * canvas.width, Math.random() * canvas.height);
    ctx.stroke();
  }
};

const refreshCaptcha = () => {
  generateCaptcha();
  loginForm.captcha = "";
  registerForm.captcha = "";
};

const toggleMode = () => {
  isLogin.value = !isLogin.value;
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate();
  }
  if (registerFormRef.value) {
    registerFormRef.value.clearValidate();
  }
  // Use nextTick to ensure DOM is updated before regenerating captcha
  nextTick(() => {
    refreshCaptcha();
  });
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      if (loginForm.captcha.toUpperCase() !== captchaCode.value) {
        ElMessage.error("验证码错误");
        refreshCaptcha();
        return;
      }

      loading.value = true;
      try {
        const response = await login({
          account: loginForm.account,
          password: loginForm.password,
        });

        if (response.code === 200) {
          if (rememberPassword.value) {
            localStorage.setItem("rememberedAccount", loginForm.account);
            localStorage.setItem("rememberedPassword", loginForm.password);
          } else {
            localStorage.removeItem("rememberedAccount");
            localStorage.removeItem("rememberedPassword");
          }

          ElMessage.success("登录成功");
          userStore.setToken(response.data.token);
          userStore.setUserInfo(response.data.userInfo);
          router.push("/");
        } else {
          ElMessage.error(response.message || "登录失败");
          refreshCaptcha();
        }
      } catch (error) {
        ElMessage.error("登录失败，请稍后重试");
        refreshCaptcha();
      } finally {
        loading.value = false;
      }
    }
  });
};

const handleRegister = async () => {
  if (!registerFormRef.value) return;

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      if (registerForm.captcha.toUpperCase() !== captchaCode.value) {
        ElMessage.error("验证码错误");
        refreshCaptcha();
        return;
      }

      loading.value = true;
      try {
        const response = await register({
          account: registerForm.account,
          password: registerForm.password,
          confirmPassword: registerForm.confirmPassword,
          name: registerForm.name,
          role: registerForm.role,
        });

        if (response.code === 200) {
          ElMessage.success("注册成功，请登录");
          isLogin.value = true;
          registerForm.account = "";
          registerForm.name = "";
          registerForm.role = "USER";
          registerForm.password = "";
          registerForm.confirmPassword = "";
          registerForm.captcha = "";
          // Use nextTick to ensure DOM is updated before regenerating captcha
          nextTick(() => {
            refreshCaptcha();
          });
        } else {
          ElMessage.error(response.message || "注册失败");
          refreshCaptcha();
        }
      } catch (error) {
        ElMessage.error("注册失败，请稍后重试");
        refreshCaptcha();
      } finally {
        loading.value = false;
      }
    }
  });
};

onMounted(() => {
  initTheme();
  themeMode.value = getThemeMode();
  dark.value = isDark();
  generateCaptcha();

  const rememberedAccount = localStorage.getItem("rememberedAccount");
  const rememberedPassword = localStorage.getItem("rememberedPassword");

  if (rememberedAccount && rememberedPassword) {
    loginForm.account = rememberedAccount;
    loginForm.password = rememberedPassword;
    rememberPassword.value = true;
  }
});
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  overflow: hidden;
  position: relative;
}

.page-controls {
  position: absolute;
  top: 20px;
  right: 24px;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 12px;
}

.theme-btn {
  border: none;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
}

.dark .theme-btn {
  background: rgba(50, 50, 50, 0.6);
  color: #ddd;
}

.lang-switch {
  cursor: pointer;
  font-size: 14px;
  color: var(--text-color-primary);
  padding: 6px 12px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  transition: all 0.3s;
}

.lang-switch:hover {
  background: rgba(255, 255, 255, 0.9);
}

.dark .lang-switch {
  background: rgba(50, 50, 50, 0.6);
  color: #ddd;
}

.dark .lang-switch:hover {
  background: rgba(50, 50, 50, 0.9);
}

.login-wrapper {
  display: flex;
  width: 100%;
  height: 100%;
}

.login-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-right {
  flex: 1;
  padding: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--card-bg);
}

.login-card {
  width: 100%;
  max-width: 520px;
  padding: 40px 35px;
}

.password-toggle-icon {
  cursor: pointer;
  font-size: 18px;
  color: var(--text-color-secondary);
  transition: color 0.3s;
}

.password-toggle-icon:hover {
  color: var(--primary-color);
}

.login-card-title {
  font-size: 26px;
  font-weight: bold;
  color: var(--text-color-primary);
  text-align: center;
  margin-bottom: 32px;
}

.login-form {
  margin-top: 16px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 10px 14px;
}

.login-form :deep(.el-input__inner) {
  height: 42px;
  font-size: 15px;
}

.login-form :deep(.el-input__prefix-inner) {
  font-size: 18px;
}

/* 用户类型和验证码同行布局 */
.form-row-inline {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.form-row-inline .role-select-item {
  flex: 0 0 130px;
  margin-bottom: 0;
}

.form-row-inline .role-select-item .el-select {
  width: 100%;
}

.form-row-inline .captcha-item {
  flex: 1;
  margin-bottom: 0;
}

.captcha-wrapper-compact {
  display: flex;
  gap: 8px;
  align-items: center;
}

.captcha-image-compact {
  cursor: pointer;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid var(--border-color-base);
  flex-shrink: 0;
  transition: all 0.3s;
}

.captcha-image-compact:hover {
  border-color: var(--primary-color);
}

.captcha-image-compact canvas {
  display: block;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

/* 验证码同行等宽布局 */
.captcha-row {
  display: flex;
  gap: 12px;
  align-items: stretch;
  width: 100%;
}

.captcha-row :deep(.el-input) {
  width: 190px !important;
  flex: none;
}

.captcha-image-box {
  flex: 1;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--border-color-base);
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.captcha-image-box:hover {
  border-color: var(--primary-color);
}

.captcha-image-box canvas {
  display: block;
  height: 60px;
}

.captcha-row .refresh-button {
  flex-shrink: 0;
  align-self: center;
}

.captcha-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-image {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--border-color-base);
  transition: all 0.3s;
}

.captcha-image:hover {
  border-color: var(--primary-color);
  transform: scale(1.02);
}

.captcha-image canvas {
  display: block;
}

.refresh-button {
  flex-shrink: 0;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: bold;
  margin-top: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.remember-checkbox {
  margin-top: 10px;
}

.remember-checkbox :deep(.el-checkbox__label) {
  font-size: 14px;
  color: var(--text-color-secondary);
}

.login-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20px;
  color: var(--text-color-secondary);
}

.footer-link {
  color: var(--primary-color);
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s;
  font-size: 14px;
}

.footer-link:hover {
  color: var(--primary-color-light);
  text-decoration: underline;
}

@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
  }

  .login-left {
    display: none;
  }

  .login-right {
    padding: 40px 20px;
  }

  .login-card {
    padding: 30px 20px;
  }

  .form-row-inline {
    flex-direction: column;
    gap: 20px;
  }

  .form-row-inline .role-select-item {
    flex: none;
    width: 100%;
  }
}
</style>
