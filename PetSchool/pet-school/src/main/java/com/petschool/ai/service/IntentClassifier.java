package com.petschool.ai.service;

/**
 * 意图分类器接口
 */
public interface IntentClassifier {

    /**
     * 对用户查询进行意图分类
     * @param query 用户查询文本
     * @return 意图类型: FAQ, Course, PetKnowledge, Order, PetProfile, UserInfo, Greeting, Other
     */
    String classify(String query);
}
