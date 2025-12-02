/*------------------------------------------------------------------------------
 * NAME : AuthorityServiceImpl.java
 * DESC : 권한 구현체
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/03/18  최상원                       초기작성
 * 2025/03/19  최상원                       사용자별 환경설정 가져오기 추가
 * 2025/05/23  최상원                       센터정보리스트 가져오기 추가
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.service.impl;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nexus.pdsw.dto.request.PostEnvironmentSettingRequestDto;
import com.nexus.pdsw.dto.request.PostEnvironmentSettingSaveRequestDto;
import com.nexus.pdsw.dto.response.ResponseDto;
import com.nexus.pdsw.dto.response.authority.GetAvailableMenuListResponseDto;
import com.nexus.pdsw.dto.response.authority.GetCenterInfoListResponseDto;
import com.nexus.pdsw.dto.response.authority.GetEnvironmentSettingResponseDto;
import com.nexus.pdsw.dto.response.authority.PostEnvironmentSettingSaveResponseDto;
import com.nexus.pdsw.entity.EnvironmentSettingEntity;
import com.nexus.pdsw.entity.MenuByRoleEntity;
import com.nexus.pdsw.entity.RoleEntity;
import com.nexus.pdsw.repository.EnvironmentSettingRepository;
import com.nexus.pdsw.repository.MenuByRoleRepository;
import com.nexus.pdsw.repository.RoleRepository;
import com.nexus.pdsw.service.AuthorityService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthorityServiceImpl implements AuthorityService {

  private final MenuByRoleRepository menuByRoleRepository;
  private final RoleRepository roleRepository;
  private final EnvironmentSettingRepository environmentSettingRepository;
  @Qualifier("1")
  private final RedisTemplate<String, Object> redisTemplate1;

  public AuthorityServiceImpl(
    MenuByRoleRepository menuByRoleRepository,
    RoleRepository roleRepository,
    EnvironmentSettingRepository environmentSettingRepository,
    @Qualifier("1") RedisTemplate<String, Object> redisTemplate1
  ) {
    this.menuByRoleRepository = menuByRoleRepository;
    this.roleRepository = roleRepository;
    this.environmentSettingRepository = environmentSettingRepository;
    this.redisTemplate1 = redisTemplate1;
  }

  /*
   *  사용가능한 메뉴 리스트 가져오기
   *
   *  @param int roleId    역할ID(1: 시스템관리자, 2: 테넌트관리자01, 3: 테넌트관리자02)
   *  @return ResponseEntity<? super GetAvailableMenuListResponseDto>
   */
  @Override
  public ResponseEntity<? super GetAvailableMenuListResponseDto> getAvailableMenuList(int roleId) {

    List<MenuByRoleEntity> menuListByRole = null;
    
    try {

      RoleEntity role = roleRepository.findByRoleId(roleId);

      if (role == null) return GetAvailableMenuListResponseDto.notExistRole();

      menuListByRole = menuByRoleRepository.findByRole(role);

    } catch (Exception e) {
      e.printStackTrace();
      ResponseDto.databaseError();
    }

    if (menuListByRole == null) {
      return GetAvailableMenuListResponseDto.notExistMenu();
    } else {
      return GetAvailableMenuListResponseDto.success(menuListByRole);
    }
  }

  /*
   *  센터정보 가져오기
   *
   *  @return ResponseEntity<? super GetCenterInfoListResponseDto>
   */
  @Override
  public ResponseEntity<? super GetCenterInfoListResponseDto> getCenterInfo() {

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

    return GetCenterInfoListResponseDto.success(arrJsonCenter);
  }

  /*
   *  사용자별 환경설정 가져오기
   *
   *  @param PostEnvironmentSettingRequestDto requestDto    전달 DTO
   *  @return ResponseEntity<? super GetEnvironmentSettingResponseDto>
   */
  @Override
  public ResponseEntity<? super GetEnvironmentSettingResponseDto> getEnvironmentSetting(PostEnvironmentSettingRequestDto requestDto) {
    
    EnvironmentSettingEntity environmentSetting = null;
    try {
      environmentSetting = environmentSettingRepository.findByEmployeeId(requestDto.getEmployeeId());

      //환경설정 내역이 존재하지 않을 경우
      if (environmentSetting == null) {
        EnvironmentSettingEntity defaultEnvironmentSetting = new EnvironmentSettingEntity(requestDto);
        environmentSettingRepository.save(defaultEnvironmentSetting);
        environmentSetting = defaultEnvironmentSetting;
      }
    } catch (Exception e) {
      e.printStackTrace();
      ResponseDto.databaseError();
    }
    return GetEnvironmentSettingResponseDto.success(environmentSetting);
  }

  /*
   *  사용자별 환경설정 저장하기
   *
   *  @param PostEnvironmentSettingSaveRequestDto requestDto    전달 DTO
   *  @return ResponseEntity<? super PostEnvironmentSettingSaveResponseDto>
   */
  @Override
  public ResponseEntity<? super PostEnvironmentSettingSaveResponseDto> postEnvironmentSetting(
    PostEnvironmentSettingSaveRequestDto requestDto
  ) {

    try {
      EnvironmentSettingEntity environmentSetting = environmentSettingRepository.findByEmployeeId(requestDto.getEmployeeId());
      environmentSetting.modifySetting(requestDto);
      environmentSettingRepository.save(environmentSetting);
      
    } catch (Exception e) {
      e.printStackTrace();
      ResponseDto.databaseError();
    }
    return PostEnvironmentSettingSaveResponseDto.success();
  }

}
