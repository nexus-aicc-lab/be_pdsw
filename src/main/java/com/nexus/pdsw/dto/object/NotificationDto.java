/*------------------------------------------------------------------------------
 * NAME : NotificationDto.java
 * DESC : 실시간 이벤트 전달 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/02/05  최상원                       초기작성
 * 2025/04/15  최상원                       campaign_id 추가
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.dto.object;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class NotificationDto {
  
  private String kind;
  private String command;
  private String announce;
  private Object data;
  private String campaign_id;
  private String skill_id;

  public NotificationDto(String kind, String command, String announce, Object data, String campaign_id, String skill_id) {
    this.kind = kind;
    this.command = command;
    this.announce = announce;
    this.data = data;
    this.campaign_id = campaign_id;
    this.skill_id = skill_id;
  }
}
