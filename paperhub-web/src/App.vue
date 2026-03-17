<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'

const STORAGE_USER_KEY = 'paperhub_login_user'
const HELP_SUBMIT_API = '/sub_lit_help'

const showWelcome = ref(true)
const authVisible = ref(false)
const publishVisible = ref(false)
const profileVisible = ref(false)
const authMode = ref('login')
const sendCodeCountdown = ref(0)
const statusMessage = ref('')
const loading = ref(false)
const currentUser = ref(null)

const litLoading = ref(false)
const litError = ref('')
const viewMode = ref('list')
const selectedLit = ref(null)
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

const profileSubmitting = ref(false)
const profileHint = ref('')
const profileForm = reactive({
  avatarUrl: '',
  nickname: '',
  email: ''
})

const helpFile = ref(null)
const helpFileName = ref('')
const helpHint = ref('')
const helpSubmitting = ref(false)

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

const codeButtonText = computed(() => (sendCodeCountdown.value > 0 ? `${sendCodeCountdown.value}s` : '发送验证码'))
const displayName = computed(() => currentUser.value?.nickname || currentUser.value?.email || '用户')
const avatarText = computed(() => displayName.value.trim().slice(0, 1).toUpperCase())
const detailAvatarText = computed(() => {
  const name = pickPublisherNickname(selectedLit.value)
  return String(name || 'U').trim().slice(0, 1).toUpperCase()
})

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
    openAuthModal()
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

function openProfileModal() {
  if (!currentUser.value) return
  profileVisible.value = true
  profileHint.value = ''
  profileForm.avatarUrl = currentUser.value.avatarUrl || ''
  profileForm.nickname = currentUser.value.nickname || ''
  profileForm.email = currentUser.value.email || ''
}

function closeProfileModal() {
  profileVisible.value = false
  profileHint.value = ''
}

function logout() {
  currentUser.value = null
  localStorage.removeItem(STORAGE_USER_KEY)
  alert('已成功退出登录')
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

function getNickname(item) {
  return item?.publisherNickname || item?.userNickname || item?.nickname || '--'
}

function getEmail(item) {
  return item?.publisherEmail || item?.userEmail || item?.email || '--'
}

function getAvatar(item) {
  return item?.publisherAvatarUrl || item?.userAvatarUrl || item?.avatarUrl || ''
}

function pickPublisherNickname(item) {
  return getNickname(item)
}

function pickPublisherEmail(item) {
  return getEmail(item)
}

function pickPublisherAvatar(item) {
  return getAvatar(item)
}

function pickPublisherId(item) {
  return item?.publisherId || item?.userId || '--'
}

function pickProcessTime(item) {
  return formatDate(item?.updateTime || item?.processTime || item?.createTime)
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

function openHelpDetail(item) {
  selectedLit.value = item
  viewMode.value = 'detail'
  helpFile.value = null
  helpFileName.value = ''
  helpHint.value = ''
}

function backToList() {
  viewMode.value = 'list'
  selectedLit.value = null
  helpFile.value = null
  helpFileName.value = ''
  helpHint.value = ''
}

function onHelpFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) return
  helpFile.value = file
  helpFileName.value = file.name
  helpHint.value = ''
}

function submitLitHelp() {
  if (!helpFile.value) {
    helpHint.value = '请先上传文献文件'
    return
  }
  helpHint.value = `提交接口已预留：${HELP_SUBMIT_API}，当前未接入后端处理逻辑`
}

async function submitLitHelpRequest() {
  if (!currentUser.value?.id) {
    helpHint.value = '请先登录后再提交应助文献'
    return
  }

  if (!selectedLit.value?.id) {
    helpHint.value = '未找到文献信息，请返回列表后重试'
    return
  }

  if (!helpFile.value) {
    helpHint.value = '请先上传文献文件'
    return
  }

  helpSubmitting.value = true
  helpHint.value = ''
  try {
    const formData = new FormData()
    formData.append('litRequestId', String(selectedLit.value.id))
    formData.append('userId', String(currentUser.value.id))
    formData.append('file', helpFile.value)

    const headers = currentUser.value?.token ? { Authorization: `Bearer ${currentUser.value.token}` } : undefined
    const response = await fetch(HELP_SUBMIT_API, {
      method: 'POST',
      headers,
      body: formData
    })

    await readApiData(response, '应助提交失败，请稍后重试')
    helpHint.value = '提交成功，等待发布者处理'
    helpFile.value = null
    helpFileName.value = ''
  } catch (error) {
    helpHint.value = error.message || '应助提交失败，请稍后重试'
  } finally {
    helpSubmitting.value = false
  }
}

function handleAvatarSelect(event) {
  const file = event.target.files?.[0]
  if (!file) return

  const ext = file.name.split('.').pop()?.toLowerCase()
  if (!ext || !['jpg', 'jpeg', 'png'].includes(ext)) {
    profileHint.value = '仅支持 jpg/png 格式图片'
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    profileHint.value = '图片大小不能超过 2MB'
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    profileForm.avatarUrl = String(reader.result || '')
    profileHint.value = ''
  }
  reader.readAsDataURL(file)
}

async function saveProfile() {
  profileHint.value = ''
  const nickname = (profileForm.nickname || '').trim()
  if (nickname.length < 2 || nickname.length > 20) {
    profileHint.value = '昵称长度需在 2-20 个字符之间'
    return
  }

  profileSubmitting.value = true
  try {
    const response = await fetch('/api/auth/update_user_info', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${currentUser.value?.token || ''}`
      },
      body: JSON.stringify({
        avatarUrl: profileForm.avatarUrl,
        nickname
      })
    })

    await readApiData(response, '修改失败')
    currentUser.value = {
      ...currentUser.value,
      avatarUrl: profileForm.avatarUrl,
      nickname
    }
    localStorage.setItem(STORAGE_USER_KEY, JSON.stringify(currentUser.value))
    closeProfileModal()
  } catch (error) {
    profileHint.value = error.message || '修改失败，请稍后重试'
  } finally {
    profileSubmitting.value = false
  }
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
      avatarUrl: data.avatarUrl || '',
      points: data.points ?? 0,
      token: data.token
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
            <a class="menu-item" href="javascript:void(0)">当日热点</a>
            <a class="menu-item" href="javascript:void(0)">交流社区</a>
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
                <button class="menu-action-btn" type="button" @click="openProfileModal">修改信息</button>
                <button class="menu-action-btn" type="button" @click="logout">退出登录</button>
              </div>
            </div>
            <button v-else class="auth-btn" @click="openAuthModal">登录</button>
          </div>
        </header>

        <main class="main-content">
          <section v-if="viewMode === 'list'" class="lit-panel">
            <div class="lit-panel-header">
              <h2>文献互助</h2>
              <span>共 {{ litPage.total }} 条</span>
            </div>

            <div v-if="litLoading" class="lit-empty">正在加载文献...</div>
            <div v-else-if="litError" class="lit-empty">{{ litError }}</div>
            <div v-else-if="litPage.records.length === 0" class="lit-empty">暂无文献互助记录</div>
            <div v-else class="lit-list">
              <article v-for="item in litPage.records" :key="item.id" class="lit-card">
                <div class="lit-card-top">
                  <div class="lit-card-body">
                    <h3 class="lit-title">{{ item.title }}</h3>
                    <p class="lit-meta">发表期刊：{{ item.journal || '--' }}</p>
                    <p class="lit-meta">DOI：{{ item.doi || '--' }}</p>
                    <div class="lit-footer">
                      <span>悬赏积分：{{ item.rewardPoints ?? 0 }}</span>
                      <span>状态：{{ statusText(item.status) }}</span>
                      <span>发布时间：{{ formatDate(item.createTime) }}</span>
                    </div>
                  </div>
                  <div class="lit-card-action">
                    <button class="primary-btn help-btn" type="button" @click="openHelpDetail(item)">我要应助</button>
                  </div>
                </div>
              </article>
            </div>

            <div class="pager">
              <button class="pager-btn" :disabled="litPage.current <= 1 || litLoading" @click="goPrevPage">上一页</button>
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

          <section v-else class="lit-panel detail-panel">
            <div class="detail-top">
              <button class="ghost-btn" type="button" @click="backToList">返回文献互助</button>
              <h2>文献详情</h2>
            </div>

            <article v-if="selectedLit" class="lit-card detail-card">
              <div class="detail-grid">
                <div class="detail-main">
                  <h3 class="lit-title">{{ selectedLit.title || '--' }}</h3>
                  <div class="detail-info-list">
                    <div class="detail-info-item">
                      <span class="detail-label">发表期刊</span>
                      <span class="detail-value">{{ selectedLit.journal || '--' }}</span>
                    </div>
                    <div class="detail-info-item">
                      <span class="detail-label">DOI</span>
                      <span class="detail-value detail-code">{{ selectedLit.doi || '--' }}</span>
                    </div>
                    <div class="detail-info-item">
                      <span class="detail-label">悬赏积分</span>
                      <span class="detail-value">{{ selectedLit.rewardPoints ?? 0 }}</span>
                    </div>
                    <div class="detail-info-item">
                      <span class="detail-label">状态</span>
                      <span class="detail-value">{{ statusText(selectedLit.status) }}</span>
                    </div>
                    <div class="detail-info-item">
                      <span class="detail-label">处理时间</span>
                      <span class="detail-value">{{ pickProcessTime(selectedLit) }}</span>
                    </div>
                  </div>
                </div>

                <aside class="publisher-card">
                  <h4>发布者信息</h4>
                  <div class="publisher-main">
                    <img
                      v-if="pickPublisherAvatar(selectedLit)"
                      class="publisher-avatar"
                      :src="pickPublisherAvatar(selectedLit)"
                      alt="publisher"
                    />
                    <div v-else class="publisher-avatar user-avatar-fallback">{{ detailAvatarText }}</div>
                    <div class="publisher-info">
                      <p>昵称：{{ pickPublisherNickname(selectedLit) }}</p>
                      <p>邮箱：{{ pickPublisherEmail(selectedLit) }}</p>
                      <p>发布者ID：{{ pickPublisherId(selectedLit) }}</p>
                    </div>
                  </div>
                </aside>
              </div>

              <div class="help-upload-panel">
                <div class="help-upload-head">
                  <h4>上传文献</h4>
                  <span class="help-api-tag">{{ HELP_SUBMIT_API }}</span>
                </div>
                <div class="help-upload-area">
                  <label class="publish-btn upload-btn" for="help-upload-input">选择文件</label>
                  <input id="help-upload-input" class="hidden-input" type="file" @change="onHelpFileChange" />
                  <span v-if="helpFileName" class="file-name">{{ helpFileName }}</span>
                  <span v-else class="file-placeholder">尚未选择文件</span>
                </div>
                <div v-if="helpFile" class="help-submit-row">
                  <button class="primary-btn" type="button" :disabled="helpSubmitting" @click="submitLitHelpRequest">
                    {{ helpSubmitting ? '提交中...' : '提交' }}
                  </button>
                </div>
                <p v-if="helpHint" class="status">{{ helpHint }}</p>
              </div>
            </article>
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
              如果没有 PaperHub 账户，
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
              已有 PaperHub 账户，
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
              <button class="ghost-btn" type="button" :disabled="publishSubmitting" @click="closePublishModal">取消</button>
              <button class="primary-btn" type="submit" :disabled="publishSubmitting">
                {{ publishSubmitting ? '发布中...' : '发布求助' }}
              </button>
            </div>
            <p v-if="publishHint" class="status">{{ publishHint }}</p>
          </form>
        </section>
      </div>
    </transition>

    <transition name="fade">
      <div v-if="profileVisible" class="modal-mask" @click.self="closeProfileModal">
        <section class="auth-modal publish-modal">
          <h3 class="auth-title">修改信息</h3>
          <form class="auth-form" @submit.prevent="saveProfile">
            <label>头像</label>
            <div class="profile-avatar-row">
              <img v-if="profileForm.avatarUrl" class="profile-avatar" :src="profileForm.avatarUrl" alt="avatar" />
              <div v-else class="profile-avatar profile-avatar-fallback">{{ avatarText }}</div>
              <input type="file" accept=".jpg,.jpeg,.png" @change="handleAvatarSelect" />
            </div>
            <label>头像地址（可选）</label>
            <input v-model.trim="profileForm.avatarUrl" type="text" placeholder="请输入头像 URL 或通过上方上传" />
            <label>昵称</label>
            <input v-model.trim="profileForm.nickname" type="text" maxlength="20" placeholder="2-20 个字符" />
            <label>邮箱（只读）</label>
            <input :value="profileForm.email" type="email" readonly disabled />

            <div class="publish-actions">
              <button class="ghost-btn" type="button" :disabled="profileSubmitting" @click="closeProfileModal">取消</button>
              <button class="primary-btn" type="submit" :disabled="profileSubmitting">
                {{ profileSubmitting ? '保存中...' : '保存修改' }}
              </button>
            </div>
            <p v-if="profileHint" class="status">{{ profileHint }}</p>
          </form>
        </section>
      </div>
    </transition>
  </div>
</template>
