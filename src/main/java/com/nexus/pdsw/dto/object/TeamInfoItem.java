/*------------------------------------------------------------------------------
 * NAME : TeamInfoItem.java
 * DESC : 상담사 팀조직 개체 DTO
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamInfoItem {
  
  private String teamId;          //상담사 팀ID
  private String teamName;        //상담사 팀명
  private List<CounselorItem> counselorInfo;

  /*
   *  상담사 팀 조직 리스트 반환 DTO 생성자
   * 
   *  @param RedisTemplate<String, Object> redisTemplate1   레디스 개체
   *  @param String baseUrl                                 기본 URL
   *  @param String sessionKey                              세션 키
   *  @param String centerId                                센터ID
   *  @param String tenantId                                테넌트ID
   *  @param String groupId                                 그룹ID
   *  @param Map<String, Object> mapTeam                    팀정보
  */
  private TeamInfoItem(
    RedisTemplate<String, Object> redisTemplate1,
    String baseUrl,
    String sessionKey,
    String centerId,
    String tenantId,
    String groupId,
    Map<String, Object> mapTeam
  ) {
    
    JSONObject objTeamData = (JSONObject) mapTeam.get("Data");

    this.teamId = objTeamData.get("id").toString();
    this.teamName = objTeamData.get("name").toString();

    this.counselorInfo = CounselorItem.getCounselorList(redisTemplate1, baseUrl, sessionKey,centerId, tenantId, groupId, objTeamData.get("id").toString());
  }

  /*
   *  상담사 팀 조직 리스트 반환 DTO로 변환하기
	 * 
   *  @param RedisTemplate<String, Object> redisTemplate1   레디스 개체
   *  @param String baseUrl                                 기본 URL
   *  @param String sessionKey                              세션 키
   *  @param String centerId                                센터ID
   *  @param String tenantId                                테넌트ID
   *  @param String groupId                                 그룹ID
	 *  @return List<TeamInfoItem>
	*/
  @SuppressWarnings("unchecked")
  public static List<TeamInfoItem> getTeamList(
    RedisTemplate<String, Object> redisTemplate1,
    String baseUrl,
    String sessionKey,
    String centerId,
    String tenantId,
    String groupId
  ) {
    
    List<TeamInfoItem> teamList = new ArrayList<>();

    String redisKey = "master.team-emp-" + centerId + "-" + tenantId;

    HashOperations<String, Object, Object> hashOperations = redisTemplate1.opsForHash();

    JSONParser jsonParser = new JSONParser();

    Map<String, Object> mapTeam = new HashMap<>();
    
    ScanOptions scanOptions = ScanOptions.scanOptions().match( groupId + "*" ).build();

    //팀정보
    Cursor<Map.Entry<Object, Object>> redisTeam = hashOperations.scan(redisKey, scanOptions);
    while ( redisTeam.hasNext() ) {
      Map.Entry<Object, Object> next = redisTeam.next();

      try {
        mapTeam = (Map<String, Object>) jsonParser.parse(next.getValue().toString());
      } catch (ParseException e) {
        e.printStackTrace();
      }

      TeamInfoItem teamInfo = new TeamInfoItem(redisTemplate1, baseUrl, sessionKey, centerId, tenantId, groupId, mapTeam); 
      teamList.add(teamInfo);
    }

    return teamList;
  }
}
