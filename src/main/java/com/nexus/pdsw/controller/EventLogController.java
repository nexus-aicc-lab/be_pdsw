/*------------------------------------------------------------------------------
 * NAME : EventLogController.java
 * DESC : 이벤트 로그 기록
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/02/05  최상원                       초기작성
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.pdsw.dto.request.PostEventLogRequestDto;
import com.nexus.pdsw.dto.response.eventLog.PostEventLogResponseDto;
import com.nexus.pdsw.service.EventLogService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api_upds/v1/log")
@RequiredArgsConstructor
@RestController
public class EventLogController {

  private final EventLogService eventLogService;

  /*
   * 이벤트 로그 저장하기
   * 
   * @param PostEventLogRequestDto requestBody 이벤트 로그 전달 DTO   * 
   * @param String clientIp                    클라이언트IP
   * @return ResponseEntity<? super PostEventLogResponseDto>
   */
  @PostMapping("/save")
  public ResponseEntity<? super PostEventLogResponseDto> saveEventLog(
    @RequestBody PostEventLogRequestDto requestBody,
    HttpServletRequest request
  ) {
    String tenantId = String.valueOf(requestBody.getTenantId());
    System.setProperty("tenantId", tenantId);
    String clientIp = request.getHeader("X-Forwarded-For");
   if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("Proxy-Client-IP");
    }
    if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("WL-Proxy-Client-IP");
    }
    if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("HTTP_CLIENT_IP");
    }
    if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("X-Real-IP");
    }
    if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("X-RealIP");
    }
    if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("REMOTE_ADDR");
    }
    if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getRemoteAddr();
    }
    // logger.info("요청 처리됨 - 클라이언트 IP: {}, 처리내용: {}", clientIp, requestBody.getDescription());
    ResponseEntity<? super PostEventLogResponseDto> response = eventLogService.saveEventLog(requestBody, clientIp);
    return response;
  }
}
