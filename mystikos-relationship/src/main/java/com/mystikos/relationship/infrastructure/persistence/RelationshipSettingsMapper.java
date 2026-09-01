package com.mystikos.relationship.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RelationshipSettingsMapper extends BaseMapper<RelationshipSettingsPO> {
}
