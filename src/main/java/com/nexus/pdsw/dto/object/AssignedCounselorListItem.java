/*------------------------------------------------------------------------------
 * NAME : AssignedCounselorListItem.java
 * DESC : 캠페인 별 할당된 상담사 리스트 항목 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/02/04  최상원                       초기작성
 * 2025/05/26  최상원                       그룹/팀 명 추가
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

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssignedCounselorListItem {
  
  private String affiliationGroupId;    //상담사 소속그룹ID
  private String affiliationGroupName;  //상담사 소속그룹명
  private String affiliationTeamId;     //상담사 소속팀ID
  private String affiliationTeamName;   //상담사 소속팀명
  private String counselorEmplNum;      //상담사 사번
  private String counselorId;           //상담사 ID
  private String counselorname;         //상담사 이름

  /*
   *  캠페인 별 할당된 상담사 리스트 반환 DTO(생성자)
   * 
   *  @param RedisTemplate<String, Object> redisTemplate1   레디스 개체
   *  @param Map<String, Object> mapCounselorInfo 반환할 상담사 정보
  */
  @SuppressWarnings("unchecked")
  private AssignedCounselorListItem(
    RedisTemplate<String, Object> redisTemplate1,
    Map<String, Object> mapCounselorInfo
  ) {

    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Object> mapCounselorAffiliation = objectMapper.convertValue(mapCounselorInfo.get("employee_group_id"), Map.class);

    HashOperations<String, Object, Object> hashOperations = redisTemplate1.opsForHash();

    JSONParser jsonParser = new JSONParser();

    String redisKey = "master.group-emp-" + mapCounselorInfo.get("center_id") + "-" + mapCounselorInfo.get("tenant_id");
    //그룹정보
    Map<Object, Object> redisGroup = hashOperations.entries(redisKey);

    redisKey = "master.team-emp-" + mapCounselorInfo.get("center_id") + "-" + mapCounselorInfo.get("tenant_id");
    //팀정보
    Map<Object, Object> redisTeam = hashOperations.entries(redisKey);

    mapCounselorAffiliation.forEach((key, value) -> {
      if (key.equals("1")) {
        this.affiliationGroupId = (String) value;
        JSONObject affiliationGroup = new JSONObject();
        try {
          affiliationGroup = (JSONObject) jsonParser.parse(redisGroup.get((String) value).toString());
        } catch (ParseException e) {
          e.printStackTrace();
        }
        Map<String, String> mapGroup = (Map<String, String>) affiliationGroup.get("Data");
        this.affiliationGroupName = mapGroup.get("name");
      } else {
        this.affiliationTeamId = (String) value;
        JSONArray arrAffiliationTeam = new JSONArray();
        try {
          arrAffiliationTeam = (JSONArray) jsonParser.parse(redisTeam.values().toString());
        } catch (ParseException e) {
          e.printStackTrace();
        }
        for (Object affiliationTeam : arrAffiliationTeam) {
          JSONObject objTeam = (JSONObject) affiliationTeam;
          if (objTeam.get("TEAM").equals(value)) {
            JSONObject jsonObjTeam = (JSONObject) objTeam.get("Data");
            this.affiliationTeamName = jsonObjTeam.get("name").toString();
          }
        }
      }
    });

    this.counselorEmplNum = (String) mapCounselorInfo.get("id");
    this.counselorId = (String) mapCounselorInfo.get("media_login_id");
    this.counselorname = (String) mapCounselorInfo.get("name");

  }

  /*
   *  캠페인 별 할당된 상담사 리스트 반환하기
	 * 
   *  @param RedisTemplate<String, Object> redisTemplate1   레디스 개체
   *  @param List<Map<String, Object>> mapCounselorInfoList 반환할 상담사 리스트
	 *  @return List<AssignedCounselorListItem>
	*/
  public static List<AssignedCounselorListItem> getAssignedCounselorList(
    RedisTemplate<String, Object> redisTemplate1,
    List<Map<String, Object>> mapCounselorInfoList
  ) {
    
    List<AssignedCounselorListItem> counselorList = new ArrayList<>();

    for (Map<String, Object> mapCounselorInfo : mapCounselorInfoList) {
      AssignedCounselorListItem counselorInfo = new AssignedCounselorListItem(redisTemplate1, mapCounselorInfo); 
      counselorList.add(counselorInfo);
    }

    return counselorList;

  }

}
