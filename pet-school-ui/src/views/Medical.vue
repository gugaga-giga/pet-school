<template>
  <div>
    <h1 class="page-title">医疗服务</h1>
    <div class="card">
      <div class="form-group"><label>宠物ID</label><input v-model.number="petId" placeholder="输入宠物ID" /></div>
      <div style="display:flex;gap:12px">
        <button class="btn btn-primary" @click="loadVaccine">疫苗记录</button>
        <button class="btn btn-secondary" @click="loadDeworming">驱虫记录</button>
        <button class="btn btn-secondary" @click="loadSurgery">手术记录</button>
      </div>
    </div>
    <div v-if="vaccines.length" class="card" style="margin-top:16px">
      <h3>💉 疫苗记录</h3>
      <table><thead><tr><th>疫苗名</th><th>接种日期</th><th>下次接种</th></tr></thead>
      <tbody><tr v-for="v in vaccines" :key="v.id"><td>{{v.vaccineName}}</td><td>{{v.injectDate}}</td><td>{{v.nextDate||'-'}}</td></tr></tbody></table>
    </div>
    <div v-if="dewormings.length" class="card" style="margin-top:16px">
      <h3>🐛 驱虫记录</h3>
      <table><thead><tr><th>类型</th><th>日期</th><th>下次驱虫</th></tr></thead>
      <tbody><tr v-for="d in dewormings" :key="d.id"><td>{{d.dewormType===1?'体内':'体外'}}</td><td>{{d.doDate}}</td><td>{{d.nextDate||'-'}}</td></tr></tbody></table>
    </div>
    <div v-if="surgeries.length" class="card" style="margin-top:16px">
      <h3>🏥 手术记录</h3>
      <table><thead><tr><th>手术名</th><th>日期</th><th>状态</th></tr></thead>
      <tbody><tr v-for="s in surgeries" :key="s.id"><td>{{s.surgeryName}}</td><td>{{s.surgeryDate}}</td><td><span :class="['tag',s.status===0?'tag-warning':s.status===2?'tag-success':'tag-info']">{{['术前','术中','恢复中','完成'][s.status]}}</span></td></tr></tbody></table>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { medicalApi } from '../api'
const petId = ref(''), vaccines = ref([]), dewormings = ref([]), surgeries = ref([])
async function loadVaccine() { vaccines.value=[]; dewormings.value=[]; surgeries.value=[]; if(!petId.value) return; try{const res=await medicalApi.vaccineByPet(petId.value);if(res.code===200) vaccines.value=res.data}catch(e){} }
async function loadDeworming() { vaccines.value=[]; dewormings.value=[]; surgeries.value=[]; if(!petId.value) return; try{const res=await medicalApi.dewormingByPet(petId.value);if(res.code===200) dewormings.value=res.data}catch(e){} }
async function loadSurgery() { vaccines.value=[]; dewormings.value=[]; surgeries.value=[]; if(!petId.value) return; try{const res=await medicalApi.surgeryByPet(petId.value);if(res.code===200) surgeries.value=res.data}catch(e){} }
</script>
