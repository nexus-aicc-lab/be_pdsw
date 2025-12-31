/*------------------------------------------------------------------------------
 * NAME : NotificationServiceImpl.java
 * DESC : 알림 이벤트 기능 구현체
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/02/04  최상원                       초기작성
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.nexus.pdsw.service.NotificationService;
import com.nexus.pdsw.service.RedisMessageService;
import com.nexus.pdsw.service.SseEmitterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final SseEmitterService sseEmitterService;
  private final RedisMessageService redisMessageService;

  /*
   *  알림 이벤트 구독
   *  
   *  @param String tenantId  테넌트ID
   *  @param String counselorId  상담원ID
   *  @return SseEmitter
   */
  @Override
  public SseEmitter subscribe(String tenantId, String counselorId) {
    String emitterKey = counselorId + "_" + tenantId;

    // 기존 Emitter 삭제 후 생성
    sseEmitterService.deleteEmitter(emitterKey);
    // emitter 생성 + 생명주기 관리는 SseEmitterService에서만
    SseEmitter sseEmitter = sseEmitterService.createEmitter(emitterKey);
    log.info("SSE Emitter created : {}", emitterKey);

    // 연결 성공 이벤트 (초기 handshake)
    try {
      sseEmitter.send(SseEmitter.event()
      .name("connect")
      .data("connected"));
    } catch (Exception e) {
      log.info("Failed to send connect event: {}", emitterKey);
    }

    // Redis 구독
    redisMessageService.subscribe(tenantId, counselorId);

    return sseEmitter;
  }

  /*
   *  알림 이벤트 전송
   *  
   *  @param PostRedisMessagePublishRequestDto requestBody   실시간 이벤트 발행 개체 DTO
   *  @return void
   */
  // @Override
  // public void publicNotification(PostRedisMessagePublishRequestDto requestBody) {
  //   redisMessageService.publish(requestBody.getTenantId(), requestBody.getNotification());
  // }
  
}
