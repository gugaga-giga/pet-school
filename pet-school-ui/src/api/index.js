import axios from 'axios'

function createRequest(tokenKey, onUnauthorized) {
  const instance = axios.create({
    baseURL: '/api',
    timeout: 10000
  })

  instance.interceptors.request.use((config) => {
    const token = localStorage.getItem(tokenKey)
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  })

  instance.interceptors.response.use(
    (response) => response.data,
    (error) => {
      if (error.response && error.response.status === 401) {
        if (onUnauthorized) {
          onUnauthorized()
        }
      }
      return Promise.reject(error)
    }
  )

  return instance
}

const clientRequest = createRequest('client_token', () => {
  const currentPath = window.location.pathname
  if (currentPath.startsWith('/client')) {
    localStorage.removeItem('client_token')
    localStorage.removeItem('client_user')
    window.location.href = '/login?from=client'
  }
})

const adminRequest = createRequest('admin_token', () => {
  const currentPath = window.location.pathname
  if (currentPath.startsWith('/admin')) {
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
    window.location.href = '/login?from=admin'
  }
})

const publicRequest = axios.create({ baseURL: '/api', timeout: 10000 })
publicRequest.interceptors.response.use((response) => response.data, (error) => Promise.reject(error))

export const authApi = {
  login: (data) => publicRequest.post('/auth/login', data),
  register: (data) => publicRequest.post('/auth/register', data),
  info: () => clientRequest.get('/auth/info')
}

export const petApi = {
  add: (data) => clientRequest.post('/pet/add', data),
  getById: (id) => clientRequest.get(`/pet/${id}`),
  getByUserId: (userId) => clientRequest.get(`/pet/user/${userId}`)
}

export const courseApi = {
  listAll: () => clientRequest.get('/course/list'),
  getById: (id) => clientRequest.get(`/course/${id}`),
  categoryList: () => clientRequest.get('/course/category/list'),
  getByCategory: (categoryId) => clientRequest.get(`/course/category/${categoryId}`),
  packageList: (courseId) => clientRequest.get('/course/package/list', { params: { courseId } }),
  packageById: (id) => clientRequest.get(`/course/package/${id}`),
  packageItems: (packageId) => clientRequest.get('/course/package/item/list', { params: { packageId } })
}

export const orderApi = {
  courseCreate: (data) => clientRequest.post('/order/course/create', data),
  courseByUser: (userId) => clientRequest.get(`/order/course/user/${userId}`),
  coursePay: (id) => clientRequest.put(`/order/course/pay/${id}`),
  courseCancel: (id) => clientRequest.put(`/order/course/cancel/${id}`)
}

export const roomApi = {
  typeList: () => clientRequest.get('/room/type/list'),
  typeAvailable: () => clientRequest.get('/room/type/available'),
  typeById: (id) => clientRequest.get(`/room/type/${id}`),
  roomList: (typeId) => clientRequest.get('/room/list', { params: { typeId } }),
  boardingCreate: (data) => clientRequest.post('/room/boarding/create', data),
  boardingById: (id) => clientRequest.get(`/room/boarding/${id}`),
  boardingByUser: (userId) => clientRequest.get(`/room/boarding/user/${userId}`),
  boardingPay: (id) => clientRequest.put(`/room/boarding/pay/${id}`),
  boardingCancel: (id) => clientRequest.put(`/room/boarding/cancel/${id}`)
}

export const videoApi = {
  upload: (formData) => adminRequest.post('/video/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  list: () => clientRequest.get('/video/list')
}

export const trainingApi = {
  recordsByPet: (petId) => clientRequest.get(`/training/record/pet/${petId}`),
  videosByRecord: (recordId) => clientRequest.get(`/training/video/record/${recordId}`)
}

export const healthApi = {
  recordsByPet: (petId) => clientRequest.get(`/health/pet/${petId}`),
  latestByPet: (petId) => clientRequest.get(`/health/latest/${petId}`),
  myHealth: (userId) => clientRequest.get('/health/my', { params: { userId } }),
  trend: (petId, limit = 30) => clientRequest.get(`/health/trend/${petId}`, { params: { limit } }),
  aiWarning: (petId) => clientRequest.get(`/health-ai/ai/warning/${petId}`),
  aiRecommend: (petId) => clientRequest.get(`/health-ai/ai/recommend/${petId}`),
  aiVaccine: (petId) => clientRequest.get(`/health-ai/ai/vaccine/${petId}`)
}

export const medicalApi = {
  departments: () => clientRequest.get('/medical/department/list'),
  doctors: () => clientRequest.get('/medical/doctor/list'),
  doctorsByDepartment: (departmentId) => clientRequest.get(`/medical/doctor/by-department/${departmentId}`),
  createOrder: (data) => clientRequest.post('/medical/order/create', data),
  myOrders: (userId) => clientRequest.get('/medical/order/my', { params: { userId } }),
  cancelOrder: (id, userId) => clientRequest.put(`/medical/order/cancel/${id}`, null, { params: { userId } }),
  orderDetail: (id) => clientRequest.get(`/medical/order/detail/${id}`),
  recordByOrder: (orderId) => clientRequest.get(`/medical/record/by-order/${orderId}`),
  recordsByPet: (petId) => clientRequest.get(`/medical/record/by-pet/${petId}`),
  vaccineUpcoming: (userId, days) => clientRequest.get('/medical/vaccine/upcoming', { params: { userId, days } }),
  vaccineExpired: (userId) => clientRequest.get('/medical/vaccine/expired', { params: { userId } }),
  dewormingUpcoming: (userId, days) => clientRequest.get('/medical/deworming/upcoming', { params: { userId, days } }),
  dewormingExpired: (userId) => clientRequest.get('/medical/deworming/expired', { params: { userId } })
}

export const certificateApi = {
  getByPet: (petId) => clientRequest.get(`/certificate/pet/${petId}`),
  myCertificates: (userId) => clientRequest.get(`/certificate/user/${userId}`),
  download: (id) => clientRequest.get(`/certificate/download/${id}`, { responseType: 'blob' }),
  verify: (certNo) => publicRequest.get(`/certificate/verify/${certNo}`)
}

export const couponApi = {
  listAll: () => clientRequest.get('/coupon/page', { params: { page: 1, pageSize: 100 } }),
  activeList: () => clientRequest.get('/coupon/active'),
  userCoupons: (userId) => clientRequest.get('/coupon/my', { params: { userId } }),
  myCoupons: (params) => clientRequest.get('/coupon/my', { params }),
  claim: (data) => clientRequest.post('/coupon/claim', data),
  receive: (couponId, userId) => clientRequest.post(`/coupon/receive/${couponId}`, null, { params: { userId } }),
  availableForOrder: (params) => clientRequest.get('/coupon/my/available-for-order', { params }),
  calculate: (data) => clientRequest.post('/coupon/calculate', data)
}

export const liveApi = {
  listAll: () => clientRequest.get('/live/list')
}

export const messageApi = {
  getByUser: (userId) => clientRequest.get(`/message/user/${userId}`),
  markRead: (id) => clientRequest.put(`/message/read/${id}`)
}

export const adminApi = {
  userList: () => adminRequest.get('/admin/user/list'),
  petListByUser: (userId) => adminRequest.get('/admin/pet/list', { params: { userId } }),
  courseList: () => adminRequest.get('/admin/course/list'),
  courseDetail: (id) => adminRequest.get(`/admin/course/${id}`),
  addCourse: (data) => adminRequest.post('/admin/course/add', data),
  updateCourse: (data) => adminRequest.put('/admin/course/update', data),
  deleteCourse: (id) => adminRequest.delete(`/admin/course/delete/${id}`),
  categoryList: () => adminRequest.get('/admin/course/category/list'),
  addCategory: (data) => adminRequest.post('/admin/course/category/add', data),
  updateCategory: (data) => adminRequest.put('/admin/course/category/update', data),
  deleteCategory: (id) => adminRequest.delete(`/admin/course/category/delete/${id}`),
  packageList: (courseId) => adminRequest.get('/admin/course/package/list', { params: { courseId } }),
  packageItems: (packageId) => adminRequest.get('/admin/course/package/item/list', { params: { packageId } }),
  addTrainingRecord: (data) => adminRequest.post('/admin/training/record/add', data),
  addTrainingVideo: (data) => adminRequest.post('/admin/training/video/add', data),
  trainingByPet: (petId) => adminRequest.get(`/admin/training/record/pet/${petId}`),
  generateCert: (data) => adminRequest.post('/admin/certificate/generate', data),
  certByPet: (petId) => adminRequest.get(`/admin/certificate/pet/${petId}`),
  certificatePage: (params) => adminRequest.get('/admin/certificate/page', { params }),
  certificateDelete: (id) => adminRequest.delete(`/admin/certificate/delete/${id}`),
  certificateBatchDelete: (ids) => adminRequest.delete('/admin/certificate/batch', { data: { ids } }),
  certificateRegenerate: (id) => adminRequest.put(`/admin/certificate/regenerate/${id}`),
  roomTypeList: () => adminRequest.get('/admin/room/type/list'),
  roomTypeDetail: (id) => adminRequest.get(`/admin/room/type/${id}`),
  addRoomType: (data) => adminRequest.post('/admin/room/type/add', data),
  updateRoomType: (data) => adminRequest.put('/admin/room/type/update', data),
  deleteRoomType: (id) => adminRequest.delete(`/admin/room/type/delete/${id}`),
  roomList: (typeId) => adminRequest.get('/admin/room/list', { params: { typeId } }),
  addRoom: (data) => adminRequest.post('/admin/room/add', data),
  updateRoom: (data) => adminRequest.put('/admin/room/update', data),
  deleteRoom: (id) => adminRequest.delete(`/admin/room/delete/${id}`),
  couponList: () => adminRequest.get('/coupon/list'),
  couponPage: (params) => adminRequest.get('/coupon/page', { params }),
  createCoupon: (data) => adminRequest.post('/coupon/create', data),
  updateCoupon: (data) => adminRequest.put('/coupon/update', data),
  deleteCoupon: (id) => adminRequest.delete(`/coupon/delete/${id}`),
  toggleCouponStatus: (data) => adminRequest.put('/coupon/update', data),
  sendCoupon: (data) => adminRequest.post('/coupon/send', data),
  healthByPet: (petId) => adminRequest.get(`/admin/health/record/pet/${petId}`),
  healthPage: (params) => adminRequest.get('/health/page', { params }),
  healthDetail: (id) => adminRequest.get(`/health/detail/${id}`),
  healthCreate: (data) => adminRequest.post('/health/create', data),
  healthUpdate: (data) => adminRequest.put('/health/update', data),
  healthDelete: (id) => adminRequest.delete(`/health/delete/${id}`),
  healthRules: () => adminRequest.get('/health/rules'),
  healthRulesByType: (petType) => adminRequest.get(`/health/rules/${petType}`),
  healthRuleCreate: (data) => adminRequest.post('/health/rules', data),
  healthRuleUpdate: (data) => adminRequest.put('/health/rules', data),
  healthRuleDelete: (id) => adminRequest.delete(`/health/rules/${id}`),
  addHealthRecord: (data) => adminRequest.post('/admin/health/record/add', data),
  aiWarning: (petId) => adminRequest.get(`/admin/health/ai/warning/${petId}`),
  aiVaccine: (petId) => adminRequest.get(`/admin/health/ai/vaccine/${petId}`),
  addVaccine: (data) => adminRequest.post('/admin/medical/vaccine/add', data),
  addDeworming: (data) => adminRequest.post('/admin/medical/deworming/add', data),
  addSurgery: (data) => adminRequest.post('/admin/medical/surgery/add', data),
  vaccineByPet: (petId) => adminRequest.get(`/admin/medical/vaccine/pet/${petId}`),
  dewormingByPet: (petId) => adminRequest.get(`/admin/medical/deworming/pet/${petId}`),
  surgeryByPet: (petId) => adminRequest.get(`/admin/medical/surgery/pet/${petId}`),
  createLive: (data) => adminRequest.post('/admin/live/create', data),
  startLive: (id) => adminRequest.put(`/admin/live/start/${id}`),
  endLive: (id) => adminRequest.put(`/admin/live/end/${id}`),
  liveList: () => adminRequest.get('/admin/live/list'),
  sendMessage: (data) => adminRequest.post('/admin/message/send', data),
  messageByUser: (userId) => adminRequest.get(`/admin/message/user/${userId}`),
  courseOrderPage: (params) => adminRequest.get('/admin/order/course/page', { params }),
  courseOrderList: () => adminRequest.get('/admin/order/course/list'),
  courseOrderDetail: (id) => adminRequest.get(`/admin/order/course/${id}`),
  courseOrderUpdateStatus: (data) => adminRequest.put('/admin/order/course/status', data),
  courseOrderDelete: (id) => adminRequest.delete(`/admin/order/course/delete/${id}`),
  courseOrderBatchDelete: (ids) => adminRequest.delete('/admin/order/course/batch', { data: { ids } }),
  boardingOrderPage: (params) => adminRequest.get('/admin/order/boarding/page', { params }),
  boardingOrderList: () => adminRequest.get('/admin/order/boarding/list'),
  boardingOrderDetail: (id) => adminRequest.get(`/admin/order/boarding/${id}`),
  boardingOrderUpdateStatus: (data) => adminRequest.put('/admin/order/boarding/status', data),
  boardingOrderDelete: (id) => adminRequest.delete(`/admin/order/boarding/delete/${id}`),
  boardingOrderBatchDelete: (ids) => adminRequest.delete('/admin/order/boarding/batch', { data: { ids } }),
  certificatePage: (params) => adminRequest.get('/certificate/page', { params }),
  certificateDetail: (id) => adminRequest.get(`/certificate/detail/${id}`),
  certificateGenerate: (orderId) => adminRequest.post(`/certificate/generate/${orderId}`),
  certificateGenerateTest: (data) => adminRequest.post('/certificate/generate-test', data),
  certificateRegenerate: (id) => adminRequest.post(`/certificate/regenerate/${id}`),
  certificateUpdateStatus: (data) => adminRequest.put('/certificate/status', data),
  certificateDelete: (id) => adminRequest.delete(`/certificate/delete/${id}`),
  certificateBatchDelete: (ids) => adminRequest.delete('/certificate/batch', { data: { ids } }),
  departmentList: () => adminRequest.get('/medical/department/list'),
  departmentAdd: (data) => adminRequest.post('/medical/department/add', data),
  departmentUpdate: (data) => adminRequest.put('/medical/department/update', data),
  departmentDelete: (id) => adminRequest.delete(`/medical/department/delete/${id}`),
  doctorList: () => adminRequest.get('/medical/doctor/list'),
  doctorAdd: (data) => adminRequest.post('/medical/doctor/add', data),
  doctorUpdate: (data) => adminRequest.put('/medical/doctor/update', data),
  doctorDelete: (id) => adminRequest.delete(`/medical/doctor/delete/${id}`),
  medicalOrderPage: (params) => adminRequest.get('/medical/order/page', { params }),
  medicalOrderUpdateStatus: (data) => adminRequest.put('/medical/order/status', data),
  medicalRecordCreate: (data) => adminRequest.post('/medical/record/create', data),
  medicalRecordByOrder: (orderId) => adminRequest.get(`/medical/record/by-order/${orderId}`),
  medicalRecordPage: (params) => adminRequest.get('/medical/record/page', { params }),
  medicalRecordUpdate: (data) => adminRequest.put('/medical/record/update', data),
  medicalRecordDelete: (id) => adminRequest.delete(`/medical/record/delete/${id}`),
  vaccineList: () => adminRequest.get('/medical/vaccine/list'),
  vaccineAdd: (data) => adminRequest.post('/medical/vaccine/add', data),
  vaccineUpdate: (data) => adminRequest.put('/medical/vaccine/update', data),
  vaccineDelete: (id) => adminRequest.delete(`/medical/vaccine/delete/${id}`),
  dewormingList: () => adminRequest.get('/medical/deworming/list'),
  dewormingAdd: (data) => adminRequest.post('/medical/deworming/add', data),
  dewormingUpdate: (data) => adminRequest.put('/medical/deworming/update', data),
  dewormingDelete: (id) => adminRequest.delete(`/medical/deworming/delete/${id}`),
  walletPage: (params) => adminRequest.get('/wallet/admin/page', { params }),
  walletAdjust: (data) => adminRequest.post('/wallet/admin/adjust', data),
  walletStatus: (data) => adminRequest.put('/wallet/admin/status', data),
  walletRecords: (params) => adminRequest.get('/wallet/admin/records', { params })
}

export const walletApi = {
  info: () => clientRequest.get('/wallet/info'),
  recharge: (data) => clientRequest.post('/wallet/recharge', data),
  records: (params) => clientRequest.get('/wallet/records', { params })
}
