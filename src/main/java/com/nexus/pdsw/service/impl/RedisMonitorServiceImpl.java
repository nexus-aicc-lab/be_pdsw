/*------------------------------------------------------------------------------
 * NAME : RedisMonitorServiceImpl.java
 * DESC : Redis 9번방에서 모니터링 정보 가져오기 구현체
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/01/31  최상원                       초기작성
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.pdsw.dto.request.PostDialerChannelStatusInfoRequestDto;
import com.nexus.pdsw.dto.request.PostSendingProgressStatusRequestDto;
import com.nexus.pdsw.dto.response.ResponseDto;
import com.nexus.pdsw.dto.response.monitor.PostDialerChannelStatusInfoResponseDto;
import com.nexus.pdsw.dto.response.monitor.GetProcessStatusInfoResponseDto;
import com.nexus.pdsw.dto.response.monitor.GetProgressInfoResponseDto;
import com.nexus.pdsw.dto.response.monitor.GetSendingProgressStatusResponseDto;
import com.nexus.pdsw.service.RedisMonitorService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisMonitorServiceImpl implements RedisMonitorService {

  private final RedisTemplate<String, Object> redisTemplate;

  @Qualifier("1")
  private final RedisTemplate<String, Object> redisTemplate1;

  public RedisMonitorServiceImpl(
    RedisTemplate<String, Object> redisTemplate,
    @Qualifier("1") RedisTemplate<String, Object> redisTemplate1
  ) {
    this.redisTemplate = redisTemplate;
    this.redisTemplate1 = redisTemplate1;
  }

  @Value("${restapi.baseurl}")
  private String baseUrl;

  /*
   *  타 시스템 프로세스 상태정보 가져오기
   *  
   *  @return ResponseEntity<? super GetProcessStatusInfoResponseDto>
   */
  @Override
  public ResponseEntity<? super GetProcessStatusInfoResponseDto> getProcessStatusInfo() {

    List<Map<String, Object>> mapMonitorProcessList = new ArrayList<Map<String, Object>>();

    try {
      
      HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
      JSONParser jsonParser = new JSONParser();
      JSONArray arrJsonMonitorProcess = new JSONArray();

      String redisKey = "monitor:process";

      Map<Object, Object> redisMonitorProcess = hashOperations.entries(redisKey);
      arrJsonMonitorProcess = (JSONArray) jsonParser.parse(redisMonitorProcess.values().toString());
      Map<String, Object> mapMonitorProcess = null;

      for(Object jsonMonitorProcess : arrJsonMonitorProcess) {
        try {
          mapMonitorProcess = new ObjectMapper().readValue(jsonMonitorProcess.toString(), Map.class);
        } catch (JsonMappingException e) {
          throw new RuntimeException(e);
        }
        mapMonitorProcessList.add(mapMonitorProcess);
      }

    } catch (Exception e) {
      e.printStackTrace();
      ResponseDto.databaseError();
    }
    return GetProcessStatusInfoResponseDto.success(mapMonitorProcessList);
  }

  /*
   *  Dialer 채널 상태 정보 가져오기
   *  
   *  @param PostDialerChannelStatusInfoRequestDto requestDto     Dialer 장비ID's
   *  @return ResponseEntity<? super PostDialerChannelStatusInfoResponseDto>
   */
  @Override
  public ResponseEntity<? super PostDialerChannelStatusInfoResponseDto> getDialerChannelStatusInfo(
    PostDialerChannelStatusInfoRequestDto requestDto
  ) {

    List<Map<String, Object>> mapDialerChannelStatusList = new ArrayList<Map<String, Object>>();

    try {

      String redisKey = "";
      HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
      JSONParser jsonParser = new JSONParser();
      JSONArray arrJson = new JSONArray();

      //API 인증 세션키가 없이 호출하였을 때
      if (requestDto.getSessionKey() == null || requestDto.getSessionKey().trim().isEmpty()) {
        return PostDialerChannelStatusInfoResponseDto.notExistSessionKey();        
      }
      
      //전체 Device에 대한 채널상태
      if (requestDto.getDeviceId().equals("0")) {        
        // Map<String, Object> bodyMap = new HashMap<>();
        WebClient webClient =
          WebClient
            .builder()
            .baseUrl(baseUrl)
            .defaultHeaders(httpHeaders -> {
              httpHeaders.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
              httpHeaders.add("Session-Key", requestDto.getSessionKey());
            })
            .build();
            
        try {        
          //API 요청
          Map<String, Object> apiDialer =
            webClient
              .post()
              .uri(uriBuilder ->
                uriBuilder
                  .path("/pds/collections/dialing-device")
                  .build()
              )
              .retrieve()
              .bodyToMono(Map.class)
              .block();

          //장비 내역 가져오기 API 요청이 실패했을 때
          if (!apiDialer.get("result_code").equals(0)) {
            return PostDialerChannelStatusInfoResponseDto.notExistDialerDevice();
          }
          List<Map<String, Object>> mapDialerList = (List<Map<String, Object>>) apiDialer.get("result_data");

          for(Map<String, Object> mapDialer : mapDialerList) {
  
            redisKey = "monitor:dialer:" + mapDialer.get("device_id") + ":channel";
  
            Map<Object, Object> redisDialerChannelStatus = hashOperations.entries(redisKey);
            arrJson = (JSONArray) jsonParser.parse(redisDialerChannelStatus.values().toString());
  
            Map<String, Object> mapItem = null;
  
            for(Object jsonItem : arrJson) {
              try {
                mapItem = new ObjectMapper().readValue(jsonItem.toString(), Map.class);
                mapItem.put("deviceId", mapDialer.get("device_id"));
              } catch (JsonMappingException e) {
                throw new RuntimeException(e);
              }
              mapDialerChannelStatusList.add(mapItem);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }

      //특정 Device에 대한 채널상태
      } else {

        redisKey = "monitor:dialer:" + requestDto.getDeviceId() + ":channel";

        Map<Object, Object> redisDialerChannelStatus = hashOperations.entries(redisKey);
        arrJson = (JSONArray) jsonParser.parse(redisDialerChannelStatus.values().toString());

        Map<String, Object> mapItem = null;

        for(Object jsonItem : arrJson) {
          try {
            mapItem = new ObjectMapper().readValue(jsonItem.toString(), Map.class);
            mapItem.put("deviceId", requestDto.getDeviceId());
          } catch (JsonMappingException e) {
            throw new RuntimeException(e);
          }
          mapDialerChannelStatusList.add(mapItem);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      ResponseDto.databaseError();
    }

    return PostDialerChannelStatusInfoResponseDto.success(mapDialerChannelStatusList);
  }

  /*
   *  캠페인별 진행정보 가져오기
   *  
   *  @param tenantId           테넌트ID
   *  @param campaignId         캠페인ID
   *  @return ResponseEntity<? super GetProgressInfoResponseDto>
   */
  @Override
  public ResponseEntity<? super GetProgressInfoResponseDto> getProgressInfo(
    String tenantId,
    String campaignId
  ) {

    List<Map<String, Object>> mapProgressInfoList = new ArrayList<Map<String, Object>>();

    try {
      
      //테넌트ID 값 없이 호출하였을 때
      if (tenantId == null || tenantId.trim().isEmpty()) {
        return GetProgressInfoResponseDto.notExistTenantId();        
      }

      //캠페인ID 값 없이 호출하였을 때
      if (campaignId == null || campaignId.trim().isEmpty()) {
        return GetProgressInfoResponseDto.notExistCampaignId();        
      }

      HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
      JSONParser jsonParser = new JSONParser();
      JSONArray arrJson = new JSONArray();
      String redisKey = "";
      Map<Object, Object> redisProgressInfo = new HashMap<>();
      Map<String, Object> mapItem = new HashMap<>();

      //호출 하는 상담원의 테넌트 ID가 "0"이면
      if (tenantId.equals("0")) {
        HashOperations<String, Object, Object> hashOperations1 = redisTemplate1.opsForHash();

        Map<Object, Object> redisTenantList = hashOperations1.entries("master.tenant-1");
        
        for (Object tenantKey : redisTenantList.keySet()) {
          redisKey = "monitor:tenant:" + tenantKey + ":campaign:" + campaignId + ":statistics";

          redisProgressInfo = hashOperations.entries(redisKey);

          if (redisProgressInfo == null) {
            continue;
          }
          arrJson = (JSONArray) jsonParser.parse(redisProgressInfo.values().toString());
        
          for(Object jsonItem : arrJson) {
            try {
              mapItem = new ObjectMapper().readValue(jsonItem.toString(), Map.class);
            } catch (JsonMappingException e) {
              throw new RuntimeException(e);
            }
            mapProgressInfoList.add(mapItem);
          }
        }
      } else {
        redisKey = "monitor:tenant:" + tenantId + ":campaign:" + campaignId + ":statistics";

        redisProgressInfo = hashOperations.entries(redisKey);

        if (redisProgressInfo == null) {
          return GetProgressInfoResponseDto.notExistRedisHash();
        }

        arrJson = (JSONArray) jsonParser.parse(redisProgressInfo.values().toString());

        for(Object jsonItem : arrJson) {
          try {
            mapItem = new ObjectMapper().readValue(jsonItem.toString(), Map.class);
          } catch (JsonMappingException e) {
            throw new RuntimeException(e);
          }
          mapProgressInfoList.add(mapItem);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      ResponseDto.databaseError();
    }

    return GetProgressInfoResponseDto.success(mapProgressInfoList);
  }

  /*
   *  발신진행상태 가져오기
   *  
   *  @param PostSendingProgressStatusRequestDto requestDto
   *  @return ResponseEntity<? super GetSendingProgressStatusResponseDto>
   */
  @Override
  public ResponseEntity<? super GetSendingProgressStatusResponseDto> getSendingProgressStatus(
    PostSendingProgressStatusRequestDto requestDto
  ) {

    List<Map<String, Object>> mapSendingProgressStatusList = new ArrayList<Map<String, Object>>();
    int waitingCounselorCnt = 0;

    try {

      //API 인증 세션키가 없이 호출하였을 때
      if (requestDto.getSessionKey() == null || requestDto.getSessionKey().trim().isEmpty()) {
        return GetSendingProgressStatusResponseDto.notExistSessionKey();
      }

      //테넌터ID가 없이 호출하였을 때
      if (requestDto.getTenantId() == null || requestDto.getTenantId().trim().isEmpty()) {
        return GetSendingProgressStatusResponseDto.notExistTenanatId();
      }

      //캠페인ID가 없이 호출하였을 때
      if (requestDto.getCampaignId() == null || requestDto.getCampaignId().trim().isEmpty()) {
        return GetSendingProgressStatusResponseDto.notExistCampaignId();
      }
      // log.info("requestDto.getCampaignId().clss : {}", requestDto.getCampaignId().getClass().getName());

      //API 호출 시 Request 개체 자료구조
      Map<String, Object> bodyMap = new HashMap<>();
      Map<String, Object> filterMap = new HashMap<>();

      // List<Object> assignedCounselorList = new ArrayList<>();

      HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
      HashOperations<String, Object, Object> hashOperations1 = redisTemplate1.opsForHash();
      JSONParser jsonParser = new JSONParser();
      JSONArray arrJson = new JSONArray();
      JSONArray arrJsonCounselorState = new JSONArray();

      String strCounselorId = "";
      String strStateCode = "";
      String _tenantId = requestDto.getTenantId();
      String _campaignId = requestDto.getCampaignId();

      String redisKey = "";
      Map<Object, Object> redisSendingProgressStatus = new HashMap<>();
      Map<Object, Object> redisCounselorStatusList = new HashMap<>();
      Map<String, Object> mapItem = new HashMap<>();

      int[] arrCampaignId = null;

      //특정 캠페인이 아닌 경우
      if (_campaignId.indexOf(",") > -1 || _campaignId.equals("0")) {

        // List<Map<String, Object>> mapCampaignList = new ArrayList<Map<String, Object>>();

        //로긴 상담원의 테넌트ID가 "0"인 경우
        if (_tenantId.indexOf(",") > -1 || _tenantId.equals("0")) {

          int[] arrTenantId = Arrays.stream(_tenantId.split(","))
                                      .mapToInt(Integer::parseInt)
                                      .toArray();
          int i = 0;

          for (Object tenantKey : _tenantId.split(",")) {
            redisKey = "monitor:tenant:" + tenantKey + ":campaign-dial";
            // redisKey = "monitor:tenant:" + tenantKey + ":campaign:dial";
            /* campaignId-dial_sequence 구조로 변경 09-02 BQSQ-38
            */
  
            redisSendingProgressStatus = hashOperations.entries(redisKey);
            arrJson = (JSONArray) jsonParser.parse(redisSendingProgressStatus.values().toString());  
  
            for(Object jsonItem : arrJson) {
              try {
                mapItem = new ObjectMapper().readValue(jsonItem.toString(), Map.class);
              } catch (JsonMappingException e) {
                throw new RuntimeException(e);
              }
              mapSendingProgressStatusList.add(mapItem);
            }

            arrTenantId[i] = Integer.parseInt(tenantKey.toString());
            i++;
          }

          filterMap.put("tenant_id", arrTenantId);
          bodyMap.put("filter", filterMap);

        } else {
          redisKey = "monitor:tenant:" + _tenantId + ":campaign-dial";
          // redisKey = "monitor:tenant:" + tenantKey + ":campaign:dial";
          /* campaignId-dial_sequence 구조로 변경 09-02 BQSQ-38
           */
  
          redisSendingProgressStatus = hashOperations.entries(redisKey);
          arrJson = (JSONArray) jsonParser.parse(redisSendingProgressStatus.values().toString());  
  
          for(Object jsonItem : arrJson) {
            try {
              mapItem = new ObjectMapper().readValue(jsonItem.toString(), Map.class);
            } catch (JsonMappingException e) {
              throw new RuntimeException(e);
            }
            mapSendingProgressStatusList.add(mapItem);
          }

          filterMap.put("tenant_id", Integer.parseInt(_tenantId));
          bodyMap.put("filter", filterMap);

        }

        //로그인 상담사 테넌트ID에 따른 캠페인 가져오기 API 요청
        // Map<String, Object> apiCampaign =
        //   webClient
        //     .post()
        //     .uri(uriBuilder ->
        //       uriBuilder
        //         .path("/pds/collections/campaign-list")
        //         .build()
        //     )
        //     .bodyValue(bodyMap)
        //     .retrieve()
        //     .bodyToMono(Map.class)
        //     .doOnError(WebClientResponseException.class, ex -> {
        //       // 추가적인 로깅이나 예외 처리
        //       log.error("WebClientResponseException: ", ex);
        //     })
        //     .block();

        //로그인 상담사 테넌트ID에 따른 캠페인 가져오기 API 요청이 실패했을 때
        // if (!apiCampaign.get("result_code").equals(0)) {
        //   ResponseDto result = new ResponseDto(apiCampaign.get("result_code").toString(), apiCampaign.get("result_msg").toString());
        //   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        // }

        // mapCampaignList = (List<Map<String, Object>>) apiCampaign.get("result_data");_campaignId

        bodyMap.clear();
        filterMap.clear();

        arrCampaignId = Arrays.stream(_campaignId.split(","))
                                      .mapToInt(Integer::parseInt)
                                      .toArray();
        int i = 0;

        //로그인 상담사 테넌트ID에 따른 캠페인에 할당된 상담원 가져오기
        // for (Map<String, Object> mapCampaign : mapCampaignList) {
        //   arrCampaignId[i] = (int) mapCampaign.get("campaign_id");
        //   i++;
        // }

        // filterMap.put("campaign_id", arrCampaignId);
        // bodyMap.put("filter", filterMap);

        // //WebClient로 API서버와 연결
        // WebClient webClient =
        //   WebClient
        //     .builder()
        //     .baseUrl(baseUrl)
        //     .defaultHeaders(httpHeaders -> {
        //       httpHeaders.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        //       httpHeaders.add("Session-Key", requestDto.getSessionKey());
        //     })
        //     .build();

        // try {
        //   //캠페인에 할당된 상담원 가져오기 API 요청
        //   Map<String, Object> apiAssignedCounselor =
        //     webClient
        //       .post()
        //       .uri(uriBuilder ->
        //         uriBuilder
        //           .path("/pds/collections/campaign-agent")
        //           .build()
        //       )
        //       .bodyValue(bodyMap)
        //       .retrieve()
        //       .bodyToMono(Map.class)
        //       .block();

        //   //해당 캠페인에 할당된 상담원ID 가져오기 API 요청이 실패했을 때
        //   if (!apiAssignedCounselor.get("result_code").equals(0)) {
        //     ResponseDto result = new ResponseDto(apiAssignedCounselor.get("result_code").toString(), apiAssignedCounselor.get("result_msg").toString());
        //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        //   }

        //   //캠페인에 할당된 상담원이 존재하면
        //   if (apiAssignedCounselor.get("result_data") != null) {             
        //     List<Map<String, Object>> mapAssignedCounselorList = (List<Map<String, Object>>) apiAssignedCounselor.get("result_data");

        //     //할당된 상담원 리스트에 누적 추가
        //     for (Map<String, Object> mapAssignedCounselor : mapAssignedCounselorList) {
        //       assignedCounselorList.addAll((List<Object>) mapAssignedCounselor.get("agent_id"));
        //     }
        //   }            
        // } catch (Exception e) {
        //   e.printStackTrace();
        // }

      //특정 캠페인인 경우
      } else {
        redisKey = "monitor:tenant:" + _tenantId + ":campaign-dial";
        // redisKey = "monitor:tenant:" + tenantKey + ":campaign:dial";
        /* campaignId-dial_sequence 구조로 변경 09-02 BQSQ-38
         */
        // redisKey = "monitor:tenant:" + requestDto.getTenantId() + ":campaign:dial";
        redisSendingProgressStatus = hashOperations.entries(redisKey);
        arrJson = (JSONArray) jsonParser.parse(redisSendingProgressStatus.values().toString());  
          
        for(Object jsonItem : arrJson) {
          try {
            mapItem = new ObjectMapper().readValue(jsonItem.toString(), Map.class);
            if( mapItem.get("campaign_id").toString().equals(_campaignId) ){
              mapSendingProgressStatusList.add(mapItem);
            }
          } catch (JsonMappingException e) {
            throw new RuntimeException(e);
          }
        }

        return GetSendingProgressStatusResponseDto.success(mapSendingProgressStatusList, waitingCounselorCnt, requestDto.getCampaignId());
      }

      // log.info("mapSendingProgressStatusList : {}", mapSendingProgressStatusList.toString());

      //수집된 할당된 상담사ID 중복제거
      // List<Object> assignedCounselorDuplicatesRemovedList = requestDto.getAgentIds() != null ?
      //   Arrays.stream(requestDto.getAgentIds()).distinct().collect(Collectors.toList()) : new ArrayList<>();
      
      
      // for (Object assignedCounselor : assignedCounselorDuplicatesRemovedList){

        for (Object tenantKey : _tenantId.split(",")) {

          redisCounselorStatusList = hashOperations1.entries("st.employee.state-1-" + tenantKey);

          arrJsonCounselorState = (JSONArray) jsonParser.parse(redisCounselorStatusList.values().toString());

          for (Object jsonCounselorState : arrJsonCounselorState) {

            JSONObject jsonObjCounselorState = (JSONObject) jsonCounselorState;

            // strCounselorId = jsonObjCounselorState.get("EMPLOYEE").toString();

            // if (assignedCounselor.toString().equals(strCounselorId)) {

              JSONObject jsonObjCounselorStateData = (JSONObject) jsonObjCounselorState.get("Data");
              strStateCode = jsonObjCounselorStateData.get("state").toString();

              //203(휴식), 204(대기), 205(처리), 206(후처리)
              if (strStateCode.equals("204")) {
                waitingCounselorCnt += 1 ;
                // break;
              }
            // }
          }
        }
      // }

    } catch (Exception e) {
      e.printStackTrace();
      ResponseDto.databaseError();
    }
    return GetSendingProgressStatusResponseDto.success(mapSendingProgressStatusList, waitingCounselorCnt, requestDto.getCampaignId());
  }
  
}
