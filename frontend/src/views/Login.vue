<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-left">
        <div class="login-left-content">
          <h1 class="login-title">非遗商城</h1>
          <p class="login-subtitle">传承文化，连接未来</p>
          <div class="login-features">
            <div class="feature-item">
              <el-icon :size="30"><User /></el-icon>
              <span>安全可靠</span>
            </div>
            <div class="feature-item">
              <el-icon :size="30"><Lock /></el-icon>
              <span>隐私保护</span>
            </div>
            <div class="feature-item">
              <el-icon :size="30"><ShoppingCart /></el-icon>
              <span>便捷购物</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="login-right">
        <div class="login-card">
          <h2 class="login-card-title">{{ isLogin ? '用户登录' : '用户注册' }}</h2>
          
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
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                size="large"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="captcha">
              <div class="captcha-wrapper">
                <el-input
                  v-model="loginForm.captcha"
                  placeholder="请输入图片中的验证码"
                  prefix-icon="Key"
                  size="large"
                  clearable
                  style="flex: 1"
                />
                <div class="captcha-image" @click="refreshCaptcha" title="点击刷新验证码">
                  <canvas ref="captchaCanvas" width="120" height="40"></canvas>
                </div>
                <el-button :icon="Refresh" circle @click="refreshCaptcha" class="refresh-button" title="刷新验证码" />
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
                placeholder="账号必须由4-20位字母、数字或下划线组成"
                prefix-icon="User"
                size="large"
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="name">
              <el-input
                v-model="registerForm.name"
                placeholder="用户名长度为2-20个字符"
                prefix-icon="UserFilled"
                size="large"
                clearable
              />
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
            
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码必须包含字母和数字，长度不少于6位"
                prefix-icon="Lock"
                size="large"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码，确保两次输入一致"
                prefix-icon="Lock"
                size="large"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item prop="captcha">
              <div class="captcha-wrapper">
                <el-input
                  v-model="registerForm.captcha"
                  placeholder="请输入图片中的验证码"
                  prefix-icon="Key"
                  size="large"
                  clearable
                  style="flex: 1"
                />
                <div class="captcha-image" @click="refreshCaptcha" title="点击刷新验证码">
                  <canvas ref="captchaCanvas" width="120" height="40"></canvas>
                </div>
                <el-button :icon="Refresh" circle @click="refreshCaptcha" class="refresh-button" title="刷新验证码" />
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
            <span class="footer-text">{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
            <a href="javascript:void(0)" @click="toggleMode" class="footer-link">
              {{ isLogin ? '立即注册' : '立即登录' }}
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key, Refresh, ShoppingCart, UserFilled } from '@element-plus/icons-vue'
import { login, register } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const registerFormRef = ref(null)
const captchaCanvas = ref(null)
const loading = ref(false)
const isLogin = ref(true)

const loginForm = reactive({
  account: '',
  password: '',
  captcha: ''
})

const rememberPassword = ref(false)

const registerForm = reactive({
  account: '',
  name: '',
  role: 'USER',
  password: '',
  confirmPassword: '',
  captcha: ''
})

const captchaCode = ref('')

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const loginRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '账号必须是4-20位字母、数字或下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const registerRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '账号必须是4-20位字母、数字或下划线', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度为2-20个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{6,}$/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const generateCaptcha = () => {
  const canvas = captchaCanvas.value
  const ctx = canvas.getContext('2d')
  const chars = 'ABCDEFGHJKMNPQRSTUVWXYZ23456789'
  let captcha = ''
  
  for (let i = 0; i < 4; i++) {
    const randomIndex = Math.floor(Math.random() * chars.length)
    captcha += chars[randomIndex]
  }
  
  captchaCode.value = captcha
  
  ctx.fillStyle = '#f0f0f0'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  
  ctx.font = '24px Arial'
  ctx.fillStyle = '#333'
  ctx.textBaseline = 'middle'
  
  for (let i = 0; i < captcha.length; i++) {
    ctx.save()
    const x = 20 + i * 25
    const y = 20 + Math.random() * 10 - 5
    const angle = Math.random() * 0.4 - 0.2
    
    ctx.translate(x, y)
    ctx.rotate(angle)
    ctx.fillText(captcha[i], 0, 0)
    ctx.restore()
  }
  
  for (let i = 0; i < 5; i++) {
    ctx.strokeStyle = `rgba(${Math.random() * 255}, ${Math.random() * 255}, ${Math.random() * 255}, 0.3)`
    ctx.beginPath()
    ctx.moveTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.lineTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.stroke()
  }
}

const refreshCaptcha = () => {
  generateCaptcha()
  loginForm.captcha = ''
  registerForm.captcha = ''
}

const toggleMode = () => {
  isLogin.value = !isLogin.value
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
  }
  if (registerFormRef.value) {
    registerFormRef.value.clearValidate()
  }
  // Use nextTick to ensure DOM is updated before regenerating captcha
  nextTick(() => {
    refreshCaptcha()
  })
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      if (loginForm.captcha.toUpperCase() !== captchaCode.value) {
        ElMessage.error('验证码错误')
        refreshCaptcha()
        return
      }
      
      loading.value = true
      try {
        const response = await login({
          account: loginForm.account,
          password: loginForm.password
        })
        
        if (response.code === 200) {
          if (rememberPassword.value) {
            localStorage.setItem('rememberedAccount', loginForm.account)
            localStorage.setItem('rememberedPassword', loginForm.password)
          } else {
            localStorage.removeItem('rememberedAccount')
            localStorage.removeItem('rememberedPassword')
          }
          
          ElMessage.success('登录成功')
          userStore.setToken(response.data.token)
          userStore.setUserInfo(response.data.userInfo)
          router.push('/')
        } else {
          ElMessage.error(response.message || '登录失败')
          refreshCaptcha()
        }
      } catch (error) {
        ElMessage.error('登录失败，请稍后重试')
        refreshCaptcha()
      } finally {
        loading.value = false
      }
    }
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      if (registerForm.captcha.toUpperCase() !== captchaCode.value) {
        ElMessage.error('验证码错误')
        refreshCaptcha()
        return
      }
      
      loading.value = true
      try {
        const response = await register({
          account: registerForm.account,
          password: registerForm.password,
          confirmPassword: registerForm.confirmPassword,
          name: registerForm.name,
          role: registerForm.role
        })
        
        if (response.code === 200) {
          ElMessage.success('注册成功，请登录')
          isLogin.value = true
          registerForm.account = ''
          registerForm.name = ''
          registerForm.role = 'USER'
          registerForm.password = ''
          registerForm.confirmPassword = ''
          registerForm.captcha = ''
          // Use nextTick to ensure DOM is updated before regenerating captcha
          nextTick(() => {
            refreshCaptcha()
          })
        } else {
          ElMessage.error(response.message || '注册失败')
          refreshCaptcha()
        }
      } catch (error) {
        ElMessage.error('注册失败，请稍后重试')
        refreshCaptcha()
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  generateCaptcha()
  
  const rememberedAccount = localStorage.getItem('rememberedAccount')
  const rememberedPassword = localStorage.getItem('rememberedPassword')
  
  if (rememberedAccount && rememberedPassword) {
    loginForm.account = rememberedAccount
    loginForm.password = rememberedPassword
    rememberPassword.value = true
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-wrapper {
  display: flex;
  width: 100%;
  max-width: 1000px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
}

.login-left-content {
  color: #fff;
  text-align: center;
}

.login-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.login-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.login-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  transition: all 0.3s;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(10px);
}

.login-right {
  flex: 1;
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 450px;
  padding: 40px;
}

.login-card-title {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  text-align: center;
  margin-bottom: 40px;
}

.login-form {
  margin-top: 20px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 12px 16px;
}

.login-form :deep(.el-input__inner) {
  height: 48px;
  font-size: 16px;
}

.login-form :deep(.el-input__prefix-inner) {
  font-size: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
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
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
}

.captcha-image:hover {
  border-color: #409EFF;
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
  height: 48px;
  font-size: 18px;
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
  color: #606266;
}

.login-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 30px;
  color: #606266;
}

.footer-text {
  font-size: 14px;
}

.footer-link {
  color: #409EFF;
  margin-left: 8px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s;
}

.footer-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
  }
  
  .login-left {
    padding: 40px 20px;
  }
  
  .login-title {
    font-size: 36px;
  }
  
  .login-subtitle {
    font-size: 16px;
  }
  
  .login-right {
    padding: 40px 20px;
  }
  
  .login-card {
    padding: 30px 20px;
  }
}
</style>
