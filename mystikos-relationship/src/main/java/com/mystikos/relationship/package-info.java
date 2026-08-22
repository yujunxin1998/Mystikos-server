/**
 * 亲密度（Relationship）限界上下文：老板×陪玩配对的关系进度、阶段。
 * 领域模型见 docs/architecture/domain-model.md；内部五层结构实现时参照
 * mystikos-booking 模块，见 docs/architecture/module-structure.md。
 *
 * 只对外暴露只读查询接口（如 getIntimacyStage），不要让 Commerce 等
 * 上下文直接读本模块的表。
 */
package com.mystikos.relationship;
