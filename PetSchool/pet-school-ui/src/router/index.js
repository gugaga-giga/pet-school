import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/client',
    component: () => import('../layouts/ClientLayout.vue'),
    meta: { requireAuth: true, endpoint: 'client' },
    children: [
      { path: '', name: 'ClientHome', component: () => import('../views/client/Home.vue'), meta: { requireAuth: true, endpoint: 'client' } },
      { path: 'course', name: 'ClientCourse', component: () => import('../views/client/Course.vue'), meta: { requireAuth: true, endpoint: 'client' } },
      { path: 'video', name: 'ClientVideo', component: () => import('../views/client/Video.vue'), meta: { requireAuth: true, endpoint: 'client' } },
      { path: 'live', name: 'ClientLive', component: () => import('../views/client/Live.vue'), meta: { requireAuth: true, endpoint: 'client' } },
      { path: 'order', name: 'ClientOrder', component: () => import('../views/client/Order.vue'), meta: { requireAuth: true, endpoint: 'client' } },
      { path: 'certificate', name: 'ClientCertificate', component: () => import('../views/client/Certificate.vue'), meta: { requireAuth: true, endpoint: 'client' } },
      { path: 'room', name: 'ClientRoom', component: () => import('../views/client/Room.vue'), meta: { requireAuth: true, endpoint: 'client' } },
      { path: 'coupon-center', name: 'ClientCouponCenter', component: () => import('../views/client/CouponCenter.vue'), meta: { requireAuth: true, endpoint: 'client' } },
      { path: 'my-coupon', name: 'ClientMyCoupon', component: () => import('../views/client/MyCoupon.vue'), meta: { requireAuth: true, endpoint: 'client' } },
      { path: 'health', name: 'ClientHealth', component: () => import('../views/client/Health.vue'), meta: { requireAuth: true, endpoint: 'client' } },
      { path: 'medical', component: () => import('../views/client/Medical.vue'), meta: { title: '医疗服务' } },
      { path: 'medical-order', component: () => import('../views/client/MedicalOrder.vue'), meta: { title: '医疗订单' } },
      { path: 'medical-record', component: () => import('../views/client/MedicalRecord.vue'), meta: { title: '我的病历' } },
      { path: 'vaccine', component: () => import('../views/client/Vaccine.vue'), meta: { title: '疫苗提醒' } },
      { path: 'wallet', component: () => import('../views/client/Wallet.vue'), meta: { title: '我的钱包' } },
      { path: 'pet', component: () => import('../views/client/Pet.vue'), meta: { title: '我的宠物' } },
      { path: 'pet/create', component: () => import('../views/client/PetEdit.vue'), meta: { title: '新增宠物' } },
      { path: 'pet/edit/:id', component: () => import('../views/client/PetEdit.vue'), meta: { title: '编辑宠物' } },
      { path: 'pet/detail/:id', component: () => import('../views/client/PetDetail.vue'), meta: { title: '宠物详情' } }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requireAuth: true, endpoint: 'admin' },
    children: [
      { path: '', redirect: '/admin/course' },
      { path: 'course', name: 'AdminCourse', component: () => import('../views/admin/Course.vue'), meta: { requireAuth: true, endpoint: 'admin' } },
      { path: 'customer', name: 'AdminCustomer', component: () => import('../views/admin/Customer.vue'), meta: { requireAuth: true, endpoint: 'admin' } },
      { path: 'certificate', name: 'AdminCertificate', component: () => import('../views/admin/Certificate.vue'), meta: { requireAuth: true, endpoint: 'admin' } },
      { path: 'room', name: 'AdminRoom', component: () => import('../views/admin/Room.vue'), meta: { requireAuth: true, endpoint: 'admin' } },
      { path: 'coupon', name: 'AdminCoupon', component: () => import('../views/admin/Coupon.vue'), meta: { requireAuth: true, endpoint: 'admin' } },
      { path: 'health', name: 'AdminHealth', component: () => import('../views/admin/Health.vue'), meta: { requireAuth: true, endpoint: 'admin' } },
      { path: 'medical', name: 'AdminMedical', component: () => import('../views/admin/Medical.vue'), meta: { requireAuth: true, endpoint: 'admin' } },
      { path: 'medical-dept', component: () => import('../views/admin/MedicalDept.vue'), meta: { title: '科室管理' } },
      { path: 'medical-doctor', component: () => import('../views/admin/MedicalDoctor.vue'), meta: { title: '医生管理' } },
      { path: 'medical-order', component: () => import('../views/admin/MedicalOrder.vue'), meta: { title: '医疗订单' } },
      { path: 'medical-record', component: () => import('../views/admin/MedicalRecord.vue'), meta: { title: '病历管理' } },
      { path: 'medical-vaccine', component: () => import('../views/admin/MedicalVaccine.vue'), meta: { title: '疫苗管理' } },
      { path: 'medical-deworming', component: () => import('../views/admin/MedicalDeworming.vue'), meta: { title: '驱虫管理' } },
      { path: 'wallet', component: () => import('../views/admin/WalletManage.vue'), meta: { title: '钱包管理' } },
      { path: 'wallet-record', component: () => import('../views/admin/WalletRecord.vue'), meta: { title: '钱包流水' } },
      { path: 'pet', component: () => import('../views/admin/PetManage.vue'), meta: { title: '宠物管理' } },
      { path: 'pet/detail/:id', component: () => import('../views/admin/PetDetail.vue'), meta: { title: '宠物详情' } },
      { path: 'order', name: 'AdminOrder', component: () => import('../views/admin/Order.vue'), meta: { requireAuth: true, endpoint: 'admin' } },
      { path: 'ai', name: 'AdminAi', component: () => import('../views/admin/AiManage.vue'), meta: { requireAuth: true, endpoint: 'admin' } }
    ]
  },
  { path: '/', redirect: '/login' },
  { path: '/certificate/verify/:certNo', name: 'CertificateVerify', component: () => import('../views/CertificateVerify.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const needAuth = to.matched.some(record => record.meta.requireAuth)

  if (needAuth) {
    const endpoint = to.matched.reduce((ep, record) => record.meta.endpoint || ep, null)
    const tokenKey = endpoint === 'admin' ? 'admin_token' : 'client_token'
    const userKey = endpoint === 'admin' ? 'admin_user' : 'client_user'

    const token = localStorage.getItem(tokenKey)
    const userStr = localStorage.getItem(userKey)

    if (!token || !userStr) {
      next('/login?from=' + (endpoint || 'client'))
      return
    }

    try {
      const user = JSON.parse(userStr)
      if (endpoint === 'admin' && user.role < 2) {
        next('/login?from=admin')
        return
      }
    } catch {
      localStorage.removeItem(tokenKey)
      localStorage.removeItem(userKey)
      next('/login?from=' + (endpoint || 'client'))
      return
    }
  }

  if (to.path === '/login') {
    const clientToken = localStorage.getItem('client_token')
    const adminToken = localStorage.getItem('admin_token')
    if (clientToken || adminToken) {
      next()
      return
    }
  }

  next()
})

export default router
