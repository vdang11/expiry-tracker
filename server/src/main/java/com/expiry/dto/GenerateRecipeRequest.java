package com.expiry.dto;

import lombok.Data;

import java.util.List;

@Data
public class GenerateRecipeRequest {

    private List<Long> excludeRecipeIds;
}