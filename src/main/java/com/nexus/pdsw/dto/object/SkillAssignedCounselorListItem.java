/*------------------------------------------------------------------------------
 * NAME : SkillAssignedCounselorListItem.java
 * DESC : 스킬 할당된 상담사 리스트 반환 항목 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/02/20  최상원                       초기작성
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
public class SkillAssignedCounselorListItem {
  
  private String affiliationGroupId;    //상담사 소속그룹ID
  private String affiliationGroupName;  //상담사 소속그룹명
  private String affiliationTeamId;     //상담사 소속팀ID
  private String affiliationTeamName;   //상담사 소속팀명
  private String counselorEmplNum;      //상담사 사번
  private String counselorId;           //상담사 ID
  private String counselorname;         //상담사 이름
  private String blendKind;             //블렌드 구분(인바운드, 아웃바운드, 블렌드)

  /*
   *  스킬킬 할당된 상담사 리스트 반환 DTO(생성자)
   * 
   *  @param RedisTemplate<String, Object> redisTemplate1   레디스 개체
   *  @param Map<String, Object> mapSkillAssignedCounselor  반환할 상담사 정보
  */
  @SuppressWarnings("unchecked")
  private SkillAssignedCounselorListItem(
    RedisTemplate<String, Object> redisTemplate1,
    Map<String, Object> mapSkillAssignedCounselor
  ) {

    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Object> mapCounselorAffiliation = objectMapper.convertValue(mapSkillAssignedCounselor.get("employee_group_id"), Map.class);

    HashOperations<String, Object, Object> hashOperations = redisTemplate1.opsForHash();

    JSONParser jsonParser = new JSONParser();

    String redisKey = "master.group-emp-" + mapSkillAssignedCounselor.get("center_id") + "-" + mapSkillAssignedCounselor.get("tenant_id");
    //그룹정보
    Map<Object, Object> redisGroup = hashOperations.entries(redisKey);

    redisKey = "master.team-emp-" + mapSkillAssignedCounselor.get("center_id") + "-" + mapSkillAssignedCounselor.get("tenant_id");
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

    this.counselorEmplNum = (String) mapSkillAssignedCounselor.get("id");
    this.counselorId = (String) mapSkillAssignedCounselor.get("media_login_id");
    this.counselorname = (String) mapSkillAssignedCounselor.get("name");
    this.blendKind = (String) mapSkillAssignedCounselor.get("blend_kind");

  }

  /*
   *  스킬 할당된 상담사 리스트 반환하기
	 * 
   *  @param RedisTemplate<String, Object> redisTemplate1             레디스 개체
   *  @param List<Map<String, Object>> mapSkillAssignedCounselorList 반환할 상담사 리스트
	 *  @return List<SkillAssignedCounselorListItem>
	*/
  public static List<SkillAssignedCounselorListItem> getSkillAssignedCounselorList(
    RedisTemplate<String, Object> redisTemplate1,
    List<Map<String, Object>> mapSkillAssignedCounselorList
  ) {
    
    List<SkillAssignedCounselorListItem> skillAssignedCounselorList = new ArrayList<>();

    for (Map<String, Object> mapSkillAssignedCounselor : mapSkillAssignedCounselorList) {
      SkillAssignedCounselorListItem counselorInfo = new SkillAssignedCounselorListItem(redisTemplate1, mapSkillAssignedCounselor); 
      skillAssignedCounselorList.add(counselorInfo);
    }

    return skillAssignedCounselorList;

  }
}
