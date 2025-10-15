/*------------------------------------------------------------------------------
 * NAME : PostCounselorListRequestDto.java
 * DESC : 상담원 목록 가져오기 호출 시 대상 전달 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/02/03  최상원                       초기작성
 * 2025/02/14  최상원                       대상 상담사 제거하고 테넌트ID와 캠페인ID추가
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostCounselorListRequestDto {

  private String tenantId;    //선택 테넌트ID("0"이면 센터에서 상담원 상태 모니터 호출 시이며 센터 내 모든 캠페인에 할당된 모든 상담원이 대상)
  private String campaignId;  //선택 캠페인ID("0"이면 테넌트에서 상담원 상태 모니터 호출 시이며 테넌트 내 모든 캠페인에 할당된 모든 상담원이 대상)
  private String sessionKey;
  private String[] agentIds; 
  
}
