/*------------------------------------------------------------------------------
 * NAME : PostSkillAssignedCounselorListRequestDto.java
 * DESC : 스킬 할당된 상담사 가져오기 호출 시 대상 전달 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/02/20  최상원                       초기작성
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostSkillAssignedCounselorListRequestDto {

  private String centerId;    //센터ID
  private String tenantId;    //테넌트ID
  private Integer skillId;    //스킬ID
  private String sessionKey;
  
}
