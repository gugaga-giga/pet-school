<template>
  <div>
    <h1 class="page-title">AI 健康助手</h1>
    <div class="card">
      <div class="form-group"><label>宠物ID</label><input v-model.number="petId" placeholder="输入宠物ID" /></div>
      <div style="display:flex;gap:12px">
        <button class="btn btn-primary" @click="checkHealth">健康预警</button>
        <button class="btn btn-secondary" @click="recommend">课程推荐</button>
        <button class="btn btn-secondary" @click="vaccineRemind">疫苗提醒</button>
      </div>
    </div>
    <div v-if="healthResult" class="card" style="margin-top:16px">
      <h3>🏥 健康预警</h3>
      <p style="margin-top:8px"><strong>宠物名：</strong>{{healthResult.petName}}</p>
      <div v-for="w in healthResult.warnings" :key="w" :class="['alert', healthResult.hasWarning?'alert-warning':'alert-success']" style="margin-top:8px">{{w}}</div>
    </div>
    <div v-if="recommendResult" class="card" style="margin-top:16px">
      <h3>📚 课程推荐</h3>
      <p style="margin-top:8px"><strong>宠物名：</strong>{{recommendResult.petName}} | <strong>年龄：</strong>{{recommendResult.age}}岁</p>
      <div class="alert alert-info" style="margin-top:8px"><strong>{{recommendResult.recommendation}}</strong> — {{recommendResult.reason}}</div>
    </div>
    <div v-if="vaccineResult" class="card" style="margin-top:16px">
      <h3>💉 疫苗提醒</h3>
      <p style="margin-top:8px"><strong>宠物名：</strong>{{vaccineResult.petName}}</p>
      <div v-if="vaccineResult.reminders&&vaccineResult.reminders.length">
        <div v-for="r in vaccineResult.reminders" :key="r.vaccineName" :class="['alert', r.overdue||r.urgent?'alert-warning':'alert-info']" style="margin-top:8px">
          {{r.vaccineName}} — 下次接种：{{r.nextDate}} <span v-if="r.overdue" style="color:red">（已逾期{{Math.abs(r.daysUntil)}}天）</span><span v-else>（还有{{r.daysUntil}}天）</span>
        </div>
      </div>
      <div v-else class="alert alert-success" style="margin-top:8px">暂无即将到期的疫苗</div>
    </div>
    <div v-if="errMsg" class="alert alert-error" style="margin-top:16px">{{errMsg}}</div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { healthApi } from '../api'
const petId = ref(''), healthResult = ref(null), recommendResult = ref(null), vaccineResult = ref(null), errMsg = ref('')
function clear() { healthResult.value=null; recommendResult.value=null; vaccineResult.value=null; errMsg.value='' }
async function checkHealth() { clear(); if(!petId.value){errMsg.value='请输入宠物ID';return} try{const res=await healthApi.aiWarning(petId.value);if(res.code===200) healthResult.value=res.data}catch(e){errMsg.value='请求失败'} }
async function recommend() { clear(); if(!petId.value){errMsg.value='请输入宠物ID';return} try{const res=await healthApi.aiRecommend(petId.value);if(res.code===200) recommendResult.value=res.data}catch(e){errMsg.value='请求失败'} }
async function vaccineRemind() { clear(); if(!petId.value){errMsg.value='请输入宠物ID';return} try{const res=await healthApi.aiVaccine(petId.value);if(res.code===200) vaccineResult.value=res.data}catch(e){errMsg.value='请求失败'} }
</script>
