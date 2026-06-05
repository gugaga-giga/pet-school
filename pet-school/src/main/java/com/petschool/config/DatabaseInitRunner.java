package com.petschool.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        createWebrtcSignalTable();
        addCapacityColumn();
        recalculateRoomTypeCapacity();
        addPetOrderColumns();
        addBoardingOrderColumns();
        createCertificateTable();
        createCertificateTemplateTable();
        insertDefaultTemplate();
        recreateCouponTable();
        recreateUserCouponTable();
        addOrderCouponColumns();
        createPetHealthRecordTable();
        createHealthRuleTable();
        insertDefaultHealthRules();
        createDepartmentTable();
        createDoctorTable();
        createMedicalOrderTable();
        createMedicalRecordTable();
        recreateVaccineRecordTable();
        recreateDewormingRecordTable();
        insertDefaultDepartments();
        insertDefaultDoctors();
        createWalletTable();
        createWalletRecordTable();
        addOrderPaymentColumns();
        addPetColumns();
    }

    private void createWebrtcSignalTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `webrtc_signal` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`live_id` BIGINT NOT NULL," +
                "`offer_sdp` TEXT DEFAULT NULL," +
                "`answer_sdp` TEXT DEFAULT NULL," +
                "`sender_candidates` TEXT DEFAULT NULL," +
                "`receiver_candidates` TEXT DEFAULT NULL," +
                "`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_live_id` (`live_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.err.println("创建webrtc_signal表失败: " + e.getMessage());
        }
    }

    private void addCapacityColumn() {
        try {
            var columns = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'room_type' AND COLUMN_NAME = 'capacity'",
                    String.class);
            if (columns.isEmpty()) {
                jdbcTemplate.execute("ALTER TABLE room_type ADD COLUMN capacity INT DEFAULT 0");
                jdbcTemplate.execute("UPDATE room_type SET capacity = (SELECT COUNT(*) FROM room WHERE room.type_id = room_type.id)");
                System.out.println("已为room_type表添加capacity字段并初始化");
            }
        } catch (Exception e) {
            System.err.println("添加capacity字段失败: " + e.getMessage());
        }
    }

    private void recalculateRoomTypeCapacity() {
        try {
            jdbcTemplate.execute(
                "UPDATE room_type rt SET capacity = (" +
                "  SELECT COUNT(*) FROM room r WHERE r.type_id = rt.id AND IFNULL(r.status, 0) != 1" +
                ")"
            );
            System.out.println("已重新计算所有房型的可用容量");
        } catch (Exception e) {
            System.err.println("重新计算房型容量失败: " + e.getMessage());
        }
    }

    private void addPetOrderColumns() {
        addColumnIfNotExists("pet_order", "order_no", "VARCHAR(32)");
        addColumnIfNotExists("pet_order", "course_name", "VARCHAR(100)");
        addColumnIfNotExists("pet_order", "package_name", "VARCHAR(50)");
        addColumnIfNotExists("pet_order", "pet_name", "VARCHAR(50)");
    }

    private void addBoardingOrderColumns() {
        addColumnIfNotExists("boarding_order", "order_no", "VARCHAR(32)");
        addColumnIfNotExists("boarding_order", "room_type_name", "VARCHAR(50)");
        addColumnIfNotExists("boarding_order", "room_number", "VARCHAR(20)");
        addColumnIfNotExists("boarding_order", "pet_name", "VARCHAR(50)");
        addColumnIfNotExists("boarding_order", "create_time", "DATETIME DEFAULT CURRENT_TIMESTAMP");
    }

    private void addColumnIfNotExists(String table, String column, String definition) {
        try {
            var columns = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    String.class, table, column);
            if (columns.isEmpty()) {
                jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
                System.out.println("已为" + table + "表添加" + column + "字段");
            }
        } catch (Exception e) {
            System.err.println("添加" + table + "." + column + "字段失败: " + e.getMessage());
        }
    }

    private void createCertificateTable() {
        try {
            var columns = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'certificate'",
                    String.class);
            boolean needsRebuild = columns.isEmpty() || !columns.contains("certificate_no") || !columns.contains("owner_name") || !columns.contains("certificate_url");
            if (needsRebuild && !columns.isEmpty()) {
                jdbcTemplate.execute("DROP TABLE IF EXISTS certificate");
                System.out.println("已删除旧版certificate表，准备重建");
            }
            String sql = "CREATE TABLE IF NOT EXISTS `certificate` (" +
                    "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                    "`certificate_no` VARCHAR(32) NOT NULL," +
                    "`user_id` BIGINT," +
                    "`pet_id` BIGINT," +
                    "`order_id` BIGINT," +
                    "`course_id` BIGINT," +
                    "`course_name` VARCHAR(100)," +
                    "`pet_name` VARCHAR(50)," +
                    "`owner_name` VARCHAR(50)," +
                    "`graduate_date` DATE," +
                    "`issue_date` DATE," +
                    "`template_id` BIGINT," +
                    "`certificate_url` VARCHAR(255)," +
                    "`status` INT DEFAULT 0," +
                    "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (`id`)," +
                    "UNIQUE KEY `uk_certificate_no` (`certificate_no`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.err.println("创建certificate表失败: " + e.getMessage());
        }
    }

    private void createCertificateTemplateTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `certificate_template` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR(50) NOT NULL," +
                "`background_url` VARCHAR(255)," +
                "`style` TEXT," +
                "`status` INT DEFAULT 1," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.err.println("创建certificate_template表失败: " + e.getMessage());
        }
    }

    private void insertDefaultTemplate() {
        try {
            jdbcTemplate.execute("INSERT INTO certificate_template (name, status) SELECT '默认模板', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM certificate_template)");
        } catch (Exception e) {
            System.err.println("插入默认证书模板失败: " + e.getMessage());
        }
    }

    private void recreateCouponTable() {
        try {
            var oldCols = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'coupon' AND COLUMN_NAME = 'coupon_type'",
                    String.class);
            if (!oldCols.isEmpty()) {
                jdbcTemplate.execute("DROP TABLE IF EXISTS `user_coupon`");
                jdbcTemplate.execute("DROP TABLE IF EXISTS `coupon`");
                System.out.println("已删除旧版coupon表，将重建新表");
            }
        } catch (Exception e) {
            System.err.println("检查coupon表结构失败: " + e.getMessage());
        }
        String sql = "CREATE TABLE IF NOT EXISTS `coupon` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR(50) NOT NULL," +
                "`type` INT NOT NULL DEFAULT 3," +
                "`discount_type` INT NOT NULL DEFAULT 1," +
                "`discount_value` DECIMAL(10,2) NOT NULL," +
                "`min_amount` DECIMAL(10,2) DEFAULT 0," +
                "`max_discount` DECIMAL(10,2) DEFAULT NULL," +
                "`scope_type` INT DEFAULT 1," +
                "`scope_id` BIGINT DEFAULT NULL," +
                "`start_time` DATETIME DEFAULT NULL," +
                "`end_time` DATETIME DEFAULT NULL," +
                "`total_count` INT DEFAULT NULL," +
                "`receive_count` INT DEFAULT 0," +
                "`per_limit` INT DEFAULT 1," +
                "`status` INT DEFAULT 1," +
                "`description` VARCHAR(255) DEFAULT NULL," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.err.println("创建coupon表失败: " + e.getMessage());
        }
    }

    private void recreateUserCouponTable() {
        try {
            var oldCols = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_coupon' AND COLUMN_NAME = 'receive_time'",
                    String.class);
            if (oldCols.isEmpty()) {
                jdbcTemplate.execute("DROP TABLE IF EXISTS `user_coupon`");
                System.out.println("已删除旧版user_coupon表，将重建新表");
            }
        } catch (Exception e) {
            System.err.println("检查user_coupon表结构失败: " + e.getMessage());
        }
        String sql = "CREATE TABLE IF NOT EXISTS `user_coupon` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`user_id` BIGINT NOT NULL," +
                "`coupon_id` BIGINT NOT NULL," +
                "`status` INT DEFAULT 0," +
                "`receive_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`use_time` DATETIME DEFAULT NULL," +
                "`expire_time` DATETIME DEFAULT NULL," +
                "`order_id` BIGINT DEFAULT NULL," +
                "`coupon_name` VARCHAR(50) DEFAULT NULL," +
                "`discount_amount` DECIMAL(10,2) DEFAULT NULL," +
                "PRIMARY KEY (`id`)," +
                "KEY `idx_user_id` (`user_id`)," +
                "KEY `idx_coupon_id` (`coupon_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.err.println("创建user_coupon表失败: " + e.getMessage());
        }
    }

    private void addOrderCouponColumns() {
        addColumnIfNotExists("pet_order", "coupon_id", "BIGINT DEFAULT NULL");
        addColumnIfNotExists("pet_order", "coupon_amount", "DECIMAL(10,2) DEFAULT 0");
        addColumnIfNotExists("pet_order", "final_price", "DECIMAL(10,2) DEFAULT NULL");
        addColumnIfNotExists("boarding_order", "coupon_id", "BIGINT DEFAULT NULL");
        addColumnIfNotExists("boarding_order", "coupon_amount", "DECIMAL(10,2) DEFAULT 0");
        addColumnIfNotExists("boarding_order", "final_price", "DECIMAL(10,2) DEFAULT NULL");
    }

    private void createPetHealthRecordTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `pet_health_record` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`pet_id` BIGINT NOT NULL," +
                "`pet_type` VARCHAR(20) DEFAULT 'dog'," +
                "`age` INT DEFAULT NULL," +
                "`weight` DECIMAL(5,2) DEFAULT NULL," +
                "`temperature` DECIMAL(4,1) DEFAULT NULL," +
                "`heart_rate` INT DEFAULT NULL," +
                "`respiration_rate` INT DEFAULT NULL," +
                "`appetite` INT DEFAULT 2," +
                "`mental_status` INT DEFAULT 2," +
                "`hair_condition` INT DEFAULT 2," +
                "`feces_status` INT DEFAULT 2," +
                "`vaccine_status` INT DEFAULT 0," +
                "`deworming_status` INT DEFAULT 0," +
                "`health_score` INT DEFAULT NULL," +
                "`risk_level` INT DEFAULT 0," +
                "`ai_advice` TEXT DEFAULT NULL," +
                "`remark` VARCHAR(500) DEFAULT NULL," +
                "`inspection_date` DATE DEFAULT NULL," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "`pet_name` VARCHAR(50) DEFAULT NULL," +
                "`owner_name` VARCHAR(50) DEFAULT NULL," +
                "PRIMARY KEY (`id`)," +
                "KEY `idx_pet_id` (`pet_id`)," +
                "KEY `idx_inspection_date` (`inspection_date`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.err.println("创建pet_health_record表失败: " + e.getMessage());
        }
    }

    private void createHealthRuleTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `health_rule` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`pet_type` VARCHAR(20) NOT NULL," +
                "`indicator_name` VARCHAR(50) NOT NULL," +
                "`min_value` DECIMAL(10,2) DEFAULT NULL," +
                "`max_value` DECIMAL(10,2) DEFAULT NULL," +
                "`warning_text` VARCHAR(200) DEFAULT NULL," +
                "`suggestion` VARCHAR(500) DEFAULT NULL," +
                "`risk_level` INT DEFAULT 1," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.err.println("创建health_rule表失败: " + e.getMessage());
        }
    }

    private void insertDefaultHealthRules() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM health_rule", Integer.class);
            if (count != null && count == 0) {
                jdbcTemplate.execute("INSERT INTO health_rule (pet_type, indicator_name, min_value, max_value, warning_text, suggestion, risk_level) VALUES ('dog', 'temperature', 38.30, 39.20, '体温异常', '建议观察是否发热或感染，增加饮水，若持续异常建议就医', 2)");
                jdbcTemplate.execute("INSERT INTO health_rule (pet_type, indicator_name, min_value, max_value, warning_text, suggestion, risk_level) VALUES ('dog', 'heartRate', 60.00, 140.00, '心率异常', '可能焦虑、疼痛或疾病，建议观察并就医', 2)");
                jdbcTemplate.execute("INSERT INTO health_rule (pet_type, indicator_name, min_value, max_value, warning_text, suggestion, risk_level) VALUES ('dog', 'respirationRate', 10.00, 30.00, '呼吸频率异常', '可能呼吸道问题，建议就医检查', 2)");
                jdbcTemplate.execute("INSERT INTO health_rule (pet_type, indicator_name, min_value, max_value, warning_text, suggestion, risk_level) VALUES ('cat', 'temperature', 38.10, 39.20, '体温异常', '建议观察是否发热，若持续异常建议就医', 2)");
                jdbcTemplate.execute("INSERT INTO health_rule (pet_type, indicator_name, min_value, max_value, warning_text, suggestion, risk_level) VALUES ('cat', 'heartRate', 120.00, 180.00, '心率异常', '可能焦虑或疾病，建议观察并就医', 2)");
                jdbcTemplate.execute("INSERT INTO health_rule (pet_type, indicator_name, min_value, max_value, warning_text, suggestion, risk_level) VALUES ('cat', 'respirationRate', 20.00, 40.00, '呼吸频率异常', '可能呼吸道问题，建议就医检查', 2)");
                System.out.println("已插入默认健康规则数据");
            }
        } catch (Exception e) {
            System.err.println("插入默认健康规则失败: " + e.getMessage());
        }
    }

    private void createDepartmentTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `department` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR(50) NOT NULL," +
                "`description` VARCHAR(200)," +
                "`icon` VARCHAR(50)," +
                "`status` INT DEFAULT 1," +
                "`sort` INT DEFAULT 0," +
                "`price` DECIMAL(10,2) DEFAULT 99.00," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_name` (`name`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try {
            jdbcTemplate.execute(sql);
            cleanupDuplicateDepartments();
            addUniqueConstraintIfNotExists("department", "name");
        } catch (Exception e) {
            System.err.println("创建department表失败: " + e.getMessage());
        }
    }

    private void createDoctorTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `doctor` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`name` VARCHAR(50) NOT NULL," +
                "`avatar` VARCHAR(255)," +
                "`title` VARCHAR(50)," +
                "`department_id` BIGINT," +
                "`specialty` VARCHAR(200)," +
                "`experience_year` INT DEFAULT 0," +
                "`introduction` TEXT," +
                "`status` INT DEFAULT 1," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_name` (`name`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try {
            jdbcTemplate.execute(sql);
            cleanupDuplicateDoctors();
            addUniqueConstraintIfNotExists("doctor", "name");
        } catch (Exception e) {
            System.err.println("创建doctor表失败: " + e.getMessage());
        }
    }

    private void cleanupDuplicateDepartments() {
        try {
            Integer dupCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM department WHERE name IN (SELECT name FROM department GROUP BY name HAVING COUNT(*) > 1)",
                    Integer.class);
            if (dupCount != null && dupCount > 0) {
                jdbcTemplate.execute(
                        "DELETE FROM department WHERE id NOT IN (SELECT min_id FROM (SELECT MIN(id) AS min_id FROM department GROUP BY name) t)");
                System.out.println("已清理department表重复数据，删除了" + dupCount + "条重复记录");
            }
        } catch (Exception e) {
            System.err.println("清理department重复数据失败: " + e.getMessage());
        }
    }

    private void cleanupDuplicateDoctors() {
        try {
            Integer dupCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM doctor WHERE name IN (SELECT name FROM doctor GROUP BY name HAVING COUNT(*) > 1)",
                    Integer.class);
            if (dupCount != null && dupCount > 0) {
                jdbcTemplate.execute(
                        "DELETE FROM doctor WHERE id NOT IN (SELECT min_id FROM (SELECT MIN(id) AS min_id FROM doctor GROUP BY name) t)");
                System.out.println("已清理doctor表重复数据，删除了" + dupCount + "条重复记录");
            }
        } catch (Exception e) {
            System.err.println("清理doctor重复数据失败: " + e.getMessage());
        }
    }

    private void addUniqueConstraintIfNotExists(String table, String column) {
        try {
            var constraints = jdbcTemplate.queryForList(
                    "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = 'uk_name'",
                    String.class, table);
            if (constraints.isEmpty()) {
                jdbcTemplate.execute("ALTER TABLE `" + table + "` ADD UNIQUE KEY `uk_name` (`" + column + "`)");
                System.out.println("已为" + table + "表添加uk_name唯一约束");
            }
        } catch (Exception e) {
            System.err.println("为" + table + "表添加唯一约束失败: " + e.getMessage());
        }
    }

    private void createMedicalOrderTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `medical_order` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`order_no` VARCHAR(32) NOT NULL," +
                "`user_id` BIGINT NOT NULL," +
                "`pet_id` BIGINT NOT NULL," +
                "`doctor_id` BIGINT," +
                "`department_id` BIGINT," +
                "`appointment_time` DATETIME," +
                "`symptom` TEXT," +
                "`price` DECIMAL(10,2)," +
                "`status` INT DEFAULT 0," +
                "`remark` TEXT," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "`pet_name` VARCHAR(50)," +
                "`owner_name` VARCHAR(50)," +
                "`doctor_name` VARCHAR(50)," +
                "`department_name` VARCHAR(50)," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_order_no` (`order_no`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.err.println("创建medical_order表失败: " + e.getMessage());
        }
    }

    private void createMedicalRecordTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `medical_record` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`medical_order_id` BIGINT," +
                "`pet_id` BIGINT," +
                "`doctor_name` VARCHAR(50)," +
                "`chief_complaint` TEXT," +
                "`physical_exam` TEXT," +
                "`diagnosis` TEXT," +
                "`medical_advice` TEXT," +
                "`medication` TEXT," +
                "`remark` TEXT," +
                "`visit_time` DATETIME," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.err.println("创建medical_record表失败: " + e.getMessage());
        }
    }

    private void recreateVaccineRecordTable() {
        try {
            var columns = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'vaccine_record' AND COLUMN_NAME = 'next_due_date'",
                    String.class);
            if (columns.isEmpty()) {
                jdbcTemplate.execute("DROP TABLE IF EXISTS `vaccine_record`");
                System.out.println("已删除旧版vaccine_record表，将重建新表");
            }
        } catch (Exception e) {
            System.err.println("检查vaccine_record表结构失败: " + e.getMessage());
        }
        String sql = "CREATE TABLE IF NOT EXISTS `vaccine_record` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`pet_id` BIGINT NOT NULL," +
                "`vaccine_name` VARCHAR(100)," +
                "`vaccination_date` DATE," +
                "`next_due_date` DATE," +
                "`status` INT DEFAULT 0," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.err.println("创建vaccine_record表失败: " + e.getMessage());
        }
    }

    private void recreateDewormingRecordTable() {
        try {
            var columns = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'deworming_record' AND COLUMN_NAME = 'next_due_date'",
                    String.class);
            if (columns.isEmpty()) {
                jdbcTemplate.execute("DROP TABLE IF EXISTS `deworming_record`");
                System.out.println("已删除旧版deworming_record表，将重建新表");
            }
        } catch (Exception e) {
            System.err.println("检查deworming_record表结构失败: " + e.getMessage());
        }
        String sql = "CREATE TABLE IF NOT EXISTS `deworming_record` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`pet_id` BIGINT NOT NULL," +
                "`deworming_name` VARCHAR(100)," +
                "`deworming_date` DATE," +
                "`next_due_date` DATE," +
                "`status` INT DEFAULT 0," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            System.err.println("创建deworming_record表失败: " + e.getMessage());
        }
    }

    private void insertDefaultDepartments() {
        try {
            jdbcTemplate.execute("INSERT IGNORE INTO `department` (`name`, `price`, `icon`, `sort`) VALUES ('普通门诊', 99.00, '🏥', 1)");
            jdbcTemplate.execute("INSERT IGNORE INTO `department` (`name`, `price`, `icon`, `sort`) VALUES ('皮肤科', 199.00, '🧴', 2)");
            jdbcTemplate.execute("INSERT IGNORE INTO `department` (`name`, `price`, `icon`, `sort`) VALUES ('肠胃科', 199.00, '🫁', 3)");
            jdbcTemplate.execute("INSERT IGNORE INTO `department` (`name`, `price`, `icon`, `sort`) VALUES ('疫苗接种', 69.00, '💉', 4)");
            jdbcTemplate.execute("INSERT IGNORE INTO `department` (`name`, `price`, `icon`, `sort`) VALUES ('驱虫护理', 59.00, '🐛', 5)");
            jdbcTemplate.execute("INSERT IGNORE INTO `department` (`name`, `price`, `icon`, `sort`) VALUES ('外科', 299.00, '🔪', 6)");
            jdbcTemplate.execute("INSERT IGNORE INTO `department` (`name`, `price`, `icon`, `sort`) VALUES ('健康检查', 129.00, '📋', 7)");
        } catch (Exception e) {
            System.err.println("插入默认科室数据失败: " + e.getMessage());
        }
    }

    private void insertDefaultDoctors() {
        try {
            jdbcTemplate.execute("INSERT IGNORE INTO `doctor` (`name`, `title`, `department_id`, `specialty`, `experience_year`) VALUES ('李明华', '主任兽医师', 1, '犬猫常见病/内科', 15)");
            jdbcTemplate.execute("INSERT IGNORE INTO `doctor` (`name`, `title`, `department_id`, `specialty`, `experience_year`) VALUES ('王芳', '副主任兽医师', 2, '犬猫皮肤病/过敏', 10)");
            jdbcTemplate.execute("INSERT IGNORE INTO `doctor` (`name`, `title`, `department_id`, `specialty`, `experience_year`) VALUES ('张伟', '主治兽医师', 6, '软组织外科/骨科', 8)");
            jdbcTemplate.execute("INSERT IGNORE INTO `doctor` (`name`, `title`, `department_id`, `specialty`, `experience_year`) VALUES ('陈静', '主治兽医师', 4, '疫苗规划/免疫程序', 6)");
            jdbcTemplate.execute("INSERT IGNORE INTO `doctor` (`name`, `title`, `department_id`, `specialty`, `experience_year`) VALUES ('刘建国', '副主任兽医师', 3, '犬猫肠胃病/消化系统', 12)");
            jdbcTemplate.execute("INSERT IGNORE INTO `doctor` (`name`, `title`, `department_id`, `specialty`, `experience_year`) VALUES ('赵敏', '主治兽医师', 5, '体内外驱虫/寄生虫防治', 7)");
            jdbcTemplate.execute("INSERT IGNORE INTO `doctor` (`name`, `title`, `department_id`, `specialty`, `experience_year`) VALUES ('孙婷婷', '主任兽医师', 7, '全面健康体检/预防医学', 14)");
        } catch (Exception e) {
            System.err.println("插入默认医生数据失败: " + e.getMessage());
        }
    }

    private void createWalletTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `wallet` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`user_id` BIGINT NOT NULL," +
                "`balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                "`total_recharge` DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                "`total_consume` DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                "`status` INT NOT NULL DEFAULT 1," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uk_user_id` (`user_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try {
            jdbcTemplate.execute(sql);
            System.out.println("wallet表已就绪");
        } catch (Exception e) {
            System.err.println("创建wallet表失败: " + e.getMessage());
        }
    }

    private void createWalletRecordTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `wallet_record` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT," +
                "`user_id` BIGINT NOT NULL," +
                "`type` INT NOT NULL," +
                "`amount` DECIMAL(12,2) NOT NULL," +
                "`balance_before` DECIMAL(12,2) NOT NULL," +
                "`balance_after` DECIMAL(12,2) NOT NULL," +
                "`business_type` VARCHAR(20) DEFAULT NULL," +
                "`business_id` BIGINT DEFAULT NULL," +
                "`transaction_no` VARCHAR(32) DEFAULT NULL," +
                "`remark` VARCHAR(200) DEFAULT NULL," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)," +
                "INDEX `idx_user_id` (`user_id`)," +
                "INDEX `idx_transaction_no` (`transaction_no`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try {
            jdbcTemplate.execute(sql);
            System.out.println("wallet_record表已就绪");
        } catch (Exception e) {
            System.err.println("创建wallet_record表失败: " + e.getMessage());
        }
    }

    private void addOrderPaymentColumns() {
        addColumnIfNotExists("pet_order", "payment_type", "INT DEFAULT NULL");
        addColumnIfNotExists("pet_order", "payment_time", "DATETIME DEFAULT NULL");
        addColumnIfNotExists("pet_order", "transaction_no", "VARCHAR(32) DEFAULT NULL");
        addColumnIfNotExists("boarding_order", "payment_type", "INT DEFAULT NULL");
        addColumnIfNotExists("boarding_order", "payment_time", "DATETIME DEFAULT NULL");
        addColumnIfNotExists("boarding_order", "transaction_no", "VARCHAR(32) DEFAULT NULL");
        addColumnIfNotExists("medical_order", "payment_type", "INT DEFAULT NULL");
        addColumnIfNotExists("medical_order", "payment_time", "DATETIME DEFAULT NULL");
        addColumnIfNotExists("medical_order", "transaction_no", "VARCHAR(32) DEFAULT NULL");
    }

    private void addPetColumns() {
        addColumnIfNotExists("pet", "avatar", "VARCHAR(255) DEFAULT NULL");
        addColumnIfNotExists("pet", "pet_type", "VARCHAR(20) DEFAULT 'dog'");
        addColumnIfNotExists("pet", "gender", "INT DEFAULT 1");
        addColumnIfNotExists("pet", "birthday", "DATE DEFAULT NULL");
        addColumnIfNotExists("pet", "color", "VARCHAR(50) DEFAULT NULL");
        addColumnIfNotExists("pet", "microchip_no", "VARCHAR(50) DEFAULT NULL");
        addColumnIfNotExists("pet", "sterilized", "INT DEFAULT 0");
        addColumnIfNotExists("pet", "blood_type", "VARCHAR(20) DEFAULT NULL");
        addColumnIfNotExists("pet", "allergy_info", "VARCHAR(500) DEFAULT NULL");
        addColumnIfNotExists("pet", "remark", "VARCHAR(500) DEFAULT NULL");
        addColumnIfNotExists("pet", "status", "INT DEFAULT 0");
        addColumnIfNotExists("pet", "create_time", "DATETIME DEFAULT CURRENT_TIMESTAMP");
        addColumnIfNotExists("pet", "update_time", "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        System.out.println("pet表字段升级完成");
    }
}
