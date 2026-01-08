/*------------------------------------------------------------------------------
 * NAME : RedisSubscriber.java
 * DESC : 채널을 구독하는 Subscriber 객체
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
package com.nexus.pdsw.common.component;

import java.io.IOException;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.pdsw.dto.object.NotificationDto;
import com.nexus.pdsw.service.SseEmitterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisSubscriber implements MessageListener {
  
  private final ObjectMapper objectMapper;
  private final SseEmitterService sseEmitterService;
  
  @Override
  public void onMessage(Message message, @Nullable byte[] pattern) {
	  String channel = new String(message.getChannel());
	  String body = new String(message.getBody()); // 원본 바디 보관 (에러 시 추적용)

	  try {
		  // REDIS 수신 로그
		  log.info("REDIS [RECEIVE] Channel: {}, Payload: {}", channel, body);
		
		  // JSON 파싱
		  NotificationDto notificationDto = objectMapper.readValue(body, NotificationDto.class);
		  
		  // SSE 전송 시도
		  log.info("REDIS [SUCCESS] SSE로 수신메시지 전달 Channel: {}, data: {}", channel, notificationDto);
		  
		  sseEmitterService.sendNotificationToClient(channel, notificationDto);
		
	  } catch (IOException e) {
		  // 파싱 에러 (JSON 형식이 맞지 않을 때)
		  log.error("REDIS [ERROR] JSON Parsing Failed. Channel: {}, Body: {}, Error: {}", 
		            channel, body, e.getMessage());
	  } catch (Exception e) {
		  // 기타 서비스 로직 에러 (SSE 전송 실패 등)
		  log.error("REDIS [ERROR] Unexpected Exception. Channel: {}, Error: {}", 
		            channel, e.getMessage(), e);
	  } 
  }  
}
