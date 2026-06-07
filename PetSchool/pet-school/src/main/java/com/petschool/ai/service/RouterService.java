package com.petschool.ai.service;

import java.util.List;
import java.util.Map;

/**
 * 智能路由服务接口，根据意图选择数据源
 */
public interface RouterService {

    /**
     * 根据意图进行路由
     * @param intent 意图类型
     * @param query 用户查询
     * @return 路由结果，包含数据源和检索参数
     */
    RouteResult route(String intent, String query);

    /**
     * 路由结果
     */
    record RouteResult(
            boolean needRAG,          // 是否需要RAG检索
            boolean needMySQL,        // 是否需要MySQL查询
            List<Integer> kbIds,      // RAG检索的知识库ID列表
            String mysqlQueryType,    // MySQL查询类型: course/order/pet/user
            String description        // 路由描述
    ) {}
}
