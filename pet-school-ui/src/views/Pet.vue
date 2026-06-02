<template>
  <div>
    <h1 class="page-title">宠物管理</h1>
    <div class="row">
      <div class="col">
        <div class="card">
          <h3 style="margin-bottom:16px">新增宠物</h3>
          <div class="form-group"><label>主人ID</label><input v-model.number="form.userId" placeholder="主人用户ID" /></div>
          <div class="form-group"><label>宠物名</label><input v-model="form.name" placeholder="请输入宠物名" /></div>
          <div class="form-group"><label>品种</label><input v-model="form.breed" placeholder="如金毛、柯基" /></div>
          <div class="form-group"><label>年龄（岁）</label><input v-model.number="form.age" type="number" placeholder="年龄" /></div>
          <div class="form-group"><label>体重（kg）</label><input v-model.number="form.weight" type="number" step="0.1" placeholder="体重" /></div>
          <button class="btn btn-primary" @click="addPet">添加</button>
          <div v-if="msg" :class="['alert', msgType==='success'?'alert-success':'alert-error']" style="margin-top:16px">{{ msg }}</div>
        </div>
      </div>
      <div class="col">
        <div class="card">
          <h3 style="margin-bottom:16px">查询宠物</h3>
          <div class="form-group"><label>主人ID</label><input v-model.number="queryUserId" placeholder="输入主人用户ID" /></div>
          <button class="btn btn-secondary" @click="queryByUser">查询</button>
          <div v-if="petList.length" style="margin-top:16px">
            <table><thead><tr><th>ID</th><th>宠物名</th><th>品种</th><th>年龄</th><th>体重</th></tr></thead>
            <tbody><tr v-for="p in petList" :key="p.id"><td>{{p.id}}</td><td>{{p.name}}</td><td>{{p.breed}}</td><td>{{p.age}}岁</td><td>{{p.weight}}kg</td></tr></tbody></table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { petApi } from '../api'
const form = ref({ userId: null, name: '', breed: '', age: null, weight: null })
const msg = ref(''), msgType = ref('success')
const queryUserId = ref(''), petList = ref([])
async function addPet() {
  if (!form.value.userId||!form.value.name) { msg.value='主人ID和宠物名为必填'; msgType.value='error'; return }
  try { const res = await petApi.add(form.value); if(res.code===200){msg.value='添加成功！ID:'+res.data.id;msgType.value='success';form.value={userId:null,name:'',breed:'',age:null,weight:null}}else{msg.value=res.message;msgType.value='error'} } catch(e){msg.value='请求失败';msgType.value='error'}
}
async function queryByUser() {
  petList.value = []
  if (!queryUserId.value) return
  try { const res = await petApi.getByUserId(queryUserId.value); if(res.code===200) petList.value=res.data } catch(e){}
}
</script>
