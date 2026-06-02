<template>
  <div>
    <h1 class="page-title">🐾 宠物培训学校管理系统</h1>
    <div class="row">
      <div class="col"><div class="card" style="text-align:center;cursor:pointer" @click="$router.push('/pet')"><div style="font-size:40px">🐕</div><h3>宠物管理</h3><p style="color:#888;font-size:13px">录入/查看宠物档案</p></div></div>
      <div class="col"><div class="card" style="text-align:center;cursor:pointer" @click="$router.push('/course')"><div style="font-size:40px">📚</div><h3>课程选课</h3><p style="color:#888;font-size:13px">浏览课程/套餐/选课下单</p></div></div>
      <div class="col"><div class="card" style="text-align:center;cursor:pointer" @click="$router.push('/room')"><div style="font-size:40px">🏠</div><h3>寄宿服务</h3><p style="color:#888;font-size:13px">房型选择/寄宿下单</p></div></div>
    </div>
    <div class="row" style="margin-top:16px">
      <div class="col"><div class="card" style="text-align:center;cursor:pointer" @click="$router.push('/training')"><div style="font-size:40px">🎬</div><h3>训练反馈</h3><p style="color:#888;font-size:13px">训练记录/视频查看</p></div></div>
      <div class="col"><div class="card" style="text-align:center;cursor:pointer" @click="$router.push('/health')"><div style="font-size:40px">🤖</div><h3>AI健康助手</h3><p style="color:#888;font-size:13px">健康预警/课程推荐/疫苗提醒</p></div></div>
      <div class="col"><div class="card" style="text-align:center;cursor:pointer" @click="$router.push('/live')"><div style="font-size:40px">📡</div><h3>远程直播</h3><p style="color:#888;font-size:13px">观看训练直播</p></div></div>
    </div>
    <div class="row" style="margin-top:16px">
      <div class="col"><div class="card" style="text-align:center;cursor:pointer" @click="$router.push('/order')"><div style="font-size:40px">📋</div><h3>订单管理</h3><p style="color:#888;font-size:13px">课程订单/寄宿订单</p></div></div>
      <div class="col"><div class="card" style="text-align:center;cursor:pointer" @click="$router.push('/medical')"><div style="font-size:40px">💉</div><h3>医疗服务</h3><p style="color:#888;font-size:13px">疫苗/驱虫/手术记录</p></div></div>
      <div class="col"><div class="card" style="text-align:center;cursor:pointer" @click="$router.push('/certificate')"><div style="font-size:40px">🎓</div><h3>毕业证书</h3><p style="color:#888;font-size:13px">查看/生成证书</p></div></div>
    </div>
  </div>
</template>
