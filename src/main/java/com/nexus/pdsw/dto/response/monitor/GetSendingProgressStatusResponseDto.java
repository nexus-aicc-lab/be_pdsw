/*------------------------------------------------------------------------------
 * NAME : GetSendingProgressStatusResponseDto.java
 * DESC : 발신진행상태 가져오기 항목 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/02/13  최상원                       초기작성
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.dto.response.monitor;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.nexus.pdsw.common.ResponseCode;
import com.nexus.pdsw.common.ResponseMessage;
import com.nexus.pdsw.dto.object.SendingProgressStatusItem;
import com.nexus.pdsw.dto.response.ResponseDto;

import lombok.Getter;

@Getter
public class GetSendingProgressStatusResponseDto extends ResponseDto {
  
  private int waitingCounselorCnt;
  private String campaignId;
  private List<SendingProgressStatusItem> sendingProgressStatusList;

  /*  
   *  발신진행상태 가져오기(생성자)
   *  
   *  @param List<Map<String, Object>> mapSendingProgressStatusList  발신진행상태 리스트
   *  @param int waitingCounselorCnt                                 대기상담원수
   *  @param String campaignId                                       캠페인ID
   *  @param boolean maskCheck 											 고객명, 고객번호 마스킹 여부
   */
  private GetSendingProgressStatusResponseDto(
    List<Map<String, Object>> mapSendingProgressStatusList,
    int waitingCounselorCnt,
    String campaignId,
    boolean maskCheck
  ) {

    super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    this.waitingCounselorCnt = waitingCounselorCnt;
    this.campaignId = campaignId;
    this.sendingProgressStatusList = SendingProgressStatusItem.getSendingProgressStatus(mapSendingProgressStatusList, maskCheck);
  }

  /*  
   *  발신진행상태 가져오기(성공)
   *  
   *  @param List<Map<String, Object>> mapSendingProgressStatusList  발신진행상태 리스트
   *  @param int waitingCounselorCnt                                 대기상담원수
   *  @param String campaignId                                       캠페인ID
   *  @return ResponseEntity<GetSendingProgressStatusResponseDto>
   */
  public static ResponseEntity<GetSendingProgressStatusResponseDto> success(
    List<Map<String, Object>> mapSendingProgressStatusList,
    int waitingCounselorCnt,
    String campaignId,
    boolean maskCheck
  ) {
    GetSendingProgressStatusResponseDto result = new GetSendingProgressStatusResponseDto(mapSendingProgressStatusList, waitingCounselorCnt, campaignId, maskCheck);
    return ResponseEntity.status(HttpStatus.OK).body(result);
  }

  /*  
   *  발신진행상태 가져오기(API 인증 세션키가 없을 경우)
   *  
   *  @return ResponseEntity<ResponseDto>
   */
  public static ResponseEntity<ResponseDto> notExistTenanatId() {
    ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_TENANTID, ResponseMessage.NOT_EXISTED_TENANTID);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
  }

  /*  
   *  발신진행상태 가져오기(테넌트ID가 없을 경우)
   *  
   *  @return ResponseEntity<ResponseDto>
   */
  public static ResponseEntity<ResponseDto> notExistCampaignId() {
    ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_CAMPAIGNID, ResponseMessage.NOT_EXISTED_CAMPAIGNID);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
  }

  /*  
   *  발신진행상태 가져오기(캠페인ID가 없을 경우)
   *  
   *  @return ResponseEntity<ResponseDto>
   */
  public static ResponseEntity<ResponseDto> notExistSessionKey() {
    ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_SESSIONKEY, ResponseMessage.NOT_EXISTED_SESSIONKEY);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
  }
}
