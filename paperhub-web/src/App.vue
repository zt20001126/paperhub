<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'

const STORAGE_USER_KEY = 'paperhub_login_user'

const showWelcome = ref(true)
const authVisible = ref(false)
const publishVisible = ref(false)
const authMode = ref('login')
const sendCodeCountdown = ref(0)
const statusMessage = ref('')
const loading = ref(false)
const currentUser = ref(null)

const litLoading = ref(false)
const litError = ref('')
const litPage = reactive({
  current: 1,
  size: 5,
  total: 0,
  pages: 0,
  records: []
})

const publishSubmitting = ref(false)
const publishHint = ref('')
const publishForm = reactive({
  title: '',
  journal: '',
  doi: '',
  rewardPoints: 10
})

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
const displayName = computed(() => currentUser.value?.nickname || currentUser.value?.email || '用户')
const avatarText = computed(() => displayName.value.trim().slice(0, 1).toUpperCase())

onMounted(() => {
  welcomeTimer = setTimeout(() => {
    showWelcome.value = false
  }, 2400)

  const savedUser = localStorage.getItem(STORAGE_USER_KEY)
  if (savedUser) {
    try {
      currentUser.value = JSON.parse(savedUser)
    } catch {
      localStorage.removeItem(STORAGE_USER_KEY)
    }
  }

  fetchLitRequests(1)
})

onBeforeUnmount(() => {
  if (welcomeTimer) clearTimeout(welcomeTimer)
  if (codeTimer) clearInterval(codeTimer)
})

function enterMain() {
  showWelcome.value = false
  if (welcomeTimer) clearTimeout(welcomeTimer)
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

function openPublishModal() {
  if (!currentUser.value?.id) {
    statusMessage.value = '请先登录后再发布求助'
    openAuthModal()
    return
  }
  if ((currentUser.value?.points ?? 0) <= 0) {
    publishHint.value = '当前积分不足，无法发布求助'
    return
  }
  publishVisible.value = true
  publishHint.value = ''
  publishForm.title = ''
  publishForm.journal = ''
  publishForm.doi = ''
  publishForm.rewardPoints = 10
}

function closePublishModal() {
  publishVisible.value = false
  publishHint.value = ''
}

function logout() {
  currentUser.value = null
  localStorage.removeItem(STORAGE_USER_KEY)
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

function startCodeCountdown(seconds = 60) {
  sendCodeCountdown.value = seconds
  if (codeTimer) clearInterval(codeTimer)
  codeTimer = setInterval(() => {
    sendCodeCountdown.value -= 1
    if (sendCodeCountdown.value <= 0) {
      clearInterval(codeTimer)
      codeTimer = null
    }
  }, 1000)
}

function formatDate(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 19)
}

function statusText(status) {
  if (status === 0) return '待处理'
  if (status === 1) return '处理中'
  if (status === 2) return '已完成'
  return '未知'
}

async function readApiData(response, defaultErrorMessage) {
  let body = null
  try {
    body = await response.json()
  } catch {
    throw new Error(defaultErrorMessage)
  }
  if (!response.ok || !body?.success) {
    throw new Error(body?.message || defaultErrorMessage)
  }
  return body.data
}

async function fetchLitRequests(page = 1) {
  litLoading.value = true
  litError.value = ''
  try {
    const response = await fetch(`/api/lit-request/page?current=${page}&size=${litPage.size}`)
    const data = await readApiData(response, '文献数据加载失败')
    litPage.current = data.current || page
    litPage.size = data.size || litPage.size
    litPage.total = data.total || 0
    litPage.pages = data.pages || 0
    litPage.records = data.records || []
  } catch (error) {
    litError.value = error.message || '文献数据加载失败'
    litPage.records = []
  } finally {
    litLoading.value = false
  }
}

function goPrevPage() {
  if (litPage.current > 1) fetchLitRequests(litPage.current - 1)
}

function goNextPage() {
  if (litPage.current < litPage.pages) fetchLitRequests(litPage.current + 1)
}

async function submitPublishForm() {
  publishHint.value = ''
  if (!publishForm.title.trim() || !publishForm.journal.trim() || !publishForm.doi.trim()) {
    publishHint.value = '请完整填写论文标题、期刊和 DOI'
    return
  }

  publishSubmitting.value = true
  try {
    const response = await fetch('/api/lit-request/sub_lit', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: currentUser.value.id,
        title: publishForm.title,
        journal: publishForm.journal,
        doi: publishForm.doi,
        rewardPoints: Number(publishForm.rewardPoints || 0)
      })
    })
    await readApiData(response, '发布求助失败')
    if (currentUser.value) {
      currentUser.value.points = Math.max(
        0,
        (currentUser.value.points || 0) - Number(publishForm.rewardPoints || 0)
      )
      localStorage.setItem(STORAGE_USER_KEY, JSON.stringify(currentUser.value))
    }
    closePublishModal()
    fetchLitRequests(1)
  } catch (error) {
    publishHint.value = error.message || '发布求助失败，请稍后重试'
  } finally {
    publishSubmitting.value = false
  }
}

async function sendCode() {
  statusMessage.value = ''
  if (!isValidEmail(registerForm.email)) {
    statusMessage.value = '请输入有效的邮箱地址'
    return
  }
  if (sendCodeCountdown.value > 0) return

  loading.value = true
  try {
    const response = await fetch('/api/auth/send-code', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: registerForm.email })
    })
    await readApiData(response, '验证码发送失败')
    startCodeCountdown(60)
    statusMessage.value = '验证码已发送，请查收邮箱'
  } catch (error) {
    statusMessage.value = error.message || '验证码发送失败，请稍后重试'
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
    await readApiData(response, '注册失败')
    statusMessage.value = '注册成功，请登录'
    authMode.value = 'login'
  } catch (error) {
    statusMessage.value = error.message || '注册失败，请稍后重试'
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
    const data = await readApiData(response, '登录失败')
    const user = {
      id: data.id,
      email: data.email,
      nickname: data.nickname,
      points: data.points ?? 0,
      token: data.token,
      avatarUrl: data.avatarUrl || ''
    }
    currentUser.value = user
    localStorage.setItem(STORAGE_USER_KEY, JSON.stringify(user))
    closeAuthModal()
  } catch (error) {
    statusMessage.value = error.message || '登录失败，请检查邮箱或密码'
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

          <div class="top-actions">
            <button class="publish-btn" type="button" @click="openPublishModal">发布求助</button>
            <div v-if="currentUser" class="user-menu-wrap">
              <div class="user-trigger">
                <img v-if="currentUser.avatarUrl" class="user-avatar" :src="currentUser.avatarUrl" alt="avatar" />
                <div v-else class="user-avatar user-avatar-fallback">{{ avatarText }}</div>
                <span class="user-name">{{ displayName }}</span>
              </div>
              <div class="user-dropdown">
                <button class="logout-btn" type="button" @click="logout">退出登录</button>
              </div>
            </div>
            <button v-else class="auth-btn" @click="openAuthModal">登录</button>
          </div>
        </header>

        <main class="main-content">
          <section class="lit-panel">
            <div class="lit-panel-header">
              <h2>文献互助</h2>
              <span>共 {{ litPage.total }} 条</span>
            </div>

            <div v-if="litLoading" class="lit-empty">正在加载文献...</div>
            <div v-else-if="litError" class="lit-empty">{{ litError }}</div>
            <div v-else-if="litPage.records.length === 0" class="lit-empty">暂无文献互助记录</div>
            <div v-else class="lit-list">
              <article v-for="item in litPage.records" :key="item.id" class="lit-card">
                <h3 class="lit-title">{{ item.title }}</h3>
                <p class="lit-meta">期刊：{{ item.journal || '--' }}</p>
                <p class="lit-meta">DOI：{{ item.doi || '--' }}</p>
                <div class="lit-footer">
                  <span>悬赏积分：{{ item.rewardPoints ?? 0 }}</span>
                  <span>状态：{{ statusText(item.status) }}</span>
                  <span>发布时间：{{ formatDate(item.createTime) }}</span>
                </div>
              </article>
            </div>

            <div class="pager">
              <button class="pager-btn" :disabled="litPage.current <= 1 || litLoading" @click="goPrevPage">
                上一页
              </button>
              <span class="pager-text">第 {{ litPage.current }} / {{ litPage.pages || 1 }} 页</span>
              <button
                class="pager-btn"
                :disabled="litPage.current >= litPage.pages || litLoading || litPage.pages === 0"
                @click="goNextPage"
              >
                下一页
              </button>
            </div>
          </section>
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
              <button class="line-btn" type="button" :disabled="loading || sendCodeCountdown > 0" @click="sendCode">
                {{ codeButtonText }}
              </button>
            </div>
            <label>密码</label>
            <input v-model="registerForm.password" type="password" placeholder="至少 6 位" />
            <label>确认密码</label>
            <input v-model="registerForm.confirmPassword" type="password" placeholder="再次输入密码" />
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

    <transition name="fade">
      <div v-if="publishVisible" class="modal-mask" @click.self="closePublishModal">
        <section class="auth-modal publish-modal">
          <h3 class="auth-title">发布文献求助</h3>
          <form class="auth-form" @submit.prevent="submitPublishForm">
            <label>论文标题</label>
            <input v-model.trim="publishForm.title" type="text" placeholder="请输入论文标题" />

            <label>发表期刊</label>
            <input v-model.trim="publishForm.journal" type="text" placeholder="请输入发表期刊" />

            <label>DOI 号</label>
            <input v-model.trim="publishForm.doi" type="text" placeholder="请输入 DOI 号" />

            <label>奖励积分</label>
            <input v-model.number="publishForm.rewardPoints" type="number" min="0" placeholder="请输入奖励积分" />

            <div class="publish-actions">
              <button class="ghost-btn" type="button" :disabled="publishSubmitting" @click="closePublishModal">
                取消
              </button>
              <button class="primary-btn" type="submit" :disabled="publishSubmitting">
                {{ publishSubmitting ? '发布中...' : '发布求助' }}
              </button>
            </div>

            <p v-if="publishHint" class="status">{{ publishHint }}</p>
          </form>
        </section>
      </div>
    </transition>
  </div>
</template>
