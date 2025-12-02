/*------------------------------------------------------------------------------
 * NAME : AssignedSkillItem.java
 * DESC : 상담사 할당 스킬 리스트 항목 DTO
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/05/22  최상원                       초기작성
 * 2025/05/29  개선                         일괄 조회 최적화 버전
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.dto.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AssignedSkillItem {
  private String SkillId;
  private String SkillName;

  /*
   *  상담사에 할당된 스킬 리스트 반환 DTO 생성자 (Map 기반)
   *
   *  @param Map<String, Object> mapAssignedSkill 스킬 정보 Map
   */
  public AssignedSkillItem(Map<String, Object> mapAssignedSkill) {
    if (mapAssignedSkill != null) {
      this.SkillId = mapAssignedSkill.get("skill_id") != null ? mapAssignedSkill.get("skill_id").toString() : "";
      this.SkillName = mapAssignedSkill.get("skill_name") != null ? mapAssignedSkill.get("skill_name").toString() : "";
    } else {
      this.SkillId = "";
      this.SkillName = "";
    }
  }

  /*
   *  단일 상담사 스킬 조회 (기존 호환성 유지)
   *
   *  @param String baseUrl               기본 URL
   *  @param String sessionKey            세션 키
   *  @param String id                    상담사 ID
   *  @return List<AssignedSkillItem>
   */
  public static List<AssignedSkillItem> getAssignedSkillList(
          String baseUrl,
          String sessionKey,
          String id
  ) {
    List<String> counselorIds = new ArrayList<>();
    counselorIds.add(id);

    Map<String, List<AssignedSkillItem>> result = getBulkAssignedSkillList(baseUrl, sessionKey, counselorIds);
    return result.getOrDefault(id, new ArrayList<>());
  }

  /*
   *  다중 상담사 스킬 일괄 조회 (최적화 버전)
   *
   *  @param String baseUrl               기본 URL
   *  @param String sessionKey            세션 키
   *  @param List<String> counselorIds    상담사 ID 리스트
   *  @return Map<String, List<AssignedSkillItem>> 상담사ID별 스킬 리스트 맵
   */
  @SuppressWarnings("unchecked")
  public static Map<String, List<AssignedSkillItem>> getBulkAssignedSkillList(
          String baseUrl,
          String sessionKey,
          List<String> counselorIds
  ) {
    Map<String, List<AssignedSkillItem>> resultMap = new HashMap<>();

    if (counselorIds == null || counselorIds.isEmpty()) {
      return resultMap;
    }

    // 모든 상담사 ID를 빈 리스트로 초기화
    for (String counselorId : counselorIds) {
      resultMap.put(counselorId, new ArrayList<>());
    }

    WebClient webClient = WebClient
            .builder()
            .baseUrl(baseUrl)
            .defaultHeaders(httpHeaders -> {
              httpHeaders.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
              httpHeaders.add("Session-Key", sessionKey);
            })
            .build();

    try {
      // === 1단계: 모든 상담원의 스킬 ID 일괄 조회 ===
      Map<String, Object> bodyMap = new HashMap<>();
      Map<String, Object> filterMap = new HashMap<>();

      String[] arrAgentIds = counselorIds.toArray(new String[0]);
      filterMap.put("agent_id", arrAgentIds);
      bodyMap.put("filter", filterMap);

      log.info("상담사 스킬 일괄 조회 시작: {} 명", counselorIds.size());

      Map<String, Object> apiAssignedSkills = webClient
              .post()
              .uri(uriBuilder -> uriBuilder.path("/pds/collections/agent-skill").build())
              .bodyValue(bodyMap)
              .retrieve()
              .bodyToMono(Map.class)
              .block();

      // 상담사별 스킬 ID 매핑
      Map<String, List<Integer>> counselorSkillMap = new HashMap<>();

      if (apiAssignedSkills != null && apiAssignedSkills.get("result_data") != null) {
        List<Map<String, Object>> mapAssignedSkillsList = (List<Map<String, Object>>) apiAssignedSkills.get("result_data");

        for (Map<String, Object> mapAssignedSkill : mapAssignedSkillsList) {
          String agentId = mapAssignedSkill.get("agent_id") != null ? mapAssignedSkill.get("agent_id").toString() : null;
          Object skillIds = mapAssignedSkill.get("skill_id");

          if (agentId != null && skillIds instanceof List) {
            List<Integer> skills = ((List<Object>) skillIds).stream()
                    .filter(skill -> skill instanceof Integer)
                    .map(skill -> (Integer) skill)
                    .collect(Collectors.toList());

            counselorSkillMap.put(agentId, skills);
          }
        }
      }

      log.info("스킬 매핑 결과: {} 명의 상담사에 스킬 할당됨", counselorSkillMap.size());

      // === 2단계: 모든 스킬 ID 수집 및 스킬 정보 일괄 조회 ===
      List<Integer> allSkillIds = counselorSkillMap.values().stream()
              .flatMap(List::stream)
              .distinct()
              .collect(Collectors.toList());

      if (!allSkillIds.isEmpty()) {
        filterMap.clear();
        bodyMap.clear();

        int[] arrSkillIds = allSkillIds.stream().mapToInt(Integer::intValue).toArray();
        filterMap.put("skill_id", arrSkillIds);
        bodyMap.put("filter", filterMap);

        log.info("스킬 정보 일괄 조회: {} 개 스킬", allSkillIds.size());

        Map<String, Object> apiSkillInfo = webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/pds/collections/skill").build())
                .bodyValue(bodyMap)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // 스킬 ID별 스킬 정보 맵 생성
        Map<String, AssignedSkillItem> skillInfoMap = new HashMap<>();

        if (apiSkillInfo != null && apiSkillInfo.get("result_data") != null) {
          List<Map<String, Object>> mapSkillInfoList = (List<Map<String, Object>>) apiSkillInfo.get("result_data");

          for (Map<String, Object> mapSkillInfo : mapSkillInfoList) {
            String skillId = mapSkillInfo.get("skill_id") != null ? mapSkillInfo.get("skill_id").toString() : "";
            if (!skillId.isEmpty()) {
              skillInfoMap.put(skillId, new AssignedSkillItem(mapSkillInfo));
            }
          }
        }

        // === 3단계: 상담사별 스킬 정보 매핑 ===
        for (Map.Entry<String, List<Integer>> entry : counselorSkillMap.entrySet()) {
          String counselorId = entry.getKey();
          List<Integer> skillIds = entry.getValue();

          List<AssignedSkillItem> counselorSkills = skillIds.stream()
                  .map(skillId -> skillInfoMap.get(skillId.toString()))
                  .filter(skill -> skill != null)
                  .collect(Collectors.toList());

          resultMap.put(counselorId, counselorSkills);
        }

        log.info("스킬 정보 매핑 완료: {} 개 스킬 정보 처리됨", skillInfoMap.size());
      }

    } catch (Exception e) {
      log.error("스킬 일괄 조회 중 오류 발생: {}", e.getMessage(), e);
    }

    // 결과 통계 로깅
    int totalSkillsAssigned = resultMap.values().stream()
            .mapToInt(List::size)
            .sum();

    log.info("스킬 조회 완료 - 대상: {} 명, 할당된 총 스킬: {} 개",
            counselorIds.size(), totalSkillsAssigned);

    return resultMap;
  }
}