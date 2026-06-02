<template>
  <div>
    <h1 class="page-title">消息通知</h1>
    <div class="card">
      <div class="form-group"><label>用户ID</label><input v-model.number="userId" placeholder="输入用户ID" /></div>
      <button class="btn btn-primary" @click="loadMessages">查看消息</button>
    </div>
    <div v-if="messages.length" style="margin-top:16px">
      <div v-for="m in messages" :key="m.id" class="card" :style="{borderLeft: m.isRead?'3px solid #ddd':'3px solid #e94560'}">
        <div style="display:flex;justify-content:space-between;align-items:center">
          <div>
            <h4>{{m.title}}</h4>
            <p style="color:#666;font-size:13px">{{m.content}}</p>
            <p style="color:#aaa;font-size:12px">{{m.createTime}}</p>
          </div>
          <button v-if="!m.isRead" class="btn btn-sm btn-secondary" @click="markRead(m.id)">标为已读</button>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { messageApi } from '../api'
const userId = ref(''), messages = ref([])
async function loadMessages() { messages.value=[]; if(!userId.value) return; try{const res=await messageApi.getByUser(userId.value);if(res.code===200) messages.value=res.data}catch(e){} }
async function markRead(id) { try{await messageApi.markRead(id);loadMessages()}catch(e){} }
</script>
