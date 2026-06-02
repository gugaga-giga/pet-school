<template>
  <div>
    <h1 class="page-title">优惠管理</h1>
    <div class="card">
      <button class="btn btn-primary" @click="loadCoupons">查看所有优惠券</button>
    </div>
    <div v-if="coupons.length" class="card" style="margin-top:16px">
      <table><thead><tr><th>ID</th><th>名称</th><th>类型</th><th>面额/折扣</th><th>最低消费</th><th>有效期</th></tr></thead>
      <tbody><tr v-for="c in coupons" :key="c.id"><td>{{c.id}}</td><td>{{c.name}}</td><td><span :class="['tag',c.couponType===1?'tag-danger':c.couponType===2?'tag-info':'tag-success']">{{['','满减','折扣','体验'][c.couponType]}}</span></td><td>{{c.couponType===2?c.value+'%':'¥'+c.value}}</td><td>¥{{c.minAmount}}</td><td>{{c.expireDate}}</td></tr></tbody></table>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { couponApi } from '../api'
const coupons = ref([])
async function loadCoupons() { try{const res=await couponApi.listAll();if(res.code===200) coupons.value=res.data}catch(e){} }
</script>
