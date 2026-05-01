<template>
  <div class="home-container">
    <header class="topbar">
      <div class="brand-mark">玄机命阁</div>
      <div class="topbar-actions">
        <button
          v-if="authState.user"
          type="button"
          class="secondary-button"
          @click="navigateTo('/dashboard')"
        >
          进入工作台
        </button>
        <button
          v-if="authState.user"
          type="button"
          class="ghost-button"
          @click="handleLogout"
        >
          退出登录
        </button>
        <button
          v-else
          type="button"
          class="secondary-button"
          @click="navigateTo('/login')"
        >
          RBAC 登录
        </button>
      </div>
    </header>

    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">玄机命阁</p>
        <h1>AI 算命大师</h1>
        <p class="subtitle">
          面向命理、手相、风水和流年运势的智能咨询入口，现已接入前后端分离 RBAC 登录、角色与权限控制。
        </p>
        <div class="actions">
          <button type="button" class="primary-button" @click="navigateTo(authState.user ? '/fortune-master' : '/login')">
            {{ authState.user ? '进入大师对话' : '登录后使用 AI' }}
          </button>
        </div>
      </div>
      <div class="hero-panel">
        <div class="panel-title">咨询方向</div>
        <div class="grid">
          <div class="grid-card">八字命理</div>
          <div class="grid-card">流年运势</div>
          <div class="grid-card">风水布局</div>
          <div class="grid-card">手相分析</div>
        </div>
      </div>
    </section>

    <AppFooter />
  </div>
</template>

<script setup>
import { useHead } from '@vueuse/head'
import { useRouter } from 'vue-router'
import { authState, clearSession } from '../auth'
import AppFooter from '../components/AppFooter.vue'

useHead({
  title: 'AI 算命大师',
  meta: [
    {
      name: 'description',
      content: 'AI 算命大师提供八字、流年、手相、风水等方向的智能咨询。'
    }
  ]
})

const router = useRouter()

const navigateTo = (path) => {
  router.push(path)
}

const handleLogout = () => {
  clearSession()
  router.push('/login')
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.topbar {
  width: min(1180px, calc(100% - 32px));
  margin: 20px auto 0;
  padding: 16px 22px;
  border-radius: 24px;
  border: 1px solid rgba(112, 72, 34, 0.14);
  background: rgba(255, 249, 241, 0.7);
  box-shadow: 0 12px 36px rgba(96, 64, 37, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.brand-mark {
  color: #704421;
  letter-spacing: 0.16em;
  font-size: 13px;
}

.topbar-actions {
  display: flex;
  gap: 12px;
}

.hero {
  width: min(1180px, calc(100% - 32px));
  margin: 0 auto;
  padding: 72px 0 56px;
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 24px;
  flex: 1;
  align-items: center;
}

.hero-copy,
.hero-panel {
  border: 1px solid rgba(112, 72, 34, 0.14);
  border-radius: 32px;
  background: rgba(255, 249, 241, 0.72);
  backdrop-filter: blur(12px);
  box-shadow: 0 20px 60px rgba(96, 64, 37, 0.1);
}

.hero-copy {
  padding: 44px;
}

.eyebrow {
  color: #8f5b31;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  font-size: 13px;
  margin-bottom: 14px;
}

h1 {
  font-size: clamp(42px, 7vw, 78px);
  line-height: 1.04;
  color: #3e2617;
}

.subtitle {
  margin-top: 20px;
  font-size: 18px;
  line-height: 1.8;
  color: #6a4830;
  max-width: 640px;
}

.actions {
  margin-top: 28px;
}

.primary-button {
  border: none;
  border-radius: 999px;
  padding: 14px 28px;
  background: linear-gradient(135deg, #7c2f1c, #c18a48);
  color: #fff7ef;
  font-size: 16px;
  box-shadow: 0 16px 36px rgba(110, 63, 30, 0.18);
}

.secondary-button,
.ghost-button {
  border: none;
  border-radius: 999px;
  padding: 12px 18px;
}

.secondary-button {
  background: rgba(117, 73, 38, 0.1);
  color: #5b391f;
}

.ghost-button {
  background: #6f2f1b;
  color: #fff6ee;
}

.hero-panel {
  padding: 34px;
}

.panel-title {
  font-size: 18px;
  font-weight: 700;
  color: #4b2f1d;
  margin-bottom: 18px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.grid-card {
  padding: 24px 18px;
  border-radius: 24px;
  background: linear-gradient(180deg, #fbf1e3, #ead3b0);
  color: #5f3a20;
  text-align: center;
  font-size: 17px;
}

@media (max-width: 860px) {
  .topbar {
    width: min(100% - 20px, 1180px);
    flex-direction: column;
    gap: 12px;
  }

  .hero {
    grid-template-columns: 1fr;
    padding-top: 36px;
  }

  .hero-copy,
  .hero-panel {
    padding: 28px;
  }
}
</style>
