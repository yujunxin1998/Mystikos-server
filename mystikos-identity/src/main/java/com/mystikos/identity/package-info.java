/**
 * 身份与访问（Identity &amp; Access）限界上下文：本地账号、RBAC。
 *
 * <p>角色是固定枚举 {@link com.mystikos.identity.domain.model.Role}（游客/会员用户/陪玩/
 * 客服/考核官/管理员），一个用户可以同时拥有多个角色。权限是 identity_role_permission
 * 表驱动的角色-权限映射，编码留空未预置，业务定义后用 {@code PermissionApplicationService}
 * （现阶段是 UserApplicationService 里的 grant/revoke，尚无独立门面）的
 * {@link com.mystikos.identity.domain.repository.PermissionRepository#grant} 接入。
 *
 * <p>会员等级只存 level/code 两个字段，具体梯度由
 * {@link com.mystikos.common.membership.MembershipTier} 的实现决定，本模块不关心梯度长什么样。
 *
 * <p>领域模型见 docs/architecture/domain-model.md；OAuth2 Resource Server 具体接入方式
 * 尚未决定（见 mystikos-common-security），当前 Controller 没有鉴权保护。
 */
package com.mystikos.identity;
