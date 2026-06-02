<template>
  <div>
    <h1 class="page-title">寄宿服务</h1>
    <div class="row">
      <div v-for="rt in roomTypes" :key="rt.id" class="col">
        <div class="card" style="border:2px solid #1a1a2e">
          <h3>{{rt.name}}</h3>
          <p style="color:#e94560;font-size:24px;font-weight:700">¥{{rt.dailyPrice}}<span style="font-size:14px;color:#888">/天</span></p>
          <p style="color:#666;font-size:13px;margin:8px 0">{{rt.facilities}}</p>
          <button class="btn btn-primary btn-sm" @click="selectType(rt)">选择入住</button>
        </div>
      </div>
    </div>
    <div v-if="selectedType" class="card" style="margin-top:16px">
      <h3 style="margin-bottom:16px">{{selectedType.name}} - 寄宿下单</h3>
      <div class="form-group"><label>用户ID</label><input v-model.number="form.userId" placeholder="用户ID" /></div>
      <div class="form-group"><label>宠物ID</label><input v-model.number="form.petId" placeholder="宠物ID" /></div>
      <div class="form-group"><label>房间ID</label><input v-model.number="form.roomId" placeholder="房间ID" /></div>
      <div class="form-group"><label>入住日期</label><input v-model="form.checkIn" type="date" /></div>
      <div class="form-group"><label>退房日期</label><input v-model="form.checkOut" type="date" /></div>
      <button class="btn btn-primary" @click="createBoarding">提交寄宿订单</button>
      <div v-if="msg" :class="['alert', msgType==='success'?'alert-success':'alert-error']" style="margin-top:16px">{{ msg }}</div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { roomApi } from '../api'
const roomTypes = ref([]), selectedType = ref(null)
const form = ref({ userId: null, petId: null, roomId: null, checkIn: '', checkOut: '' })
const msg = ref(''), msgType = ref('success')
onMounted(async () => { const res=await roomApi.typeList(); if(res.code===200) roomTypes.value=res.data })
function selectType(rt) { selectedType.value=rt }
async function createBoarding() {
  if (!form.value.userId||!form.value.petId||!form.value.roomId||!form.value.checkIn||!form.value.checkOut) { msg.value='所有字段为必填'; msgType.value='error'; return }
  try { const res=await roomApi.boardingCreate(form.value); if(res.code===200){msg.value='寄宿下单成功！ID:'+res.data.id+' 总价:¥'+res.data.totalPrice;msgType.value='success'}else{msg.value=res.message;msgType.value='error'} } catch(e){msg.value='请求失败';msgType.value='error'}
}
</script>
