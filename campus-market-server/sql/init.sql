SET NAMES utf8mb4;

 -- 校园二手交易平台数据库初始化脚本
  CREATE DATABASE IF NOT EXISTS campus_market DEFAULT CHARACTER SET utf8mb4 COLLATE
  utf8mb4_unicode_ci;
  USE campus_market;

  DROP TABLE IF EXISTS user;
  CREATE TABLE user (
      id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
      username     VARCHAR(32)  NOT NULL                COMMENT '用户名',
      password     VARCHAR(100) NOT NULL                COMMENT 'BCrypt 加密后的密码',
      nickname     VARCHAR(32)            DEFAULT NULL  COMMENT '昵称',
      avatar       VARCHAR(255)           DEFAULT NULL  COMMENT '头像 URL',
      contact      VARCHAR(64)            DEFAULT NULL  COMMENT '联系方式',
      role         VARCHAR(16)  NOT NULL  DEFAULT 'USER' COMMENT '角色: USER / ADMIN',
      status       TINYINT      NOT NULL  DEFAULT 0      COMMENT '状态: 0 正常 1 封禁',
      deleted      TINYINT      NOT NULL  DEFAULT 0      COMMENT '逻辑删除: 0 否 1 是',
      created_at   DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      updated_at   DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  COMMENT '更新时间',
      PRIMARY KEY (id),
      UNIQUE KEY uk_username (username)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

  -- 预置一个管理员账号: admin / admin123 (BCrypt)
  INSERT INTO user (username, password, nickname, role)
  VALUES ('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员',
  'ADMIN');


  -- =====================================================
  -- 成员二：商品模块建表（2026-06-21）

  -- =====================================================

  -- 分类字典表
  DROP TABLE IF EXISTS category;
  CREATE TABLE category (
      id           INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
      name         VARCHAR(32)  NOT NULL                COMMENT '分类名称',
      sort_order   INT          NOT NULL  DEFAULT 0     COMMENT '排序',
      deleted      TINYINT      NOT NULL  DEFAULT 0     COMMENT '逻辑删除: 0 否 1 是',
      created_at   DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      updated_at   DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
      PRIMARY KEY (id)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '分类字典表';

  -- 预置分类数据
  INSERT INTO category (id, name, sort_order) VALUES
  (1, '数码电子', 1),
  (2, '书籍教材', 2),
  (3, '生活用品', 3),
  (4, '服饰鞋包', 4),
  (5, '运动户外', 5),
  (6, '美妆护肤', 6),
  (7, '食品饮料', 7),
  (8, '其他', 99);

  -- 商品主表
  DROP TABLE IF EXISTS goods;
  CREATE TABLE goods (
      id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
      title           VARCHAR(100)  NOT NULL                COMMENT '商品标题',
      description     TEXT          NOT NULL                COMMENT '商品描述（AI 优化后）',
      raw_description TEXT                    DEFAULT NULL  COMMENT '用户原始描述',
      price           DECIMAL(10,2) NOT NULL                COMMENT '售价',
      original_price  DECIMAL(10,2)           DEFAULT NULL  COMMENT '原价（可选）',
      category_id     INT           NOT NULL                COMMENT '分类 ID',
      category_name   VARCHAR(32)   NOT NULL                COMMENT '分类名称（冗余）',
      seller_id       BIGINT        NOT NULL                COMMENT '卖家用户 ID',
      seller_name     VARCHAR(32)   NOT NULL                COMMENT '卖家用户名（冗余）',
      seller_contact  VARCHAR(64)             DEFAULT NULL  COMMENT '联系方式',
      status          VARCHAR(16)   NOT NULL  DEFAULT 'ON_SALE'   COMMENT '商品状态: ON_SALE / SOLD / OFF_SHELF',
      audit_status    VARCHAR(16)   NOT NULL  DEFAULT 'PENDING'   COMMENT '审核状态: PENDING / APPROVED / REJECTED',
      audit_remark    VARCHAR(255)            DEFAULT NULL  COMMENT '审核拒绝原因',
      view_count      INT           NOT NULL  DEFAULT 0     COMMENT '浏览次数',
      deleted         TINYINT       NOT NULL  DEFAULT 0     COMMENT '逻辑删除: 0 否 1 是',
      created_at      DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      updated_at      DATETIME      NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
      PRIMARY KEY (id),
      INDEX idx_category (category_id),
      INDEX idx_seller (seller_id),
      INDEX idx_audit_status (audit_status),
      INDEX idx_status (status),
      INDEX idx_created_at (created_at)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品主表';

  -- 商品图片表
  DROP TABLE IF EXISTS goods_image;
  CREATE TABLE goods_image (
      id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
      goods_id    BIGINT       NOT NULL                COMMENT '所属商品 ID',
      url         VARCHAR(500) NOT NULL                COMMENT '图片 URL',
      sort_order  INT          NOT NULL  DEFAULT 0     COMMENT '排序',
      created_at  DATETIME     NOT NULL  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      PRIMARY KEY (id),
      INDEX idx_goods_id (goods_id)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品图片表';
