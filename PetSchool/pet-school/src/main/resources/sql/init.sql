CREATE DATABASE IF NOT EXISTS pet_school DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE pet_school;

CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `role` TINYINT NOT NULL DEFAULT 0,
    `avatar` VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `pet` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `breed` VARCHAR(50) DEFAULT NULL,
    `age` INT DEFAULT NULL,
    `weight` DECIMAL(5,2) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `course_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `sort_order` INT DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `category_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `duration` INT NOT NULL DEFAULT 1,
    `monthly_price` DECIMAL(10,2) NOT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `course_package` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `course_id` BIGINT NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `price` DECIMAL(10,2) NOT NULL,
    `level` INT DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `package_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `package_id` BIGINT NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `included` TINYINT DEFAULT 1,
    `extra_price` DECIMAL(10,2) DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `pet_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `pet_id` BIGINT NOT NULL,
    `package_id` BIGINT NOT NULL,
    `months` INT NOT NULL DEFAULT 1,
    `total_price` DECIMAL(10,2) NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `room_type` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `daily_price` DECIMAL(10,2) NOT NULL,
    `facilities` VARCHAR(200) DEFAULT NULL,
    `image` VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `room` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `type_id` BIGINT NOT NULL,
    `room_number` VARCHAR(20) NOT NULL,
    `status` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `boarding_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `pet_id` BIGINT NOT NULL,
    `room_id` BIGINT NOT NULL,
    `check_in` DATE NOT NULL,
    `check_out` DATE NOT NULL,
    `total_price` DECIMAL(10,2) NOT NULL,
    `status` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `training_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `pet_id` BIGINT NOT NULL,
    `trainer_id` BIGINT NOT NULL,
    `item_name` VARCHAR(50) NOT NULL,
    `content` VARCHAR(500) DEFAULT NULL,
    `record_date` DATE NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `training_video` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `record_id` BIGINT NOT NULL,
    `video_url` VARCHAR(255) NOT NULL,
    `cover_url` VARCHAR(255) DEFAULT NULL,
    `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `health_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `pet_id` BIGINT NOT NULL,
    `temperature` DECIMAL(4,1) DEFAULT NULL,
    `weight` DECIMAL(5,2) DEFAULT NULL,
    `appetite` TINYINT DEFAULT 2,
    `spirit` TINYINT DEFAULT 2,
    `check_date` DATE NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `vaccine_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `pet_id` BIGINT NOT NULL,
    `vaccine_name` VARCHAR(50) NOT NULL,
    `inject_date` DATE NOT NULL,
    `next_date` DATE DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `deworming_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `pet_id` BIGINT NOT NULL,
    `deworm_type` TINYINT NOT NULL,
    `do_date` DATE NOT NULL,
    `next_date` DATE DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `surgery_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `pet_id` BIGINT NOT NULL,
    `surgery_name` VARCHAR(100) NOT NULL,
    `surgery_date` DATE NOT NULL,
    `status` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `certificate` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `pet_id` BIGINT NOT NULL,
    `course_id` BIGINT NOT NULL,
    `cert_no` VARCHAR(50) NOT NULL,
    `issue_date` DATE NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `coupon` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `coupon_type` TINYINT NOT NULL,
    `value` DECIMAL(10,2) NOT NULL,
    `min_amount` DECIMAL(10,2) DEFAULT 0,
    `expire_date` DATE NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_coupon` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `coupon_id` BIGINT NOT NULL,
    `status` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `live_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `trainer_id` BIGINT NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME DEFAULT NULL,
    `status` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(100) NOT NULL,
    `content` VARCHAR(500) DEFAULT NULL,
    `is_read` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `user` (username, password, phone, role) VALUES
('admin', '123456', '13800000000', 2),
('trainer_wang', '123456', '13800001111', 1),
('trainer_li', '123456', '13800002222', 1),
('zhangsan', '123456', '13900001111', 0),
('lisi', '123456', '13900002222', 0);

INSERT INTO pet (user_id, name, breed, age, weight) VALUES
(4, '旺财', '金毛', 1, 15.50),
(4, '小黑', '德牧', 3, 32.00),
(5, '球球', '柯基', 2, 12.00),
(5, '豆豆', '泰迪', 1, 5.50);

INSERT INTO course_category (name, sort_order) VALUES
('日常行为管理', 1),
('社会化训练', 2),
('定点大小便训练', 3),
('基础服从训练', 4),
('表演类训练', 5);

INSERT INTO course (category_id, name, duration, monthly_price, description) VALUES
(1, '日常行为管理班', 1, 2999.00, '纠正扑人、乱叫、拆家等不良行为'),
(2, '社会化训练班', 2, 4999.00, '含每周4天外出泛化训练，商场/市场真实环境'),
(3, '定点大小便训练班', 1, 1999.00, '7天快速养成定点排便习惯'),
(4, '基础服从训练班', 1, 2599.00, '坐下、握手、等待、随行等基本指令'),
(5, '表演类训练班', 2, 5999.00, '接飞盘、跳跃、装死等表演技能（额外收费）');

INSERT INTO course_package (course_id, name, price, level) VALUES
(1, '基础套餐', 2999.00, 0),
(1, '进阶套餐', 4599.00, 1),
(2, '基础套餐', 4999.00, 0),
(2, '旗舰套餐', 8999.00, 2),
(4, '基础套餐', 2599.00, 0),
(4, '进阶套餐', 3999.00, 1);

INSERT INTO package_item (package_id, name, included, extra_price) VALUES
(1, '性格测试', 1, 0),
(1, '行为训练', 1, 0),
(1, '清洁', 1, 0),
(1, '健康检查', 0, 200.00),
(1, '社交活动', 0, 300.00),
(1, '训练监控', 0, 500.00),
(2, '性格测试', 1, 0),
(2, '行为训练', 1, 0),
(2, '清洁', 1, 0),
(2, '健康检查', 1, 0),
(2, '社交活动', 1, 0),
(2, '训练监控', 0, 300.00),
(3, '性格测试', 1, 0),
(3, '行为训练', 1, 0),
(3, '清洁', 1, 0),
(3, '健康检查', 1, 0),
(3, '社交活动', 1, 0),
(3, '训练监控', 1, 0),
(3, '社会化泛化外出', 1, 0);

INSERT INTO room_type (name, daily_price, facilities, image) VALUES
('标准间', 128.00, '独立犬舍、每日清洁、基础喂食', '/images/standard.jpg'),
('豪华间', 258.00, '大空间犬舍、空调、每日清洁+梳毛、定制喂食', '/images/deluxe.jpg'),
('VIP套房', 458.00, '独立房间、监控摄像头、空调、定制喂食+加餐、每日梳毛+洗澡', '/images/vip.jpg');

INSERT INTO room (type_id, room_number, status) VALUES
(1, 'A-101', 0), (1, 'A-102', 0), (1, 'A-103', 1),
(2, 'B-201', 0), (2, 'B-202', 0),
(3, 'C-301', 0);

INSERT INTO health_record (pet_id, temperature, weight, appetite, spirit, check_date) VALUES
(1, 38.5, 15.50, 2, 3, '2026-04-20'),
(2, 39.8, 32.00, 1, 1, '2026-04-20'),
(3, 38.0, 12.00, 3, 3, '2026-04-19');

INSERT INTO vaccine_record (pet_id, vaccine_name, inject_date, next_date) VALUES
(1, '狂犬疫苗', '2026-01-15', '2027-01-15'),
(1, '犬瘟疫苗', '2026-01-15', '2026-07-15'),
(2, '狂犬疫苗', '2025-12-01', '2026-12-01');

INSERT INTO deworming_record (pet_id, deworm_type, do_date, next_date) VALUES
(1, 1, '2026-03-01', '2026-06-01'),
(1, 2, '2026-03-01', '2026-04-01'),
(2, 1, '2026-02-15', '2026-05-15');

INSERT INTO coupon (name, coupon_type, value, min_amount, expire_date) VALUES
('新用户体验券', 3, 0.00, 0.00, '2026-12-31'),
('满3000减300', 1, 300.00, 3000.00, '2026-06-30'),
('续报9折券', 2, 10.00, 1000.00, '2026-12-31');

INSERT INTO user_coupon (user_id, coupon_id, status) VALUES
(4, 1, 0), (4, 2, 0), (5, 1, 0);

INSERT INTO live_session (trainer_id, title, start_time, end_time, status) VALUES
(2, '旺财服从训练直播', '2026-04-22 10:00:00', '2026-04-22 11:00:00', 0),
(3, '社会化训练直播', '2026-04-23 14:00:00', NULL, 0);

INSERT INTO message (user_id, title, content, is_read) VALUES
(4, '训练视频已更新', '旺财今日训练视频已上传，请查看', 0),
(4, '健康预警', '小黑体温39.8℃，超过正常范围，请关注', 0),
(5, '课程即将开始', '您报名的基础服从训练班将于明日开课', 1);

CREATE TABLE IF NOT EXISTS `webrtc_signal` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `live_id` BIGINT NOT NULL,
    `offer_sdp` TEXT DEFAULT NULL,
    `answer_sdp` TEXT DEFAULT NULL,
    `sender_candidates` TEXT DEFAULT NULL,
    `receiver_candidates` TEXT DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_live_id` (`live_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
