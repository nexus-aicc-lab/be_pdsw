/*------------------------------------------------------------------------------
 * NAME : GetEnvironmentSettingResponseDto.java
 * DESC : 환경설정 항목 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/03/19  최상원                       초기작성
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.dto.response.authority;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.nexus.pdsw.common.ResponseCode;
import com.nexus.pdsw.common.ResponseMessage;
import com.nexus.pdsw.dto.response.ResponseDto;
import com.nexus.pdsw.entity.EnvironmentSettingEntity;

import lombok.Getter;

@Getter
public class GetEnvironmentSettingResponseDto extends ResponseDto {

  private int campaignListAlram;          //캠페인 리스트 잔량 부족시의 알람모드를 설정합니다.
  private int statisticsUpdateCycle;      //캠페인 통계를 서버로부터 가져오는 주기를 설정합니다.
  private int serverConnectionTime;       //서버와의 접속시간을 설정합니다.(프로그램 재시작시 적용)
  private int showChannelCampaignDayScop; //채널을 캠페인 모드로 할당시 화면에 보여주는 캠페인의 범위를 선택합니다.현재 날짜를 기준으로 설정한 값만큼의 범위안에서 캠페인을 보여줍니다.
  private int personalCampaignAlertOnly;  //본인 캠페인만 업링크 알림 여부
  private int useAlramPopup;              //알람 팝업 사용여부
  private int unusedWorkHoursCalc;        //업무 시간 계산 사용여부
  private String sendingWorkStartHours;   //발신업무시작시간
  private String sendingWorkEndHours;     //발신업무종료시간
  private String dayOfWeekSetting;        //요일을 설정할 수 있습니다.
  private int maskInfo;        			  // 고객명, 고객번호 마스킹 처리여부 (디폴트 1 마스킹처리, 0 비마스킹)

  /*  
   *  환경설정 가져오기(생성자)
   *  
   *  @param List<MenuByRoleEntity> menuListByRole  반환할 메뉴 리스트
   */
  private GetEnvironmentSettingResponseDto(
    EnvironmentSettingEntity environmentSetting
  ) {
    super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    this.campaignListAlram = environmentSetting.getCampaignListAlram();
    this.statisticsUpdateCycle = environmentSetting.getStatisticsPollingCycle();
    this.serverConnectionTime = environmentSetting.getServerConnectionTime();
    this.showChannelCampaignDayScop = environmentSetting.getShowChannelCampaignDayScop();
    this.personalCampaignAlertOnly = environmentSetting.getPersonalCampaignAlertOnly();
    this.useAlramPopup = environmentSetting.getUseAlramPopup();
    this.unusedWorkHoursCalc = environmentSetting.getUnusedWorkHoursCalc();
    this.sendingWorkStartHours = environmentSetting.getSendingWorkStartHours();
    this.sendingWorkEndHours = environmentSetting.getSendingWorkEndHours();
    this.dayOfWeekSetting = environmentSetting.getDayOfWeekSetting();
    this.maskInfo = environmentSetting.getMaskInfo();
  }

  /*  
   *  환경설정 가져오기(성공)
   *  
   *  @param EnvironmentSettingEntity environmentSetting  반환할 환경설정
   *  @return ResponseEntity<GetEnvironmentSettingResponseDto>
   */
  public static ResponseEntity<GetEnvironmentSettingResponseDto> success(
    EnvironmentSettingEntity environmentSetting
  ) {

    GetEnvironmentSettingResponseDto result = new GetEnvironmentSettingResponseDto(environmentSetting);
    return ResponseEntity.status(HttpStatus.OK).body(result);
  }
}
