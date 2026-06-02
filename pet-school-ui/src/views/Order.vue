<template>
  <div>
    <h1 class="page-title">订单管理</h1>
    <div class="row">
      <div class="col">
        <div class="card">
          <h3 style="margin-bottom:16px">创建课程订单</h3>
          <div class="form-group"><label>用户ID</label><input v-model.number="form.userId" placeholder="用户ID" /></div>
          <div class="form-group"><label>宠物ID</label><input v-model.number="form.petId" placeholder="宠物ID" /></div>
          <div class="form-group"><label>套餐ID</label><input v-model.number="form.packageId" placeholder="套餐ID" /></div>
          <div class="form-group"><label>月数</label><input v-model.number="form.months" type="number" min="1" max="3" placeholder="1-3" /></div>
          <div class="form-group"><label>总价</label><input v-model.number="form.totalPrice" type="number" step="0.01" placeholder="总价" /></div>
          <button class="btn btn-primary" @click="createOrder">下单</button>
          <div v-if="msg" :class="['alert', msgType==='success'?'alert-success':'alert-error']" style="margin-top:16px">{{ msg }}</div>
        </div>
      </div>
      <div class="col">
        <div class="card">
          <h3 style="margin-bottom:16px">我的订单</h3>
          <div class="form-group"><label>用户ID</label><input v-model.number="queryUserId" placeholder="输入用户ID" /></div>
          <button class="btn btn-secondary" @click="queryOrders">查询</button>
          <div v-if="orders.length" style="margin-top:16px">
            <table><thead><tr><th>ID</th><th>宠物ID</th><th>套餐ID</th><th>月数</th><th>总价</th><th>状态</th></tr></thead>
            <tbody><tr v-for="o in orders" :key="o.id"><td>{{o.id}}</td><td>{{o.petId}}</td><td>{{o.packageId}}</td><td>{{o.months}}</td><td>¥{{o.totalPrice}}</td><td><span :class="['tag',o.status===0?'tag-warning':o.status===1?'tag-info':'tag-success']">{{['待支付','已支付','已完成'][o.status]}}</span></td></tr></tbody></table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { orderApi } from '../api'
const form = ref({ userId: null, petId: null, packageId: null, months: 1, totalPrice: null })
const msg = ref(''), msgType = ref('success')
const queryUserId = ref(''), orders = ref([])
async function createOrder() {
  if (!form.value.userId||!form.value.petId||!form.value.packageId) { msg.value='所有ID为必填'; msgType.value='error'; return }
  try { const res=await orderApi.create(form.value); if(res.code===200){msg.value='下单成功！ID:'+res.data.id;msgType.value='success';form.value={userId:null,petId:null,packageId:null,months:1,totalPrice:null}}else{msg.value=res.message;msgType.value='error'} } catch(e){msg.value='请求失败';msgType.value='error'}
}
async function queryOrders() { orders.value=[]; if(!queryUserId.value) return; try{const res=await orderApi.getByUserId(queryUserId.value);if(res.code===200) orders.value=res.data}catch(e){} }
</script>
