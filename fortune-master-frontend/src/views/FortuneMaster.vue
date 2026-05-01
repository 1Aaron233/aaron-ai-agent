<template>
  <div class="fortune-master-container">
    <header class="header">
      <button type="button" class="back-button" @click="goBack">返回首页</button>
      <div>
        <h1 class="title">AI 算命大师</h1>
        <p class="subtitle">结合多轮对话记忆，先了解你的问题，再给出命理角度的参考分析。</p>
      </div>
      <div class="header-meta">
        <div class="user-badge">{{ authState.user?.nickname }}</div>
        <div class="chat-id">会话ID: {{ chatId }}</div>
      </div>
    </header>

    <main class="content-wrapper">
      <aside class="side-panel">
        <div class="panel-block">
          <div class="panel-title">建议先提供</div>
          <ul>
            <li>出生年月日时</li>
            <li>想重点咨询的方向</li>
            <li>最近遇到的具体问题</li>
          </ul>
        </div>
        <div class="panel-block warm">
          <div class="panel-title">咨询范围</div>
          <ul>
            <li>八字命理</li>
            <li>流年趋势</li>
            <li>居家风水</li>
            <li>手相启发</li>
          </ul>
        </div>
      </aside>
      <section class="chat-area">
        <ChatRoom
          :messages="messages"
          :connection-status="connectionStatus"
          @send-message="sendMessage"
        />
      </section>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useHead } from '@vueuse/head'
import { useRouter } from 'vue-router'
import { chatWithFortuneApp } from '../api'
import { authState } from '../auth'
import AppFooter from '../components/AppFooter.vue'
import ChatRoom from '../components/ChatRoom.vue'

useHead({
  title: 'AI 算命大师对话',
  meta: [
    {
      name: 'description',
      content: 'AI 算命大师聊天页，支持流式对话和多轮上下文记忆。'
    }
  ]
})

const router = useRouter()
const messages = ref([])
const chatId = ref('')
const connectionStatus = ref('disconnected')
let eventSource = null

const generateChatId = () => `fortune_${Math.random().toString(36).slice(2, 10)}`

const addMessage = (content, isUser) => {
  messages.value.push({
    content,
    isUser,
    time: Date.now()
  })
}

const sendMessage = (message) => {
  addMessage(message, true)

  if (eventSource) {
    eventSource.close()
  }

  const aiMessageIndex = messages.value.length
  addMessage('', false)

  connectionStatus.value = 'connecting'
  eventSource = chatWithFortuneApp(message, chatId.value)

  eventSource.onmessage = (event) => {
    const data = event.data
    if (data === '[DONE]') {
      connectionStatus.value = 'disconnected'
      eventSource.close()
      return
    }
    if (aiMessageIndex < messages.value.length) {
      messages.value[aiMessageIndex].content += data
    }
  }

  eventSource.onerror = () => {
    connectionStatus.value = 'error'
    if (aiMessageIndex < messages.value.length && !messages.value[aiMessageIndex].content) {
      messages.value[aiMessageIndex].content = '连接中断，请检查后端服务或稍后重试。'
    }
    eventSource?.close()
  }
}

const goBack = () => {
  router.push('/dashboard')
}

onMounted(() => {
  chatId.value = generateChatId()
  addMessage('欢迎来到 AI 算命大师。你可以告诉我出生信息、想咨询的方向，以及最近最困扰你的问题。', false)
})

onBeforeUnmount(() => {
  eventSource?.close()
})
</script>

<style scoped>
.fortune-master-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  width: min(1180px, calc(100% - 32px));
  margin: 20px auto 0;
  padding: 24px 28px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 20px;
  align-items: center;
  background: rgba(255, 248, 238, 0.78);
  border: 1px solid rgba(112, 72, 34, 0.14);
  border-radius: 30px;
  box-shadow: 0 18px 52px rgba(96, 64, 37, 0.08);
}

.back-button {
  border: none;
  border-radius: 999px;
  padding: 10px 18px;
  background: rgba(117, 73, 38, 0.08);
  color: #5b391f;
}

.title {
  margin: 0;
  font-size: 32px;
  color: #402617;
}

.subtitle {
  margin-top: 6px;
  color: #76533a;
}

.chat-id {
  color: #8b6543;
  font-size: 14px;
}

.header-meta {
  text-align: right;
}

.user-badge {
  margin-bottom: 6px;
  color: #6f4729;
}

.content-wrapper {
  width: min(1180px, calc(100% - 32px));
  margin: 18px auto 32px;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 18px;
  flex: 1;
}

.side-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-block {
  padding: 24px 20px;
  border-radius: 28px;
  background: rgba(255, 249, 241, 0.72);
  border: 1px solid rgba(112, 72, 34, 0.14);
  color: #5c3a24;
}

.panel-block.warm {
  background: linear-gradient(180deg, rgba(244, 230, 208, 0.92), rgba(233, 204, 161, 0.92));
}

.panel-title {
  font-weight: 700;
  margin-bottom: 12px;
}

ul {
  padding-left: 18px;
  line-height: 1.9;
}

.chat-area {
  min-width: 0;
}

@media (max-width: 980px) {
  .header,
  .content-wrapper {
    width: min(100% - 20px, 1180px);
  }

  .content-wrapper {
    grid-template-columns: 1fr;
  }

  .side-panel {
    order: 2;
  }

  .header {
    grid-template-columns: 1fr;
  }

  .chat-id,
  .header-meta {
    display: none;
  }
}
</style>
