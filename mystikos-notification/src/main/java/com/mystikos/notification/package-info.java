/**
 * 通知（Notification）限界上下文：站内信/邮件/推送（未来含 Discord）。
 * 消费几乎所有上下文的事件 fan-out 发送；发送失败不能回滚业务订单。
 * 领域模型见 docs/architecture/domain-model.md；内部五层结构实现时参照
 * mystikos-booking 模块，见 docs/architecture/module-structure.md。
 */
package com.mystikos.notification;
