<template>
  <div>
    <h1 class="page-title">毕业证书</h1>
    <div class="card">
      <div class="form-group"><label>宠物ID</label><input v-model.number="petId" placeholder="输入宠物ID" /></div>
      <button class="btn btn-primary" @click="loadCerts">查看证书</button>
    </div>
    <div v-if="certs.length" style="margin-top:16px">
      <div v-for="c in certs" :key="c.id" class="card" style="border:2px solid gold;background:#fffdf0">
        <div style="text-align:center">
          <h2 style="color:#b8860b">🎓 毕业证书</h2>
          <p style="font-size:18px;margin:12px 0">证书编号：<strong>{{c.certNo}}</strong></p>
          <p>宠物ID：{{c.petId}} | 课程ID：{{c.courseId}}</p>
          <p>颁发日期：{{c.issueDate}}</p>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { certificateApi } from '../api'
const petId = ref(''), certs = ref([])
async function loadCerts() { certs.value=[]; if(!petId.value) return; try{const res=await certificateApi.getByPet(petId.value);if(res.code===200) certs.value=res.data}catch(e){} }
</script>
