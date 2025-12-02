/*------------------------------------------------------------------------------
 * NAME : GetCenterInfoListResponseDto.java
 * DESC : 센터정보 리스트 항목 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/05/23  최상원                       초기작성
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.dto.response.authority;

import java.util.List;

import org.json.simple.JSONArray;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.nexus.pdsw.common.ResponseCode;
import com.nexus.pdsw.common.ResponseMessage;
import com.nexus.pdsw.dto.object.CenterListItem;
import com.nexus.pdsw.dto.response.ResponseDto;

import lombok.Getter;

@Getter
public class GetCenterInfoListResponseDto extends ResponseDto {

  private List<CenterListItem> centerInfoList;

  /*  
   *  센터정보 리스트 가져오기(생성자)
   *  
   *  @param JSONArray arrJsonCenter                        센터 정보
   */
  private GetCenterInfoListResponseDto(JSONArray arrJsonCenter) {
    super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    this.centerInfoList = CenterListItem.getCenterInfoList(arrJsonCenter);
  }

  /*  
   *  센터정보 리스트 가져오기(성공)
   *  
   *  @param JSONArray arrJsonCenter                        센터 정보
   *  @return ResponseEntity<GetCenterInfoListResponseDto>
   */
  public static ResponseEntity<GetCenterInfoListResponseDto> success(JSONArray arrJsonCenter) {

    GetCenterInfoListResponseDto result = new GetCenterInfoListResponseDto(arrJsonCenter);
    return ResponseEntity.status(HttpStatus.OK).body(result);
  }
}
