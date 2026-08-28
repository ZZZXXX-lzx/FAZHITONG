-- ============================================================
-- 法智通法律服务平台 - 演示种子数据
-- 执行方式: mysql -u root -p123456 --default-character-set=utf8mb4
--           mysql> source d:/FAZHITONG/docs/seed-data.sql
-- 说明: 已使用 INSERT IGNORE 保证可重复执行（用户/律师按唯一键去重）
-- ============================================================
USE fazhitong;

-- ===== 1. 裁判文书案例 =====
INSERT INTO `case_government`
(`cause_name`, `court_name`, `case_year`, `judgment_result`, `keywords`, `abstract`, `focus_points`, `full_text`, `source`, `status`) VALUES
('买卖合同纠纷', '北京市朝阳区人民法院', '2023', '支持原告全部诉讼请求，判决被告支付货款及违约金。',
 '买卖合同,货款,违约责任', '原告与被告签订供货合同，被告收货后未按约支付货款，法院判决被告支付剩余货款并承担违约金。',
 '合同效力；逾期付款违约金计算标准', '原告某公司诉被告某公司买卖合同纠纷一案，本院依法适用简易程序公开开庭审理。原告诉称：双方签订《供货合同》，原告依约供货，被告未付余款……', '中国裁判文书网', 1),
('民间借贷纠纷', '上海市浦东新区人民法院', '2023', '判决被告偿还原告借款本金及利息。',
 '民间借贷,借条,利息', '被告向原告出具借条借款，到期未还，法院支持原告要求返还本金的请求，利息按LPR计算。',
 '借条效力；利率是否超出法定上限', '原告持借条诉请被告返还借款……', '中国裁判文书网', 1),
('劳动合同纠纷', '深圳市南山区人民法院', '2023', '判决用人单位支付违法解除劳动合同赔偿金。',
 '劳动合同,违法解除,赔偿金', '用人单位无正当理由单方解除劳动合同，法院认定构成违法解除，判令支付赔偿金。',
 '解除是否合法；赔偿金计算', '原告与被告存在劳动关系，被告以经营调整为由解除合同……', '中国裁判文书网', 1),
('离婚后财产分割纠纷', '广州市天河区人民法院', '2022', '判决夫妻共同财产平均分割。',
 '离婚,财产分割,共同财产', '双方离婚后就房产、存款等共同财产分割产生争议，法院按照照顾子女及女方权益原则分割。',
 '共同财产范围；房产归属', '原告与被告原系夫妻，离婚后对共同财产分割未能达成一致……', '中国裁判文书网', 1),
('机动车交通事故责任纠纷', '成都市武侯区人民法院', '2023', '判决保险公司在交强险及商业三者险限额内赔偿。',
 '交通事故,保险理赔,人身损害', '被告驾驶机动车与原告发生碰撞致原告受伤，法院判决保险公司及被告按责任比例赔偿。',
 '责任划分；赔偿项目与标准', '原告因交通事故受伤，诉请被告及保险公司赔偿医疗费、误工费等……', '中国裁判文书网', 1),
('商标权侵权纠纷', '杭州市中级人民法院', '2023', '判决被告停止侵权并赔偿经济损失。',
 '商标,知识产权,侵权', '被告未经许可在同类商品上使用与原告注册商标近似的标识，构成商标侵权。',
 '商标近似认定；赔偿数额', '原告系注册商标权利人，被告在其产品上使用近似标识……', '中国裁判文书网', 1),
('商品房买卖合同纠纷', '南京市鼓楼区人民法院', '2022', '判决开发商承担逾期交房违约金。',
 '商品房,逾期交房,违约金', '开发商未按合同约定时间交房，法院判决其支付逾期交房违约金。',
 '逾期交房认定；违约金标准', '原告与被告签订商品房买卖合同，约定交房时间，被告逾期未交付……', '中国裁判文书网', 1),
('消费者权益保护纠纷', '重庆市渝中区人民法院', '2023', '判决商家退还货款并三倍赔偿。',
 '消费者权益,欺诈,三倍赔偿', '商家销售商品存在欺诈行为，法院依据消费者权益保护法判决退还货款并三倍赔偿。',
 '欺诈认定；惩罚性赔偿适用', '原告在被告处购买商品，后发现商品存在虚假宣传……', '中国裁判文书网', 1);

-- ===== 2. 文书模板 =====
INSERT INTO `document_template` (`type`, `name`, `content`, `category`, `status`) VALUES
('CIVIL_COMPLAINT', '民事起诉状（买卖合同）', '原告：{{原告}}\n被告：{{被告}}\n\n诉讼请求：\n1. 判令被告支付货款人民币{{金额}}元；\n2. 判令被告承担本案诉讼费用。\n\n事实与理由：\n{{事实与理由}}\n\n此致\n{{法院名称}}\n\n具状人：{{原告}}\n{{日期}}', '民事起诉状', 1),
('CIVIL_COMPLAINT', '民事起诉状（民间借贷）', '原告：{{原告}}\n被告：{{被告}}\n\n诉讼请求：\n1. 判令被告返还借款本金人民币{{金额}}元及利息；\n2. 判令被告承担本案诉讼费用。\n\n事实与理由：\n{{事实与理由}}\n\n此致\n{{法院名称}}\n\n具状人：{{原告}}\n{{日期}}', '民事起诉状', 1),
('CRIMINAL_COMPLAINT', '刑事自诉状', '自诉人：{{自诉人}}\n被告人：{{被告人}}\n\n案由：{{案由}}\n\n诉讼请求：依法追究被告人{{被告人}}的刑事责任。\n\n事实与理由：\n{{事实与理由}}\n\n此致\n{{法院名称}}\n\n自诉人：{{自诉人}}\n{{日期}}', '刑事起诉状', 1),
('LABOR_ARBITRATION', '劳动仲裁申请书', '申请人：{{申请人}}\n被申请人：{{被申请人}}\n\n仲裁请求：\n1. 裁决被申请人支付拖欠工资人民币{{金额}}元；\n2. 裁决被申请人支付经济补偿金。\n\n事实与理由：\n{{事实与理由}}\n\n此致\n{{仲裁委员会名称}}\n\n申请人：{{申请人}}\n{{日期}}', '劳动仲裁', 1),
('APPEAL', '民事上诉状', '上诉人：{{上诉人}}\n被上诉人：{{被上诉人}}\n\n上诉人因{{案由}}一案，不服{{原审法院}}作出的{{原审案号}}判决，现提出上诉。\n\n上诉请求：\n{{上诉请求}}\n\n上诉理由：\n{{上诉理由}}\n\n此致\n{{上级法院名称}}\n\n上诉人：{{上诉人}}\n{{日期}}', '上诉状', 1),
('DEFENSE', '民事答辩状', '答辩人：{{答辩人}}\n\n答辩人因{{案由}}一案，提出答辩如下：\n\n{{答辩意见}}\n\n综上所述，请求人民法院依法驳回原告的诉讼请求。\n\n此致\n{{法院名称}}\n\n答辩人：{{答辩人}}\n{{日期}}', '答辩状', 1),
('AGREEMENT', '借款协议书', '甲方（出借人）：{{甲方}}\n乙方（借款人）：{{乙方}}\n\n一、借款金额：人民币{{金额}}元（大写：{{大写金额}}）。\n二、借款期限：自{{起始日期}}至{{到期日期}}。\n三、利率：年利率{{利率}}%。\n四、还款方式：{{还款方式}}。\n\n甲方（签字）：\n乙方（签字）：\n{{日期}}', '协议书', 1);

-- ===== 3. 演示律师账号（密码均为 admin123）=====
INSERT IGNORE INTO `user` (`account`, `password`, `nickname`, `user_type`, `status`) VALUES
('lawyer_zhangwei', '$2a$10$bHXcLAjYcwXJt3yjXE9DhOddWnRTUuoPbiUpIfhuVM0lN6UcwvAsS', '张伟律师', 'LAWYER', 1),
('lawyer_lina', '$2a$10$bHXcLAjYcwXJt3yjXE9DhOddWnRTUuoPbiUpIfhuVM0lN6UcwvAsS', '李娜律师', 'LAWYER', 1),
('lawyer_wangqiang', '$2a$10$bHXcLAjYcwXJt3yjXE9DhOddWnRTUuoPbiUpIfhuVM0lN6UcwvAsS', '王强律师', 'LAWYER', 1),
('lawyer_zhaomin', '$2a$10$bHXcLAjYcwXJt3yjXE9DhOddWnRTUuoPbiUpIfhuVM0lN6UcwvAsS', '赵敏律师', 'LAWYER', 1),
('lawyer_chenjie', '$2a$10$bHXcLAjYcwXJt3yjXE9DhOddWnRTUuoPbiUpIfhuVM0lN6UcwvAsS', '陈杰律师', 'LAWYER', 1);

-- 律师信息（关联上面的用户）
INSERT IGNORE INTO `lawyer_info` (`user_id`, `practice_cert_no`, `law_firm`, `specialty`, `years_exp`, `description`, `status`)
SELECT id, '1110120220001', '北京京师律师事务所', '合同纠纷,公司法,民商事诉讼', 12, '专注民商事诉讼与企业法律顾问，办理合同纠纷、股权争议等案件逾500件。', 1 FROM `user` WHERE account='lawyer_zhangwei';
INSERT IGNORE INTO `lawyer_info` (`user_id`, `practice_cert_no`, `law_firm`, `specialty`, `years_exp`, `description`, `status`)
SELECT id, '1110120190002', '上海锦天城律师事务所', '婚姻家庭,继承纠纷', 8, '专注婚姻家事领域，擅长离婚财产分割、子女抚养权等案件。', 1 FROM `user` WHERE account='lawyer_lina';
INSERT IGNORE INTO `lawyer_info` (`user_id`, `practice_cert_no`, `law_firm`, `specialty`, `years_exp`, `description`, `status`)
SELECT id, '1110120180003', '广东广和律师事务所', '劳动争议,工伤赔偿', 10, '深耕劳动法领域，代理大量劳动争议仲裁与诉讼案件。', 1 FROM `user` WHERE account='lawyer_wangqiang';
INSERT IGNORE INTO `lawyer_info` (`user_id`, `practice_cert_no`, `law_firm`, `specialty`, `years_exp`, `description`, `status`)
SELECT id, '1110120210004', '浙江六和律师事务所', '知识产权,商标侵权', 7, '专注知识产权保护，代理商标、专利侵权纠纷案件。', 1 FROM `user` WHERE account='lawyer_zhaomin';
INSERT IGNORE INTO `lawyer_info` (`user_id`, `practice_cert_no`, `law_firm`, `specialty`, `years_exp`, `description`, `status`)
SELECT id, '1110120200005', '四川泰和泰律师事务所', '刑事辩护,刑事合规', 9, '专注刑事辩护与刑事法律风险防控，执业经验丰富。', 1 FROM `user` WHERE account='lawyer_chenjie';

-- 律师服务价格（供"律师委托"展示）
INSERT IGNORE INTO `lawyer_service_price` (`lawyer_id`, `service_type`, `service_name`, `price`, `unit`, `description`, `status`)
SELECT id, 'CONSULT', '法律咨询', 200.00, '次', '电话/线上咨询 30 分钟', 1 FROM `user` WHERE account='lawyer_zhangwei';
INSERT IGNORE INTO `lawyer_service_price` (`lawyer_id`, `service_type`, `service_name`, `price`, `unit`, `description`, `status`)
SELECT id, 'DOCUMENT', '合同起草', 800.00, '份', '根据需求起草标准合同文本', 1 FROM `user` WHERE account='lawyer_zhangwei';
INSERT IGNORE INTO `lawyer_service_price` (`lawyer_id`, `service_type`, `service_name`, `price`, `unit`, `description`, `status`)
SELECT id, 'LITIGATION', '诉讼代理', 5000.00, '件', '一审诉讼代理（按案件复杂度协商）', 1 FROM `user` WHERE account='lawyer_lina';
