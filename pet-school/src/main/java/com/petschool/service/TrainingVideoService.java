package com.petschool.service;

import com.petschool.entity.TrainingVideo;
import java.util.List;

public interface TrainingVideoService {
    int add(TrainingVideo video);
    List<TrainingVideo> getByRecordId(Long recordId);
}
