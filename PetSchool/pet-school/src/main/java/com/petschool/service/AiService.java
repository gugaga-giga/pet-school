package com.petschool.service;

import java.util.Map;

public interface AiService {
    Map<String, Object> healthWarning(Long petId);
    Map<String, Object> courseRecommend(Long petId);
    Map<String, Object> vaccineReminder(Long petId);
}
