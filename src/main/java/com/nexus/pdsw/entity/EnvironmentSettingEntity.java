/*------------------------------------------------------------------------------
 * NAME : EnvironmentSettingEntity.java
 * DESC : 환경설정 엔티티
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
package com.nexus.pdsw.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;

import com.nexus.pdsw.dto.request.PostEnvironmentSettingRequestDto;
import com.nexus.pdsw.dto.request.PostEnvironmentSettingSaveRequestDto;
import com.nexus.pdsw.entity.primaryKey.EnvironmentSettingPk;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="T_EMP_PDSAPPCONFIG")
@DynamicInsert
@IdClass(EnvironmentSettingPk.class)
public class EnvironmentSettingEntity {
  @Id
  @Column(name = "CENTER_ID", nullable = false)
  private int centerId;                   //센터ID
  @Id
  @Column(name = "TENANT_ID", nullable = false)
  private int tenantId;                   //테넌트ID
  @Id
  @Column(name = "EMPLOYEE_ID", nullable = false)
  private String employeeId;              //상담원ID
  @Column(name = "LIST_ALRAM")
  private int campaignListAlram;          //캠페인 리스트 잔량 부족시의 알람모드를 설정합니다.(0: 한번만, 1: 주기적으로 계속)
  @Column(name = "REDIS_POLLING_CYCLE")
  private int statisticsPollingCycle;     //캠페인 통계를 서버로부터 가져오는 주기를 설정합니다.
  @Column(name = "SERVER_CONN_TIME")
  private int serverConnectionTime;       //서버와의 접속시간을 설정합니다.(프로그램 재시작시 적용)
  @Column(name = "SHOW_CAMPAIGN_SCOP")
  private int showChannelCampaignDayScop; //채널을 캠페인 모드로 할당시 화면에 보여주는 캠페인의 범위를 선택합니다.현재 날짜를 기준으로 설정한 값만큼의 범위안에서 캠페인을 보여줍니다.
  @Column(name = "ALRAM_ONLYSELF_FLAG")
  private int personalCampaignAlertOnly;  //본인 캠페인만 업링크 알림 여부
  @Column(name = "ALRAM_POPUP_FLAG")
  private int useAlramPopup;              //알람 팝업 사용여부
  @Column(name = "WORK_HOURS_FLAG")
  private int unusedWorkHoursCalc;        //업무 시간 계산 사용여부
  @Column(name = "WORK_HOURS_START")
  private String sendingWorkStartHours;   //발신업무시작시간
  @Column(name = "WORK_HOURS_END")
  private String sendingWorkEndHours;     //발신업무종료시간
  @Column(name = "WORK_HOURS_WEEK")
  private String dayOfWeekSetting;        //요일을 설정할 수 있습니다.
  @Column(name = "UPDATE_TIME")
  private LocalDateTime updateTime;       //최종수정일시
  
  @Column(name = "MASK_INFO")
  private int maskInfo;       			  // 고객명, 고객번호 마스킹 처리여부 (디폴트 1 마스킹처리, 0 비마스킹)


  /*
   * 생성자(디폴트 값 설정)
	 * 
	 * @param PostEnvironmentSettingRequestDto requestDto
	*/
  public EnvironmentSettingEntity(PostEnvironmentSettingRequestDto requestDto) {
    this.centerId = requestDto.getCenterId();
    this.tenantId = requestDto.getTenantId();
    this.employeeId = requestDto.getEmployeeId();
    this.campaignListAlram = 0;               //0: 한번만, 1: 주기적으로 계속
    this.statisticsPollingCycle = 30;
    this.serverConnectionTime = 100;
    this.showChannelCampaignDayScop = 5;
    this.personalCampaignAlertOnly = 0;       //0:전체, 1:본인
    this.useAlramPopup = 0;                   //0:알리지 않음, 1:알림
    this.unusedWorkHoursCalc = 1;             //0:사용, 1:미사용
    this.sendingWorkStartHours = "0000";
    this.sendingWorkEndHours = "0000";
    this.dayOfWeekSetting = "f,f,f,f,f,f,f";
    this.updateTime = LocalDateTime.now();
    this.maskInfo = 1;
  }
  
  /*
   * 생성자(디폴트 값 설정)
	 * 
	 * @param PostEnvironmentSettingSaveRequestDto requestDto
	*/
  public void modifySetting(PostEnvironmentSettingSaveRequestDto requestDto) {

    this.campaignListAlram = requestDto.getCampaignListAlram();
    this.statisticsPollingCycle = requestDto.getStatisticsUpdateCycle();
    this.serverConnectionTime = requestDto.getServerConnectionTime();
    this.showChannelCampaignDayScop = requestDto.getShowChannelCampaignDayScop();
    this.personalCampaignAlertOnly = requestDto.getPersonalCampaignAlertOnly();
    this.useAlramPopup = requestDto.getUseAlramPopup();
    this.unusedWorkHoursCalc = requestDto.getUnusedWorkHoursCalc();
    this.sendingWorkStartHours = requestDto.getSendingWorkStartHours();
    this.sendingWorkEndHours = requestDto.getSendingWorkEndHours();
    this.dayOfWeekSetting = requestDto.getDayOfWeekSetting();
    this.updateTime = LocalDateTime.now();
    this.maskInfo = requestDto.getMaskInfo();

  }
}
