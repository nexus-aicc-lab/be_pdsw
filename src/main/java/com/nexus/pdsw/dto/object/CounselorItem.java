/*------------------------------------------------------------------------------
 * NAME : CounselorItem.java
 * DESC : 상담사리스트 항목 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/01/15  최상원                       초기작성
 * 2025/01/21  최상원                       센터명, 테넌트명, 그룹명, 팀명 추가
 * 2025/05/29  개선                         스킬 조회 최적화 (일괄 처리)
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.dto.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CounselorItem {
  private String counselorId;                                   //상담사 ID
  private String counselorLoginId;                              //상담사 로그인 ID
  private String counselorname;                                 //상담사 이름
  private String blendKind;                                     //블랜딩 종류(1: 인바운드, 2: 아웃바운드, 3: 블랜드)
  @Setter
  private List<AssignedSkillItem> assignedSkills;               //상담사 보유 스킬 리스트

  /*
   *  상담사 리스트 반환 DTO 생성자 (스킬 정보 없이)
   *
   *  @param JSONObject objCounselorData  상담사 정보
   */
  private CounselorItem(JSONObject objCounselorData) {
    this.counselorId = (String) objCounselorData.get("id");
    this.counselorLoginId = (String) objCounselorData.get("media_login_id");
    this.counselorname = (String) objCounselorData.get("name");
    this.blendKind = (String) objCounselorData.get("blend_kind");
    this.assignedSkills = new ArrayList<>(); // 초기에는 빈 리스트
  }

  /*
   *  기존 생성자 (하위 호환성 유지)
   *
   *  @param String baseUrl               기본 URL
   *  @param String sessionKey            세션 키
   *  @param JSONObject objCounselorData  상담사 정보
   */
  private CounselorItem(
          String baseUrl,
          String sessionKey,
          JSONObject objCounselorData
  ) {
    this.counselorId = (String) objCounselorData.get("id");
    this.counselorLoginId = (String) objCounselorData.get("media_login_id");
    this.counselorname = (String) objCounselorData.get("name");
    this.blendKind = (String) objCounselorData.get("blend_kind");
    this.assignedSkills = AssignedSkillItem.getAssignedSkillList(baseUrl, sessionKey, (String) objCounselorData.get("id"));
  }

  /*
   *  상담사 리스트 반환 DTO로 변환하기 (최적화 버전)
   *
   *  @param RedisTemplate<String, Object> redisTemplate1   레디스 개체
   *  @param String baseUrl                                 기본 URL
   *  @param String sessionKey                              세션 키
   *  @param String centerId                                센터ID
   *  @param String tenantId                                테넌트ID
   *  @param String groupId                                 그룹ID
   *  @param String teamId                                  팀ID
   *  @return List<CounselorItem>
   */
  @SuppressWarnings("unchecked")
  public static List<CounselorItem> getCounselorList(
          RedisTemplate<String, Object> redisTemplate1,
          String baseUrl,
          String sessionKey,
          String centerId,
          String tenantId,
          String groupId,
          String teamId
  ) {

    List<CounselorItem> counselorList = new ArrayList<>();

    try {
      log.info("상담사 리스트 조회 시작 - 센터: {}, 테넌트: {}, 그룹: {}, 팀: {}",
              centerId, tenantId, groupId, teamId);

      // === 1단계: Redis에서 상담원 기본 정보 조회 ===
      String redisKey = "master.employee-" + centerId + "-" + tenantId;
      HashOperations<String, Object, Object> hashOperations = redisTemplate1.opsForHash();
      JSONParser jsonParser = new JSONParser();
      ScanOptions scanOptions = ScanOptions.scanOptions().match( groupId + "-" + teamId + "*" ).build();

      Cursor<Map.Entry<Object, Object>> redisCounselor = hashOperations.scan(redisKey, scanOptions);

      while ( redisCounselor.hasNext() ) {
        Map.Entry<Object, Object> next = redisCounselor.next();

        try {
          Map<String, Object> mapCounselor = (Map<String, Object>) jsonParser.parse(next.getValue().toString());
          JSONObject objCounselorData = (JSONObject) mapCounselor.get("Data");

          // only_work가 call인 상담원만 필터링
          if (objCounselorData != null &&
                  !objCounselorData.get("media_login_id").equals("NULL") &&
                  (objCounselorData.get("only_work").equals("call") || objCounselorData.get("only_work").equals("callbot"))) {

            CounselorItem counselorInfo = new CounselorItem(objCounselorData);
            counselorList.add(counselorInfo);
          }

        } catch (ParseException e) {
          log.error("Redis 데이터 파싱 중 오류 발생", e);
          continue;
        }
      }

      log.info("기본 상담사 정보 조회 완료: {} 명", counselorList.size());

      // === 2단계: 스킬 정보 일괄 조회 및 매핑 ===
      if (!counselorList.isEmpty()) {
        List<String> counselorIds = counselorList.stream()
                .map(CounselorItem::getCounselorId)
                .collect(Collectors.toList());

        log.info("스킬 정보 일괄 조회 시작: {} 명의 상담사", counselorIds.size());

        Map<String, List<AssignedSkillItem>> skillMap =
                AssignedSkillItem.getBulkAssignedSkillList(baseUrl, sessionKey, counselorIds);

        // 각 상담원에 스킬 정보 설정
        for (CounselorItem counselor : counselorList) {
          List<AssignedSkillItem> skills = skillMap.getOrDefault(counselor.getCounselorId(), new ArrayList<>());
          counselor.setAssignedSkills(skills);
        }

        // 스킬 할당 통계
        int counselorsWithSkills = (int) counselorList.stream()
                .mapToLong(c -> c.getAssignedSkills().size())
                .filter(count -> count > 0)
                .count();

        int totalSkills = counselorList.stream()
                .mapToInt(c -> c.getAssignedSkills().size())
                .sum();

        log.info("스킬 매핑 완료 - 스킬 보유 상담사: {} 명, 총 스킬: {} 개",
                counselorsWithSkills, totalSkills);
      }

      log.info("상담사 리스트 조회 최종 완료: {} 명", counselorList.size());

    } catch (Exception e) {
      log.error("상담사 리스트 조회 중 전체 오류 발생", e);
    }

    return counselorList;
  }

  /*
   *  기존 방식 상담사 리스트 조회 (하위 호환성 유지)
   *
   *  @param RedisTemplate<String, Object> redisTemplate1   레디스 개체
   *  @param String baseUrl                                 기본 URL
   *  @param String sessionKey                              세션 키
   *  @param String centerId                                센터ID
   *  @param String tenantId                                테넌트ID
   *  @param String groupId                                 그룹ID
   *  @param String teamId                                  팀ID
   *  @return List<CounselorItem>
   */
  @SuppressWarnings("unchecked")
  public static List<CounselorItem> getCounselorListLegacy(
          RedisTemplate<String, Object> redisTemplate1,
          String baseUrl,
          String sessionKey,
          String centerId,
          String tenantId,
          String groupId,
          String teamId
  ) {

    List<CounselorItem> counselorList = new ArrayList<>();

    try {
      String redisKey = "master.employee-" + centerId + "-" + tenantId;
      HashOperations<String, Object, Object> hashOperations = redisTemplate1.opsForHash();
      JSONParser jsonParser = new JSONParser();
      ScanOptions scanOptions = ScanOptions.scanOptions().match( groupId + "-" + teamId + "*" ).build();

      Cursor<Map.Entry<Object, Object>> redisCounselor = hashOperations.scan(redisKey, scanOptions);
      while ( redisCounselor.hasNext() ) {
        Map.Entry<Object, Object> next = redisCounselor.next();

        try {
          Map<String, Object> mapCounselor = (Map<String, Object>) jsonParser.parse(next.getValue().toString());
          JSONObject objCounselorData = (JSONObject) mapCounselor.get("Data");

          if (objCounselorData != null &&
                  !objCounselorData.get("media_login_id").equals("NULL") &&
                  (objCounselorData.get("only_work").equals("call") || objCounselorData.get("only_work").equals("callbot"))) {

            CounselorItem counselorInfo = new CounselorItem(baseUrl, sessionKey, objCounselorData);
            counselorList.add(counselorInfo);
          }

        } catch (ParseException e) {
          log.error("Redis 데이터 파싱 중 오류 발생", e);
          continue;
        }
      }

      log.info("상담사 리스트 조회 완료 (기존 방식): {} 명", counselorList.size());

    } catch (Exception e) {
      log.error("상담사 리스트 조회 중 전체 오류 발생", e);
    }

    return counselorList;
  }
}