<template>
  <div class="course-page">
    <div class="page-header">
      <h1 class="page-title">课程管理</h1>
      <p class="page-desc">管理课程内容与分类信息</p>
    </div>

    <div class="row">
      <div class="col">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">课程列表</h3>
            <div class="card-actions">
              <button class="btn btn-ghost btn-sm" @click="loadCourses">
                <span class="action-icon">↻</span> 刷新
              </button>
              <button class="btn btn-primary btn-sm" @click="openAdd"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" width="14" height="14"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg> 新增课程</button>
            </div>
          </div>
          <div class="table-wrap" v-if="courses.length">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>课程名</th>
                  <th>分类</th>
                  <th>周期</th>
                  <th>月费</th>
                  <th>描述</th>
                  <th style="text-align:right">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="c in courses" :key="c.id">
                  <td><span class="cell-id">#{{ c.id }}</span></td>
                  <td><span class="cell-name">{{ c.name }}</span></td>
                  <td><span class="tag tag-primary">{{ getCategoryName(c.categoryId) }}</span></td>
                  <td>{{ c.duration }}个月</td>
                  <td><span class="cell-price">¥{{ c.monthlyPrice }}</span></td>
                  <td><span class="cell-desc">{{ c.description || '-' }}</span></td>
                  <td class="cell-actions">
                    <button class="btn btn-ghost btn-sm" @click="openEdit(c)">编辑</button>
<button class="btn btn-ghost btn-sm" @click="openPkgManager(c)">套餐</button>
<button class="btn btn-danger btn-sm" @click="handleDelete(c)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="empty-state" v-else>
            <div class="empty-icon">📚</div>
            <div class="empty-text">暂无课程数据</div>
          </div>
        </div>
      </div>

      <div class="col">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">{{ isEdit ? '编辑课程' : '新增课程' }}</h3>
            <span v-if="isEdit" class="tag tag-accent">编辑中</span>
          </div>
          <div class="form-group">
            <label>课程名称</label>
            <input v-model="courseForm.name" placeholder="请输入课程名称" />
          </div>
          <div class="form-group">
            <label>课程分类</label>
            <select v-model.number="courseForm.categoryId">
              <option :value="null" disabled>请选择分类</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </div>
          <div class="row" style="gap:var(--space-3)">
            <div class="col form-group" style="margin-bottom:0">
              <label>培训周期（月）</label>
              <input v-model.number="courseForm.duration" type="number" min="1" placeholder="如 3" />
            </div>
            <div class="col form-group" style="margin-bottom:0">
              <label>月费（元）</label>
              <input v-model.number="courseForm.monthlyPrice" type="number" min="0" step="0.01" placeholder="如 2999.00" />
            </div>
          </div>
          <div class="form-group">
            <label>课程描述</label>
            <textarea v-model="courseForm.description" rows="3" placeholder="请输入课程描述"></textarea>
          </div>
          <div class="form-actions">
            <button class="btn btn-primary" @click="handleSave">{{ isEdit ? '保存修改' : '添加课程' }}</button>
            <button v-if="isEdit" class="btn btn-secondary" @click="resetForm">取消编辑</button>
          </div>
          <div v-if="courseMsg" :class="['alert', courseMsgType==='success'?'alert-success':'alert-error']" style="margin-top:var(--space-3)">{{ courseMsg }}</div>
        </div>

        <div class="card">
          <div class="card-header">
            <h3 class="card-title">课程分类</h3>
            <button class="btn btn-ghost btn-sm" @click="showCatForm = !showCatForm">
              {{ showCatForm ? '收起' : '+ 新增分类' }}
            </button>
          </div>

          <div v-if="showCatForm" class="cat-form-area">
            <div class="form-group">
              <label>分类名称</label>
              <input v-model="catForm.name" placeholder="如 基础训练" />
            </div>
            <div class="form-group">
              <label>排序</label>
              <input v-model.number="catForm.sortOrder" type="number" min="0" placeholder="数字越小越靠前" />
            </div>
            <div class="form-actions">
              <button class="btn btn-primary btn-sm" @click="handleCatSave">{{ isCatEdit ? '保存修改' : '添加分类' }}</button>
              <button v-if="isCatEdit" class="btn btn-secondary btn-sm" @click="resetCatForm">取消</button>
            </div>
          </div>

          <div class="table-wrap" v-if="categories.length">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>分类名</th>
                  <th>排序</th>
                  <th style="text-align:right">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="cat in categories" :key="cat.id">
                  <td><span class="cell-id">#{{ cat.id }}</span></td>
                  <td><span class="cell-name">{{ cat.name }}</span></td>
                  <td>{{ cat.sortOrder }}</td>
                  <td class="cell-actions">
                    <button class="btn btn-ghost btn-sm" @click="openCatEdit(cat)">编辑</button>
<button class="btn btn-danger btn-sm" @click="handleCatDelete(cat)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 套餐管理弹窗 -->
    <div v-if="showPkgModal" class="modal-overlay" @click.self="showPkgModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>套餐管理 - {{ pkgCourse.name }}</h3>
          <button class="btn-close" @click="showPkgModal = false">✕</button>
        </div>
        <div class="modal-body">
          <!-- 套餐列表 -->
          <div v-if="pkgList.length" class="pkg-manager-list">
            <div v-for="pkg in pkgList" :key="pkg.id" class="pkg-manager-item">
              <div class="pkg-manager-header">
                <span class="pkg-manager-name">{{ pkg.name }}</span>
                <span class="pkg-manager-price">¥{{ pkg.price }}</span>
                <span class="tag tag-primary">{{ pkg.level === 0 ? '基础' : pkg.level === 1 ? '进阶' : pkg.level === 2 ? '旗舰' : '等级' + pkg.level }}</span>
                <div class="pkg-manager-actions">
                  <button class="btn btn-ghost btn-sm" @click="openEditPkg(pkg)">编辑</button>
                  <button class="btn btn-danger btn-sm" @click="handleDeletePkg(pkg)">删除</button>
                </div>
              </div>
              <!-- 套餐项 -->
              <div class="pkg-items">
                <div v-for="item in pkgItemsMap[pkg.id] || []" :key="item.id" class="pkg-item-row">
                  <span>{{ item.name }}</span>
                  <span :class="['tag', item.included === 1 ? 'tag-success' : 'tag-warning']">{{ item.included === 1 ? '包含' : '额外' }}</span>
                  <span v-if="item.included !== 1">¥{{ item.extraPrice }}</span>
                  <button class="btn btn-danger btn-xs" @click="handleDeleteItem(item)">删除</button>
                </div>
                <button class="btn btn-ghost btn-xs" @click="openAddItem(pkg)">+ 添加项目</button>
              </div>
            </div>
          </div>
          <div v-else class="empty-hint">暂无套餐</div>

          <!-- 添加/编辑套餐表单 -->
          <div v-if="showPkgForm" class="pkg-form-area">
            <h4>{{ isPkgEdit ? '编辑套餐' : '新增套餐' }}</h4>
            <div class="form-group">
              <label>套餐名称</label>
              <input v-model="pkgForm.name" placeholder="如 基础套餐" />
            </div>
            <div class="row" style="gap:var(--space-3)">
              <div class="col form-group" style="margin-bottom:0">
                <label>价格（元）</label>
                <input v-model.number="pkgForm.price" type="number" min="0" step="0.01" />
              </div>
              <div class="col form-group" style="margin-bottom:0">
                <label>等级</label>
                <select v-model.number="pkgForm.level">
                  <option :value="0">基础</option>
                  <option :value="1">进阶</option>
                  <option :value="2">旗舰</option>
                </select>
              </div>
            </div>
            <div class="form-actions">
              <button class="btn btn-primary btn-sm" @click="handleSavePkg">{{ isPkgEdit ? '保存' : '添加' }}</button>
              <button class="btn btn-secondary btn-sm" @click="showPkgForm = false">取消</button>
            </div>
          </div>
          <button v-if="!showPkgForm" class="btn btn-primary btn-sm" style="margin-top:var(--space-3)" @click="openAddPkg">+ 新增套餐</button>

          <!-- 添加套餐项表单 -->
          <div v-if="showItemForm" class="pkg-form-area" style="margin-top:var(--space-3)">
            <h4>添加项目到 {{ itemTargetPkg.name }}</h4>
            <div class="form-group">
              <label>项目名称</label>
              <input v-model="itemForm.name" placeholder="如 一对一指导" />
            </div>
            <div class="row" style="gap:var(--space-3)">
              <div class="col form-group" style="margin-bottom:0">
                <label>是否包含</label>
                <select v-model.number="itemForm.included">
                  <option :value="1">包含</option>
                  <option :value="0">额外付费</option>
                </select>
              </div>
              <div class="col form-group" style="margin-bottom:0">
                <label>额外费用（元）</label>
                <input v-model.number="itemForm.extraPrice" type="number" min="0" step="0.01" :disabled="itemForm.included === 1" />
              </div>
            </div>
            <div class="form-actions">
              <button class="btn btn-primary btn-sm" @click="handleSaveItem">添加</button>
              <button class="btn btn-secondary btn-sm" @click="showItemForm = false">取消</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../api'

const courses = ref([])
const categories = ref([])
const isEdit = ref(false)
const courseMsg = ref('')
const courseMsgType = ref('success')
const courseForm = ref({ id: null, name: '', categoryId: null, duration: 1, monthlyPrice: null, description: '' })

const showCatForm = ref(false)
const isCatEdit = ref(false)
const catForm = ref({ id: null, name: '', sortOrder: 0 })

function getCategoryName(categoryId) {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat ? cat.name : categoryId
}

async function loadCourses() {
  try {
    const res = await adminApi.courseList()
    if (res.code === 200) courses.value = res.data
  } catch (e) { console.error(e) }
}

async function loadCategories() {
  try {
    const res = await adminApi.categoryList()
    if (res.code === 200) categories.value = res.data
  } catch (e) { console.error(e) }
}

function openAdd() {
  isEdit.value = false
  courseForm.value = { id: null, name: '', categoryId: null, duration: 1, monthlyPrice: null, description: '' }
  courseMsg.value = ''
}

function openEdit(course) {
  isEdit.value = true
  courseForm.value = { ...course }
  courseMsg.value = ''
}

function resetForm() {
  openAdd()
}

async function handleSave() {
  const f = courseForm.value
  if (!f.name || !f.categoryId || !f.duration || f.monthlyPrice === null) {
    courseMsg.value = '课程名称、分类、周期和月费为必填项'
    courseMsgType.value = 'error'
    return
  }
  try {
    let res
    if (isEdit.value) {
      res = await adminApi.updateCourse(f)
    } else {
      res = await adminApi.addCourse(f)
    }
    if (res.code === 200) {
      courseMsg.value = isEdit.value ? '修改成功' : '添加成功'
      courseMsgType.value = 'success'
      openAdd()
      loadCourses()
    } else {
      courseMsg.value = res.message || '操作失败'
      courseMsgType.value = 'error'
    }
  } catch (e) {
    courseMsg.value = '请求失败'
    courseMsgType.value = 'error'
  }
}

async function handleDelete(course) {
  if (!confirm(`确定删除课程「${course.name}」吗？`)) return
  try {
    const res = await adminApi.deleteCourse(course.id)
    if (res.code === 200) {
      loadCourses()
    } else {
      alert(res.message || '删除失败')
    }
  } catch (e) {
    alert('删除请求失败')
  }
}

function openCatEdit(cat) {
  isCatEdit.value = true
  showCatForm.value = true
  catForm.value = { ...cat }
}

function resetCatForm() {
  isCatEdit.value = false
  catForm.value = { id: null, name: '', sortOrder: 0 }
}

async function handleCatSave() {
  const f = catForm.value
  if (!f.name) {
    alert('分类名称不能为空')
    return
  }
  try {
    let res
    if (isCatEdit.value) {
      res = await adminApi.updateCategory(f)
    } else {
      res = await adminApi.addCategory(f)
    }
    if (res.code === 200) {
      resetCatForm()
      showCatForm.value = false
      loadCategories()
      loadCourses()
    } else {
      alert(res.message || '操作失败')
    }
  } catch (e) {
    alert('请求失败')
  }
}

async function handleCatDelete(cat) {
  if (!confirm(`确定删除分类「${cat.name}」吗？该分类下的课程将不受影响。`)) return
  try {
    const res = await adminApi.deleteCategory(cat.id)
    if (res.code === 200) {
      loadCategories()
    } else {
      alert(res.message || '删除失败')
    }
  } catch (e) {
    alert('删除请求失败')
  }
}

// 套餐管理
const showPkgModal = ref(false)
const pkgCourse = ref({})
const pkgList = ref([])
const pkgItemsMap = ref({})
const showPkgForm = ref(false)
const isPkgEdit = ref(false)
const pkgForm = ref({ id: null, courseId: null, name: '', price: null, level: 0 })
const showItemForm = ref(false)
const itemTargetPkg = ref({})
const itemForm = ref({ packageId: null, name: '', included: 1, extraPrice: 0 })

async function openPkgManager(course) {
  pkgCourse.value = course
  showPkgModal.value = true
  showPkgForm.value = false
  showItemForm.value = false
  await loadPackages(course.id)
}

async function loadPackages(courseId) {
  try {
    const res = await adminApi.packageList(courseId)
    if (res.code === 200) {
      pkgList.value = res.data || []
      // 加载每个套餐的项目
      const map = {}
      for (const pkg of pkgList.value) {
        try {
          const itemRes = await adminApi.packageItems(pkg.id)
          map[pkg.id] = itemRes.code === 200 ? (itemRes.data || []) : []
        } catch { map[pkg.id] = [] }
      }
      pkgItemsMap.value = map
    }
  } catch (e) { console.error(e) }
}

function openAddPkg() {
  isPkgEdit.value = false
  pkgForm.value = { id: null, courseId: pkgCourse.value.id, name: '', price: null, level: 0 }
  showPkgForm.value = true
  showItemForm.value = false
}

function openEditPkg(pkg) {
  isPkgEdit.value = true
  pkgForm.value = { ...pkg }
  showPkgForm.value = true
  showItemForm.value = false
}

async function handleSavePkg() {
  const f = pkgForm.value
  if (!f.name || f.price === null) { alert('套餐名称和价格为必填'); return }
  try {
    const res = isPkgEdit.value ? await adminApi.updatePackage(f) : await adminApi.addPackage(f)
    if (res.code === 200) {
      showPkgForm.value = false
      await loadPackages(pkgCourse.value.id)
    } else { alert(res.message || '操作失败') }
  } catch { alert('请求失败') }
}

async function handleDeletePkg(pkg) {
  if (!confirm(`确定删除套餐「${pkg.name}」吗？`)) return
  try {
    const res = await adminApi.deletePackage(pkg.id)
    if (res.code === 200) { await loadPackages(pkgCourse.value.id) }
    else { alert(res.message || '删除失败') }
  } catch { alert('请求失败') }
}

function openAddItem(pkg) {
  itemTargetPkg.value = pkg
  itemForm.value = { packageId: pkg.id, name: '', included: 1, extraPrice: 0 }
  showItemForm.value = true
  showPkgForm.value = false
}

async function handleSaveItem() {
  const f = itemForm.value
  if (!f.name) { alert('项目名称为必填'); return }
  try {
    const res = await adminApi.addPackageItem(f)
    if (res.code === 200) {
      showItemForm.value = false
      await loadPackages(pkgCourse.value.id)
    } else { alert(res.message || '添加失败') }
  } catch { alert('请求失败') }
}

async function handleDeleteItem(item) {
  if (!confirm(`确定删除项目「${item.name}」吗？`)) return
  try {
    const res = await adminApi.deletePackageItem(item.id)
    if (res.code === 200) { await loadPackages(pkgCourse.value.id) }
    else { alert(res.message || '删除失败') }
  } catch { alert('请求失败') }
}

onMounted(() => {
  loadCategories()
  loadCourses()
})
</script>

<style scoped>
.course-page {
  animation: fadeInUp var(--transition-base) ease both;
}

.page-header {
  margin-bottom: var(--space-5);
}

.page-desc {
  font-size: var(--font-size-base);
  color: var(--text-muted);
  margin-top: var(--space-1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.card-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
  margin: 0;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.action-icon {
  font-size: var(--font-size-sm);
}

.table-wrap {
  overflow-x: auto;
  margin: 0 calc(var(--space-5) * -1);
  padding: 0 var(--space-5);
}

.cell-id {
  font-family: var(--font-mono);
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.cell-name {
  font-weight: var(--font-weight-medium);
  color: var(--text-title);
}

.cell-price {
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
}

.cell-desc {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: var(--text-muted);
  font-size: var(--font-size-sm);
  max-width: 160px;
}

.cell-actions {
  text-align: right;
  white-space: nowrap;
}

.form-actions {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-3);
}

.cat-form-area {
  padding: var(--space-3);
  background: var(--bg-input);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-4);
  border: 1px solid var(--border-light);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-light);
}
.modal-header h3 {
  margin: 0;
  font-size: var(--font-size-md);
  color: var(--text-title);
}
.btn-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: var(--text-muted);
  padding: 4px 8px;
  border-radius: var(--radius-md);
}
.btn-close:hover { background: var(--bg-hover); }
.modal-body {
  padding: var(--space-5);
}
.pkg-manager-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.pkg-manager-item {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: var(--space-3);
}
.pkg-manager-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}
.pkg-manager-name {
  font-weight: var(--font-weight-semibold);
  color: var(--text-title);
}
.pkg-manager-price {
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}
.pkg-manager-actions {
  margin-left: auto;
  display: flex;
  gap: var(--space-1);
}
.pkg-items {
  padding-left: var(--space-3);
  border-top: 1px solid var(--border-light);
  padding-top: var(--space-2);
}
.pkg-item-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 4px 0;
  font-size: var(--font-size-sm);
  color: var(--text-body);
}
.pkg-form-area {
  padding: var(--space-3);
  background: var(--bg-input);
  border-radius: var(--radius-md);
  margin-top: var(--space-3);
  border: 1px solid var(--border-light);
}
.pkg-form-area h4 {
  margin: 0 0 var(--space-3) 0;
  font-size: var(--font-size-base);
  color: var(--text-title);
}
.btn-xs {
  padding: 2px 8px;
  font-size: var(--font-size-xs);
}
</style>
