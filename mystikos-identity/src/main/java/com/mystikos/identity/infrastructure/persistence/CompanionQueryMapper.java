package com.mystikos.identity.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * identity_user 与 identity_companion_profile 的联表只读查询——打手列表要按昵称/手机号
 * （identity_user）和身份证号（identity_companion_profile）一起搜关键字，天然要跨表，
 * 单表的 BaseMapper/LambdaQueryWrapper 撑不住，所以这里跟 {@link UserRoleMapper}/
 * {@link UserTagMapper} 一样用注解式 SQL，不建 XML 文件（本项目约定不用 XML mapper）。
 * PageHelper 的分页拦截器在 MyBatis Executor 层生效，不区分 XML/注解/BaseMapper，
 * 调用前照常 {@code PageHelper.startPage(...)} 即可。
 */
@Mapper
public interface CompanionQueryMapper {

    @Select("<script>"
            + "SELECT u.id AS user_id, u.phone, u.email, u.nickname, u.avatar_url, u.created_at, "
            + "c.level, c.skill_tags, c.hourly_rate, c.companion_status, c.id_card_no, "
            + "c.bank_account_name, c.bank_account_no, c.bank_name "
            + "FROM identity_companion_profile c JOIN identity_user u ON u.id = c.user_id "
            + "<where>"
            + "<if test='status != null'>AND c.companion_status = #{status}</if> "
            + "<if test='keyword != null'>AND (u.nickname LIKE CONCAT('%',#{keyword},'%') "
            + "OR u.phone LIKE CONCAT('%',#{keyword},'%') OR c.id_card_no LIKE CONCAT('%',#{keyword},'%'))</if> "
            + "<if test='createdFrom != null'>AND u.created_at &gt;= #{createdFrom}</if> "
            + "<if test='createdTo != null'>AND u.created_at &lt;= #{createdTo}</if> "
            + "</where>"
            + "ORDER BY u.created_at DESC"
            + "</script>")
    List<CompanionRowPO> search(@Param("status") String status, @Param("keyword") String keyword,
                                 @Param("createdFrom") OffsetDateTime createdFrom,
                                 @Param("createdTo") OffsetDateTime createdTo);

    @Select("SELECT count(*) FROM identity_companion_profile")
    long countTotal();

    @Select("SELECT count(*) FROM identity_companion_profile WHERE companion_status = 'AVAILABLE'")
    long countAvailable();

    @Select("SELECT count(*) FROM identity_companion_profile WHERE companion_status = 'BUSY'")
    long countBusy();

    @Select("SELECT COALESCE(AVG(hourly_rate), 0) FROM identity_companion_profile")
    BigDecimal avgHourlyRate();
}
