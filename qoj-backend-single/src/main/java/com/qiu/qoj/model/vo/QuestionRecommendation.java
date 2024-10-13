package com.qiu.qoj.model.vo;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionRecommendation {
    private String recommendation;

    private List<QuestionVOForRecommend> questions;
}
