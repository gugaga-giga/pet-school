# 《项目技术分析与毕业答辩说明书（完整版）》

# 宠物综合服务平台（Pet School Management System）

---

## 第一部分：项目概述

### 1.1 项目名称

**宠物综合服务平台（Pet Comprehensive Service Platform）**

### 1.2 项目背景

随着中国宠物经济的高速发展，2025年宠物市场规模已突破3000亿元。然而，传统宠物服务行业仍以线下手工记录为主，存在信息孤岛、流程混乱、数据不可追溯等问题。宠物主人在选择宠物训练、寄宿、医疗服务时，往往面临预约不便、价格不透明、健康数据缺失等痛点。宠物店管理者也因缺乏信息化手段，难以高效管理课程排期、房间调度、医疗预约和财务流水。

### 1.3 解决的问题

本系统解决以下核心问题：

1. **信息孤岛问题**：传统宠物店训练、寄宿、医疗三大业务各自独立，数据无法互通。本系统将三大业务统一在一个平台上，共享用户、宠物、钱包数据。
2. **预约流程混乱**：传统电话/到店预约效率低、易出错。本系统提供在线预约、自动排期、状态追踪的完整预约流程。
3. **支付体系缺失**：传统宠物店缺乏统一的支付和账户体系。本系统实现了虚拟钱包+余额支付+充值+退款+流水的完整支付闭环。
4. **健康数据不可追溯**：宠物健康数据散落在各家医院，无法形成连续的健康档案。本系统实现了健康体检录入、评分、趋势分析、智能建议的完整健康管理。
5. **证书管理手工化**：毕业证书手工制作效率低、易伪造。本系统实现了证书自动生成、二维码验证、在线下载。

### 1.4 目标用户

| 用户角色 | 需求描述 |
|---|---|
| **宠物主人（客户端）** | 在线选课、预约寄宿、预约就诊、查看健康报告、管理钱包、领取优惠券、查看毕业证书 |
| **管理员（管理端）** | 管理课程/房型/科室/医生、处理订单/医疗预约、录入病历、管理优惠券、管理钱包、查看流水 |

### 1.5 系统定位

本系统定位为**中小型宠物综合服务机构的一站式管理平台**，覆盖"训练+寄宿+医疗"三大核心业务场景，同时集成钱包支付、健康体检、优惠券营销、证书管理等增值功能，形成完整的商业闭环。

### 1.6 项目价值

相比传统宠物店管理方式，本系统的优势：

- **效率提升**：在线预约替代电话预约，自动排期替代手工调度
- **数据驱动**：健康评分系统量化宠物健康状态，趋势图表辅助决策
- **财务透明**：钱包流水完整记录每笔收支，可追溯、可审计
- **营销赋能**：优惠券系统支持满减/折扣/指定课程/新用户等多种营销策略
- **信任增强**：毕业证书二维码验证，防止伪造，提升品牌可信度

---

## 第二部分：技术架构分析

### 2.1 技术栈

| 层次 | 技术 | 版本 | 作用 |
|---|---|---|---|
| **前端框架** | Vue 3 | 3.x | 响应式UI，Composition API |
| **前端构建** | Vite | 最新 | 快速热更新，模块打包 |
| **HTTP客户端** | Axios | 最新 | 前后端通信，拦截器统一处理 |
| **图表库** | ECharts | 5.x | 健康趋势图、体重/体温/心率曲线 |
| **后端框架** | Spring Boot | 3.4.1 | RESTful API，自动配置 |
| **ORM框架** | MyBatis | 3.x | SQL映射，灵活查询 |
| **数据库** | MySQL | 8.0 | 数据持久化 |
| **认证授权** | JWT (jjwt) | 0.12.6 | 无状态Token认证 |
| **证书生成** | Java Graphics2D | JDK内置 | 图片绘制，证书生成 |
| **二维码** | ZXing | 3.5.3 | 证书验证二维码 |
| **构建工具** | Maven | 3.9.x | 依赖管理，项目构建 |
| **实时通信** | WebRTC | 浏览器原生 | 远程直播 |

### 2.2 技术架构图

```
┌─────────────────────────────────────────────────────────┐
│                      用户（浏览器）                        │
│                   ┌──────┬──────┐                        │
│                   │客户端│管理端│                        │
│                   └──┬───┴──┬───┘                        │
└──────────────────────┼──────┼───────────────────────────┘
                       │      │
┌──────────────────────▼──────▼───────────────────────────┐
│                    Vue 3 + Vite                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │ClientLayout│ │AdminLayout│ │  Router  │ │  API层   │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
│              ┌──────────────────────┐                    │
│              │  Axios（双Token拦截器）│                    │
│              └──────────┬───────────┘                    │
└─────────────────────────┼───────────────────────────────┘
                          │ HTTP/REST
┌─────────────────────────▼───────────────────────────────┐
│                  Spring Boot 3.4.1                       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │JwtFilter │ │Controller│ │ Service  │ │  Mapper   │   │
│  │(权限过滤) │ │(REST接口) │ │(业务逻辑) │ │(数据访问)  │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
│              ┌──────────────────────┐                    │
│              │  DatabaseInitRunner  │                    │
│              │  (自动建表+数据迁移)   │                    │
│              └──────────┬───────────┘                    │
└─────────────────────────┼───────────────────────────────┘
                          │ JDBC/MyBatis
┌─────────────────────────▼───────────────────────────────┐
│                     MySQL 8.0                            │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐     │
│  │user│ │pet │ │order│ │room│ │medical│ │wallet│ │coupon│ │
│  └────┘ └────┘ └────┘ └────┘ └────┘ └────┘ └────┘     │
│  31张表 · 6个唯一约束 · 5个索引                           │
└─────────────────────────────────────────────────────────┘
```

### 2.3 双端认证架构

本系统采用**双Token独立认证**架构，客户端和管理端使用不同的Token存储：

```
客户端登录 → client_token + client_user → clientRequest（Axios实例）
管理端登录 → admin_token + admin_user → adminRequest（Axios实例）
```

- 两个Axios实例各自携带对应的Token，互不干扰
- 管理端路由守卫检查 `role >= 2`
- JwtFilter在后端统一校验Token有效性
- 写操作（POST/PUT/DELETE）对敏感路径额外校验管理员权限

---

## 第三部分：数据库设计分析

### 3.1 数据库概览

系统共使用 **31张数据表**，按业务领域分为8个模块：

| 模块 | 表名 | 数量 |
|---|---|---|
| 用户与宠物 | user, pet | 2 |
| 课程训练 | course_category, course, course_package, package_item, training_record, training_video | 6 |
| 订单 | pet_order, boarding_order | 2 |
| 寄宿 | room_type, room | 2 |
| 医疗 | department, doctor, medical_order, medical_record, vaccine_record, deworming_record, surgery_record | 7 |
| 健康体检 | pet_health_record, health_rule, health_record | 3 |
| 钱包与优惠券 | wallet, wallet_record, coupon, user_coupon | 4 |
| 其他 | certificate, certificate_template, live_session, webrtc_signal, message | 5 |

### 3.2 核心表结构详解

#### 3.2.1 user（用户表）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(100) | 密码（加密存储） |
| phone | VARCHAR(20) | 手机号，登录凭证 |
| role | TINYINT | 角色：0=客户端，1=训练师，2=管理员 |
| avatar | VARCHAR(255) | 头像URL |

**关联关系**：user → pet（1:N）、user → pet_order（1:N）、user → wallet（1:1）、user → user_coupon（1:N）

#### 3.2.2 wallet（钱包表）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID，**UNIQUE**（每用户仅一个钱包） |
| balance | DECIMAL(12,2) | 当前余额 |
| total_recharge | DECIMAL(12,2) | 累计充值金额 |
| total_consume | DECIMAL(12,2) | 累计消费金额 |
| status | INT | 状态：0=冻结，1=正常 |

**设计要点**：钱包与用户1:1关系，通过 `uk_user_id` 唯一约束保证。余额、充值、消费三个字段冗余存储，避免每次查询都需要SUM流水记录。

#### 3.2.3 wallet_record（钱包流水表）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| type | INT | 类型：1=充值，2=消费，3=退款，4=后台调整 |
| amount | DECIMAL(12,2) | 金额（正数） |
| balance_before | DECIMAL(12,2) | 操作前余额 |
| balance_after | DECIMAL(12,2) | 操作后余额 |
| business_type | VARCHAR(20) | 业务来源：course/boarding/medical/system |
| business_id | BIGINT | 关联业务ID |
| transaction_no | VARCHAR(32) | 交易流水号（TXN+时间戳+序号） |
| remark | VARCHAR(200) | 备注 |

**设计要点**：`balance_before` 和 `balance_after` 记录每次操作前后的余额快照，可用于对账和审计。`transaction_no` 自动生成（格式：TXN202606011430000001），保证唯一性。

#### 3.2.4 pet_order（课程订单表）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键 |
| order_no | VARCHAR(32) | 订单号（C+时间戳+随机数） |
| user_id | BIGINT | 下单用户 |
| pet_id | BIGINT | 关联宠物 |
| package_id | BIGINT | 关联课程套餐 |
| months | INT | 购买月数 |
| total_price | DECIMAL(10,2) | 原价 |
| coupon_id | BIGINT | 使用的优惠券ID |
| coupon_amount | DECIMAL(10,2) | 优惠金额 |
| final_price | DECIMAL(10,2) | 实付金额 = total_price - coupon_amount |
| status | TINYINT | 0=待支付，1=已支付，2=已取消，4=已退款 |
| payment_type | INT | 支付方式：1=钱包 |
| transaction_no | VARCHAR(32) | 交易流水号 |

**状态机设计**：
```
0(待支付) → 1(已支付) → 4(已退款)
0(待支付) → 2(已取消)
1(已支付) → 2(已取消，含退款)
```

#### 3.2.5 medical_order（医疗订单表）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键 |
| order_no | VARCHAR(32) | 订单号（MED+时间戳+随机数），**UNIQUE** |
| user_id/pet_id/doctor_id/department_id | BIGINT | 关联用户/宠物/医生/科室 |
| appointment_time | DATETIME | 预约时间 |
| symptom | TEXT | 症状描述 |
| price | DECIMAL(10,2) | 挂号费（从department.price获取） |
| status | INT | 0=待确认，1=已预约，2=就诊中，3=已完成，4=已取消 |

**状态机设计**（与课程订单不同）：
```
0(待确认) → 1(已预约，管理员确认+扣款) → 2(就诊中) → 3(已完成)
0(待确认) → 4(已取消)
1(已预约) → 4(已取消，含退款)
```

#### 3.2.6 pet_health_record（宠物健康体检表）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键 |
| pet_id | BIGINT | 关联宠物 |
| pet_type | VARCHAR(20) | 宠物类型：dog/cat |
| temperature | DECIMAL(4,1) | 体温 |
| weight | DECIMAL(5,2) | 体重 |
| heart_rate | INT | 心率 |
| respiration_rate | INT | 呼吸频率 |
| appetite | INT | 食欲：0=差，1=一般，2=好 |
| mental_status | INT | 精神状态：0=差，1=一般，2=好 |
| vaccine_status | INT | 疫苗状态：0=未接种，1=已接种 |
| deworming_status | INT | 驱虫状态：0=未驱虫，1=已驱虫 |
| health_score | INT | 健康评分（0-100） |
| risk_level | INT | 风险等级：0=安全，1=注意，2=风险，3=危险 |
| ai_advice | TEXT | AI智能建议 |

### 3.3 数据库ER关系图

```
                          ┌──────────┐
                          │   user   │
                          └────┬─────┘
                 ┌─────────────┼─────────────┬──────────────┐
                 │             │             │              │
            ┌────▼────┐  ┌────▼────┐  ┌─────▼─────┐  ┌────▼────┐
            │   pet   │  │  wallet │  │user_coupon│  │ message │
            └────┬────┘  └────┬────┘  └─────┬─────┘  └─────────┘
        ┌────────┼─────────────┼─────────────┘
        │        │             │
   ┌────▼──┐ ┌──▼──────┐ ┌───▼──────────┐
   │pet_order│ │boarding_order│ │wallet_record │
   └────┬──┘ └──┬──────┘ └──────────────┘
        │       │
        │  ┌────▼──────────────────────────────────┐
        │  │              medical_order             │
        │  └────┬──────────────┬────────────────────┘
        │       │              │
   ┌────▼───────▼──┐    ┌─────▼──────────┐
   │medical_record │    │   department    │
   └───────────────┘    └────┬───────────┘
                             │
                        ┌────▼────┐
                        │  doctor │
                        └─────────┘

   ┌────────────────────────────────────────────────┐
   │              pet (中心节点)                      │
   ├────────────────────────────────────────────────┤
   │ pet → pet_health_record (健康体检)              │
   │ pet → vaccine_record (疫苗记录)                 │
   │ pet → deworming_record (驱虫记录)               │
   │ pet → certificate (毕业证书)                    │
   │ pet → training_record (训练记录)                │
   └────────────────────────────────────────────────┘

   ┌────────────────────────────────────────────────┐
   │              coupon (优惠券体系)                  │
   ├────────────────────────────────────────────────┤
   │ coupon → user_coupon (用户领取)                  │
   │ user_coupon → pet_order/boarding_order (使用)   │
   └────────────────────────────────────────────────┘
```

---

## 第四部分：功能模块分析

### 4.1 用户管理模块

**实现原理**：
- 后端：`AuthController` 提供登录/注册接口，`JwtUtil` 生成Token
- 前端：`Login.vue` 实现登录/注册表单，根据 `role` 重定向到不同端
- 认证：`JwtFilter` 拦截所有请求，校验Token，敏感操作校验 `role >= 2`

**核心代码**：
```java
// AuthController.java - 登录逻辑
@PostMapping("/login")
public Map<String, Object> login(@RequestBody User user) {
    User dbUser = userService.getByPhone(user.getPhone());
    if (dbUser != null && dbUser.getPassword().equals(user.getPassword())) {
        String token = JwtUtil.generateToken(dbUser.getId(), dbUser.getRole());
        // 返回token和用户信息
    }
}
```

**业务流程**：
```
用户输入手机号+密码 → 后端查询user表 → 验证密码 → 生成JWT Token
→ 前端存储token到localStorage → 后续请求携带Token → JwtFilter校验
```

### 4.2 宠物管理模块

**实现原理**：
- 用户可添加多只宠物，宠物与用户1:N关系
- 宠物信息在订单、病历、健康体检、证书等多处被冗余存储（pet_name字段），避免JOIN查询

**核心代码**：
```java
// PetController.java
@PostMapping("/add")
public Map<String, Object> add(@RequestBody Pet pet) {
    petMapper.insert(pet);
    return ok("添加成功", pet);
}
```

### 4.3 课程训练模块

**实现原理**：
- 课程按分类组织：`course_category` → `course` → `course_package` → `package_item`
- 用户选课流程：浏览分类 → 选择课程 → 选择套餐 → 选择宠物 → 应用优惠券 → 创建订单

**核心代码**（订单创建）：
```java
// PetOrderServiceImpl.java - create()
@Transactional
public int create(PetOrder order) {
    // 1. 查询套餐价格，计算totalPrice = package.price * months
    // 2. 如果有couponId，调用couponService.calculateDiscount()计算优惠
    // 3. 设置finalPrice = totalPrice - couponAmount
    // 4. 生成订单号 orderNo = "C" + timestamp + random
    // 5. 冗余存储courseName, packageName, petName
    // 6. 标记优惠券为已使用 couponService.useCoupon()
    // 7. 插入订单，status=0（待支付）
}
```

**支付流程**：
```java
// PetOrderServiceImpl.java - pay()
@Transactional
public int pay(Long id, Long userId) {
    // 1. 校验订单存在且属于当前用户
    // 2. 校验status==0（只有待支付可支付）
    // 3. 获取实付金额 finalPrice 或 totalPrice
    // 4. 调用 walletService.deduct() 扣减余额
    // 5. 更新订单状态为1（已支付）
}
```

**取消退款流程**：
```java
// PetOrderServiceImpl.java - cancel()
@Transactional
public int cancel(Long id, Long userId) {
    // 1. 校验订单存在且属于当前用户
    // 2. 校验status==0或1（待支付或已支付可取消）
    // 3. 如果status==1（已支付），调用walletService.refund()退款
    // 4. 如果有优惠券，调用couponService.returnCoupon()退回
    // 5. 更新订单状态为2（已取消）
}
```

### 4.4 寄宿模块

**实现原理**：
- 房型管理：`room_type`（含容量capacity）→ `room`（具体房间）
- 容量自动管理：添加房间时自动增加房型容量，删除房间时自动减少
- 支付时检查容量，支付成功扣减容量；取消已支付订单时释放容量

**核心代码**（容量管理）：
```java
// RoomServiceImpl.java
public void add(Room room) {
    roomMapper.insert(room);
    roomTypeService.incrementCapacity(room.getTypeId()); // 自动增加容量
}

// BoardingOrderServiceImpl.java - pay()
@Transactional
public int pay(Long id, Long userId) {
    // 1. 检查房型容量
    // 2. 钱包扣款
    // 3. 扣减房型容量 roomTypeService.decrementCapacity()
    // 4. 更新订单状态
}
```

**业务流程**：
```
选择房型 → 查看房间 → 选择入住/退房日期 → 选择宠物 → 应用优惠券
→ 计算价格（日价×天数-优惠）→ 创建订单 → 钱包支付 → 扣减容量
```

### 4.5 医疗模块

**实现原理**：
- 科室管理：`department`（含挂号费price）→ `doctor`（含科室关联）
- 医疗订单5状态流转：待确认→已预约→就诊中→已完成 / 已取消
- 管理员确认预约时自动扣款，录入病历时自动完成订单

**核心代码**（管理员确认+扣款）：
```java
// MedicalController.java - updateOrderStatus()
if (status == 1 && order.getStatus() == 0) {
    // 管理员确认预约 → 自动扣款
    walletService.deduct(order.getUserId(), order.getPrice(),
        "medical", order.getId(), "医疗支付-" + order.getOrderNo());
}
```

**病历录入**：
```java
// MedicalRecordServiceImpl.java - create()
@Transactional
public int create(MedicalRecord record) {
    medicalRecordMapper.insert(record);
    // 同时更新医疗订单状态为3（已完成）
    medicalOrderMapper.updateStatus(record.getMedicalOrderId(), 3);
}
```

**业务流程**：
```
用户：选科室 → 选医生 → 选宠物 → 选时间 → 填症状 → 提交预约(status=0)
管理员：确认预约(status=1, 自动扣款) → 开始诊疗(status=2) → 录入病历(status=3, 自动完成)
用户取消：status=0或1 → 退款(如已支付) → status=4
```

### 4.6 健康体检模块

**实现原理**：
- 100分制健康评分系统，基于可配置的 `health_rule` 规则表
- 评分维度：体温(20分)、体重(20分)、心率(15分)、呼吸频率(15分)、食欲(10分)、精神状态(10分)、疫苗(5分)、驱虫(5分)
- 风险等级：0=安全，1=注意，2=风险，3=危险
- AI建议基于规则引擎生成，根据异常指标组合给出针对性建议

**核心代码**（评分计算）：
```java
// PetHealthServiceImpl.java - calculateScore()
private int calculateScore(PetHealthRecord record, List<HealthRule> rules) {
    int score = 100;
    for (HealthRule rule : rules) {
        // 对每个指标，检查是否在正常范围
        // 超出范围则扣减对应分数
        // 根据偏离程度设置风险等级
    }
    return Math.max(0, score);
}
```

**前端展示**：
- ECharts环形图展示健康评分
- ECharts折线图展示体重/体温/心率趋势
- 时间线展示历史体检记录
- AI建议卡片展示风险提示和改善建议

### 4.7 优惠券模块

**实现原理**：
- 7种优惠券类型：满减、折扣、无门槛、指定课程、寄宿专属、新用户、限时
- 优惠券状态：`coupon.status`(1=启用/0=禁用) + `user_coupon.status`(0=未使用/1=已使用/2=已过期)
- 优惠券使用：下单时选择优惠券 → 后端计算折扣 → 标记优惠券已使用
- 优惠券退回：取消订单时自动退回优惠券

**核心代码**（折扣计算）：
```java
// CouponServiceImpl.java - calculateDiscount()
public BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount) {
    UserCoupon uc = userCouponMapper.selectById(userCouponId);
    Coupon coupon = couponMapper.selectById(uc.getCouponId());
    // 满减：orderAmount >= minAmount → 减 discountValue
    // 折扣：orderAmount * (1 - discountValue/100)，不超过maxDiscount
    // 无门槛：直接减 discountValue
}
```

### 4.8 钱包系统

**实现原理**：
- 每用户一个钱包账户（wallet表，user_id唯一约束）
- 4种流水类型：充值(1)、消费(2)、退款(3)、后台调整(4)
- 流水记录包含操作前后余额快照，支持对账
- 交易流水号自动生成：TXN+日期时间+4位序号

**核心代码**（扣款逻辑）：
```java
// WalletServiceImpl.java - deduct()
@Transactional
public void deduct(Long userId, BigDecimal amount, ...) {
    Wallet wallet = ensureWallet(userId);
    if (wallet.getStatus() == 0) throw new RuntimeException("钱包已冻结");
    if (wallet.getBalance().compareTo(amount) < 0) throw new RuntimeException("余额不足");
    BigDecimal balanceBefore = wallet.getBalance();
    BigDecimal balanceAfter = balanceBefore.subtract(amount);
    walletMapper.updateBalance(userId, balanceAfter, ...);
    // 生成流水记录
    WalletRecord record = new WalletRecord();
    record.setType(2); // 消费
    record.setBalanceBefore(balanceBefore);
    record.setBalanceAfter(balanceAfter);
    record.setTransactionNo(generateTransactionNo());
    walletRecordMapper.insert(record);
}
```

**安全措施**：
- 所有金额计算后端完成，前端不传支付金额
- 支付时重新查询订单金额，不信任前端
- 余额不足时拒绝支付
- 禁止重复支付（状态检查）
- 钱包冻结时禁止充值和支付
- 管理端接口权限控制（role >= 2）

### 4.9 毕业证书系统

**实现原理**：
- 使用Java Graphics2D绘制1200×850像素的证书图片
- ZXing生成二维码，嵌入证书验证URL
- 证书编号自动生成，唯一约束保证不重复
- 支持在线查看、下载、重新生成

**核心代码**（证书生成）：
```java
// CertificateService.java - generateCertificateImage()
BufferedImage image = new BufferedImage(1200, 850, BufferedImage.TYPE_INT_RGB);
Graphics2D g = image.createGraphics();
// 绘制背景、边框、标题
// 绘制宠物名、主人名、课程名、毕业日期
// 生成二维码（验证URL: /certificate/verify/{certNo}）
// 嵌入二维码到证书右下角
// 保存为PNG文件
```

**验证流程**：
```
扫描证书二维码 → 打开/certificate/verify/{certNo} → 查询certificate表
→ 显示证书信息（宠物名、主人、课程、日期、状态）
```

---

## 第五部分：核心业务流程

### 5.1 用户完整业务流程

```
注册账号 → 登录系统（客户端）
    ↓
添加宠物（可添加多只）
    ↓
┌───────────────┬───────────────┬───────────────┐
│   课程训练     │   寄宿服务     │   医疗服务     │
│  浏览课程      │  选择房型      │  选择科室      │
│  选择套餐      │  选择房间      │  选择医生      │
│  应用优惠券    │  选择日期      │  填写症状      │
│  创建订单      │  应用优惠券    │  提交预约      │
│  钱包支付      │  创建订单      │  管理员确认    │
│  课程完成      │  钱包支付      │  (自动扣款)    │
│  获得证书      │  寄宿完成      │  就诊诊疗      │
│               │               │  录入病历      │
└───────────────┴───────────────┴───────────────┘
    ↓               ↓               ↓
┌───────────────────────────────────────────────┐
│              我的钱包                           │
│  查看余额 · 充值 · 查看流水                      │
│  余额可用于：课程支付/寄宿支付/医疗支付            │
└───────────────────────────────────────────────┘
    ↓
┌───────────────────────────────────────────────┐
│              宠物健康                           │
│  体检记录 · 健康评分 · 趋势图表 · AI建议          │
│  疫苗提醒 · 驱虫提醒                            │
└───────────────────────────────────────────────┘
```

### 5.2 医疗业务流程图

```
用户端                           管理端
  │                               │
  ├─ 选科室 ──────────────────────┤
  ├─ 选医生 ──────────────────────┤
  ├─ 选宠物+时间+症状              │
  ├─ 提交预约 (status=0) ────────→│
  │                               ├─ 确认预约 (status=1)
  │                               │  └─ 自动扣款(wallet.deduct)
  │                               ├─ 开始诊疗 (status=2)
  │                               ├─ 录入病历
  │                               │  └─ 自动完成 (status=3)
  │                               │
  ├─ 取消预约 (status=0/1)        │
  │  └─ 如已支付 → 自动退款        │
  │     (wallet.refund)           │
  │                               │
  ├─ 查看我的预约                  │
  ├─ 查看我的病历                  │
  ├─ 疫苗/驱虫提醒                 │
```

### 5.3 健康管理流程图

```
管理员录入体检数据
    ↓
系统自动计算健康评分（100分制）
    ├─ 体温评分（20分）
    ├─ 体重评分（20分）
    ├─ 心率评分（15分）
    ├─ 呼吸频率评分（15分）
    ├─ 食欲评分（10分）
    ├─ 精神状态评分（10分）
    ├─ 疫苗状态评分（5分）
    └─ 驱虫状态评分（5分）
    ↓
根据评分确定风险等级
    ├─ 90-100分 → 风险0（安全）
    ├─ 70-89分 → 风险1（注意）
    ├─ 50-69分 → 风险2（风险）
    └─ 0-49分 → 风险3（危险）
    ↓
基于规则引擎生成AI建议
    ├─ 针对异常指标给出具体建议
    ├─ 组合多个异常给出综合建议
    └─ 推荐就医/复查/日常护理
    ↓
用户端查看
    ├─ 健康评分环形图
    ├─ 各指标状态
    ├─ ECharts趋势图（体重/体温/心率）
    └─ AI建议卡片
```

### 5.4 钱包支付流程图

```
用户点击"余额支付"
    ↓
后端接收支付请求
    ↓
检查订单状态（必须为待支付）
    ↓
重新查询订单金额（不信任前端）
    ↓
检查钱包状态（必须为正常）
    ↓
检查余额是否充足
    ├─ 不足 → 返回"余额不足，请先充值"
    └─ 充足 ↓
        扣减余额（wallet.balance -= amount）
            ↓
        更新累计消费（wallet.total_consume += amount）
            ↓
        生成消费流水（wallet_record, type=2）
            ↓
        记录余额变化（balance_before → balance_after）
            ↓
        生成交易流水号（TXN+时间戳+序号）
            ↓
        更新订单状态为"已支付"
            ↓
        返回支付成功
```

---

## 第六部分：项目亮点分析

### 亮点1：虚拟钱包支付系统

**为什么是亮点**：不是简单的余额字段，而是实现了完整的钱包账户体系——独立wallet表、wallet_record流水表、4种流水类型、交易流水号、余额快照（before/after）、冻结/启用机制。支持充值→支付→退款→后台调整的完整闭环，流水可追溯、可对账。

### 亮点2：医疗预约与电子病历系统

**为什么是亮点**：实现了5状态医疗订单流转（待确认→已预约→就诊中→已完成→已取消），管理员确认预约时自动扣款，录入病历时自动完成订单。病历包含主诉、体格检查、诊断、医嘱、用药等完整医疗信息，符合真实医疗业务流程。

### 亮点3：健康评分与智能建议系统

**为什么是亮点**：基于可配置规则表的100分制评分系统，8个维度加权评分，4级风险等级。规则存储在数据库health_rule表中，可动态调整，无需修改代码。AI建议基于规则引擎，根据异常指标组合生成针对性建议，不是简单的if-else硬编码。

### 亮点4：毕业证书自动生成与验证

**为什么是亮点**：使用Java Graphics2D程序化绘制证书图片，ZXing生成二维码嵌入验证URL。证书编号唯一约束，支持在线验证真伪。整个生成过程无需人工干预，订单完成后可自动触发。

### 亮点5：多业务统一支付中心

**为什么是亮点**：课程、寄宿、医疗三种完全不同的业务共享同一个钱包支付系统。通过business_type字段区分业务来源，通过统一的WalletService接口处理所有支付和退款，避免了每种业务各自实现支付逻辑的重复代码。

### 亮点6：优惠券与订单深度集成

**为什么是亮点**：7种优惠券类型覆盖满减、折扣、无门槛、指定课程、寄宿专属、新用户、限时等场景。优惠券在下单时使用、取消时自动退回，折扣金额后端计算不信任前端，支持指定课程/寄宿的定向优惠。

### 亮点7：双端独立认证架构

**为什么是亮点**：客户端和管理端使用独立的Token（client_token/admin_token）和独立的Axios实例，互不干扰。路由守卫根据endpoint类型选择对应的Token，JwtFilter在后端统一校验，敏感写操作额外校验role >= 2。

### 亮点8：数据库自动迁移机制

**为什么是亮点**：DatabaseInitRunner在应用启动时自动创建缺失的表、添加缺失的字段、清理重复数据、补加唯一约束。支持`CREATE TABLE IF NOT EXISTS`和`addColumnIfNotExists`模式，无需手动执行SQL脚本，降低了部署复杂度。

### 亮点9：房型容量自动管理

**为什么是亮点**：添加/删除房间时自动增减房型容量，支付时检查容量并扣减，取消/退款时自动释放容量。启动时重新计算所有房型容量（recalculateRoomTypeCapacity），确保数据一致性。

### 亮点10：防重复数据与数据一致性保护

**为什么是亮点**：department和doctor表通过UNIQUE KEY uk_name约束防止重复插入，启动时自动清理历史重复数据。medical_order通过uk_order_no约束防止重复订单。钱包操作通过状态检查防止重复支付和重复退款。

### 亮点11：ECharts健康趋势可视化

**为什么是亮点**：前端使用ECharts绑定体重、体温、心率三个维度的趋势折线图，支持30条历史记录的趋势展示。环形图展示健康评分，直观反映宠物健康状态。

### 亮点12：WebRTC远程直播

**为什么是亮点**：基于WebRTC实现管理员到客户端的实时视频直播，支持SDP协商和ICE候选交换。live_session管理直播状态，webrtc_signal表存储信令数据。

---

## 第七部分：答辩老师可能提问的问题

### Q1：为什么采用SpringBoot而不是SSM？

**答**：SpringBoot 3.x相比传统SSM有以下优势：(1)自动配置减少了大量XML配置工作；(2)内嵌Tomcat无需外部部署；(3)starter依赖管理简化了Maven配置；(4)与Spring生态无缝集成，方便后续扩展。对于本项目的快速开发和迭代需求，SpringBoot是更优选择。

### Q2：为什么使用JWT而不是Session？

**答**：JWT是无状态认证，服务器不需要存储会话信息，天然支持分布式部署。本系统前后端分离架构，客户端和管理端独立运行，JWT Token可以方便地在两个前端应用中独立管理。此外，JWT携带用户ID和角色信息，JwtFilter可以直接从Token中提取，无需额外查询数据库。

### Q3：订单状态如何设计？为什么课程订单和医疗订单的状态不同？

**答**：课程订单采用简单的3状态模型（待支付→已支付→已取消），因为课程购买是"先付后用"模式。医疗订单采用5状态模型（待确认→已预约→就诊中→已完成→已取消），因为医疗业务是"先预约后付费"模式——用户提交预约后需要管理员确认，确认时才扣款，诊疗过程需要"就诊中"状态。两种状态模型反映了不同业务场景的流程差异。

### Q4：钱包为什么单独建表，而不是在user表加balance字段？

**答**：(1)单一职责原则：用户表负责身份信息，钱包表负责财务信息；(2)钱包有独立的状态（冻结/正常）和统计字段（累计充值/消费），放在user表会导致字段膨胀；(3)钱包流水(wallet_record)需要与钱包关联，独立建表更清晰；(4)后续扩展（如多币种、积分）只需修改钱包相关表，不影响用户表。

### Q5：健康评分如何计算？为什么是100分制？

**答**：100分制评分基于8个维度加权：体温20分、体重20分、心率15分、呼吸频率15分、食欲10分、精神状态10分、疫苗5分、驱虫5分。评分规则存储在health_rule表中，包含每个指标的正常范围和扣分规则。100分制直观易懂，类似于人类健康评分，用户容易理解。评分算法从100分开始，对每个异常指标扣减对应分数，最终不低于0分。

### Q6：如何防止重复支付？

**答**：(1)后端支付方法首先检查订单状态，只有status=0（待支付）的订单才能支付；(2)支付方法使用@Transactional注解，状态检查和余额扣减在同一事务中完成；(3)支付成功后状态立即更新为1（已支付），后续支付请求会因为状态检查失败而被拒绝。

### Q7：如何保证钱包余额的一致性？

**答**：(1)所有钱包操作（扣款、退款、充值）都使用@Transactional注解，保证原子性；(2)每次操作先查询当前余额，计算新余额后更新，操作前后余额都记录在wallet_record中；(3)wallet_record表的balance_before和balance_after字段可用于事后对账，发现不一致可追溯。

### Q8：优惠券的折扣计算为什么在后端完成？

**答**：安全考虑。如果折扣计算在前端完成，用户可以通过修改前端代码或拦截请求来篡改折扣金额。后端计算时，服务器从数据库读取优惠券规则和订单金额，独立计算折扣，前端只传递couponId，不传递折扣金额，有效防止了金额篡改。

### Q9：医疗订单和课程订单有什么区别？

**答**：(1)状态模型不同：课程3状态，医疗5状态；(2)支付时机不同：课程是用户主动支付，医疗是管理员确认时自动扣款；(3)价格来源不同：课程价格=套餐价×月数，医疗价格=科室挂号费；(4)后续流程不同：课程支付后即完成，医疗支付后还有就诊和病历录入环节；(5)优惠券支持不同：课程和寄宿支持优惠券，医疗暂不支持。

### Q10：数据库没有外键约束，如何保证数据一致性？

**答**：本系统采用应用层保证一致性，而非数据库外键约束。原因：(1)外键约束会降低批量操作性能；(2)外键约束使得数据迁移和表重构更复杂；(3)应用层通过Service层的业务逻辑校验（如检查用户是否存在、宠物是否属于当前用户）来保证一致性。这种方式在互联网项目中更为常见。

### Q11：为什么使用MyBatis而不是JPA/Hibernate？

**答**：(1)MyBatis对SQL有完全控制力，复杂查询（如多表JOIN、分页、条件筛选）更容易优化；(2)本系统有大量自定义SQL（如按用户查订单、按宠物查病历、按类型查流水），MyBatis的XML映射更灵活；(3)MyBatis学习曲线低，代码可读性高。

### Q12：前端为什么使用Vue 3而不是React？

**答**：(1)Vue 3的Composition API（`<script setup>`）代码更简洁，单文件组件结构清晰；(2)Vue的模板语法更直观，适合快速开发；(3)Vue生态的Vite构建工具开发体验优秀，热更新速度快；(4)Vue在国内社区更活跃，中文文档更完善。

### Q13：毕业证书的二维码验证原理是什么？

**答**：证书生成时，使用ZXing库将验证URL（`/certificate/verify/{certNo}`）编码为二维码图片，嵌入证书右下角。验证时，用户扫描二维码打开验证页面，前端从URL中提取证书编号，调用后端API查询certificate表，返回证书信息（宠物名、主人、课程、毕业日期、状态），与纸质证书对比即可验证真伪。

### Q14：钱包流水的transaction_no如何保证唯一？

**答**：流水号格式为TXN+日期时间+4位序号，例如TXN202606011430000001。日期时间精确到秒，4位序号使用AtomicInteger线程安全计数器，每秒最多支持9999个流水号。同时wallet_record表有idx_transaction_no索引，如果出现重复（极端情况），数据库会报错，事务回滚。

### Q15：如果用户钱包被冻结了怎么办？

**答**：钱包冻结后(status=0)，充值和支付操作都会被拒绝，但退款操作不受影响（退款是资金返还，不应被冻结阻止）。管理员可以在钱包管理页面点击"启用"按钮解冻钱包。冻结操作需要管理员权限（role >= 2），普通用户无法自行冻结/解冻。

### Q16：前端如何区分客户端和管理端的请求？

**答**：通过两个独立的Axios实例（clientRequest和adminRequest）实现。clientRequest从localStorage读取client_token，adminRequest读取admin_token。两个实例的请求拦截器分别添加对应的Token到Authorization头，响应拦截器分别处理401跳转（客户端跳/client，管理端跳/admin）。

### Q17：DatabaseInitRunner的作用是什么？为什么不使用Flyway？

**答**：DatabaseInitRunner在应用启动时自动执行数据库初始化，包括创建缺失的表、添加缺失的字段、插入默认数据、清理重复数据。不使用Flyway的原因：(1)本项目是学习项目，数据库结构频繁变化，Flyway的版本化管理反而增加了维护成本；(2)DatabaseInitRunner的`CREATE TABLE IF NOT EXISTS`和`addColumnIfNotExists`模式更灵活，支持幂等执行。

### Q18：健康评分的规则为什么存在数据库而不是代码中？

**答**：规则存储在health_rule表中，有以下优势：(1)管理员可以通过界面动态调整评分规则，无需修改代码和重新部署；(2)不同宠物类型（狗/猫）可以有不同的正常范围，通过pet_type字段区分；(3)新增指标只需插入新规则记录，不需要修改Java代码；(4)符合开闭原则——对扩展开放，对修改关闭。

### Q19：寄宿订单取消时如何处理房型容量？

**答**：取消已支付的寄宿订单时，系统自动调用roomTypeService.incrementCapacity()释放房型容量。这确保了其他用户可以继续预约该房型。同时，如果订单使用了优惠券，也会自动退回。整个操作在@Transactional事务中完成，保证容量释放和订单状态更新的原子性。

### Q20：系统的权限控制是如何实现的？

**答**：三层权限控制：(1)JwtFilter校验Token有效性，未登录请求返回401；(2)JwtFilter对敏感写操作（POST/PUT/DELETE）检查role >= 2，非管理员返回403；(3)Service层业务校验（如检查订单是否属于当前用户），防止越权操作。敏感路径包括：/medical/department、/medical/doctor、/medical/order/status、/medical/record、/wallet/admin等。

### Q21：为什么医疗订单没有优惠券功能？

**答**：医疗业务与课程/寄宿业务有本质区别——医疗服务价格由科室挂号费决定，通常金额较低且固定，不适合使用满减/折扣类优惠券。此外，医疗行业有价格监管要求，折扣促销可能涉及合规问题。如果未来需要，可以在medical_order表增加coupon_id、coupon_amount、final_price字段，并复用现有的CouponService。

### Q22：前端路由守卫是如何工作的？

**答**：router.beforeEach全局守卫检查目标路由的meta.requireAuth，如果需要认证，根据meta.endpoint（'admin'或'client'）从localStorage读取对应的token和user。如果token不存在，重定向到登录页。对于admin路由，额外检查user.role >= 2。如果JSON解析失败，清除localStorage并重定向。

### Q23：如何处理并发支付问题？

**答**：当前方案通过@Transactional和状态检查来防止并发问题：(1)支付方法首先检查订单状态为0，然后扣款，最后更新状态为1，整个过程在一个事务中；(2)如果两个请求同时到达，第一个事务更新状态后，第二个事务的状态检查会失败。更严格的方案可以使用SELECT FOR UPDATE悲观锁或乐观锁（version字段），但当前方案对于本项目的并发量已足够。

### Q24：优惠券的"发给全部用户"功能如何实现？

**答**：管理员在优惠券管理页面点击"发给全部用户"，前端调用adminApi.sendCoupon()，后端CouponController的send方法查询所有用户，为每个用户创建一条user_coupon记录。这保证了每个用户都能领取到优惠券，且状态为0（未使用）。

### Q25：ECharts图表如何避免内存泄漏？

**答**：Vue 3的onUnmounted生命周期中调用chart.dispose()释放ECharts实例。每次重新渲染图表前，先检查实例是否存在，如果存在则先dispose再重新初始化。窗口resize事件也在onUnmounted中移除监听。

---

## 第八部分：系统不足与优化方向

### 8.1 当前不足

| 不足 | 说明 |
|---|---|
| **未接入真实支付** | 钱包充值是模拟的，没有对接微信/支付宝支付 |
| **并发控制不够严格** | 钱包操作没有使用SELECT FOR UPDATE或乐观锁，高并发下可能出现余额不一致 |
| **通知系统不完善** | message表存在但前端没有消息提醒UI，用户无法及时收到预约确认、退款等通知 |
| **医疗订单无优惠券** | medical_order缺少coupon_id/coupon_amount/final_price字段 |
| **文件上传无CDN** | 视频、头像等文件存储在本地服务器，没有使用OSS/CDN |
| **无接口文档** | 没有集成Swagger/SpringDoc生成API文档 |
| **前端无状态管理** | 没有使用Pinia/Vuex，用户信息通过localStorage直接管理 |
| **无单元测试** | 后端没有JUnit测试用例 |
| **无日志系统** | 没有集成Logback/ELK，异常排查困难 |
| **无分库分表** | 所有数据在单一数据库，大数据量下性能受限 |

### 8.2 优化方向

| 方向 | 方案 |
|---|---|
| **接入真实支付** | 集成微信支付/支付宝SDK，payment_type字段已预留2=微信、3=支付宝 |
| **并发优化** | 钱包操作使用SELECT FOR UPDATE悲观锁，或增加version字段实现乐观锁 |
| **消息推送** | 集成WebSocket实现实时消息推送，预约确认、退款成功等事件自动通知 |
| **医疗优惠券** | medical_order增加coupon相关字段，复用CouponService |
| **对象存储** | 集成阿里云OSS/腾讯云COS，文件上传到云端，CDN加速访问 |
| **API文档** | 集成SpringDoc OpenAPI，自动生成Swagger UI文档 |
| **状态管理** | 引入Pinia管理全局状态（用户信息、钱包余额），减少重复API调用 |
| **单元测试** | 使用JUnit5+Mockito编写Service层单元测试 |
| **日志系统** | 集成Logback+ELK，实现日志采集、搜索、可视化 |
| **AI模型接入** | 健康建议从规则引擎升级为调用大语言模型（如ChatGPT/文心一言），提供更智能的建议 |

---

## 第九部分：三分钟答辩讲稿

各位老师好，我答辩的项目是**宠物综合服务平台**。

随着宠物经济快速发展，传统宠物店面临预约不便、数据孤岛、支付混乱等问题。本系统将训练、寄宿、医疗三大业务统一在一个平台上，实现了从预约到支付到服务的完整闭环。

技术方面，后端采用SpringBoot 3.4 + MyBatis + MySQL 8.0，前端采用Vue 3 + Vite + ECharts，认证采用JWT双Token架构，客户端和管理端独立认证、互不干扰。

功能方面，系统实现了9大核心模块：课程训练支持分类浏览、套餐选择、在线支付；寄宿服务支持房型管理、容量自动调度；医疗系统实现了5状态预约流转和电子病历；健康体检采用100分制评分和规则引擎生成智能建议；钱包系统实现了充值、支付、退款、流水的完整支付闭环；优惠券系统支持7种类型；毕业证书使用Graphics2D自动生成并支持二维码验证。

项目亮点包括：虚拟钱包支付系统实现了完整的账户体系和流水审计；健康评分系统基于可配置规则表，无需修改代码即可调整评分标准；医疗预约与电子病历系统符合真实医疗业务流程；毕业证书自动生成与二维码验证提升了品牌可信度。

以上是我的项目介绍，请各位老师提问。

---

## 第十部分：五分钟答辩讲稿

各位老师好，我答辩的项目是**宠物综合服务平台**。

### 一、项目背景

2025年中国宠物市场规模突破3000亿元，但传统宠物服务行业仍以线下手工记录为主。宠物主人面临预约不便、价格不透明、健康数据缺失等痛点，宠物店管理者也因缺乏信息化手段难以高效运营。本系统旨在通过信息化手段解决这些问题。

### 二、技术架构

系统采用前后端分离架构。后端使用SpringBoot 3.4 + MyBatis + MySQL 8.0，提供RESTful API。前端使用Vue 3 + Vite + ECharts，实现响应式界面。认证采用JWT双Token架构——客户端和管理端使用独立的Token和Axios实例，JwtFilter在后端统一校验，敏感操作额外校验管理员权限。数据库共31张表，覆盖用户、宠物、课程、寄宿、医疗、健康、钱包、优惠券、证书等全部业务领域。

### 三、核心功能

**课程训练模块**：支持课程分类、套餐选择、在线支付。用户浏览课程→选择套餐→应用优惠券→钱包支付，完整闭环。

**寄宿服务模块**：支持房型管理、房间选择、日期选择。系统自动管理房型容量——支付时扣减，取消时释放，启动时重新计算。

**医疗模块**：实现了5状态预约流转——用户提交预约→管理员确认（自动扣款）→开始诊疗→录入病历（自动完成）。电子病历包含主诉、体格检查、诊断、医嘱、用药等完整医疗信息。

**健康体检模块**：100分制评分系统，8个维度加权评分。评分规则存储在数据库health_rule表中，管理员可动态调整。AI建议基于规则引擎，根据异常指标组合生成针对性建议。前端使用ECharts展示健康评分环形图和体重/体温/心率趋势图。

**钱包系统**：每用户一个钱包账户，支持充值、支付、退款、后台调整4种流水类型。流水记录包含操作前后余额快照和交易流水号，支持对账审计。支付时后端重新查询订单金额，不信任前端，有效防止金额篡改。

**优惠券系统**：7种优惠券类型覆盖满减、折扣、无门槛、指定课程、寄宿专属、新用户、限时等场景。下单时使用、取消时自动退回。

**毕业证书**：使用Java Graphics2D自动绘制证书图片，ZXing生成二维码嵌入验证URL，支持在线验证真伪。

### 四、项目亮点

第一，虚拟钱包支付系统实现了完整的账户体系和流水审计，不是简单的余额字段。第二，健康评分系统基于可配置规则表，无需修改代码即可调整评分标准，符合开闭原则。第三，医疗预约与电子病历系统实现了5状态流转和自动扣款，符合真实医疗业务流程。第四，多业务统一支付中心，课程、寄宿、医疗三种业务共享同一个钱包系统，通过business_type字段区分。第五，毕业证书自动生成与二维码验证，提升了品牌可信度。

### 五、总结

本系统覆盖了宠物综合服务的核心业务场景，实现了从预约到支付到服务的完整闭环。系统采用前后端分离架构，模块划分清晰，代码可维护性好。数据库设计遵循单一职责原则，钱包、流水、优惠券等独立建表。安全方面，JWT双Token认证、后端金额计算、状态防重复支付等措施保障了系统安全。

未来优化方向包括接入微信/支付宝真实支付、引入WebSocket实时通知、集成大语言模型提升健康建议质量等。

以上是我的项目介绍，请各位老师提问。

---

## 第十一部分：项目总结

本项目实现了一个功能完整的宠物综合服务平台，覆盖训练、寄宿、医疗三大核心业务，同时集成钱包支付、健康体检、优惠券营销、证书管理等增值功能。

### 技术层面

- 后端采用SpringBoot 3.4 + MyBatis + MySQL 8.0，遵循Controller-Service-Mapper三层架构，职责清晰
- 前端采用Vue 3 Composition API + Vite，31个Vue页面（16个管理端+15个客户端），约110个API接口
- 数据库31张表，6个唯一约束，5个索引，逻辑外键关系清晰
- JWT双Token认证架构，JwtFilter权限控制，敏感操作管理员校验
- DatabaseInitRunner自动建表和数据迁移，降低部署复杂度

### 业务层面

- 课程训练：分类→课程→套餐→订单→支付→证书，完整业务闭环
- 寄宿服务：房型→房间→日期→订单→支付→容量管理，自动化调度
- 医疗服务：科室→医生→预约→确认(扣款)→诊疗→病历(完成)，5状态流转
- 健康体检：8维度评分→风险等级→AI建议→趋势图表，数据驱动决策
- 钱包系统：充值→支付→退款→流水，完整支付闭环
- 优惠券：7种类型→领取→使用→退回，营销闭环

### 工程层面

- 代码规范：统一的返回格式（code/message/data）、统一的异常处理、统一的按钮风格
- 安全措施：后端金额计算、状态防重复、权限校验、Token认证
- 数据一致性：@Transactional事务保证、唯一约束防重复、启动时数据修复
- 可扩展性：payment_type预留微信/支付宝、health_rule可配置规则、wallet支持多业务类型

本项目虽然是一个学习项目，但在业务完整性、技术架构合理性、代码规范性方面都力求接近真实产品标准。系统不是简单的CRUD，而是实现了钱包支付闭环、医疗预约流转、健康评分引擎、证书自动生成等具有技术深度的功能，体现了对真实业务场景的深入理解和工程化实践能力。
