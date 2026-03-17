<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'

const showWelcome = ref(true)
const authVisible = ref(false)
const authMode = ref('login')
const sendCodeCountdown = ref(0)
const statusMessage = ref('')
const loading = ref(false)

const loginForm = reactive({
  email: '',
  password: ''
})

const registerForm = reactive({
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
})

let welcomeTimer = null
let codeTimer = null

const codeButtonText = computed(() =>
  sendCodeCountdown.value > 0 ? `${sendCodeCountdown.value}s` : '发送验证码'
)

onMounted(() => {
  welcomeTimer = setTimeout(() => {
    showWelcome.value = false
  }, 2400)
})

onBeforeUnmount(() => {
  if (welcomeTimer) {
    clearTimeout(welcomeTimer)
  }
  if (codeTimer) {
    clearInterval(codeTimer)
  }
})

function enterMain() {
  showWelcome.value = false
  if (welcomeTimer) {
    clearTimeout(welcomeTimer)
  }
}

function openAuthModal() {
  authVisible.value = true
  authMode.value = 'login'
  statusMessage.value = ''
}

function closeAuthModal() {
  authVisible.value = false
  statusMessage.value = ''
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

function startCodeCountdown(seconds = 60) {
  sendCodeCountdown.value = seconds
  if (codeTimer) {
    clearInterval(codeTimer)
  }
  codeTimer = setInterval(() => {
    sendCodeCountdown.value -= 1
    if (sendCodeCountdown.value <= 0) {
      clearInterval(codeTimer)
      codeTimer = null
    }
  }, 1000)
}

async function sendCode() {
  statusMessage.value = ''
  if (!isValidEmail(registerForm.email)) {
    statusMessage.value = '请输入有效的邮箱地址'
    return
  }
  if (sendCodeCountdown.value > 0) {
    return
  }

  loading.value = true
  try {
    const response = await fetch('/api/auth/send-code', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: registerForm.email })
    })

    if (!response.ok) {
      throw new Error('发送失败')
    }

    startCodeCountdown(60)
    statusMessage.value = '验证码已发送，请查收邮箱'
  } catch (error) {
    statusMessage.value = '验证码发送失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  statusMessage.value = ''
  if (!isValidEmail(registerForm.email)) {
    statusMessage.value = '请输入有效的邮箱地址'
    return
  }
  if (!registerForm.code.trim()) {
    statusMessage.value = '请输入邮箱验证码'
    return
  }
  if (registerForm.password.length < 6) {
    statusMessage.value = '密码至少 6 位'
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    statusMessage.value = '两次输入的密码不一致'
    return
  }

  loading.value = true
  try {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: registerForm.email,
        code: registerForm.code,
        password: registerForm.password
      })
    })

    if (!response.ok) {
      throw new Error('注册失败')
    }

    statusMessage.value = '注册成功，请登录'
    authMode.value = 'login'
  } catch (error) {
    statusMessage.value = '注册失败，请确认验证码或稍后重试'
  } finally {
    loading.value = false
  }
}

async function handleLogin() {
  statusMessage.value = ''
  if (!isValidEmail(loginForm.email)) {
    statusMessage.value = '请输入有效的邮箱地址'
    return
  }
  if (!loginForm.password) {
    statusMessage.value = '请输入密码'
    return
  }

  loading.value = true
  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(loginForm)
    })

    if (!response.ok) {
      throw new Error('登录失败')
    }

    statusMessage.value = '登录成功'
    closeAuthModal()
  } catch (error) {
    statusMessage.value = '登录失败，请检查邮箱或密码'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="paperhub-app">
    <transition name="fade">
      <section v-if="showWelcome" class="welcome-page">
        <div class="welcome-overlay"></div>
        <div class="welcome-content">
          <p class="welcome-kicker">Scientific Collaboration Network</p>
          <h1>Welcome PaperHub</h1>
          <p class="welcome-copy">连接研究者，互助共享文献与学术资源。</p>
          <button class="primary-btn" @click="enterMain">进入平台</button>
        </div>
      </section>
    </transition>

    <transition name="rise">
      <section v-if="!showWelcome" class="main-page">
        <header class="top-nav">
          <div class="brand">PaperHub</div>
          <nav class="menu">
            <a class="menu-item active" href="javascript:void(0)">文献互助</a>
          </nav>
          <button class="auth-btn" @click="openAuthModal">登录</button>
        </header>
        <main class="main-content">
          <h2>文献互助</h2>
          <p>在这里发起求助、共享论文、建立学术连接。</p>
        </main>
      </section>
    </transition>

    <transition name="fade">
      <div v-if="authVisible" class="modal-mask" @click.self="closeAuthModal">
        <section class="auth-modal">
          <h3 class="auth-title">{{ authMode === 'login' ? '登录 PaperHub' : '注册 PaperHub' }}</h3>

          <form v-if="authMode === 'login'" class="auth-form" @submit.prevent="handleLogin">
            <label>邮箱</label>
            <input v-model.trim="loginForm.email" type="email" placeholder="you@example.com" />
            <label>密码</label>
            <input v-model="loginForm.password" type="password" placeholder="请输入密码" />
            <button class="primary-btn full" type="submit" :disabled="loading">
              {{ loading ? '提交中...' : '登录' }}
            </button>
            <p class="switch-tip">
              如果没有 PaperHub 账户？
              <button class="switch-link" type="button" @click="authMode = 'register'">去注册</button>
            </p>
          </form>

          <form v-else class="auth-form" @submit.prevent="handleRegister">
            <label>邮箱</label>
            <input v-model.trim="registerForm.email" type="email" placeholder="you@example.com" />
            <label>邮箱验证码</label>
            <div class="code-row">
              <input v-model.trim="registerForm.code" type="text" placeholder="请输入验证码" />
              <button
                class="line-btn"
                type="button"
                :disabled="loading || sendCodeCountdown > 0"
                @click="sendCode"
              >
                {{ codeButtonText }}
              </button>
            </div>
            <label>密码</label>
            <input v-model="registerForm.password" type="password" placeholder="至少 6 位" />
            <label>确认密码</label>
            <input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="再次输入密码"
            />
            <button class="primary-btn full" type="submit" :disabled="loading">
              {{ loading ? '提交中...' : '注册' }}
            </button>
            <p class="switch-tip">
              已有 PaperHub 账户？
              <button class="switch-link" type="button" @click="authMode = 'login'">去登录</button>
            </p>
          </form>

          <p v-if="statusMessage" class="status">{{ statusMessage }}</p>
        </section>
      </div>
    </transition>
  </div>
</template>
