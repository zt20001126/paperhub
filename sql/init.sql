CREATE DATABASE IF NOT EXISTS paperhub
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE paperhub;

CREATE TABLE IF NOT EXISTS paperhub_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  email VARCHAR(255) NOT NULL COMMENT '邮箱',
  password VARCHAR(255) NOT NULL COMMENT 'BCrypt加密后的密码',
  nickname VARCHAR(100) DEFAULT NULL COMMENT '昵称',
  avatar_url TEXT DEFAULT NULL COMMENT '头像地址',
  status INT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  points INT NOT NULL DEFAULT 100 COMMENT '积分',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_paperhub_user_email (email),
  KEY idx_paperhub_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS literature_request (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '文献求助ID',
  user_id BIGINT NOT NULL COMMENT '发布用户ID',
  title VARCHAR(500) NOT NULL COMMENT '论文标题',
  journal VARCHAR(255) NOT NULL COMMENT '期刊',
  doi VARCHAR(255) NOT NULL COMMENT 'DOI',
  reward_points INT NOT NULL DEFAULT 0 COMMENT '奖励积分',
  status INT NOT NULL DEFAULT 0 COMMENT '状态：0待处理，1处理中，2已完成',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_literature_request_user_id (user_id),
  KEY idx_literature_request_create_time (create_time),
  KEY idx_literature_request_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文献求助表';

CREATE TABLE IF NOT EXISTS literature_assist (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '应助记录ID',
  lit_request_id BIGINT NOT NULL COMMENT '文献求助ID',
  user_id BIGINT NOT NULL COMMENT '应助用户ID',
  original_filename VARCHAR(255) NOT NULL COMMENT '原始PDF文件名',
  object_name VARCHAR(500) NOT NULL COMMENT 'MinIO对象名称',
  file_size BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小，单位字节',
  status INT NOT NULL DEFAULT 0 COMMENT '状态：0已提交，1已采纳，2已拒绝',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_literature_assist_lit_request_id (lit_request_id),
  KEY idx_literature_assist_user_id (user_id),
  KEY idx_literature_assist_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文献应助记录表';

CREATE TABLE IF NOT EXISTS post (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  user_id BIGINT NOT NULL COMMENT '发布用户ID',
  title VARCHAR(200) NOT NULL COMMENT '帖子标题',
  content TEXT NOT NULL COMMENT '帖子内容',
  view_count INT NOT NULL DEFAULT 0 COMMENT '浏览数',
  like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  status INT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0隐藏',
  PRIMARY KEY (id),
  KEY idx_post_user_id (user_id),
  KEY idx_post_publish_time (publish_time),
  KEY idx_post_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子表';

CREATE TABLE IF NOT EXISTS hot_topic (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '热点ID',
  title VARCHAR(255) NOT NULL COMMENT '热点标题',
  content TEXT DEFAULT NULL COMMENT '热点内容',
  cover_url TEXT DEFAULT NULL COMMENT '封面地址',
  view_count INT NOT NULL DEFAULT 0 COMMENT '浏览数',
  publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_hot_topic_view_count (view_count),
  KEY idx_hot_topic_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热点话题表';
