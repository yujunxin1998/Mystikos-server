package com.mystikos.membership.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MembershipTierDefinitionMapper extends BaseMapper<MembershipTierDefinitionPO> {
}
