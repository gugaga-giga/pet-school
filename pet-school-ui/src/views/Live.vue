<template>
  <div>
    <h1 class="page-title">远程直播</h1>
    <div class="card">
      <button class="btn btn-primary" @click="loadLives">刷新直播列表</button>
    </div>
    <div v-if="lives.length" style="margin-top:16px">
      <div v-for="l in lives" :key="l.id" class="card">
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div>
            <h3>{{l.title}}</h3>
            <p style="color:#888;font-size:13px">训练师ID：{{l.trainerId}} | 开始：{{l.startTime}}</p>
          </div>
          <div>
            <span :class="['tag',l.status===0?'tag-warning':l.status===1?'tag-danger':'tag-success']">{{['未开始','直播中','已结束'][l.status]}}</span>
            <button v-if="l.status===0" class="btn btn-sm btn-primary" style="margin-left:8px" @click="startLive(l.id)">开始</button>
            <button v-if="l.status===1" class="btn btn-sm btn-secondary" style="margin-left:8px" @click="endLive(l.id)">结束</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { liveApi } from '../api'
const lives = ref([])
async function loadLives() { try{const res=await liveApi.listAll();if(res.code===200) lives.value=res.data}catch(e){} }
async function startLive(id) { try{await liveApi.start(id);loadLives()}catch(e){} }
async function endLive(id) { try{await liveApi.end(id);loadLives()}catch(e){} }
onMounted(()=>loadLives())
</script>
