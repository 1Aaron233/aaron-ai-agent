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

        <div v-else-if="section === 'users'" class="management-layout">
          <article class="editor-card">
            <div class="card-head">
              <div>
                <div class="card-title">{{ editingUserId ? '编辑用户' : '新增用户' }}</div>
                <p class="helper-text">用户信息与角色分配都会直接写入数据库。</p>
              </div>
              <button v-if="editingUserId" type="button" class="ghost-button small" @click="resetUserForm">取消编辑</button>
            </div>

            <div class="form-grid">
              <label>
                <span>用户名</span>
                <input v-model.trim="userForm.username" :disabled="Boolean(editingUserId)" type="text" placeholder="如 manager01" />
              </label>
              <label>
                <span>昵称</span>
                <input v-model.trim="userForm.nickname" type="text" placeholder="显示名称" />
              </label>
              <label>
                <span>密码</span>
                <input v-model="userForm.password" type="password" :placeholder="editingUserId ? '留空则不修改密码' : '初始密码'" />
              </label>
              <label>
                <span>状态</span>
                <select v-model="userForm.status">
                  <option value="ACTIVE">ACTIVE</option>
                  <option value="DISABLED">DISABLED</option>
                </select>
              </label>
            </div>

            <div class="selection-block">
              <span class="selection-title">分配角色</span>
              <div class="check-grid">
                <label v-for="role in roles" :key="role.code" class="check-item">
                  <input v-model="userForm.roleCodes" type="checkbox" :value="role.code" />
                  <span>{{ role.name }}</span>
                  <small>{{ role.code }}</small>
                </label>
              </div>
            </div>

            <p v-if="userError" class="error-text">{{ userError }}</p>

            <div class="form-actions">
              <button type="button" class="primary-button" @click="submitUserForm">
                {{ editingUserId ? '保存用户' : '创建用户' }}
              </button>
            </div>
          </article>

          <article class="table-card">
            <div class="card-head">
              <div class="card-title">用户列表</div>
              <button type="button" class="ghost-button small" @click="loadUsers">刷新</button>
            </div>
            <p v-if="loadingUsers" class="helper-text">加载中...</p>
            <table v-else>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户名</th>
                  <th>昵称</th>
                  <th>状态</th>
                  <th>角色</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in users" :key="item.userId">
                  <td>{{ item.userId }}</td>
                  <td>{{ item.username }}</td>
                  <td>{{ item.nickname }}</td>
                  <td>{{ item.status }}</td>
                  <td>{{ item.roles.join('、') }}</td>
                  <td>
                    <button type="button" class="inline-button" @click="editUser(item)">编辑</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </article>
        </div>

        <div v-else-if="section === 'roles'" class="management-layout">
          <article class="editor-card">
            <div class="card-head">
              <div>
                <div class="card-title">{{ editingRoleCode ? '编辑角色' : '新增角色' }}</div>
                <p class="helper-text">角色与权限映射会直接影响工作台菜单和接口授权。</p>
              </div>
              <button v-if="editingRoleCode" type="button" class="ghost-button small" @click="resetRoleForm">取消编辑</button>
            </div>

            <div class="form-grid">
              <label>
                <span>角色编码</span>
                <input v-model.trim="roleForm.code" :disabled="Boolean(editingRoleCode)" type="text" placeholder="如 OPERATOR" />
              </label>
              <label>
                <span>角色名称</span>
                <input v-model.trim="roleForm.name" type="text" placeholder="如 运营人员" />
              </label>
            </div>

            <div class="selection-block">
              <span class="selection-title">分配权限</span>
              <div class="check-grid">
                <label v-for="permission in permissions" :key="permission.code" class="check-item">
                  <input v-model="roleForm.permissionCodes" type="checkbox" :value="permission.code" />
                  <span>{{ permission.name }}</span>
                  <small>{{ permission.code }}</small>
                </label>
              </div>
            </div>

            <p v-if="roleError" class="error-text">{{ roleError }}</p>

            <div class="form-actions">
              <button type="button" class="primary-button" @click="submitRoleForm">
                {{ editingRoleCode ? '保存角色' : '创建角色' }}
              </button>
            </div>
          </article>

          <article class="table-card">
            <div class="card-head">
              <div class="card-title">角色权限</div>
              <button type="button" class="ghost-button small" @click="loadRoles">刷新</button>
            </div>
            <p v-if="loadingRoles" class="helper-text">加载中...</p>
            <table v-else>
              <thead>
                <tr>
                  <th>角色编码</th>
                  <th>角色名称</th>
                  <th>权限</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in roles" :key="item.code">
                  <td>{{ item.code }}</td>
                  <td>{{ item.name }}</td>
                  <td>
                    <div class="tag-row compact">
                      <span v-for="permission in item.permissions" :key="permission" class="tag">{{ permission }}</span>
                    </div>
                  </td>
                  <td>
                    <button type="button" class="inline-button" @click="editRole(item)">编辑</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </article>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearSession, authState } from '../auth'
import {
  createRole,
  createUser,
  getPermissions,
  getRoles,
  getUsers,
  updateRole,
  updateUser
} from '../api'

const route = useRoute()
const router = useRouter()

const users = ref([])
const roles = ref([])
const permissions = ref([])
const loadingUsers = ref(false)
const loadingRoles = ref(false)
const userError = ref('')
const roleError = ref('')
const editingUserId = ref(null)
const editingRoleCode = ref('')
const section = computed(() => route.meta.section || 'overview')

const userForm = reactive({
  username: '',
  nickname: '',
  password: '',
  status: 'ACTIVE',
  roleCodes: []
})

const roleForm = reactive({
  code: '',
  name: '',
  permissionCodes: []
})

const resetUserForm = () => {
  editingUserId.value = null
  userForm.username = ''
  userForm.nickname = ''
  userForm.password = ''
  userForm.status = 'ACTIVE'
  userForm.roleCodes = []
  userError.value = ''
}

const resetRoleForm = () => {
  editingRoleCode.value = ''
  roleForm.code = ''
  roleForm.name = ''
  roleForm.permissionCodes = []
  roleError.value = ''
}

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
    const [roleResult, permissionResult] = await Promise.all([getRoles(), getPermissions()])
    roles.value = roleResult.data
    permissions.value = permissionResult.data
  } catch (error) {
    roleError.value = error.message || '加载角色失败'
  } finally {
    loadingRoles.value = false
  }
}

const ensureRolesLoaded = async () => {
  if (!roles.value.length) {
    await loadRoles()
  }
}

const submitUserForm = async () => {
  userError.value = ''
  try {
    if (editingUserId.value) {
      await updateUser(editingUserId.value, {
        nickname: userForm.nickname,
        password: userForm.password,
        status: userForm.status,
        roleCodes: userForm.roleCodes
      })
    } else {
      await createUser({
        username: userForm.username,
        nickname: userForm.nickname,
        password: userForm.password,
        status: userForm.status,
        roleCodes: userForm.roleCodes
      })
    }
    await loadUsers()
    resetUserForm()
  } catch (error) {
    userError.value = error.message || '保存用户失败'
  }
}

const submitRoleForm = async () => {
  roleError.value = ''
  try {
    if (editingRoleCode.value) {
      await updateRole(editingRoleCode.value, {
        name: roleForm.name,
        permissionCodes: roleForm.permissionCodes
      })
    } else {
      await createRole({
        code: roleForm.code,
        name: roleForm.name,
        permissionCodes: roleForm.permissionCodes
      })
    }
    await loadRoles()
    resetRoleForm()
  } catch (error) {
    roleError.value = error.message || '保存角色失败'
  }
}

const editUser = async (user) => {
  await ensureRolesLoaded()
  editingUserId.value = user.userId
  userForm.username = user.username
  userForm.nickname = user.nickname
  userForm.password = ''
  userForm.status = user.status
  userForm.roleCodes = [...user.roleCodes]
  userError.value = ''
}

const editRole = async (role) => {
  if (!permissions.value.length) {
    await loadRoles()
  }
  editingRoleCode.value = role.code
  roleForm.code = role.code
  roleForm.name = role.name
  roleForm.permissionCodes = [...role.permissions]
  roleError.value = ''
}

const handleLogout = () => {
  clearSession()
  router.push('/login')
}

watch(
  section,
  async (value) => {
    if (value === 'users') {
      await Promise.all([loadUsers(), ensureRolesLoaded()])
    }
    if (value === 'roles') {
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

.header-actions,
.form-actions {
  display: flex;
  gap: 12px;
}

.ghost-button,
.danger-button,
.primary-button,
.inline-button {
  border: none;
  border-radius: 999px;
}

.ghost-button {
  background: rgba(109, 67, 37, 0.08);
  color: #5e3d23;
  padding: 12px 18px;
}

.ghost-button.small,
.inline-button {
  padding: 8px 14px;
}

.danger-button {
  background: #6f2f1b;
  color: #fff3eb;
  padding: 12px 18px;
}

.primary-button {
  padding: 12px 20px;
  background: linear-gradient(135deg, #71321e, #bc8443);
  color: #fff7ef;
}

.inline-button {
  background: rgba(117, 73, 38, 0.1);
  color: #5b391f;
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

.overview-grid,
.management-layout {
  display: grid;
  gap: 16px;
}

.overview-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.management-layout {
  grid-template-columns: minmax(320px, 420px) minmax(0, 1fr);
}

.metric-card,
.table-card,
.editor-card {
  border-radius: 24px;
  padding: 22px;
  background: rgba(255, 255, 255, 0.66);
}

.metric-card.wide {
  grid-column: 1 / -1;
}

.card-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.metric-label,
.card-title,
.selection-title {
  color: #7f5b3a;
  font-size: 14px;
}

strong {
  display: block;
  margin-top: 10px;
  color: #402617;
  font-size: 22px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-top: 18px;
}

label {
  display: grid;
  gap: 8px;
  color: #6b4830;
}

input,
select {
  border: 1px solid rgba(127, 86, 51, 0.18);
  border-radius: 16px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.78);
}

.selection-block {
  margin-top: 18px;
}

.check-grid {
  display: grid;
  gap: 12px;
  margin-top: 12px;
}

.check-item {
  grid-template-columns: auto 1fr;
  column-gap: 10px;
  padding: 12px 14px;
  border-radius: 18px;
  background: rgba(255, 248, 239, 0.8);
  align-items: center;
}

.check-item small {
  grid-column: 2;
  color: #876444;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.tag-row.compact {
  margin-top: 0;
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
  margin-top: 8px;
  line-height: 1.6;
}

.error-text {
  color: #a53030;
  margin-top: 14px;
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

@media (max-width: 1080px) {
  .management-layout,
  .dashboard-main {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 960px) {
  .dashboard-header,
  .dashboard-main {
    width: min(100%, 1180px);
  }

  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .overview-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
