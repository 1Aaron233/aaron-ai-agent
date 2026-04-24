<template>
  <div class="chat-container">
    <div ref="messagesContainer" class="chat-messages">
      <div v-for="(msg, index) in messages" :key="index" class="message-row" :class="{ user: msg.isUser }">
        <div v-if="!msg.isUser" class="avatar ai-avatar">
          <AiAvatarFallback />
        </div>
        <div class="message-bubble">
          <div class="message-content">
            {{ msg.content }}
            <span v-if="connectionStatus === 'connecting' && index === messages.length - 1 && !msg.isUser" class="typing-indicator">▋</span>
          </div>
          <div class="message-time">{{ formatTime(msg.time) }}</div>
        </div>
        <div v-if="msg.isUser" class="avatar user-avatar">我</div>
      </div>
    </div>

    <div class="quick-prompts">
      <button
        v-for="prompt in quickPrompts"
        :key="prompt"
        type="button"
        class="quick-prompt"
        :disabled="connectionStatus === 'connecting'"
        @click="emitPrompt(prompt)"
      >
        {{ prompt }}
      </button>
    </div>

    <div class="chat-input-container">
      <textarea
        v-model="inputMessage"
        class="input-box"
        :disabled="connectionStatus === 'connecting'"
        placeholder="输入你的困惑，例如：我想看事业和财运，需要提供哪些信息？"
        @keydown.enter.exact.prevent="sendMessage"
      />
      <button
        type="button"
        class="send-button"
        :disabled="connectionStatus === 'connecting' || !inputMessage.trim()"
        @click="sendMessage"
      >
        发送
      </button>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref, watch } from 'vue'
import AiAvatarFallback from './AiAvatarFallback.vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  connectionStatus: {
    type: String,
    default: 'disconnected'
  }
})

const emit = defineEmits(['send-message'])

const inputMessage = ref('')
const messagesContainer = ref(null)
const quickPrompts = [
  '帮我看看近期事业运',
  '我适合往哪个方向发展',
  '想了解今年桃花运',
  '家里财位怎么布置'
]

const sendMessage = () => {
  const message = inputMessage.value.trim()
  if (!message) {
    return
  }
  emit('send-message', message)
  inputMessage.value = ''
}

const emitPrompt = (prompt) => {
  emit('send-message', prompt)
}

const formatTime = (timestamp) =>
  new Date(timestamp).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

watch(() => props.messages.length, scrollToBottom)
watch(() => props.messages.map((message) => message.content).join(''), scrollToBottom)

onMounted(scrollToBottom)
</script>

<style scoped>
.chat-container {
  display: grid;
  grid-template-rows: 1fr auto auto;
  min-height: 68vh;
  background: rgba(255, 251, 245, 0.76);
  border: 1px solid rgba(128, 91, 53, 0.15);
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 24px 70px rgba(92, 58, 30, 0.12);
}

.chat-messages {
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 88%;
}

.message-row.user {
  align-self: flex-end;
}

.message-row.user .message-bubble {
  background: linear-gradient(135deg, #7f3d22, #a76031);
  color: #fff9f2;
}

.message-bubble {
  padding: 14px 16px;
  border-radius: 20px;
  background: #f1e4d2;
  color: #332015;
}

.message-content {
  white-space: pre-wrap;
  line-height: 1.7;
}

.message-time {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.68;
  text-align: right;
}

.avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-avatar {
  background: #e3c49a;
  color: #59311b;
  font-weight: 700;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 0 20px 16px;
}

.quick-prompt {
  border: 1px solid rgba(126, 81, 42, 0.18);
  background: rgba(255, 248, 239, 0.96);
  color: #684226;
  padding: 8px 14px;
  border-radius: 999px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.quick-prompt:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(114, 75, 42, 0.12);
}

.chat-input-container {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  padding: 18px 20px 20px;
  background: rgba(251, 243, 232, 0.95);
  border-top: 1px solid rgba(128, 91, 53, 0.12);
}

.input-box {
  min-height: 60px;
  max-height: 150px;
  resize: vertical;
  border: 1px solid rgba(128, 91, 53, 0.16);
  border-radius: 18px;
  padding: 14px 16px;
  background: #fffdf9;
  color: #2b1b10;
  outline: none;
}

.input-box:focus {
  border-color: #aa6e39;
  box-shadow: 0 0 0 3px rgba(170, 110, 57, 0.12);
}

.send-button {
  min-width: 100px;
  border: none;
  border-radius: 18px;
  background: linear-gradient(135deg, #7c2f1c, #bf8746);
  color: #fffaf2;
  padding: 0 18px;
}

.typing-indicator {
  display: inline-block;
  margin-left: 2px;
  animation: blink 0.8s infinite;
}

.input-box:disabled,
.send-button:disabled,
.quick-prompt:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@keyframes blink {
  0%, 100% { opacity: 0; }
  50% { opacity: 1; }
}

@media (max-width: 768px) {
  .chat-container {
    min-height: 74vh;
  }

  .message-row {
    max-width: 100%;
  }

  .chat-input-container {
    grid-template-columns: 1fr;
  }

  .send-button {
    min-height: 48px;
  }
}
</style>
