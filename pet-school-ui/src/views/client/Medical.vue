<template>
  <div class="medical-page">
    <div class="medical-header">
      <h1 class="page-title">🏥 宠物医疗服务</h1>
      <p class="page-subtitle">专业兽医团队，为爱宠健康保驾护航</p>
      <div class="sub-nav">
        <router-link to="/client/medical" class="sub-nav-item active">
          <span class="sub-nav-icon">🩺</span>预约挂号
        </router-link>
        <router-link to="/client/medical-order" class="sub-nav-item">
          <span class="sub-nav-icon">📋</span>我的预约
        </router-link>
        <router-link to="/client/medical-record" class="sub-nav-item">
          <span class="sub-nav-icon">📄</span>我的病历
        </router-link>
      </div>
    </div>

    <div class="medical-layout">
      <div class="dept-sidebar">
        <h4 class="sidebar-title">科室分类</h4>
        <div class="dept-list">
          <div
            v-for="dept in departments"
            :key="dept.id"
            class="dept-item"
            :class="{ 'dept-item--active': selDept?.id === dept.id }"
            @click="selectDept(dept)"
          >
            <span class="dept-icon">{{ dept.icon || '🩺' }}</span>
            <div class="dept-info">
              <span class="dept-name">{{ dept.name }}</span>
              <span class="dept-price">¥{{ dept.price || dept.consultationFee }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="doctor-panel">
        <div v-if="selDept" class="doctor-header">
          <h3 class="doctor-panel-title">{{ selDept.name }} · 医生列表</h3>
          <span class="doctor-count">{{ doctors.length }} 位医生</span>
        </div>
        <div v-if="doctors.length" class="doctor-grid">
          <div
            v-for="doc in doctors"
            :key="doc.id"
            class="doctor-card"
            :class="{ 'doctor-card--selected': selDoctor?.id === doc.id }"
          >
            <div class="doctor-avatar">
              <img v-if="doc.avatar" :src="doc.avatar" alt="" />
              <span v-else class="avatar-placeholder">{{ (doc.name || '?')[0] }}</span>
            </div>
            <div class="doctor-body">
              <div class="doctor-top">
                <h4 class="doctor-name">{{ doc.name }}</h4>
                <span class="doctor-title">{{ doc.title || doc.titleName }}</span>
              </div>
              <p class="doctor-exp">{{ doc.experienceYear }}年经验</p>
              <p class="doctor-specialty">{{ doc.specialty || doc.specialization || '综合诊疗' }}</p>
            </div>
            <button
              class="btn btn-primary btn-sm doctor-book-btn"
              @click="selectDoctor(doc)"
            >
              {{ selDoctor?.id === doc.id ? '已选择' : '立即预约' }}
            </button>
          </div>
        </div>
        <div v-else-if="selDept" class="empty-doctors">
          <div class="empty-icon">👨‍⚕️</div>
          <p class="empty-text">该科室暂无医生</p>
        </div>
        <div v-else class="empty-doctors">
          <div class="empty-icon">🩺</div>
          <p class="empty-text">请先选择科室</p>
          <p class="empty-hint">从左侧选择一个科室查看医生</p>
        </div>
      </div>

      <div class="booking-panel">
        <template v-if="selDoctor">
          <h4 class="booking-title">预约信息</h4>
          <div class="booking-doctor-info">
            <div class="booking-doctor-avatar">
              <span class="avatar-placeholder-sm">{{ (selDoctor.name || '?')[0] }}</span>
            </div>
            <div>
              <p class="booking-doctor-name">{{ selDoctor.name }}</p>
              <p class="booking-doctor-dept">{{ selDept?.name }}</p>
            </div>
          </div>

          <div class="form-group">
            <label>选择宠物</label>
            <select v-model="form.petId">
              <option :value="null" disabled>请选择宠物</option>
              <option v-for="p in myPets" :key="p.id" :value="p.id">{{ p.name }}（{{ p.breed || p.type }}）</option>
            </select>
          </div>

          <div class="form-group">
            <label>预约时间</label>
            <input type="datetime-local" v-model="form.appointmentTime" />
          </div>

          <div class="form-group">
            <label>症状描述</label>
            <textarea v-model="form.symptoms" rows="3" placeholder="请描述宠物的症状或就诊原因"></textarea>
          </div>

          <div class="fee-display">
            <span class="fee-label">就诊费用</span>
            <span class="fee-value">¥{{ selDept?.price || selDept?.consultationFee || 0 }}</span>
          </div>

          <div v-if="msg" :class="['alert', msgType === 'success' ? 'alert-success' : 'alert-error']">
            {{ msg }}
          </div>

          <button
            class="btn btn-primary booking-submit-btn"
            :disabled="submitting"
            @click="placeOrder"
          >
            {{ submitting ? '提交中...' : '确认预约' }}
          </button>
        </template>

        <div v-else class="empty-booking">
          <div class="empty-icon">📋</div>
          <p class="empty-text">请选择医生</p>
          <p class="empty-hint">选择医生后在此填写预约信息</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { medicalApi, petApi } from '../../api'

const user = computed(() => JSON.parse(localStorage.getItem('client_user') || 'null'))

const departments = ref([])
const selDept = ref(null)
const doctors = ref([])
const selDoctor = ref(null)
const myPets = ref([])
const submitting = ref(false)
const msg = ref('')
const msgType = ref('')

const form = ref({
  petId: null,
  appointmentTime: '',
  symptoms: ''
})

async function loadDepartments() {
  try {
    const res = await medicalApi.departments()
    departments.value = res.data || res || []
  } catch (e) {
    console.error(e)
  }
}

async function selectDept(dept) {
  selDept.value = dept
  selDoctor.value = null
  form.value = { petId: null, appointmentTime: '', symptoms: '' }
  msg.value = ''
  try {
    const res = await medicalApi.doctorsByDepartment(dept.id)
    doctors.value = res.data || res || []
  } catch (e) {
    try {
      const res = await medicalApi.doctors()
      const all = res.data || res || []
      doctors.value = all.filter(d => d.departmentId === dept.id)
    } catch (e2) {
      doctors.value = []
    }
  }
}

function selectDoctor(doc) {
  selDoctor.value = doc
  msg.value = ''
}

async function loadMyPets() {
  if (!user.value?.id) return
  try {
    const res = await petApi.getByUserId(user.value.id)
    myPets.value = res.data || res || []
  } catch (e) {
    console.error(e)
  }
}

async function placeOrder() {
  if (!form.value.petId) { msg.value = '请选择宠物'; msgType.value = 'error'; return }
  if (!form.value.appointmentTime) { msg.value = '请选择预约时间'; msgType.value = 'error'; return }
  if (!selDoctor.value) { msg.value = '请选择医生'; msgType.value = 'error'; return }

  submitting.value = true
  msg.value = ''
  try {
    await medicalApi.createOrder({
      userId: user.value.id,
      petId: form.value.petId,
      doctorId: selDoctor.value.id,
      departmentId: selDept.value.id,
      appointmentTime: form.value.appointmentTime,
      symptom: form.value.symptoms
    })
    msg.value = '预约成功！'
    msgType.value = 'success'
    selDoctor.value = null
    form.value = { petId: null, appointmentTime: '', symptoms: '' }
  } catch (e) {
    msg.value = e.response?.data?.message || '预约失败，请重试'
    msgType.value = 'error'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadDepartments()
  loadMyPets()
})
</script>

<style scoped>
.medical-page {
  min-height: 100vh;
  background: var(--bg-page);
  padding: var(--space-6) var(--space-8);
}

.medical-header {
  text-align: center;
  margin-bottom: var(--space-8);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.page-subtitle {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin: 0;
}

.sub-nav {
  display: inline-flex;
  justify-content: center;
  gap: var(--space-2);
  margin-top: var(--space-5);
  padding: var(--space-2);
  background: var(--bg-card);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.sub-nav-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: 8px 20px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--text-body);
  text-decoration: none;
  transition: all var(--transition-base);
  white-space: nowrap;
}

.sub-nav-item:hover {
  background: var(--bg-hover);
  color: var(--color-primary);
}

.sub-nav-item.active,
.sub-nav-item.router-link-active {
  background: var(--color-primary);
  color: var(--text-inverse);
  box-shadow: var(--shadow-button);
}

.sub-nav-icon {
  font-size: 14px;
}

.medical-layout {
  display: flex;
  gap: var(--space-5);
  align-items: flex-start;
}

.dept-sidebar {
  width: 200px;
  flex-shrink: 0;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--space-4);
  position: sticky;
  top: var(--space-6);
  border: 1px solid var(--border-light);
}

.sidebar-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--text-muted);
  margin: 0 0 var(--space-3) 0;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.dept-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.dept-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
  border: 1.5px solid transparent;
}

.dept-item:hover {
  background: var(--color-primary-bg);
  border-color: rgba(79, 124, 255, 0.15);
}

.dept-item--active {
  background: var(--color-primary-bg);
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(79, 124, 255, 0.1);
}

.dept-icon {
  font-size: 20px;
  width: 28px;
  text-align: center;
  flex-shrink: 0;
}

.dept-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.dept-name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dept-item--active .dept-name {
  color: var(--color-primary);
}

.dept-price {
  font-size: var(--font-size-xs);
  color: var(--color-accent);
  font-weight: var(--font-weight-semibold);
}

.doctor-panel {
  flex: 1;
  min-width: 0;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
  min-height: 600px;
  border: 1px solid var(--border-light);
}

.doctor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.doctor-panel-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  margin: 0;
}

.doctor-count {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  background: var(--bg-hover);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
}

.doctor-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}

.doctor-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  border: 1.5px solid var(--border-light);
  background: var(--bg-card);
  transition: all var(--transition-base);
  text-align: center;
  position: relative;
}

.doctor-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-card-hover);
  border-color: var(--color-primary-bg);
}

.doctor-card--selected {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-card-hover), 0 0 0 3px var(--color-primary-bg);
  background: linear-gradient(135deg, rgba(79, 124, 255, 0.02), rgba(255, 184, 107, 0.02));
}

.doctor-avatar {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-full);
  overflow: hidden;
  margin-bottom: var(--space-3);
  background: linear-gradient(135deg, var(--color-primary-bg), var(--color-accent-bg));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.doctor-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.doctor-body {
  margin-bottom: var(--space-3);
}

.doctor-top {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.doctor-name {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  margin: 0;
}

.doctor-title {
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  background: var(--color-primary-bg);
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-weight: var(--font-weight-semibold);
}

.doctor-exp {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin: 0 0 var(--space-1) 0;
}

.doctor-specialty {
  font-size: var(--font-size-sm);
  color: var(--text-body);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.doctor-book-btn {
  width: 100%;
  margin-top: auto;
}

.doctor-card--selected .doctor-book-btn {
  background: var(--color-success);
  box-shadow: 0 2px 8px rgba(60, 203, 127, 0.3);
}

.empty-doctors {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: var(--text-muted);
}

.empty-icon {
  font-size: 56px;
  margin-bottom: var(--space-4);
  opacity: 0.5;
}

.empty-text {
  font-size: var(--font-size-md);
  color: var(--text-muted);
  margin: 0 0 var(--space-1) 0;
}

.empty-hint {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  opacity: 0.7;
  margin: 0;
}

.booking-panel {
  width: 320px;
  flex-shrink: 0;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
  position: sticky;
  top: var(--space-6);
  border: 1px solid var(--border-light);
  min-height: 500px;
}

.booking-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--text-title);
  margin: 0 0 var(--space-4) 0;
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-light);
}

.booking-doctor-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  background: linear-gradient(135deg, var(--color-primary-bg), rgba(255, 184, 107, 0.06));
  border-radius: var(--radius-md);
  margin-bottom: var(--space-4);
}

.booking-doctor-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-placeholder-sm {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-bold);
  color: var(--text-inverse);
}

.booking-doctor-name {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  margin: 0;
}

.booking-doctor-dept {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  margin: 0;
}

.fee-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  background: var(--color-accent-bg);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-4);
  border: 1px solid rgba(255, 184, 107, 0.2);
}

.fee-label {
  font-size: var(--font-size-sm);
  color: var(--text-body);
  font-weight: var(--font-weight-medium);
}

.fee-value {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-accent);
}

.booking-submit-btn {
  width: 100%;
  padding: var(--space-3);
  font-size: var(--font-size-base);
  border-radius: var(--radius-md);
  font-weight: var(--font-weight-semibold);
  margin-top: var(--space-2);
}

.booking-submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.empty-booking {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: var(--text-muted);
}

@media (max-width: 1100px) {
  .doctor-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 860px) {
  .medical-layout {
    flex-direction: column;
  }

  .dept-sidebar {
    width: 100%;
    position: static;
  }

  .dept-list {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .booking-panel {
    width: 100%;
    position: static;
  }
}
</style>
