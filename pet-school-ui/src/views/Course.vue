<template>
  <div>
    <h1 class="page-title">课程选课</h1>
    <div class="card">
      <h3 style="margin-bottom:12px">课程分类</h3>
      <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:16px">
        <button v-for="c in categories" :key="c.id" :class="['btn','btn-sm',selectedCat===c.id?'btn-primary':'btn-secondary']" @click="selectCat(c.id)">{{c.name}}</button>
      </div>
    </div>
    <div v-if="courses.length" class="card">
      <table><thead><tr><th>ID</th><th>课程名</th><th>周期</th><th>月费</th><th>描述</th><th>操作</th></tr></thead>
      <tbody><tr v-for="c in courses" :key="c.id"><td>{{c.id}}</td><td>{{c.name}}</td><td>{{c.duration}}个月</td><td style="color:#e94560;font-weight:600">¥{{c.monthlyPrice}}</td><td>{{c.description}}</td><td><button class="btn btn-sm btn-primary" @click="showPackages(c)">选课</button></td></tr></tbody></table>
    </div>
    <div v-if="showPkg" class="card" style="margin-top:16px">
      <h3 style="margin-bottom:12px">{{selectedCourse.name}} - 套餐选择</h3>
      <div class="row">
        <div v-for="pkg in packages" :key="pkg.id" class="col">
          <div class="card" style="border:2px solid #e94560;cursor:pointer" @click="selectPkg(pkg)">
            <h4>{{pkg.name}}</h4>
            <p style="color:#e94560;font-size:20px;font-weight:700">¥{{pkg.price}}</p>
            <p style="font-size:12px;color:#888">等级：{{['基础','进阶','旗舰'][pkg.level]}}</p>
          </div>
        </div>
      </div>
      <div v-if="items.length" style="margin-top:12px">
        <h4>套餐项目</h4>
        <table><thead><tr><th>项目</th><th>是否包含</th><th>额外费用</th></tr></thead>
        <tbody><tr v-for="it in items" :key="it.id"><td>{{it.name}}</td><td><span :class="['tag',it.included?'tag-success':'tag-warning']">{{it.included?'包含':'额外'}}</span></td><td>{{it.included?'-':'¥'+it.extraPrice}}</td></tr></tbody></table>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { courseApi, orderApi } from '../api'
const categories = ref([]), selectedCat = ref(null), courses = ref([])
const showPkg = ref(false), selectedCourse = ref({}), packages = ref([]), items = ref([])
onMounted(async () => { const res = await courseApi.categoryList(); if(res.code===200) { categories.value=res.data; if(res.data.length) selectCat(res.data[0].id) } })
async function selectCat(id) { selectedCat.value=id; const res=await courseApi.getByCategory(id); if(res.code===200) courses.value=res.data; showPkg.value=false }
async function showPackages(c) { selectedCourse.value=c; const res=await courseApi.packageList(c.id); if(res.code===200) packages.value=res.data; items.value=[]; showPkg.value=true }
async function selectPkg(pkg) { const res=await courseApi.packageItems(pkg.id); if(res.code===200) items.value=res.data }
</script>
