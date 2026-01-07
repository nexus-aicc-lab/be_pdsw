/*------------------------------------------------------------------------------
 * NAME : CounselorServiceImpl.java
 * DESC : 상담사 관련 구현체
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/01/15  최상원                       초기작성
 * 2025/01/18  최상원                       상담사 상태정보 가져오기 추가
 * 2025/01/21  최상원                       상담사 내역 가져오기에 센터명, 테넌트명, 그룹명, 팀명 추가
 * 2025/02/14  최상원                       상담사 상태 모니터, 캠페인별 할당 상담사 정보 가져오기 API 수정
 * 2025/02/20  최상원                       스킬 할당된 상담사 리스트 가져오기 추가
 * 2025/04/11  최상원                       상담사 이름 가져오기 추가
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParseException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.pdsw.dto.request.PostCounselorListRequestDto;
import com.nexus.pdsw.dto.request.PostSkillAssignedCounselorListRequestDto;
import com.nexus.pdsw.dto.response.ResponseDto;
import com.nexus.pdsw.dto.response.counselor.GetCounselorInfoListResponseDto;
import com.nexus.pdsw.dto.response.counselor.GetCounselorListResponseDto;
import com.nexus.pdsw.dto.response.counselor.PostCounselorStatusListResponseDto;
import com.nexus.pdsw.dto.response.counselor.PostSkillAssignedCounselorListResponseDto;
import com.nexus.pdsw.service.CounselorService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CounselorServiceImpl implements CounselorService {

  @Qualifier("1")
  private final RedisTemplate<String, Object> redisTemplate1;

  public CounselorServiceImpl(
    @Qualifier("1") RedisTemplate<String, Object> redisTemplate1
  ) {
    this.redisTemplate1 = redisTemplate1;
  }

  @Value("${restapi.baseurl}")
  private String baseUrl;

  /*  
   *  상담사 리스트 가져오기
   *  
   *  @param PostCounselorListRequestDto requestBody  상담사 리스트 전달 DTO
   *  @return ResponseEntity<? super GetCounselorListResponseDto>
   */
  @Override
  public ResponseEntity<? super GetCounselorListResponseDto> getCounselorList(
    PostCounselorListRequestDto requestBody
  ) {

    JSONArray arrJsonCenter = new JSONArray();

    try {

      String redisKey = "";
      HashOperations<String, Object, Object> hashOperations = redisTemplate1.opsForHash();
      JSONParser jsonParser = new JSONParser();
  
      //센터정보
      redisKey = "master.center";
      Map<Object, Object> redisCenter = hashOperations.entries(redisKey);

      arrJsonCenter = (JSONArray) jsonParser.parse(redisCenter.values().toString());

    } catch (Exception e) {
      e.printStackTrace();
      ResponseDto.databaseError();
    }
    return GetCounselorListResponseDto.success(redisTemplate1, baseUrl, requestBody.getSessionKey(), requestBody.getTenantId(), arrJsonCenter);
  }

  /*
   *  상담사 상태정보 가져오기
   *  
   *  @param PostCounselorListRequestDto requestBody    전달 DTO
   *  @return ResponseEntity<? super PostCounselorStatusListResponseDto>
   */
  @Override
  @SuppressWarnings("unchecked")
  public ResponseEntity<? super PostCounselorStatusListResponseDto> getCounselorStatusList(
    PostCounselorListRequestDto requestBody
  ) {

    List<Map<String, Object>> mapCounselorStatusList = new ArrayList<Map<String, Object>>();

    try {

      String redisKey = "";
      // String redisCounselorIdx = "";
      HashOperations<String, Object, Object> hashOperations = redisTemplate1.opsForHash();
      Map<Object, Object> redisCounselorStatusList = new HashMap<>();

      JSONArray arrJsonCounselorState = new JSONArray();      
      JSONParser jsonParser = new JSONParser();

      String strCounselorId = "";
      String strStateCode = "";
      String strBlendKind = ""; // 2025-01-07 인바운드, 아웃바운드 확인 추가 - rody
      String[] _agentIds = requestBody.getAgentIds();
      int[] arrTenantId = null;

      Map<String, Object> mapCounselorState = new HashMap<>();

      //테넌트ID 값 없이 호출하였을 때
      if (requestBody.getTenantId() == null || requestBody.getTenantId().trim().isEmpty()) {
        return PostCounselorStatusListResponseDto.notExistTenantId();        
      }

      //캠페인ID 값 없이 호출하였을 때
      if (!requestBody.getCampaignId().chars().allMatch(Character::isDigit) || requestBody.getCampaignId() == null || requestBody.getCampaignId().trim().isEmpty()) {
        return PostCounselorStatusListResponseDto.notExistCampaignId();        
      }

      //API 인증 세션키가 없이 호출하였을 때
      if (requestBody.getSessionKey() == null || requestBody.getSessionKey().trim().isEmpty()) {
        return PostCounselorStatusListResponseDto.notExistSessionKey();
      }
      
      String _centerId = requestBody.getCenterId();
      if (requestBody.getCenterId() == null || requestBody.getCenterId().trim().isEmpty()) {
        _centerId = "1";       
      }

      if( _agentIds.length > 0 
        && !requestBody.getTenantId().equals("A") && requestBody.getTenantId().indexOf(',') == -1 ) {

        for (Object assignedCounselor : _agentIds){

          redisKey = "st.employee.state-"+ _centerId + "-" + requestBody.getTenantId();
          redisCounselorStatusList = hashOperations.entries(redisKey);

          arrJsonCounselorState = (JSONArray) jsonParser.parse(redisCounselorStatusList.values().toString());

          for (Object jsonCounselorState : arrJsonCounselorState) {

            JSONObject jsonObjCounselorState = (JSONObject) jsonCounselorState;

            strCounselorId = jsonObjCounselorState.get("EMPLOYEE").toString();

            if (assignedCounselor.equals(strCounselorId)) {

              JSONObject jsonObjCounselorStateData = (JSONObject) jsonObjCounselorState.get("Data");
              strStateCode = jsonObjCounselorStateData.get("state").toString();
              strBlendKind = jsonObjCounselorStateData.get("blend_kind").toString();

              //203(휴식), 204(대기), 205(처리), 206(후처리)
              if ( (strStateCode.equals("203") || strStateCode.equals("204") ||
                strStateCode.equals("205") || strStateCode.equals("206") ) && 
            		  !"1".equals(strBlendKind) ) {

                try {
                  mapCounselorState = new ObjectMapper().readValue(jsonObjCounselorStateData.toString(), Map.class);
                } catch (JsonParseException e) {
                  e.printStackTrace();
                } catch (JsonMappingException e) {
                  e.printStackTrace();
                } catch (IOException e) {
                  e.printStackTrace();
                }
                mapCounselorStatusList.add(mapCounselorState);
                break;
              }
            }
          }
        }
      }else if( _agentIds.length > 0 ){
        String[] tenantIdStrings = requestBody.getTenantId().split(",");
        arrTenantId = new int[tenantIdStrings.length];
        for (int i = 0; i < tenantIdStrings.length; i++) {
          arrTenantId[i] = Integer.parseInt(tenantIdStrings[i].trim());
        }
        
        for (Object assignedCounselor : _agentIds){

          for (Object tenantKey : arrTenantId) {

            redisKey = "st.employee.state-"+_centerId+"-" + tenantKey;
            redisCounselorStatusList = hashOperations.entries(redisKey);

            arrJsonCounselorState = (JSONArray) jsonParser.parse(redisCounselorStatusList.values().toString());

            for (Object jsonCounselorState : arrJsonCounselorState) {

              JSONObject jsonObjCounselorState = (JSONObject) jsonCounselorState;

              strCounselorId = jsonObjCounselorState.get("EMPLOYEE").toString();

              if (assignedCounselor.toString().equals(strCounselorId)) {

                JSONObject jsonObjCounselorStateData = (JSONObject) jsonObjCounselorState.get("Data");
                strStateCode = jsonObjCounselorStateData.get("state").toString();
                strBlendKind = jsonObjCounselorStateData.get("blend_kind").toString();

                //203(휴식), 204(대기), 205(처리), 206(후처리)
                if ( (strStateCode.equals("203") || strStateCode.equals("204") ||
                  strStateCode.equals("205") || strStateCode.equals("206") ) && 
              		  !"1".equals(strBlendKind) ) {

                  try {
                    mapCounselorState = new ObjectMapper().readValue(jsonObjCounselorStateData.toString(), Map.class);
                  } catch (JsonParseException e) {
                    e.printStackTrace();
                  } catch (JsonMappingException e) {
                    e.printStackTrace();
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                  mapCounselorStatusList.add(mapCounselorState);
                  break;
                }
              }
            }
          }
        }       

      }else{
        Map<Object, Object> redisTenantList = hashOperations.entries("master.tenant-"+_centerId);

        //WebClient로 API서버와 연결
        WebClient webClient =
          WebClient
            .builder()
            .baseUrl(baseUrl)
            .defaultHeaders(httpHeaders -> {
              httpHeaders.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
              httpHeaders.add("Session-Key", requestBody.getSessionKey());
            })
            .build();

        //API 호출 시 Request 개체 자료구조
        Map<String, Object> bodyMap = new HashMap<>();
        Map<String, Object> filterMap = new HashMap<>();

        int[] arrCampaignId = null;

        List<Object> assignedCounselorList = new ArrayList<>();

        //사이드 바(캠페인)에서 캠페인 노드에서 상담원 상태 모니터를 호출했을 때
        if (!requestBody.getCampaignId().equals("0")) {
          arrCampaignId = new int[1];
          arrCampaignId[0] = Integer.parseInt(requestBody.getCampaignId());

          filterMap.put("campaign_id", arrCampaignId);
          bodyMap.put("filter", filterMap);

          try {
            //캠페인에 할당된 상담원 가져오기 API 요청
            Map<String, Object> apiAssignedCounselor =
              webClient
                .post()
                .uri(uriBuilder ->
                  uriBuilder
                    .path("/pds/collections/campaign-agent")
                    .build()
                )
                .bodyValue(bodyMap)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    
            //해당 캠페인에 할당된 상담원ID 가져오기 API 요청이 실패했을 때
            if (!apiAssignedCounselor.get("result_code").equals(0)) {
              String resultCode = "";
              String resultMessage = "";
              if (apiAssignedCounselor.get("result_code") != null) {
                resultCode = apiAssignedCounselor.get("result_code").toString();
              }
              if (apiAssignedCounselor.get("result_message") != null) {
                resultMessage = apiAssignedCounselor.get("result_message").toString();
              }
              ResponseDto result = new ResponseDto(resultCode, resultMessage);
              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
    
            //캠페인에 할당된 상담원이 존재하면
            if (apiAssignedCounselor.get("result_data") != null) {          
              List<Map<String, Object>> mapAssignedCounselorList = (List<Map<String, Object>>) apiAssignedCounselor.get("result_data");
            
              for (Map<String, Object> mapAssignedCounselor : mapAssignedCounselorList) {
                assignedCounselorList.addAll((List<Object>) mapAssignedCounselor.get("agent_id"));
              }
            }
            // log.info(">>>할당상담원: {}", assignedCounselorList.toString());
            
          } catch (Exception e) {
            e.printStackTrace();
          }
    
        } else {
          int i = 0;
          List<Object> campaignList = new ArrayList<>();

          if (requestBody.getTenantId().equals("A")) {
            arrTenantId = new int[redisTenantList.size()];

            for (Object tenantKey : redisTenantList.keySet()) {
              arrTenantId[i] = Integer.parseInt(tenantKey.toString());
              i++;
            }

            filterMap.put("tenant_id", arrTenantId);
            bodyMap.put("filter", filterMap);

            //센터 노드에서 호출했으면 전체 캠페인을 테넌트 노드에서 호출했으면 해당 테넌트의 캠페인을 가져오기 API 요청
            Map<String, Object> apiCampaign =
              webClient
                .post()
                .uri(uriBuilder ->
                  uriBuilder
                    .path("/pds/collections/campaign-list")
                    .build()
                )
                .bodyValue(bodyMap)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            //캠페인 가져오기 API 요청이 실패했을 때
            if (!apiCampaign.get("result_code").equals(0)) {
              String resultCode = "";
              String resultMessage = "";
              if (apiCampaign.get("result_code") != null) {
                resultCode = apiCampaign.get("result_code").toString();
              }
              if (apiCampaign.get("result_message") != null) {
                resultMessage = apiCampaign.get("result_message").toString();
              }
              ResponseDto result = new ResponseDto(resultCode, resultMessage);
              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            List<Map<String, Object>> mapCampaignList = (List<Map<String, Object>>) apiCampaign.get("result_data");
            for (Map<String, Object> mapCampaign : mapCampaignList) {
              campaignList.add((Object) mapCampaign.get("campaign_id"));
            }

          } else {

            if (!requestBody.getTenantId().chars().allMatch(Character::isDigit)) {
              return PostCounselorStatusListResponseDto.notExistCampaignId();        
            }
            
            arrTenantId = new int[1];
            arrTenantId[0] = Integer.parseInt(requestBody.getTenantId());
            filterMap.put("tenant_id", arrTenantId);
    
            bodyMap.put("filter", filterMap);

            //센터 노드에서 호출했으면 전체 캠페인을 테넌트 노드에서 호출했으면 해당 테넌트의 캠페인을 가져오기 API 요청
            Map<String, Object> apiCampaign =
              webClient
                .post()
                .uri(uriBuilder ->
                  uriBuilder
                    .path("/pds/collections/campaign-list")
                    .build()
                )
                .bodyValue(bodyMap)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            //캠페인 가져오기 API 요청이 실패했을 때
            if (!apiCampaign.get("result_code").equals(0)) {
              String resultCode = "";
              String resultMessage = "";
              if (apiCampaign.get("result_code") != null) {
                resultCode = apiCampaign.get("result_code").toString();
              }
              if (apiCampaign.get("result_message") != null) {
                resultMessage = apiCampaign.get("result_message").toString();
              }
              ResponseDto result = new ResponseDto(resultCode, resultMessage);
              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

            List<Map<String, Object>> mapCampaignList = (List<Map<String, Object>>) apiCampaign.get("result_data");

            // log.info(">>>소속 캠페인: {}", mapCampaignList.toString());

            for (Map<String, Object> mapCampaign : mapCampaignList) {
              // log.info(">>>캠페인 ID: {}", mapCampaign.get("campaign_id").toString());
              campaignList.add((Object) mapCampaign.get("campaign_id"));
            }

          }

          // log.info(">>>소속 캠페인: {}", campaignList.toString());

          arrCampaignId = new int[campaignList.size()];
          int j = 0;

          //가져온 캠페인의 할당 상담원 가져오기
          for (Object campaign : campaignList) {
            arrCampaignId[j] = (int) campaign;
            j++;
          }

          filterMap.put("campaign_id", arrCampaignId);
          bodyMap.put("filter", filterMap);

          //캠페인에 할당된 상담원 가져오기 API 요청
          Map<String, Object> apiAssignedCounselor =
            webClient
              .post()
              .uri(uriBuilder ->
                uriBuilder
                  .path("/pds/collections/campaign-agent")
                  .build()
              )
              .bodyValue(bodyMap)
              .retrieve()
              .bodyToMono(Map.class)
              .block();

          //해당 캠페인에 할당된 상담원ID 가져오기 API 요청이 실패했을 때
          if (!apiAssignedCounselor.get("result_code").equals(0)) {
            String resultCode = "";
            String resultMessage = "";
            if (apiAssignedCounselor.get("result_code") != null) {
              resultCode = apiAssignedCounselor.get("result_code").toString();
            }
            if (apiAssignedCounselor.get("result_message") != null) {
              resultMessage = apiAssignedCounselor.get("result_message").toString();
            }
            ResponseDto result = new ResponseDto(resultCode, resultMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
          }

          //캠페인에 할당된 상담원이 존재하면
          if (apiAssignedCounselor.get("result_data") != null) {            
            List<Map<String, Object>> mapAssignedCounselorList = (List<Map<String, Object>>) apiAssignedCounselor.get("result_data");

            //할당된 상담원 리스트에 누적 추가
            for (Map<String, Object> mapAssignedCounselor : mapAssignedCounselorList) {
              assignedCounselorList.addAll((List<Object>) mapAssignedCounselor.get("agent_id"));
            }
          }

        }

        //수집된 할당된 상담사ID 중복제거
        List<Object> assignedCounselorDuplicatesRemovedList = assignedCounselorList.stream().distinct().collect(Collectors.toList());

        // log.info(">>>중복 제거 할당 상담사: {}", assignedCounselorDuplicatesRemovedList.toString());

        for (Object assignedCounselor : assignedCounselorDuplicatesRemovedList){

          for (Object tenantKey : redisTenantList.keySet()) {

            redisKey = "st.employee.state-"+_centerId+"-" + tenantKey;
            redisCounselorStatusList = hashOperations.entries(redisKey);

            arrJsonCounselorState = (JSONArray) jsonParser.parse(redisCounselorStatusList.values().toString());

            for (Object jsonCounselorState : arrJsonCounselorState) {

              JSONObject jsonObjCounselorState = (JSONObject) jsonCounselorState;

              strCounselorId = jsonObjCounselorState.get("EMPLOYEE").toString();

              if (assignedCounselor.toString().equals(strCounselorId)) {

                JSONObject jsonObjCounselorStateData = (JSONObject) jsonObjCounselorState.get("Data");
                strStateCode = jsonObjCounselorStateData.get("state").toString();
                strBlendKind = jsonObjCounselorStateData.get("blend_kind").toString();

                //203(휴식), 204(대기), 205(처리), 206(후처리)
                if ( (strStateCode.equals("203") || strStateCode.equals("204") ||
                  strStateCode.equals("205") || strStateCode.equals("206") ) && 
              		  !"1".equals(strBlendKind) ) {

                  try {
                    mapCounselorState = new ObjectMapper().readValue(jsonObjCounselorStateData.toString(), Map.class);
                  } catch (JsonParseException e) {
                    e.printStackTrace();
                  } catch (JsonMappingException e) {
                    e.printStackTrace();
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                  mapCounselorStatusList.add(mapCounselorState);
                  break;
                }
              }
            }
          }
        }
        
      }

    } catch (Exception e) {
      e.printStackTrace();
      ResponseDto.databaseError();
    }
    return PostCounselorStatusListResponseDto.success(mapCounselorStatusList);
  }

  /*
   *  캠페인 할당 상담사정보 가져오기
   *  
   *  @param PostCounselorListRequestDto requestBody    전달 매개변수 개체 DTO
   *  @return ResponseEntity<? super GetCounselorInfoListResponseDto>
   */
  @Override
  public ResponseEntity<? super GetCounselorInfoListResponseDto> getCounselorInfoList(
    PostCounselorListRequestDto requestBody
  ) {

    List<Map<String, Object>> mapCounselorInfoList = new ArrayList<Map<String, Object>>();

    try {

      //테넌트ID 값 없이 호출하였을 때
      if (requestBody.getTenantId() == null || requestBody.getTenantId().trim().isEmpty()) {
        return GetCounselorInfoListResponseDto.notExistTenantId();        
      }

      //캠페인ID 값 없이 호출하였을 때
      if (requestBody.getCampaignId() == null || requestBody.getCampaignId().trim().isEmpty()) {
        return GetCounselorInfoListResponseDto.notExistCampaignId();        
      }

      //API 인증 세션키가 없이 호출하였을 때
      if (requestBody.getSessionKey() == null || requestBody.getSessionKey().trim().isEmpty()) {
        return GetCounselorInfoListResponseDto.notExistSessionKey();        
      }

      String redisKey = "";
      HashOperations<String, Object, Object> hashOperations = redisTemplate1.opsForHash();
      Map<Object, Object> redisCounselorList = new HashMap<>();
      JSONArray arrJson = new JSONArray();      
      JSONParser jsonParser = new JSONParser();
      TypeReference<Map<String, Object>> mapType = new TypeReference<Map<String, Object>>() {};

      String _centerId = requestBody.getCenterId();
      if (requestBody.getCenterId() == null || requestBody.getCenterId().trim().isEmpty()) {
        _centerId = "1";       
      }

      String[] agentIds = requestBody.getAgentIds();
      if( agentIds != null && agentIds.length > 0){

        redisKey = "master.employee-"+_centerId+"-" + requestBody.getTenantId();
        redisCounselorList = hashOperations.entries(redisKey);
  
        arrJson = (JSONArray) jsonParser.parse(redisCounselorList.values().toString());
        Map<String, Object> mapItem = null;
  
        for (String mapAssignedCounselor : agentIds){
          for (Object jsonItem : arrJson) {
            JSONObject jsonObj = (JSONObject) jsonItem;
  
            //해당 테넌트에 소속된 상담사 중 대상 상담사의 ID와 동일하면
            if (mapAssignedCounselor.equals(jsonObj.get("EMPLOYEE"))) {
              JSONObject jsonObjData = (JSONObject) jsonObj.get("Data");
  
              try {
                //mapItem = new ObjectMapper().readValue(jsonObjData.toString(), Map.class);
                mapItem = new ObjectMapper().readValue(jsonObjData.toString(), mapType);
              } catch (JsonMappingException e) {
                throw new RuntimeException(e);
              }
              mapCounselorInfoList.add(mapItem);
              break;
            }
          }
        }
      }
      
    } catch (Exception e) {
      e.printStackTrace();
      ResponseDto.databaseError();
    }

    return GetCounselorInfoListResponseDto.success(redisTemplate1, mapCounselorInfoList);
  }

  /*
   *  스킬 할당 상담사정보 가져오기
   *  
   *  @param PostSkillAssignedCounselorListRequestDto requestBody    전달 매개변수 개체 DTO
   *  @return ResponseEntity<? super PostSkillAssignedCounselorListResponseDto>
   */
  @Override
  @SuppressWarnings("unchecked")
  public ResponseEntity<? super PostSkillAssignedCounselorListResponseDto> getSillAssignedCounselorList(
    PostSkillAssignedCounselorListRequestDto requestBody
  ) {
    List<Map<String, Object>> mapSkillAssignedCounselorList = new ArrayList<Map<String, Object>>();

    try {

      //테넌트ID 값 없이 호출하였을 때
      if (requestBody.getTenantId() == null || requestBody.getTenantId().trim().isEmpty()) {
        return PostSkillAssignedCounselorListResponseDto.notExistTenantId();        
      }

      //스킬ID 값 없이 호출하였을 때
      if (requestBody.getSkillId() == null) {
        return PostSkillAssignedCounselorListResponseDto.notExistSkillId();        
      }

      //API 인증 세션키가 없이 호출하였을 때
      if (requestBody.getSessionKey() == null || requestBody.getSessionKey().trim().isEmpty()) {
        return PostSkillAssignedCounselorListResponseDto.notExistSessionKey();        
      }

      String _centerId = requestBody.getCenterId();
      if (requestBody.getCenterId() == null || requestBody.getCenterId().trim().isEmpty()) {
        _centerId = "1";       
      }
      
      //WebClient로 API서버와 연결
      WebClient webClient =
        WebClient
          .builder()
          .baseUrl(baseUrl)
          .defaultHeaders(httpHeaders -> {
            httpHeaders.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            httpHeaders.add("Session-Key", requestBody.getSessionKey());
          })
          .build();

      //API 호출 시 Request 개체 자료구조
      Map<String, Object> bodyMap = new HashMap<>();
      Map<String, Object> filterMap = new HashMap<>();

      Map<String, Object> skillMap = new HashMap<>();
      skillMap.put("start", requestBody.getSkillId());
      skillMap.put("end", requestBody.getSkillId());
          
      filterMap.put("skill_id", skillMap);
      bodyMap.put("filter", filterMap);

      List<Object> skillAssignedCounselorList = new ArrayList<>();

      //스킬에 할당된 상담원 가져오기 API 요청
      Map<String, Object> apiSkillAssignedCounselor =
        webClient
          .post()
          .uri(uriBuilder ->
            uriBuilder
              .path("/pds/collections/skill-agent")
              .build()
          )
          .bodyValue(bodyMap)
          .retrieve()
          .bodyToMono(Map.class)
          .block();

      //해당 스킬에 할당된 상담원ID 가져오기 API 요청이 실패했을 때
      if (!apiSkillAssignedCounselor.get("result_code").equals(0)) {
        ResponseDto result = new ResponseDto(apiSkillAssignedCounselor.get("result_code").toString(), apiSkillAssignedCounselor.get("result_message").toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
      }

      String redisKey = "";
      HashOperations<String, Object, Object> hashOperations = redisTemplate1.opsForHash();
      Map<Object, Object> redisCounselorList = new HashMap<>();
      JSONArray arrJson = new JSONArray();      
      JSONParser jsonParser = new JSONParser();

      //스킬에 할당된 상담원이 존재하면
      if (apiSkillAssignedCounselor.get("result_data") != null) {
        List<Map<String, Object>> mapAssignedCounselorList = (List<Map<String, Object>>) apiSkillAssignedCounselor.get("result_data");

        for (Map<String, Object> mapAssignedCounselor : mapAssignedCounselorList) {
          skillAssignedCounselorList.addAll((List<Object>) mapAssignedCounselor.get("agent_id"));
        }

        //수집된 할당된 상담사ID 중복제거
        List<Object> assignedCounselorDuplicatesRemovedList = skillAssignedCounselorList.stream().distinct().collect(Collectors.toList());

        redisKey = "master.employee-"+_centerId+"-" + requestBody.getTenantId();
        redisCounselorList = hashOperations.entries(redisKey);
  
        arrJson = (JSONArray) jsonParser.parse(redisCounselorList.values().toString());
        Map<String, Object> mapItem = null;
        TypeReference<Map<String, Object>> mapType = new TypeReference<Map<String, Object>>() {};
  
        for (Object mapSkillAssignedCounselor : assignedCounselorDuplicatesRemovedList){
          for (Object jsonItem : arrJson) {
            JSONObject jsonObj = (JSONObject) jsonItem;
  
            //해당 테넌트에 소속된 상담사 중 대상 상담사의 ID와 동일하면
            if (mapSkillAssignedCounselor.equals(jsonObj.get("EMPLOYEE"))) {
              JSONObject jsonObjData = (JSONObject) jsonObj.get("Data");
  
              try {
                // mapItem = new ObjectMapper().readValue(jsonObjData.toString(), Map.class);
                mapItem = new ObjectMapper().readValue(jsonObjData.toString(), mapType);
              } catch (JsonMappingException e) {
                throw new RuntimeException(e);
              }
              mapSkillAssignedCounselorList.add(mapItem);
              break;
            }
          }
        }
      }
      
    } catch (Exception e) {
      e.printStackTrace();
      ResponseDto.databaseError();
    }

    return PostSkillAssignedCounselorListResponseDto.success(redisTemplate1, mapSkillAssignedCounselorList);
  }

}
