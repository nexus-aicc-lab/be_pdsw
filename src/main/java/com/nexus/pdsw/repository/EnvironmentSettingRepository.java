/*------------------------------------------------------------------------------
 * NAME : EnvironmentSettingRepository.java
 * DESC : 환경설정 레포지토리 개체
 * VER  : V1.0
 * PROJ : 웹 기반 PDS 구축 프로젝트
 * Copyright 2024 Dootawiz All rights reserved
 *------------------------------------------------------------------------------
 *                               MODIFICATION LOG
 *------------------------------------------------------------------------------
 *    DATE     AUTHOR                       DESCRIPTION
 * ----------  ------  -----------------------------------------------------------
 * 2025/03/19  최상원                       초기작성
 *------------------------------------------------------------------------------*/
package com.nexus.pdsw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexus.pdsw.entity.EnvironmentSettingEntity;
import com.nexus.pdsw.entity.primaryKey.EnvironmentSettingPk;

@Repository
public interface EnvironmentSettingRepository extends JpaRepository<EnvironmentSettingEntity, EnvironmentSettingPk> {
  EnvironmentSettingEntity findByEmployeeId(String employeeId);
}
