/*------------------------------------------------------------------------------
 * NAME : RedisMonitorController.java
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
 * 2025/02/13  최상원                       발신진행상태 추가
 *------------------------------------------------------------------------------*/
// src\main\java\com\nexus\pdsw\controller\RedisMonitorController.java
package com.nexus.pdsw.controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.pdsw.dto.request.PostDialerChannelStatusInfoRequestDto;
import com.nexus.pdsw.dto.request.PostSendingProgressStatusRequestDto;
import com.nexus.pdsw.dto.request.PostAllProgressInfoRequestDto;
import com.nexus.pdsw.dto.response.monitor.PostDialerChannelStatusInfoResponseDto;
import com.nexus.pdsw.dto.response.monitor.GetProcessStatusInfoResponseDto;
import com.nexus.pdsw.dto.response.monitor.GetProgressInfoResponseDto;
import com.nexus.pdsw.dto.response.monitor.GetAllProgressInfoResponseDto;
import com.nexus.pdsw.dto.response.monitor.GetSendingProgressStatusResponseDto;
import com.nexus.pdsw.service.RedisMonitorService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api_upds/v1/monitor")
@RequiredArgsConstructor
public class RedisMonitorController {

  private final RedisMonitorService redisMonitorService;

  /*
   *  타 시스템 프로세스 상태정보 가져오기
   *  
   *  @return ResponseEntity<? super GetProcessStatusInfoResponseDto>
   */
  @GetMapping("/process")
  public ResponseEntity<? super GetProcessStatusInfoResponseDto> getProcessStatusInfo() {
    ResponseEntity<? super GetProcessStatusInfoResponseDto> response = redisMonitorService.getProcessStatusInfo();
    return response;
  }
  
  /*
   *  Dialer 채널 상태 정보 가져오기 
   *  
   *  @param PostDialerChannelStatusInfoRequestDto requestDto     Dialer 장비ID's
   *  @return ResponseEntity<? super PostDialerChannelStatusInfoResponseDto>
   */
  @PostMapping("/dialer/channel")
  public ResponseEntity<? super PostDialerChannelStatusInfoResponseDto> getDialerChannelStatusInfo(
    @RequestBody PostDialerChannelStatusInfoRequestDto requestDto
  ) {
    ResponseEntity<? super PostDialerChannelStatusInfoResponseDto> response = redisMonitorService.getDialerChannelStatusInfo(requestDto);
    return response;
  }

  /*
   *  캠페인별 진행정보 가져오기
   *  
   *  @param tenantId           테넌트ID
   *  @param campaignId         캠페인ID
   *  @return ResponseEntity<? super GetProgressInfoResponseDto>
   */
  @GetMapping("/tenant/{tenantId}/campaign/{campaignId}/statistics")
  public ResponseEntity<? super GetProgressInfoResponseDto> getProgressInfo(
    @PathVariable("tenantId") String tenantId,
    @PathVariable("campaignId") String campaignId
  ) {
    ResponseEntity<? super GetProgressInfoResponseDto> response = redisMonitorService.getProgressInfo(tenantId, campaignId);
    return response;
  }

  /*
   *  발신진행상태 가져오기
   *  
   *  @param PostSendingProgressStatusRequestDto requestDto
   *  @return ResponseEntity<? super GetSendingProgressStatusResponseDto>
   */
  @PostMapping("/tenant/campaign/dial")
  public ResponseEntity<? super GetSendingProgressStatusResponseDto> getSendingProgressStatus(
    @RequestBody PostSendingProgressStatusRequestDto requestDto
  ) {
    ResponseEntity<? super GetSendingProgressStatusResponseDto> response = redisMonitorService.getSendingProgressStatus(requestDto);
    return response;
  }

  /*
   *  전체 진행정보 가져오기
   *  
   *  @param tenantId           테넌트ID
   *  @param campaignId         캠페인ID
   *  @return ResponseEntity<? super GetAllProgressInfoResponseDto>
   */
  @PostMapping("/tenant/campaign/statistics")
  public ResponseEntity<? super GetAllProgressInfoResponseDto> getAllProgressInfo(
    @RequestBody PostAllProgressInfoRequestDto requestDto
  ) {
    ResponseEntity<? super GetAllProgressInfoResponseDto> response = redisMonitorService.getAllProgressInfo(requestDto);
    return response;
  }

}
