<template>
  <div>
    <h1 class="page-title">训练反馈</h1>
    <div class="card">
      <div class="form-group"><label>宠物ID</label><input v-model.number="petId" placeholder="输入宠物ID" /></div>
      <button class="btn btn-primary" @click="loadRecords">查看训练记录</button>
    </div>
    <div v-if="records.length" class="card" style="margin-top:16px">
      <table><thead><tr><th>ID</th><th>训练项目</th><th>训练师ID</th><th>内容</th><th>日期</th></tr></thead>
      <tbody><tr v-for="r in records" :key="r.id"><td>{{r.id}}</td><td>{{r.itemName}}</td><td>{{r.trainerId}}</td><td>{{r.content}}</td><td>{{r.recordDate}}</td></tr></tbody></table>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { trainingApi } from '../api'
const petId = ref(''), records = ref([])
async function loadRecords() { records.value=[]; if(!petId.value) return; try{const res=await trainingApi.recordsByPet(petId.value);if(res.code===200) records.value=res.data}catch(e){} }
</script>
