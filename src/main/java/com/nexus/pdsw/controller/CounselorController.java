/*------------------------------------------------------------------------------
 * NAME : CounselorController.java
 * DESC : 상담사 내역 불러오기
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/01/15  최상원                       초기작성
 * 2025/01/18  최상원                       상담사 상태정보 가져오기 추가
 * 2025/02/20  최상원                       스킬 할당된 상담사 리스트 가져오기 추가
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.pdsw.dto.request.PostCounselorListRequestDto;
import com.nexus.pdsw.dto.request.PostSkillAssignedCounselorListRequestDto;
import com.nexus.pdsw.dto.response.counselor.GetCounselorInfoListResponseDto;
import com.nexus.pdsw.dto.response.counselor.GetCounselorListResponseDto;
import com.nexus.pdsw.dto.response.counselor.PostCounselorStatusListResponseDto;
import com.nexus.pdsw.dto.response.counselor.PostSkillAssignedCounselorListResponseDto;
import com.nexus.pdsw.service.CounselorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
@RequestMapping("/api_upds/v1/counselor")
@RequiredArgsConstructor
public class CounselorController {

    private final CounselorService counselorService;

    /*
     *  상담사 리스트 가져오기
     *
     *  @param PostCounselorListRequestDto requestBody  상담사 리스트 전달 DTO
     *  @return ResponseEntity<? super GetCounselorListResponseDto>
     */
    @PostMapping("/list")
    public ResponseEntity<? super GetCounselorListResponseDto> getCounselorList(
            @RequestBody PostCounselorListRequestDto requestBody
    ) {
        ResponseEntity<? super GetCounselorListResponseDto> response = counselorService.getCounselorList(requestBody);

        // JSON으로 응답 전체 출력 (ObjectMapper 필요)
        try {
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("응답 JSON: " + mapper.writeValueAsString(response.getBody()));
        } catch (Exception e) {
            System.out.println("응답 객체: " + response.getBody());
        }

        return response;
    }

    /*
     *  상담사 상태정보 가져오기
     *
     *  @param PostCounselorListRequestDto requestBody    전달 DTO
     *  @return ResponseEntity<? super PostCounselorStatusListResponseDto>
     */
    @PostMapping("/state")
    public ResponseEntity<? super PostCounselorStatusListResponseDto> getCounselorStatusList(
      @RequestBody PostCounselorListRequestDto requestBody
    ) {
      ResponseEntity<? super PostCounselorStatusListResponseDto> response = counselorService.getCounselorStatusList(requestBody);
      return response;
    }

  /*
   *  캠페인 할당 상담사정보 가져오기
   *  
   *  @param PostCounselorListRequestDto requestBody    전달 매개변수 개체 DTO
   *  @return ResponseEntity<? super GetCounselorInfoListResponseDto>
   */
  @PostMapping("/counselorInfo")
    public ResponseEntity<? super GetCounselorInfoListResponseDto> getCounselorInfoList(
      @RequestBody PostCounselorListRequestDto requestBody
    ) {
      ResponseEntity<? super GetCounselorInfoListResponseDto> response = counselorService.getCounselorInfoList(requestBody);
      return response;
    }

  /*
   *  스킬 할당 상담사 목록 가져오기
   *  
   *  @param PostSkillAssignedCounselorListRequestDto requestBody    전달 매개변수 개체 DTO
   *  @return ResponseEntity<? super PostSkillAssignedCounselorListResponseDto>
   */
  @PostMapping("/sillAssigned/list")
    public ResponseEntity<? super PostSkillAssignedCounselorListResponseDto> getCounselorInfoList(
      @RequestBody PostSkillAssignedCounselorListRequestDto requestBody
    ) {
      ResponseEntity<? super PostSkillAssignedCounselorListResponseDto> response = counselorService.getSillAssignedCounselorList(requestBody);
      return response;
    }
}
