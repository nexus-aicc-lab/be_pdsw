/*------------------------------------------------------------------------------
 * NAME : PostSendingProgressStatusRequestDto.java
 * DESC : 발신진행상태 요청 시 전달 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/03/14  최상원                       초기작성
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostSendingProgressStatusRequestDto {
  
  private String centerId;    // center ID
  private String tenantId;    //선택 테넌트ID("0"이면 전체 캠페인)
  private String campaignId;  //선택 캠페인ID("0"이면 전체 캠페인)
  private String[] agentIds;  //선택 캠페인ID("0"이면 전체 캠페인)
  private String sessionKey;
}
