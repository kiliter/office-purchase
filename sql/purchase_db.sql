-- 附录：线上办公用品采购管理系统建库脚本
-- 演示账号密码均为 123456，库中保存 BCrypt 密文
CREATE DATABASE IF NOT EXISTS purchase_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE purchase_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS purchase_order;
DROP TABLE IF EXISTS purchase_apply_item;
DROP TABLE IF EXISTS purchase_apply;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS goods;
DROP TABLE IF EXISTS `user`;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `user` (
  user_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键 id',
  username VARCHAR(50) NOT NULL COMMENT '登录账号',
  password VARCHAR(100) NOT NULL COMMENT '加密密码',
  real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
  role VARCHAR(20) NOT NULL COMMENT '角色：admin/audit/staff',
  phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

CREATE TABLE goods (
  goods_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品主键 id',
  goods_name VARCHAR(100) NOT NULL COMMENT '商品名称',
  goods_type VARCHAR(50) NOT NULL COMMENT '商品类别',
  spec VARCHAR(100) DEFAULT NULL COMMENT '规格',
  price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
  stock INT NOT NULL COMMENT '库存数量',
  create_time DATETIME NOT NULL COMMENT '录入时间',
  PRIMARY KEY (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='办公用品商品表';

CREATE TABLE purchase_apply (
  apply_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请主键 id',
  apply_user_id BIGINT NOT NULL COMMENT '申请人 id',
  apply_reason VARCHAR(500) NOT NULL COMMENT '申请理由',
  apply_time DATETIME NOT NULL COMMENT '申请提交时间',
  audit_status VARCHAR(20) NOT NULL COMMENT '审批状态：待审批/已通过/已驳回',
  audit_user_id BIGINT DEFAULT NULL COMMENT '审核人 id',
  audit_opinion VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  audit_time DATETIME DEFAULT NULL COMMENT '审批时间',
  PRIMARY KEY (apply_id),
  KEY idx_apply_user (apply_user_id),
  KEY idx_audit_status (audit_status),
  CONSTRAINT fk_apply_user FOREIGN KEY (apply_user_id) REFERENCES `user` (user_id),
  CONSTRAINT fk_apply_audit_user FOREIGN KEY (audit_user_id) REFERENCES `user` (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购申请表';

CREATE TABLE purchase_apply_item (
  item_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细主键 id',
  apply_id BIGINT NOT NULL COMMENT '采购申请 id',
  goods_id BIGINT NOT NULL COMMENT '商品 id',
  buy_num INT NOT NULL COMMENT '采购申请数量',
  PRIMARY KEY (item_id),
  KEY idx_item_apply (apply_id),
  CONSTRAINT fk_item_apply FOREIGN KEY (apply_id) REFERENCES purchase_apply (apply_id),
  CONSTRAINT fk_item_goods FOREIGN KEY (goods_id) REFERENCES goods (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购申请明细表';

CREATE TABLE purchase_order (
  order_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单主键 id',
  apply_id BIGINT NOT NULL COMMENT '采购申请 id',
  order_status VARCHAR(20) NOT NULL COMMENT '订单状态：待采购、采购中、已到货、已完成',
  create_time DATETIME NOT NULL COMMENT '订单生成时间',
  PRIMARY KEY (order_id),
  UNIQUE KEY uk_order_apply (apply_id),
  CONSTRAINT fk_order_apply FOREIGN KEY (apply_id) REFERENCES purchase_apply (apply_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

CREATE TABLE notice (
  notice_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告主键 id',
  title VARCHAR(100) NOT NULL COMMENT '公告标题',
  content TEXT NOT NULL COMMENT '公告内容',
  publish_time DATETIME NOT NULL COMMENT '发布时间',
  PRIMARY KEY (notice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

INSERT INTO `user` (username, password, real_name, role, phone, create_time) VALUES
('admin', '$2a$10$z0dFTYvkzmvyso0sCbCYFu5ox6I1T5ixFYg.00Ep/zsZE6yOzrwSO', '系统管理员', 'admin', '13800000001', NOW()),
('audit01', '$2a$10$z0dFTYvkzmvyso0sCbCYFu5ox6I1T5ixFYg.00Ep/zsZE6yOzrwSO', '采购审核员', 'audit', '13800000002', NOW()),
('staff01', '$2a$10$z0dFTYvkzmvyso0sCbCYFu5ox6I1T5ixFYg.00Ep/zsZE6yOzrwSO', '普通员工', 'staff', '13800000003', NOW());

INSERT INTO goods (goods_name, goods_type, spec, price, stock, create_time) VALUES
('A4打印纸', '纸张文具', '70g 500张/包', 25.00, 200, NOW()),
('中性签字笔', '书写工具', '0.5mm 黑色', 2.50, 500, NOW()),
('牛皮档案袋', '纸张文具', 'A4 10个/包', 12.00, 120, NOW()),
('U盘', '存储设备', '32GB', 49.00, 40, NOW()),
('无线鼠标', '电脑配件', '2.4G', 69.00, 30, NOW()),
('便利贴', '纸张文具', '76mm 黄色', 6.50, 180, NOW()),
('订书机', '桌面用品', '12号钉', 15.00, 60, NOW()),
('抽杆文件夹', '纸张文具', 'A4', 8.00, 150, NOW());

INSERT INTO notice (title, content, publish_time) VALUES
('办公用品采购系统启用说明', '请通过本系统提交采购申请。提交后由采购审核人员审批，通过后自动生成订单。管理员在订单管理中更新到货进度。当前版本不自动扣减库存，库存由管理员在商品管理中维护。', NOW()),
('申请被驳回后如何处理', '员工可在“我的申请”中查看驳回意见，修改采购数量和申请理由后重新提交。重新提交后状态回到待审批。', NOW());
