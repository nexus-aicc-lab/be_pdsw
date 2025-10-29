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
import org.slf4j.MDC;

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
          HttpServletRequest request) {

    String tenantId = String.valueOf(requestBody.getTenantId());

      // MDC에 tenantId 등록, 반드시 finally에서 제거
      MDC.put("tenantId", tenantId);

      try {
          String clientIp = extractClientIp(request);

          log.info("이벤트 로그 요청 수신 - 테넌트: {}, 클라이언트IP: {}, 제목: {}, 내용: {}", tenantId, clientIp, requestBody.getActivation(), requestBody.getDescription());
          // 서비스 호출 및 로그 기록
          ResponseEntity<? super PostEventLogResponseDto> response =
                  eventLogService.saveEventLog(requestBody, clientIp);

          return response;
      } finally {
          // 요청 종료 후 MDC 정리
          MDC.remove("tenantId");
      }
  }

  /**
   * 클라이언트 IP 추출
   */
  private String extractClientIp(HttpServletRequest request) {
      String[] headers = {
              "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
              "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR", "X-Real-IP",
              "X-RealIP", "REMOTE_ADDR"
      };

      for (String header : headers) {
          String ip = request.getHeader(header);
          if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
              return ip;
          }
      }

      return request.getRemoteAddr();
  }
}
