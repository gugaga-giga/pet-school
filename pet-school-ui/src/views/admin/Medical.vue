<template>
  <div class="medical-page">
    <div class="page-header">
      <h1 class="page-title">医疗服务管理</h1>
      <p class="page-desc">管理宠物疫苗接种、驱虫与手术记录</p>
    </div>

    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">
              <span class="card-icon card-icon-vaccine">💉</span>
              添加疫苗记录
            </h3>
          </div>
          <div class="form-group">
            <label>宠物ID</label>
            <input v-model.number="vForm.petId" placeholder="宠物ID" />
          </div>
          <div class="form-group">
            <label>疫苗名称</label>
            <input v-model="vForm.vaccineName" placeholder="如狂犬疫苗" />
          </div>
          <div class="form-group">
            <label>接种日期</label>
            <input v-model="vForm.injectDate" type="date" />
          </div>
          <div class="form-group">
            <label>下次接种</label>
            <input v-model="vForm.nextDate" type="date" />
          </div>
          <button class="btn btn-primary" @click="addVaccine"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg> 添加</button>
          <div v-if="vMsg" :class="['alert', vMsgType === 'success' ? 'alert-success' : 'alert-error']" style="margin-top: var(--space-3)">{{ vMsg }}</div>
        </div>
      </div>

      <div class="col">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">
              <span class="card-icon card-icon-deworm">🐛</span>
              添加驱虫记录
            </h3>
          </div>
          <div class="form-group">
            <label>宠物ID</label>
            <input v-model.number="dForm.petId" placeholder="宠物ID" />
          </div>
          <div class="form-group">
            <label>驱虫类型</label>
            <select v-model.number="dForm.dewormType">
              <option :value="1">体内</option>
              <option :value="2">体外</option>
            </select>
          </div>
          <div class="form-group">
            <label>驱虫日期</label>
            <input v-model="dForm.doDate" type="date" />
          </div>
          <div class="form-group">
            <label>下次驱虫</label>
            <input v-model="dForm.nextDate" type="date" />
          </div>
          <button class="btn btn-secondary" @click="addDeworming"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg> 添加</button>
          <div v-if="dMsg" :class="['alert', dMsgType === 'success' ? 'alert-success' : 'alert-error']" style="margin-top: var(--space-3)">{{ dMsg }}</div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <h3 class="card-title">查询宠物医疗记录</h3>
      </div>
      <div class="query-row">
        <div class="form-group" style="flex: 1; margin-bottom: 0">
          <input v-model.number="queryPetId" placeholder="输入宠物ID" />
        </div>
        <button class="btn btn-sm btn-primary" @click="loadVaccine">疫苗</button>
        <button class="btn btn-sm btn-secondary" @click="loadDeworming">驱虫</button>
        <button class="btn btn-sm btn-secondary" @click="loadSurgery">手术</button>
      </div>

      <div v-if="vaccines.length" class="table-section">
        <h4 class="section-title">疫苗记录</h4>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>疫苗</th>
                <th>接种日</th>
                <th>下次</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="v in vaccines" :key="v.id">
                <td class="td-name">{{ v.vaccineName }}</td>
                <td>{{ v.injectDate }}</td>
                <td>{{ v.nextDate || '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="dewormings.length" class="table-section">
        <h4 class="section-title">驱虫记录</h4>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>类型</th>
                <th>日期</th>
                <th>下次</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="d in dewormings" :key="d.id">
                <td>
                  <span :class="['tag', d.dewormType === 1 ? 'tag-primary' : 'tag-accent']">
                    {{ d.dewormType === 1 ? '体内' : '体外' }}
                  </span>
                </td>
                <td>{{ d.doDate }}</td>
                <td>{{ d.nextDate || '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="surgeries.length" class="table-section">
        <h4 class="section-title">手术记录</h4>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>手术名</th>
                <th>日期</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in surgeries" :key="s.id">
                <td class="td-name">{{ s.surgeryName }}</td>
                <td>{{ s.surgeryDate }}</td>
                <td>
                  <span :class="['tag', surgeryTagClass(s.status)]">
                    {{ ['', '术前', '术中', '恢复', '完成'][s.status] }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { adminApi } from '../../api'
const vForm = ref({ petId: null, vaccineName: '', injectDate: '', nextDate: '' })
const vMsg = ref(''), vMsgType = ref('success')
const dForm = ref({ petId: null, dewormType: 1, doDate: '', nextDate: '' })
const dMsg = ref(''), dMsgType = ref('success')
const queryPetId = ref(''), vaccines = ref([]), dewormings = ref([]), surgeries = ref([])
function surgeryTagClass(status) {
  const map = { 1: 'tag-warning', 2: 'tag-danger', 3: 'tag-info', 4: 'tag-success' }
  return map[status] || 'tag-info'
}
async function addVaccine() {
  if (!vForm.value.petId || !vForm.value.vaccineName || !vForm.value.injectDate) { vMsg.value = '必填项不能为空'; vMsgType.value = 'error'; return }
  try { const res = await adminApi.addVaccine(vForm.value); if (res.code === 200) { vMsg.value = '添加成功'; vMsgType.value = 'success'; vForm.value = { petId: null, vaccineName: '', injectDate: '', nextDate: '' } } else { vMsg.value = res.message; vMsgType.value = 'error' } } catch (e) { vMsg.value = '请求失败'; vMsgType.value = 'error' }
}
async function addDeworming() {
  if (!dForm.value.petId || !dForm.value.doDate) { dMsg.value = '必填项不能为空'; dMsgType.value = 'error'; return }
  try { const res = await adminApi.addDeworming(dForm.value); if (res.code === 200) { dMsg.value = '添加成功'; dMsgType.value = 'success'; dForm.value = { petId: null, dewormType: 1, doDate: '', nextDate: '' } } else { dMsg.value = res.message; dMsgType.value = 'error' } } catch (e) { dMsg.value = '请求失败'; dMsgType.value = 'error' }
}
async function loadVaccine() { vaccines.value = []; dewormings.value = []; surgeries.value = []; if (!queryPetId.value) return; try { const res = await adminApi.vaccineByPet(queryPetId.value); if (res.code === 200) vaccines.value = res.data } catch (e) { } }
async function loadDeworming() { vaccines.value = []; dewormings.value = []; surgeries.value = []; if (!queryPetId.value) return; try { const res = await adminApi.dewormingByPet(queryPetId.value); if (res.code === 200) dewormings.value = res.data } catch (e) { } }
async function loadSurgery() { vaccines.value = []; dewormings.value = []; surgeries.value = []; if (!queryPetId.value) return; try { const res = await adminApi.surgeryByPet(queryPetId.value); if (res.code === 200) surgeries.value = res.data } catch (e) { } }
</script>

<style scoped>
.medical-page {
  animation: fadeInUp var(--transition-base) ease both;
}

.page-header {
  margin-bottom: var(--space-5);
}

.page-desc {
  font-size: var(--font-size-base);
  color: var(--text-muted);
  margin-top: var(--space-2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-light);
}

.card-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.card-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
}

.card-icon-vaccine {
  background: var(--color-info-bg);
}

.card-icon-deworm {
  background: var(--color-success-bg);
}

.query-row {
  display: flex;
  gap: var(--space-3);
  align-items: flex-end;
  margin-bottom: var(--space-4);
}

.table-section {
  margin-top: var(--space-4);
  animation: fadeInUp var(--transition-base) ease both;
}

.section-title {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  margin-bottom: var(--space-3);
  padding-bottom: var(--space-2);
  border-bottom: 1px solid var(--border-light);
}

.table-wrap {
  overflow-x: auto;
}

.td-name {
  font-weight: var(--font-weight-medium);
  color: var(--text-title);
}
</style>
