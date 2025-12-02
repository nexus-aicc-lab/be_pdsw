/*------------------------------------------------------------------------------
 * NAME : TenantInfoItem.java
 * DESC : 상담사 테넌트 개체 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/02/18  최상원                       초기작성
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.dto.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TenantInfoItem {
  
  private String tenantId;                //테넌트 ID
  private String tenantName;              //테넌트 명
  private List<GroupInfoItem> groupInfo;  //상담사 그룹정보

  /*
   *  상담사 테넌트 조직 리스트 반환 DTO 생성자
   * 
   *  @param RedisTemplate<String, Object> redisTemplate1   레디스 개체
   *  @param String baseUrl                                 기본 URL
   *  @param String sessionKey                              세션 키
   *  @param String centerId                                센터ID
   *  @param JSONObject jsonObjTenant                       테넌트 정보
  */
  private TenantInfoItem(
    RedisTemplate<String, Object> redisTemplate1,
    String baseUrl,
    String sessionKey,
    String centerId,
    JSONObject jsonObjTenant
  ) {

    JSONObject jsonObjTenantData = (JSONObject) jsonObjTenant.get("Data");

    this.tenantId = jsonObjTenantData.get("id").toString();
    this.tenantName = jsonObjTenantData.get("name").toString();

    this.groupInfo = GroupInfoItem.getGroupList(redisTemplate1, baseUrl, sessionKey, centerId, jsonObjTenantData.get("id").toString());

  }

  /*
   *  상담사 테넌트 조직 리스트 반환 DTO로 변환하기
   * 
   *  @param RedisTemplate<String, Object> redisTemplate1   레디스 개체
   *  @param String baseUrl                                 기본 URL
   *  @param String sessionKey                              세션 키
   *  @param String tenantId                                테넌트 ID
   *  @param String centerId                                센터ID
  */
  public static List<TenantInfoItem> getTenantList(
    RedisTemplate<String, Object> redisTemplate1,
    String baseUrl,
    String sessionKey,
    String tenantId,
    String centerId
  ) {
    
    List<TenantInfoItem> tanantList = new ArrayList<>();

    String redisKey = "master.tenant-" + centerId;

    HashOperations<String, Object, Object> hashOperations = redisTemplate1.opsForHash();

    JSONParser jsonParser = new JSONParser();

    //테넌트정보
    Map<Object, Object> redisTenant = hashOperations.entries(redisKey);

    JSONArray arrJsonTenant = new JSONArray();

    try {
      arrJsonTenant = (JSONArray) jsonParser.parse(redisTenant.values().toString());
    } catch (ParseException e) {
      e.printStackTrace();
    }

    for (Object jsonTenant : arrJsonTenant) {
      JSONObject jsonObjTenant = (JSONObject) jsonTenant;
      //로그인 상담사의 테넌트ID가 "0"이 아니고 해당 테넌트ID와 다를 경우 테넌트를 미포함한다.
      if (!tenantId.equals("0") && !jsonObjTenant.get("TENANT").equals(tenantId)) {
        continue;
      }
      TenantInfoItem tanantInfo = new TenantInfoItem(redisTemplate1, baseUrl, sessionKey, centerId, jsonObjTenant);
      tanantList.add(tanantInfo);
    }

    return tanantList;
  }
}
