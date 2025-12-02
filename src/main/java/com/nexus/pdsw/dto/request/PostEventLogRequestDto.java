/*------------------------------------------------------------------------------
 * NAME : PostEventLogRequestDto.java
 * DESC : 이벤트 로그 저장 시 전달 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/02/08  최상원                       초기작성
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostEventLogRequestDto {
  
  private int tenantId;             //테넌트ID
  private String employeeId;        //상담원ID
  private String userHost;          //접속ip, hostname등
  private String queryId;           //web query id
  private String queryType;         //이벤트타입 I:insert, U:update, D:delete, R:select, A:자원변경
  private String activation;        //행위 로그
  private String description;       //부가정보
  private int successFlag;          //성공여부 1 : 성공 0 : 실패 또는 DB 에러코드
  private String eventName;         //이벤트명 login, logout, 테이블명, employee.postion_level
  private int queryRows;            //조회건수
  private String targetId;          //대상자ID
  private int userSessionType;      //사용자접속타입 0 : 로컬PC 1 : VDI 또는 재택 연결 시
  private int exportFlag;           //export실행여부 1 : export 실행
  private String memo;              //메모 ex)파일다운사유 등
  private String updateEmployeeId;  //수정상담원ID
}
