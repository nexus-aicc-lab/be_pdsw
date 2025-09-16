/*------------------------------------------------------------------------------
 * NAME : RedisMonitorService.java
 * DESC : Redis 9번방에서 모니터링 정보 가져오기
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/01/31  최상원                       초기작성
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.service;

import org.springframework.http.ResponseEntity;

import com.nexus.pdsw.dto.request.PostAllProgressInfoRequestDto;
import com.nexus.pdsw.dto.request.PostDialerChannelStatusInfoRequestDto;
import com.nexus.pdsw.dto.request.PostSendingProgressStatusRequestDto;
import com.nexus.pdsw.dto.response.monitor.PostDialerChannelStatusInfoResponseDto;
import com.nexus.pdsw.dto.response.monitor.GetProcessStatusInfoResponseDto;
import com.nexus.pdsw.dto.response.monitor.GetProgressInfoResponseDto;
import com.nexus.pdsw.dto.response.monitor.GetAllProgressInfoResponseDto;
import com.nexus.pdsw.dto.response.monitor.GetSendingProgressStatusResponseDto;

public interface RedisMonitorService {
  
  /*
   *  타 시스템 프로세스 상태정보 가져오기
   *  
   *  @return ResponseEntity<? super GetProcessStatusInfoResponseDto>
   */
  ResponseEntity<? super GetProcessStatusInfoResponseDto> getProcessStatusInfo();

  /*
   *  Dialer 채널 상태 정보 가져오기
   *  
   *  @param PostDialerChannelStatusInfoRequestDto requestDto     Dialer 장비ID's
   *  @return ResponseEntity<? super PostDialerChannelStatusInfoResponseDto>
   */
  ResponseEntity<? super PostDialerChannelStatusInfoResponseDto> getDialerChannelStatusInfo(PostDialerChannelStatusInfoRequestDto requestDto);

  /*
   *  캠페인별 진행정보 가져오기
   *  
   *  @param tenantId           테넌트ID
   *  @param campaignId         캠페인ID
   *  @return ResponseEntity<? super GetProgressInfoResponseDto>
   */
  ResponseEntity<? super GetProgressInfoResponseDto> getProgressInfo(String tenantId, String campaignId);
  
  /*
   *  발신진행상태 가져오기
   *  
   *  @param PostSendingProgressStatusRequestDto requestDto
   *  @return ResponseEntity<? super GetSendingProgressStatusResponseDto>
   */
  ResponseEntity<? super GetSendingProgressStatusResponseDto> getSendingProgressStatus(PostSendingProgressStatusRequestDto requestDto);
  /*
   *  캠페인별 진행정보 가져오기
   *  
   *  @param tenantId           테넌트ID
   *  @param campaignId         캠페인ID
   *  @return ResponseEntity<? super GetAllProgressInfoResponseDto>
   */
  ResponseEntity<? super GetAllProgressInfoResponseDto> getAllProgressInfo(PostAllProgressInfoRequestDto requestDto);
  
}
