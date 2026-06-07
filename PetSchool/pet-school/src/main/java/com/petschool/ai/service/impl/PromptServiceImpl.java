package com.petschool.ai.service.impl;

import com.petschool.ai.context.CourseContext;
import com.petschool.ai.context.PetContext;
import com.petschool.ai.context.UserContext;
import com.petschool.ai.entity.MemoryProfile;
import com.petschool.ai.service.PromptService;
import com.petschool.entity.Course;
import com.petschool.mapper.CourseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Prompt模板管理和构建服务实现
 */
@Service
public class PromptServiceImpl implements PromptService {

    private final CourseMapper courseMapper;

    public PromptServiceImpl(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    @Override
    public String buildSystemPrompt(UserContext userContext, List<PetContext> petContexts,
                                     List<CourseContext> courseContexts, List<MemoryProfile> memories) {
        StringBuilder sb = new StringBuilder();

        // 核心身份与业务知识
        sb.append("你是PetSchool宠物学校的智能客服助手。请严格基于以下业务信息回答用户问题，不要编造不存在的课程、价格或服务。\n\n");

        sb.append("=== PetSchool业务知识 ===\n\n");

        // 动态查询课程目录
        sb.append("【课程列表】\n");
        try {
            List<Course> courses = courseMapper.selectAll();
            int idx = 1;
            for (Course course : courses) {
                sb.append(idx++).append(". ").append(course.getName());
                if (course.getMonthlyPrice() != null) sb.append(" - ¥").append(course.getMonthlyPrice()).append("/月");
                if (course.getDuration() != null) sb.append(" - ").append(course.getDuration()).append("个月");
                if (course.getDescription() != null) sb.append(" - ").append(course.getDescription());
                sb.append("\n");
            }
        } catch (Exception e) {
            // 查询失败时使用静态数据
            sb.append("1. 日常行为管理班 - ¥2,999/月 - 1个月\n");
            sb.append("2. 社会化训练班 - ¥4,999/月 - 2个月\n");
            sb.append("3. 定点大小便训练班 - ¥1,999/月 - 1个月\n");
            sb.append("4. 基础服从训练班 - ¥2,599/月 - 1个月\n");
            sb.append("5. 表演类训练班 - ¥5,999/月 - 2个月\n");
        }
        sb.append("\n");

        sb.append("【退款政策】\n");
        sb.append("- 未开课可全额退款\n");
        sb.append("- 开课后7天内退剩余课时费用的80%\n");
        sb.append("- 因学校原因取消课程全额退款\n");
        sb.append("- 退款3-5个工作日原路返回\n\n");

        sb.append("【购买流程】\n");
        sb.append("登录 → 浏览课程 → 选择套餐 → 确认订单 → 支付（微信/支付宝/钱包余额）→ 开始上课\n\n");

        sb.append("【营业时间】每天9:00-21:00，周末及节假日正常营业\n\n");

        sb.append("【服务项目】训练课程、寄宿服务、医疗服务、健康管理、美容服务、在线商城\n\n");

        sb.append("=== 用户信息 ===\n\n");

        // 用户信息
        if (userContext != null) {
            sb.append("当前用户：").append(userContext.getUsername() != null ? userContext.getUsername() : "未知")
              .append("（").append(userContext.getRoleName() != null ? userContext.getRoleName() : "未知").append("）");
            if (userContext.getPhone() != null && !userContext.getPhone().isEmpty()) {
                sb.append("，手机：").append(userContext.getPhone());
            }
            sb.append("\n\n");
        }

        // 宠物信息
        if (petContexts != null && !petContexts.isEmpty()) {
            sb.append("用户宠物：\n");
            for (PetContext pet : petContexts) {
                sb.append("- ").append(pet.getPetName() != null ? pet.getPetName() : "未命名");
                if (pet.getBreed() != null) sb.append("（").append(pet.getBreed()).append("）");
                if (pet.getAge() != null) sb.append("，").append(pet.getAge()).append("岁");
                if (pet.getWeight() != null) sb.append("，").append(pet.getWeight()).append("kg");
                if (pet.getSterilized() != null) sb.append("，").append(pet.getSterilizedDesc());
                if (pet.getVaccineStatus() != null) sb.append("，").append(pet.getVaccineStatus());
                if (pet.getHealthStatus() != null && !"暂无记录".equals(pet.getHealthStatus())) {
                    sb.append("，健康: ").append(pet.getHealthStatus());
                }
                if (pet.getAllergyInfo() != null && !pet.getAllergyInfo().isEmpty()) {
                    sb.append("，过敏: ").append(pet.getAllergyInfo());
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        // 课程信息
        if (courseContexts != null && !courseContexts.isEmpty()) {
            sb.append("用户已购课程：\n");
            for (CourseContext course : courseContexts) {
                sb.append("- ").append(course.getCourseName() != null ? course.getCourseName() : "未知课程");
                if (course.getStatus() != null) sb.append("（").append(course.getStatusDesc()).append("）");
                sb.append("\n");
            }
            sb.append("\n");
        }

        // 历史记忆
        if (memories != null && !memories.isEmpty()) {
            sb.append("历史记忆：\n");
            for (MemoryProfile memory : memories) {
                sb.append("- ").append(memory.getValue()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("重要规则：\n");
        sb.append("1. 请优先基于以上业务信息回答，不要编造不存在的课程或服务\n");
        sb.append("2. 如果用户问到的信息不在以上范围内，请诚实告知并建议联系客服\n");
        sb.append("3. 回答使用中文，保持专业友好的语气\n");

        return sb.toString();
    }

    @Override
    public String buildRAGPrompt(String query, String ragContext) {
        return "以下是从知识库中检索到的相关内容：\n\n" + ragContext +
                "\n\n请根据以上检索内容和用户问题进行回答。如果检索内容与问题不相关，请根据自身知识回答。\n\n用户问题：" + query;
    }

    @Override
    public String buildIntentPrompt(String query) {
        return "请判断以下用户问题的意图类型，只能从以下选项中选择一个：\n" +
                "- FAQ: 常见问题咨询（如营业时间、地址、价格等）\n" +
                "- Course: 课程相关（如课程推荐、课程详情、课程对比等）\n" +
                "- PetKnowledge: 宠物知识（如饲养、训练、健康知识等）\n" +
                "- Order: 订单相关（如订单查询、支付、退款等）\n" +
                "- PetProfile: 宠物档案（如宠物信息查询、修改等）\n" +
                "- UserInfo: 用户信息（如个人信息、账户等）\n" +
                "- Membership: 会员套餐相关（如会员等级、优惠、套餐、充值等）\n" +
                "- Service: 服务咨询（如寄宿、美容、医疗、体检等）\n" +
                "- Greeting: 问候闲聊\n" +
                "- Other: 其他\n\n" +
                "用户问题：" + query + "\n\n" +
                "请只回复意图类型的英文单词，不要回复其他内容。";
    }
}
