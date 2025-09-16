/*------------------------------------------------------------------------------
 * NAME : GetAllProgressInfoResponseDto.java
 * DESC : 전체 진행정보 가져오기 항목 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/09/16  이승용                       초기작성
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.dto.response.monitor;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.nexus.pdsw.common.ResponseCode;
import com.nexus.pdsw.common.ResponseMessage;
import com.nexus.pdsw.dto.object.ProgressInfoItem;
import com.nexus.pdsw.dto.response.ResponseDto;

import lombok.Getter;

@Getter
public class GetAllProgressInfoResponseDto extends ResponseDto {
  
  private List<ProgressInfoItem> progressInfoList;

  /*  
   *  전체 진행정보 가져오기(생성자)
   *  
   *  @param List<Map<String, Object>> mapProgressInfoList  전체 진행정보 리스트
   */
  private GetAllProgressInfoResponseDto(
    List<Map<String, Object>> mapProgressInfoList
  ) {

    super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    this.progressInfoList = ProgressInfoItem.getProgressInfo(mapProgressInfoList);
  }

  /*  
   *  전체 진행정보 가져오기(성공)
   *  
   *  @param List<Map<String, Object>> mapProgressInfoList  캠페인 별 발신 상태정보 리스트
   *  @return ResponseEntity<GetAllProgressInfoResponseDto>
   */
  public static ResponseEntity<GetAllProgressInfoResponseDto> success(
    List<Map<String, Object>> mapProgressInfoList
  ) {
    GetAllProgressInfoResponseDto result = new GetAllProgressInfoResponseDto(mapProgressInfoList);
    return ResponseEntity.status(HttpStatus.OK).body(result);
  }

  /*  
   *  전체 진행정보 가져오기(테넌트ID가 없을 경우)
   *  
   *  @return ResponseEntity<GetDialerChannelStatusInfoResponseDto>
   */
  public static ResponseEntity<ResponseDto> notExistTenantId() {
    ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_TENANTID, ResponseMessage.NOT_EXISTED_TENANTID);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
  }

  /*  
   *  전체 진행정보 가져오기(캠페인ID가 존재하지 않을 경우)
   *  
   *  @return ResponseEntity<GetDialerChannelStatusInfoResponseDto>
   */
  public static ResponseEntity<ResponseDto> notExistCampaignId() {
    ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_CAMPAIGNID, ResponseMessage.NOT_EXISTED_CAMPAIGNID);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
  }

  /*  
   *  전체 진행정보 가져오기(레디스 Hash 테이브이 존재하지 않을 경우)
   *  
   *  @return ResponseEntity<GetDialerChannelStatusInfoResponseDto>
   */
  public static ResponseEntity<ResponseDto> notExistRedisHash() {
    ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_REDISHASH, ResponseMessage.NOT_EXISTED_REDISHASH);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
  }
}
