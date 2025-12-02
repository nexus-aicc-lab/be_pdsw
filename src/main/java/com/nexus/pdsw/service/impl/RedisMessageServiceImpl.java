/*------------------------------------------------------------------------------
 * NAME : RedisMessageServiceImpl.java
 * DESC : Redis의 채널을 구독하고 이벤트를 발행하는 기능 구현체
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

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import com.nexus.pdsw.common.component.RedisSubscriber;
import com.nexus.pdsw.dto.object.NotificationDto;
import com.nexus.pdsw.dto.request.PostRedisMessagePublishRequestDto;
import com.nexus.pdsw.service.RedisMessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageServiceImpl implements RedisMessageService {
  
  private final RedisMessageListenerContainer container;
  private final RedisSubscriber subscriber;
  private final RedisTemplate<String, Object> redisTemplate;

  /*
   *  Redis 채널 구독
   *  
   *  @param String channel   채널정보
   *  @param String counselorId  상담원ID
   *  @return void
   */
  @Override
  public void subscribe(String channel, String counselorId) {
    container.addMessageListener(subscriber, ChannelTopic.of(getChannelName(channel)));
  }

  /*
   *  Redis 채널 배포
   *  
   *  @param String tenantId                   채널정보
   *  @param NotificationDto notificationDto  배포 메시지
   *  @return void
   */
  @Override
  public void publish(
    String tenantId,
    NotificationDto notificationDto
  ) {
    redisTemplate.convertAndSend(getChannelName(tenantId), notificationDto);
  }

  /*
   *  Redis 채널 배포
   *  
   *  @param PostRedisMessagePublishRequestDto requestBody  배포 메시지 전달 개체 DTO
   *  @return void
   */
  @Override
  public void publicNotification(PostRedisMessagePublishRequestDto requestBody) {
    redisTemplate.convertAndSend(getChannelName(requestBody.getTenantId()), requestBody.getNotification());
  }

  /*
   *  Redis 구독 채널 삭제
   *  
   *  @param String channel                   채널정보
   *  @return void
   */
  @Override
  public void removeSubscribe(String channel) {
    container.removeMessageListener(subscriber, ChannelTopic.of(getChannelName(channel)));
  }
  
  /*
   *  채널 명 가져오기
   *  
   *  @param String channelId     채널ID
   *  @return String channelName
   */
  private String getChannelName(String channelId) {
    return "pds:tenant:" + channelId;
  }
}
