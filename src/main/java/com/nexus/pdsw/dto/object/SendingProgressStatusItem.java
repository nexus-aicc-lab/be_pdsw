/*------------------------------------------------------------------------------
 * NAME : SendingProgressStatusItem.java
 * DESC : Redis 9번방의 발신진행상태 DTO
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
package com.nexus.pdsw.dto.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
public class SendingProgressStatusItem {
  private int campaignId;           //캠페인ID
  private String campaignName;      //캠페인 이름
  private int waitingLstCnt;        //대기리스트 건수(전체리스트 수 - 발신건수)
  private int event;                //채널에 발생한 마지막 이벤트(0(NONE), 1(ON_HOOK), 2(OFF_HOOK), 3(PRESS_DIGIT), 4(NETWORK_DELAY), 5(INTERRUPT_CALL), 6(RINGBACK), 7(CONNECT), 8(DETECT_BEGIN), 9(DETECT_END), 10(ROUTE))
  private int dialSequence;         //발신 일련 번호
  private int dialResult;           //발신 결과 코드(0(NONE), 1(MAN), 2(BUSY), 3(NO_ANSWER), 4(FAX_MODEM), 5(ANSWERING_MACHINE), 6(ETC_FAIL), 7(INVALID_NUMBER), 8(DIALING), 9(LINE_STOP), 10(CUSTOMER_ONHOOK), 11(SILENCE), 12(DIALTONE_SILENCE), 13(BLACK_LIST), 14(ROUTE_FAIL), 15(BEFORE_BLACKLIST), 2501(MACHINE_BUSY), 2502(MACHINE_NOANSWER), 2503(MACHINE_POWEROFF), 2504(MACHINE_ROAMING), 2505(MACHINE_MISSING_NUMBER), 2506(MACHINE_ETC))
  private String customerName;      //고객 이름
  private String customerKey;       //고객 키
  private List<String> phoneNumber;     //발신 번호
  private List<Integer> phoneDialCount;     //발신 번호 별 시도 회수
  private int dialedPhone;          //발신 번호 인덱스
  private int reuseCount;           //캠페인 재사용 회수 : 1(최초발신), 2~(재발신)
  private int retryCall;            //재시도 여부 : 0(재시도 없음), 1(재시도 있음)

  /*
   *  Redis 9번방의 발신진행상태 DTO(생성자)
	 * 
   *  @param Map<String, Object> mapSendingProgressStatus  발신진행상태
	*/
  private SendingProgressStatusItem(
    Map<String, Object> mapSendingProgressStatus
  ) {

    // log.info("mapSendingProgressStatus: {}", mapSendingProgressStatus.toString());

    // log.info("campaignId: {}", mapSendingProgressStatus.get("campaign_id"));
    this.campaignId = (int) mapSendingProgressStatus.get("campaign_id");

    // log.info("campaignName: {}", mapSendingProgressStatus.get("campaign_name"));
    this.campaignName = (String) mapSendingProgressStatus.get("campaign_name");

    // log.info("event: {}", mapSendingProgressStatus.get("event"));
    this.event = (int) mapSendingProgressStatus.get("event");

    // log.info("dialSequence: {}", mapSendingProgressStatus.get("dial_sequence"));
    this.dialSequence = (int) mapSendingProgressStatus.get("dial_sequence");

    // log.info("dialResult: {}", mapSendingProgressStatus.get("dial_result"));
    this.dialResult = (int) mapSendingProgressStatus.get("dial_result");

    // log.info("customerName: {}", mapSendingProgressStatus.get("customer_name"));
    this.customerName = (String) mapSendingProgressStatus.get("customer_name");

    // log.info("customerKey: {}", mapSendingProgressStatus.get("customer_key"));
    this.customerKey = (String) mapSendingProgressStatus.get("customer_key");

    this.phoneNumber = (List<String>) mapSendingProgressStatus.get("phone_number");
    // log.info("phoneNumber: {}", this.phoneNumber.toString());

    this.phoneDialCount = (List<Integer>) mapSendingProgressStatus.get("phone_dial_count");
    // log.info("phoneDialCount: {}", this.phoneDialCount.toString());

    this.dialedPhone = (int) mapSendingProgressStatus.get("dialed_phone");
    this.reuseCount = (int) mapSendingProgressStatus.get("reuse_count");
    this.retryCall = (int) mapSendingProgressStatus.get("retry_call");
  }

  /*
   *  Redis 9번방의 발신진행상태 DTO로 변환하기
	 * 
   *  @param List<Map<String, String>> mapSendingProgressStatusList  발신진행상태 리스트
	 *  @return List<SendingProgressStatusItem> sendingProgressStatusList
	*/
  public static List<SendingProgressStatusItem> getSendingProgressStatus(
    List<Map<String, Object>> mapSendingProgressStatusList
  ) {

    List<SendingProgressStatusItem> sendingProgressStatusList = new ArrayList<>();

    for(Map<String, Object> mapSendingProgressStatus : mapSendingProgressStatusList) {
      SendingProgressStatusItem mapSendingProgressStatusItem = new SendingProgressStatusItem(mapSendingProgressStatus);
      sendingProgressStatusList.add(mapSendingProgressStatusItem);
    }

    return sendingProgressStatusList;
  }

}
