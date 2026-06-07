package com.petschool.ai.service.impl;

import com.petschool.ai.service.RouterService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 智能路由服务实现，根据意图选择数据源
 */
@Service
public class RouterServiceImpl implements RouterService {

    @Override
    public RouteResult route(String intent, String query) {
        return switch (intent) {
            case "FAQ" -> new RouteResult(
                    true, false,
                    List.of(1, 2), // 默认检索FAQ和通用知识库
                    null,
                    "FAQ问题，使用RAG检索知识库"
            );
            case "Course" -> new RouteResult(
                    true, true,
                    List.of(1), // 课程知识库
                    "course",
                    "课程相关，MySQL查询 + RAG检索"
            );
            case "PetKnowledge" -> new RouteResult(
                    true, false,
                    List.of(1, 2), // 宠物知识库
                    null,
                    "宠物知识，使用RAG检索知识库"
            );
            case "Order" -> new RouteResult(
                    false, true,
                    Collections.emptyList(),
                    "order",
                    "订单相关，MySQL查询"
            );
            case "PetProfile" -> new RouteResult(
                    false, true,
                    Collections.emptyList(),
                    "pet",
                    "宠物档案，MySQL查询"
            );
            case "UserInfo" -> new RouteResult(
                    false, true,
                    Collections.emptyList(),
                    "user",
                    "用户信息，MySQL查询"
            );
            case "Greeting" -> new RouteResult(
                    false, false,
                    Collections.emptyList(),
                    null,
                    "问候闲聊，无需额外数据"
            );
            case "Membership" -> new RouteResult(
                    true, true,
                    List.of(2), // FAQ知识库
                    "membership",
                    "会员/套餐，RAG检索 + 数据库查询"
            );
            case "Service" -> new RouteResult(
                    true, false,
                    List.of(1, 2), // 宠物知识库 + FAQ
                    null,
                    "服务咨询，RAG检索知识库"
            );
            default -> new RouteResult(
                    true, false,
                    List.of(1, 2), // 默认尝试RAG检索
                    null,
                    "未知意图，默认RAG检索"
            );
        };
    }
}
