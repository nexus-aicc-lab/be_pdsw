/*------------------------------------------------------------------------------
 * NAME : GetCounselorListResponseDto.java
 * DESC : 상담사 리스트 항목 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/01/15  최상원                       초기작성
 * 2025/01/21  최상원                       센터명, 테넌트명, 그룹명, 팀명 추가
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.dto.response.counselor;

import java.util.List;

import org.json.simple.JSONArray;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.nexus.pdsw.common.ResponseCode;
import com.nexus.pdsw.common.ResponseMessage;
import com.nexus.pdsw.dto.object.OrganizationItem;
import com.nexus.pdsw.dto.response.ResponseDto;

import lombok.Getter;

@Getter
public class GetCounselorListResponseDto extends ResponseDto  {

  private List<OrganizationItem> organizationList;

  /*  
   *  상담사 리스트 가져오기(생성자)
   *  
   *  @param RedisTemplate<String, Object> redisTemplate1   레디스 개체
   *  @param String baseUrl                                  기본 URL
   *  @param String sessionKey                               세션 키
   *  @param String tenantId                                 테넌트 ID
   *  @param JSONArray arrJsonCenter                        센터 정보
   */
  private GetCounselorListResponseDto(
    RedisTemplate<String, Object> redisTemplate1,
    String baseUrl,
    String sessionKey,
    String tenantId,
    JSONArray arrJsonCenter
  ) {
    super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    this.organizationList = OrganizationItem.getOrganizationList(redisTemplate1, baseUrl, sessionKey, tenantId, arrJsonCenter);
  }

  /*  
   *  상담사 리스트 가져오기(성공)
   *  
   *  @param RedisTemplate<String, Object> redisTemplate1    레디스 개체
   *  @param String baseUrl                                  기본 URL
   *  @param String sessionKey                               세션 키
   *  @param String tenantId                                 테넌트 ID
   *  @param JSONArray arrJsonCenter                         센터 정보
   *  @return ResponseEntity<GetCounselorListResponseDto>
   */
  public static ResponseEntity<GetCounselorListResponseDto> success(
    RedisTemplate<String, Object> redisTemplate1,
    String baseUrl,
    String sessionKey,
    String tenantId,
    JSONArray arrJsonCenter
  ) {
    GetCounselorListResponseDto result = new GetCounselorListResponseDto(redisTemplate1, baseUrl, sessionKey, tenantId, arrJsonCenter);
    return ResponseEntity.status(HttpStatus.OK).body(result);
  }
}
