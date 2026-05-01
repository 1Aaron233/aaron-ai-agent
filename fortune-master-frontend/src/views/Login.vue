<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="login-brand">
        <p class="eyebrow">Fortune Agent RBAC</p>
        <h1>若依式前后端分离权限入口</h1>
        <p class="intro">
          这版工作台把用户、角色、权限和 AI 对话接进统一登录态，保留你当前项目的轻量前端结构。
        </p>
        <div class="demo-card">
          <div class="demo-title">演示账号</div>
          <p>`admin / admin123`：管理员，拥有用户和角色查看权限。</p>
          <p>`fortune / fortune123`：顾问账号，只能进入工作台和 AI 对话。</p>
        </div>
      </section>

      <section class="login-panel">
        <div class="panel-header">
          <span class="panel-kicker">统一认证</span>
          <h2>登录系统</h2>
        </div>

        <form class="login-form" @submit.prevent="handleSubmit">
          <label>
            <span>用户名</span>
            <input v-model.trim="form.username" type="text" placeholder="请输入用户名" autocomplete="username" />
          </label>
          <label>
            <span>密码</span>
            <input v-model="form.password" type="password" placeholder="请输入密码" autocomplete="current-password" />
          </label>

          <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

          <button class="submit-button" type="submit" :disabled="submitting">
            {{ submitting ? '登录中...' : '进入工作台' }}
          </button>
        </form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '../auth'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)
const errorMessage = ref('')

const form = reactive({
  username: 'admin',
  password: 'admin123'
})

const handleSubmit = async () => {
  if (!form.username || !form.password) {
    errorMessage.value = '请输入用户名和密码'
    return
  }

  submitting.value = true
  errorMessage.value = ''

  try {
    await login(form.username, form.password)
    router.push(route.query.redirect || '/dashboard')
  } catch (error) {
    errorMessage.value = error.message || '登录失败'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
}

.login-shell {
  width: min(1120px, 100%);
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  border-radius: 36px;
  overflow: hidden;
  border: 1px solid rgba(89, 48, 22, 0.12);
  box-shadow: 0 28px 80px rgba(77, 45, 24, 0.16);
  background: rgba(255, 248, 240, 0.8);
}

.login-brand {
  padding: 52px 48px;
  background:
    radial-gradient(circle at top left, rgba(215, 175, 96, 0.34), transparent 36%),
    linear-gradient(160deg, rgba(88, 34, 17, 0.96), rgba(140, 90, 49, 0.92));
  color: #fff5ea;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-size: 12px;
  opacity: 0.76;
}

h1 {
  margin-top: 16px;
  font-size: clamp(34px, 5vw, 58px);
  line-height: 1.08;
}

.intro {
  margin-top: 18px;
  max-width: 520px;
  color: rgba(255, 243, 231, 0.86);
  line-height: 1.8;
}

.demo-card {
  margin-top: 28px;
  padding: 24px;
  border-radius: 24px;
  background: rgba(255, 248, 239, 0.12);
  border: 1px solid rgba(255, 240, 226, 0.2);
  line-height: 1.9;
}

.demo-title {
  font-weight: 700;
  margin-bottom: 8px;
}

.login-panel {
  padding: 48px 40px;
  background: linear-gradient(180deg, rgba(255, 252, 247, 0.92), rgba(248, 238, 223, 0.96));
}

.panel-kicker {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #8a623e;
}

h2 {
  margin-top: 14px;
  color: #3f2414;
  font-size: 32px;
}

.login-form {
  margin-top: 28px;
  display: grid;
  gap: 18px;
}

label {
  display: grid;
  gap: 8px;
  color: #6b4830;
}

input {
  border: 1px solid rgba(127, 86, 51, 0.18);
  border-radius: 18px;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.78);
  outline: none;
}

input:focus {
  border-color: #a56b38;
  box-shadow: 0 0 0 4px rgba(165, 107, 56, 0.12);
}

.submit-button {
  margin-top: 8px;
  border: none;
  border-radius: 18px;
  padding: 15px 18px;
  color: #fff9f3;
  background: linear-gradient(135deg, #6f2f1b, #c08946);
}

.submit-button:disabled {
  opacity: 0.7;
}

.error-text {
  color: #a53030;
}

@media (max-width: 900px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .login-brand,
  .login-panel {
    padding: 32px 24px;
  }
}
</style>
