package com.petschool.ai.service.impl;

import com.petschool.ai.service.IntentClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 意图分类器实现：基于关键词规则 + LLM分类双保险
 */
@Service
public class IntentClassifierImpl implements IntentClassifier {

    private static final Logger log = LoggerFactory.getLogger(IntentClassifierImpl.class);

    /** 关键词到意图的映射规则 */
    private static final Map<String, List<String>> KEYWORD_RULES = new LinkedHashMap<>();

    static {
        KEYWORD_RULES.put("Greeting", List.of("你好", "嗨", "hello", "hi", "早上好", "下午好", "晚上好", "在吗", "在不在"));
        KEYWORD_RULES.put("FAQ", List.of("营业时间", "地址", "怎么去", "电话", "联系方式", "价格", "收费", "多少钱", "几点", "开门", "关门", "预约", "退款", "退课", "退费"));
        KEYWORD_RULES.put("Course", List.of("课程", "训练班", "培训", "上课", "报名", "课程推荐", "有什么课", "训练课", "行为纠正", "选课"));
        KEYWORD_RULES.put("PetKnowledge", List.of("怎么养", "怎么训练", "喂养", "饲养", "疫苗", "驱虫", "绝育", "洗澡", "换毛", "发情", "配种", "狗粮", "猫粮", "零食", "补钙", "金毛", "拉布拉多", "柯基", "泰迪", "品种"));
        KEYWORD_RULES.put("Order", List.of("订单", "支付", "退款", "购买记录", "消费", "账单", "已购", "订单状态", "我的订单"));
        KEYWORD_RULES.put("PetProfile", List.of("我的宠物", "宠物信息", "宠物档案", "修改宠物", "添加宠物", "宠物列表", "宠物详情"));
        KEYWORD_RULES.put("UserInfo", List.of("我的信息", "个人信息", "修改密码", "账号", "头像", "个人中心", "手机号"));
        KEYWORD_RULES.put("Membership", List.of("会员", "套餐", "VIP", "优惠", "折扣", "优惠券", "活动", "会员等级", "充值", "钱包"));
        KEYWORD_RULES.put("Service", List.of("寄宿", "美容", "医疗", "看病", "挂号", "寄养", "体检", "健康检查"));
    }

    @Override
    public String classify(String query) {
        if (query == null || query.isBlank()) {
            return "Other";
        }

        String trimmedQuery = query.trim().toLowerCase();

        // 第一层：关键词规则匹配
        String ruleResult = classifyByKeywords(trimmedQuery);
        if (ruleResult != null) {
            log.debug("关键词规则匹配意图: query={}, intent={}", query, ruleResult);
            return ruleResult;
        }

        // 第二层：如果关键词未匹配，返回Other（LLM分类在AiChatServiceImpl中按需调用）
        log.debug("关键词未匹配，返回Other: query={}", query);
        return "Other";
    }

    /**
     * 基于关键词规则分类
     */
    private String classifyByKeywords(String query) {
        for (Map.Entry<String, List<String>> entry : KEYWORD_RULES.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (query.contains(keyword.toLowerCase())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
}
