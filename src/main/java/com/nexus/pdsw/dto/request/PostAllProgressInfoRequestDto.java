/*------------------------------------------------------------------------------
 * NAME : PostAllProgressInfoRequestDto.java
 * DESC : 전체 진행상태 요청 시 전달 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/09/16  이승용                       초기작성
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostAllProgressInfoRequestDto {

    private String sessionKey;

    private List<PostAllProgressInfoDto> campaignList;
}