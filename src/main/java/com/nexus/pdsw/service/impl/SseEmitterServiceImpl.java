/*------------------------------------------------------------------------------
 * NAME : SseEmitterServiceImpl.java
 * DESC : SseEmitter 관련 기능 구현체
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/02/04  최상원                       초기작성
 * 2025/04/08  최상원                       클라이언트로 SSE 메시지 전송 수정
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.service.impl;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.nexus.pdsw.dto.object.NotificationDto;
import com.nexus.pdsw.repository.SseEmitterRepository;
import com.nexus.pdsw.service.SseEmitterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class SseEmitterServiceImpl implements SseEmitterService {
  
  private final SseEmitterRepository sseEmitterRepository;

  @Value("${sse.timeout}")
  private Long timeout;

  private static final Long TIMEOUT = 0L;

  /*
   *  SSE Emitter 생성
   *  
   *  @param String emitterKey   Emitter 키
   *  @return SseEmitter
   */
  @Override
  public SseEmitter createEmitter(String emitterKey) {
    // return sseEmitterRepository.save(emitterKey, new SseEmitter(timeout));
    // 기존 emitter 있으면 제거
    sseEmitterRepository.deleteById(emitterKey);

    SseEmitter emitter = new SseEmitter(TIMEOUT); // 무한타임아웃 설정

    emitter.onCompletion(() -> {
        log.info("SSE completed: {} {}", emitterKey, TIMEOUT);
        sseEmitterRepository.deleteById(emitterKey);
    });

    // emitter.onTimeout(() -> {
    //     log.info("SSE timeout: {} {}", emitterKey, timeout);
    //     sseEmitterRepository.deleteById(emitterKey);
    // });

    emitter.onError((ex) -> {
        log.warn("SSE error: {} - {}", emitterKey, ex.getMessage());
        sseEmitterRepository.deleteById(emitterKey);
    });

    return sseEmitterRepository.save(emitterKey, emitter);
  }

  // 클라이언트로 Heartbeat 메시지 전송
  @Scheduled(fixedRate = 30000)
  public void sendHeartbeat() {
    sseEmitterRepository.findAll().forEach((key, emitter) -> {
        try {
            emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
        } catch (Exception e) {
            log.warn("Heartbeat failed: {}", key);
            sseEmitterRepository.deleteById(key);
        }
    });
  }

  /*
   *  SSE Emitter 삭제
   *  
   *  @param String emitterKey   Emitter 키
   *  @return SseEmitter
   */
  @Override
  public void deleteEmitter(String emitterKey) {
    sseEmitterRepository.deleteById(emitterKey);
  }

  /*
   *  클라이언트로 알림 메시지 전송
   *  
   *  @param String emitterKey   Emitter 키
   *  @param NotificationDto notificationDto   알림 DTO객체
   *  @return void
   */
  @Override
  public void sendNotificationToClient(String channelKey, NotificationDto notificationDto) {
    Map<String, SseEmitter> emitters = sseEmitterRepository.findAll();
    String[] arrEmitterKey = channelKey.split(":");
    emitters.forEach((counselorId, emitter) -> {
      String[] arrCounselorId = counselorId.split("_");
      if (arrEmitterKey[2].equals(arrCounselorId[1])) {
        send(notificationDto, counselorId, emitter);
      }
    });
    // sseEmitterRepository.findById(emitterKey).ifPresent(emitter -> send(notificationDto, emitterKey, emitter));
  }

  /*
   *  알림 메시지 전송
   *  
   *  @param Object data            Emitter 키
   *  @param String emitterKey      Emitter 키 이유
   *  @param SseEmitter sseEmitter  SSE Emitter 개체
   *  @return void
   */
  @Override
  public void send(Object data, String emitterKey, SseEmitter sseEmitter) {

    try {
      log.info("send to client {}:[{}]", emitterKey, data);
      // log.info("emitterValue {}", sseEmitter);
      sseEmitter.send(SseEmitter.event()
        .name("message")
        .id(emitterKey)
        .data(data, MediaType.APPLICATION_JSON)
        .reconnectTime(1000L)
      );
    } catch (IOException | IllegalStateException e) {
      log.info("IOException | IllegalStateException is occurred. ", e);
      sseEmitterRepository.deleteById(emitterKey);
    }
  }

}
