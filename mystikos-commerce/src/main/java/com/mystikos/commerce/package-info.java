/**
 * 商城（Commerce）限界上下文：货架、分类、库存、购物车、心愿单、商品订单。
 * 领域模型见 docs/architecture/domain-model.md；内部五层结构实现时参照
 * mystikos-booking 模块，见 docs/architecture/module-structure.md。
 *
 * 注意："传奇搭档"等限购商品的准入规则要经 application/port 查询
 * Relationship 上下文的亲密度阶段，不要直接读它的表（见 domain-model.md）。
 */
package com.mystikos.commerce;
