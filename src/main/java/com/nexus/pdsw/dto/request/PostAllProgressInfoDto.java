package com.nexus.pdsw.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostAllProgressInfoDto {

    private String tenantId;   // 선택 테넌트ID ("0"이면 전체 캠페인)
    private String campaignId; // 선택 캠페인ID ("0"이면 전체 캠페인)
}