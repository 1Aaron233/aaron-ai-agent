<template>
  <div class="dashboard-page">
    <header class="dashboard-header">
      <div>
        <p class="kicker">Fortune AI Admin</p>
        <h1>RBAC 工作台</h1>
        <p class="summary">当前用户：{{ authState.user?.nickname }}（{{ authState.user?.username }}）</p>
      </div>
      <div class="header-actions">
        <button type="button" class="ghost-button" @click="router.push('/fortune-master')">进入 AI 对话</button>
        <button type="button" class="danger-button" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <main class="dashboard-main">
      <aside class="sidebar">
        <router-link
          v-for="menu in authState.menus"
          :key="menu.path"
          :to="menu.path"
          class="sidebar-link"
          :class="{ active: route.path === menu.path }"
        >
          <span>{{ menu.name }}</span>
          <small>{{ menu.permission }}</small>
        </router-link>
      </aside>

      <section class="content-panel">
        <div v-if="section === 'overview'" class="overview-grid">
          <article class="metric-card">
            <span class="metric-label">用户角色</span>
            <strong>{{ authState.user?.roles?.join(' / ') }}</strong>
          </article>
          <article class="metric-card">
            <span class="metric-label">权限数量</span>
            <strong>{{ authState.user?.permissions?.length || 0 }}</strong>
          </article>
          <article class="metric-card wide">
            <span class="metric-label">权限清单</span>
            <div class="tag-row">
              <span v-for="permission in authState.user?.permissions || []" :key="permission" class="tag">
                {{ permission }}
              </span>
            </div>
          </article>
        </div>

        <div v-else-if="section === 'users'" class="table-card">
          <div class="card-title">用户列表</div>
          <p v-if="loadingUsers" class="helper-text">加载中...</p>
          <p v-else-if="userError" class="error-text">{{ userError }}</p>
          <table v-else>
            <thead>
              <tr>
                <th>ID</th>
                <th>用户名</th>
                <th>昵称</th>
                <th>状态</th>
                <th>角色</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in users" :key="item.userId">
                <td>{{ item.userId }}</td>
                <td>{{ item.username }}</td>
                <td>{{ item.nickname }}</td>
                <td>{{ item.status }}</td>
                <td>{{ item.roles.join('、') }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else-if="section === 'roles'" class="table-card">
          <div class="card-title">角色权限</div>
          <p v-if="loadingRoles" class="helper-text">加载中...</p>
          <p v-else-if="roleError" class="error-text">{{ roleError }}</p>
          <table v-else>
            <thead>
              <tr>
                <th>角色编码</th>
                <th>角色名称</th>
                <th>权限</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in roles" :key="item.code">
                <td>{{ item.code }}</td>
                <td>{{ item.name }}</td>
                <td>
                  <div class="tag-row">
                    <span v-for="permission in item.permissions" :key="permission" class="tag">{{ permission }}</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearSession, authState } from '../auth'
import { getRoles, getUsers } from '../api'

const route = useRoute()
const router = useRouter()

const users = ref([])
const roles = ref([])
const loadingUsers = ref(false)
const loadingRoles = ref(false)
const userError = ref('')
const roleError = ref('')
const section = computed(() => route.meta.section || 'overview')

const loadUsers = async () => {
  loadingUsers.value = true
  userError.value = ''
  try {
    const result = await getUsers()
    users.value = result.data
  } catch (error) {
    userError.value = error.message || '加载用户失败'
  } finally {
    loadingUsers.value = false
  }
}

const loadRoles = async () => {
  loadingRoles.value = true
  roleError.value = ''
  try {
    const result = await getRoles()
    roles.value = result.data
  } catch (error) {
    roleError.value = error.message || '加载角色失败'
  } finally {
    loadingRoles.value = false
  }
}

const handleLogout = () => {
  clearSession()
  router.push('/login')
}

watch(
  section,
  async (value) => {
    if (value === 'users' && users.value.length === 0) {
      await loadUsers()
    }
    if (value === 'roles' && roles.value.length === 0) {
      await loadRoles()
    }
  },
  { immediate: true }
)

onMounted(() => {
  if (!authState.menus.length) {
    router.push('/dashboard')
  }
})
</script>

<style scoped>
.dashboard-page {
  min-height: 100vh;
  padding: 20px;
}

.dashboard-header,
.content-panel,
.sidebar {
  border: 1px solid rgba(112, 72, 34, 0.14);
  background: rgba(255, 249, 241, 0.78);
  box-shadow: 0 18px 52px rgba(96, 64, 37, 0.08);
}

.dashboard-header {
  width: min(1180px, 100%);
  margin: 0 auto 18px;
  padding: 28px 30px;
  border-radius: 30px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.kicker {
  text-transform: uppercase;
  letter-spacing: 0.14em;
  font-size: 12px;
  color: #8a603c;
}

h1 {
  margin-top: 10px;
  color: #452717;
}

.summary {
  margin-top: 8px;
  color: #735239;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.ghost-button,
.danger-button {
  border: none;
  border-radius: 999px;
  padding: 12px 18px;
}

.ghost-button {
  background: rgba(109, 67, 37, 0.08);
  color: #5e3d23;
}

.danger-button {
  background: #6f2f1b;
  color: #fff3eb;
}

.dashboard-main {
  width: min(1180px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 18px;
}

.sidebar {
  border-radius: 28px;
  padding: 18px;
  display: grid;
  gap: 12px;
  align-content: start;
}

.sidebar-link {
  border-radius: 20px;
  padding: 14px 16px;
  display: grid;
  gap: 4px;
  background: rgba(255, 255, 255, 0.58);
}

.sidebar-link small {
  color: #876444;
}

.sidebar-link.active {
  background: linear-gradient(135deg, #71321e, #bc8443);
  color: #fff7ef;
}

.sidebar-link.active small {
  color: rgba(255, 246, 236, 0.82);
}

.content-panel {
  border-radius: 28px;
  padding: 22px;
  min-height: 520px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.metric-card,
.table-card {
  border-radius: 24px;
  padding: 22px;
  background: rgba(255, 255, 255, 0.66);
}

.metric-card.wide {
  grid-column: 1 / -1;
}

.metric-label,
.card-title {
  color: #7f5b3a;
  font-size: 14px;
}

strong {
  display: block;
  margin-top: 10px;
  color: #402617;
  font-size: 22px;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.tag {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(121, 76, 41, 0.1);
  color: #6a442a;
  font-size: 13px;
}

.helper-text {
  color: #876444;
  margin-top: 10px;
}

.error-text {
  color: #a53030;
  margin-top: 10px;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 18px;
}

th,
td {
  text-align: left;
  padding: 14px 10px;
  border-bottom: 1px solid rgba(125, 86, 52, 0.12);
  vertical-align: top;
}

th {
  color: #765238;
  font-weight: 700;
}

@media (max-width: 960px) {
  .dashboard-header,
  .dashboard-main {
    width: min(100%, 1180px);
  }

  .dashboard-header,
  .dashboard-main {
    grid-template-columns: 1fr;
  }

  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
