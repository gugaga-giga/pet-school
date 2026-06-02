<template>
  <div>
    <h1 class="page-title">用户管理</h1>
    <div class="row">
      <div class="col">
        <div class="card">
          <h3 style="margin-bottom:16px">用户注册</h3>
          <div class="form-group"><label>用户名</label><input v-model="form.username" placeholder="请输入用户名" /></div>
          <div class="form-group"><label>密码</label><input v-model="form.password" type="password" placeholder="请输入密码" /></div>
          <div class="form-group"><label>手机号</label><input v-model="form.phone" placeholder="请输入手机号" /></div>
          <button class="btn btn-primary" @click="register">注册</button>
          <div v-if="msg" :class="['alert', msgType==='success'?'alert-success':'alert-error']" style="margin-top:16px">{{ msg }}</div>
        </div>
      </div>
      <div class="col">
        <div class="card">
          <h3 style="margin-bottom:16px">查询用户</h3>
          <div class="form-group"><label>用户ID</label><input v-model.number="queryId" placeholder="请输入用户ID" /></div>
          <button class="btn btn-secondary" @click="query">查询</button>
          <div v-if="userInfo" class="card" style="margin-top:16px;background:#fafafa">
            <p><strong>ID：</strong>{{ userInfo.id }}</p>
            <p><strong>用户名：</strong>{{ userInfo.username }}</p>
            <p><strong>手机号：</strong>{{ userInfo.phone }}</p>
            <p><strong>角色：</strong>{{ ['客户','训练师','管理员'][userInfo.role] }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { userApi } from '../api'
const form = ref({ username: '', password: '', phone: '' })
const msg = ref(''), msgType = ref('success')
const queryId = ref(''), userInfo = ref(null)
async function register() {
  if (!form.value.username || !form.value.password) { msg.value='用户名和密码不能为空'; msgType.value='error'; return }
  try { const res = await userApi.register(form.value); if(res.code===200){msg.value='注册成功！ID:'+res.data.id;msgType.value='success';form.value={username:'',password:'',phone:''}}else{msg.value=res.message;msgType.value='error'} } catch(e){msg.value='请求失败';msgType.value='error'}
}
async function query() {
  userInfo.value = null
  if (!queryId.value) return
  try { const res = await userApi.getById(queryId.value); if(res.code===200) userInfo.value=res.data } catch(e){}
}
</script>
