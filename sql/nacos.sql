/*
 Navicat Premium Data Transfer

 Source Server         : 106.53.81.243
 Source Server Type    : MySQL
 Source Server Version : 80021
 Source Host           : 106.53.81.243:9106
 Source Schema         : nacos

 Target Server Type    : MySQL
 Target Server Version : 80021
 File Encoding         : 65001

 Date: 11/02/2022 10:33:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for config_info
-- ----------------------------
DROP TABLE IF EXISTS `config_info`;
CREATE TABLE `config_info`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `gmt_modified` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT 'source user',
  `src_ip` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'source ip',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '租户字段',
  `c_desc` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `c_use` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `effect` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `type` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `c_schema` text CHARACTER SET utf8 COLLATE utf8_bin NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfo_datagrouptenant`(`data_id`, `group_id`, `tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'config_info' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_info
-- ----------------------------
INSERT INTO `config_info` VALUES (1, 'application-dev.yml', 'DEFAULT_GROUP', '\nspring:\n  redis:\n    host: 127.0.0.1\n    password: jiujiard991\n    port: 9151\n  servlet:\n    multipart:\n      enabled: true\n      max-file-size: 4GB # 文件大小\n      max-request-size: 4GB # 请求大小\n  cloud:\n    sentinel:\n      eager: true\n      filter:\n        url-patterns: /** \n      transport:\n        dashboard: platform-sentinel:5020\nfeign:\n  sentinel:\n    enabled: true\n  okhttp:\n    enabled: true\n  httpclient:\n    enabled: false\n  client:\n    config:\n      default:\n        connectTimeout: 10000\n        readTimeout: 10000\n  compression:\n    request:\n      enabled: true\n    response:\n      enabled: true\nribbon:\n  ReadTimeout: 10000\n  ConnectTimeout: 10000\nsecurity:\n  oauth2:\n    client:\n      ignore-urls:\n        - /actuator/**\n        - /v2/api-docs\n    resource:\n      loadBalanced: true\n      token-info-uri: http://platform-auth/oauth/check_token #这里一定要配置服务名称，不能配置ip\noss:\n  endpoint: http://106.53.81.243:9153\n  # endpoint: http://192.168.1.251:9153\n  enable: true\n  info: false\n  access-key: admin\n  secret-key: jiujia@mn991\n  bucket-name: cloud\n#开启灰度\ngray:\n  rule:\n    enabled: true', 'ac6c70ae3f07e971a113b25ac010e8e7', '2020-12-15 02:37:10', '2021-12-16 16:56:26', NULL, '119.123.69.117', '', '', '', '', '', 'yaml', '');
INSERT INTO `config_info` VALUES (2, 'platform-upms-dev.yml', 'DEFAULT_GROUP', '\nspring:\n  jackson:\n    date-format: yyyy-MM-dd HH:mm:ss\n  resources:\n    static-locations:\n      - classpath:/META-INF/resources/\n      - classpath:/resources/\n      - classpath:/static/\n      - classpath:/public/\n      - file:${user.dir}/\n  datasource:\n    url: jdbc:mysql://127.0.0.1:9106/ecdispatch?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai\n    username: root\n    password: jj@admin\n    driver-class-name: com.mysql.cj.jdbc.Driver\n    type: com.zaxxer.hikari.HikariDataSource\n    hikari:\n      # 连接池最大连接数，默认是10\n      maximum-pool-size: 20\n      # 此属性控制池中连接的最长生命周期，值0表示无限生命周期，默认1800000即30分钟\n      max-lifetime: 180000\n      # 空闲连接存活最大时间，默认600000（10分钟）\n      idle-timeout: 60000\n      # 数据库连接超时时间,默认30秒，即30000\n      connection-timeout: 20000\n      # 最小空闲连接数量\n      minimum-idle: 5\n      # 此属性控制从池返回的连接的默认自动提交行为,默认值：true\n      auto-commit: true\n      connection-test-query: SELECT 1\nsecurity:\n  oauth2:\n    client:\n      client-id: admin\n      client-secret: admin # 这里必须是加密前的密钥\n      scope: all\n      ignore-urls:\n        - /druid/**\n        - /actuator/**\n        - /v2/api-docs\n        - /v3/api-docs\n        - /register/**\n        - /imserver/**\n        - /file/**\n        - /openApi/**\nribbon:\n  eager-load: #饥饿加载,防止第一次调用服务出现超时的问题\n    enabled: true\n    clients:\n      - platform-upms\nmybatis-plus:\n  # MyBatis Mapper 所对应的 XML 文件位置，如果您在 Mapper 中有自定义方法（XML 中有自定义实现），\n  mapper-locations: classpath:/mapper/sys/*.xml\n  # 如果是放在src/main/java目录下 classpath:/com/yourpackage/*/mapper/*Mapper.xml\n  # 如果是放在resource目录 classpath:/mapper/*Mapper.xml\n  #实体扫描，多个package用逗号或者分号分隔\n  typeAliasesPackage: com.yyh.sys.api.entity\n  global-config:\n    #主键类型  0:\"数据库ID自增\", 1:\"用户输入ID\",2:\"全局唯一ID (数字类型唯一ID)\", 3:\"全局唯一ID UUID\";\n    db-config:\n      id-type: AUTO\n      table-underline: true\n      logic-delete-field: delFlag # 全局逻辑删除的实体字段名\n      logic-delete-value: 1 # 逻辑已删除值(默认为 1)\n      logic-not-delete-value: 0 # 逻辑未删除值(默认为 0)\n      #数据库大写下划线转换\n      capital-mode: true\n  configuration:\n    #配置返回数据库(column下划线命名&&返回java实体是驼峰命名)，自动匹配无需as（没开启这个，SQL需要写as： select user_id as userId）\n    map-underscore-to-camel-case: true\n    cache-enabled: false\n    #配置JdbcTypeForNull, oracle数据库必须配置\n    jdbc-type-for-null: \'null\'\n    #log-impl: org.apache.ibatis.logging.stdout.StdOutImpl #开启sql日志\nlogging:\n  level:\n    com:\n      alibaba:\n        nacos: warn\nsystem:\n  app:\n    #register-url: http://106.52.5.128:7001/getappkey\n    register-url: http://pocdj.com:7001/getappkey\n  web:\n    upload-path: ${user.dir}\n  face:\n    ip: sx.pocdj.net\n    port: 9195\nswagger:\n  enabled: true\n  title: 指挥调度平台API\n  license: 久嘉智能\n  licenseUrl: https://baidu.com/\n  terms-of-service-url: https://baidu.com/\n  prefix: ${spring.application.name}\n  version: 1.0\n  contact:\n    email: yyh@longtell.cn\n    url: https://baidu.com\n  authorization:\n    name: jj OAuth\n    auth-regex: ^.*$\n    authorization-scope-list:\n      - scope: all\n        description: server all\n    token-url-list:\n      - http://106.53.81.243:9152/platform-auth/oauth/token', '1a5cb89c73193072e4393951cad28037', '2020-12-15 02:37:10', '2021-12-16 16:57:12', NULL, '119.123.69.117', '', '', '', '', '', 'yaml', '');
INSERT INTO `config_info` VALUES (3, 'platform-auth-dev.yml', 'DEFAULT_GROUP', 'logging:\n  level:\n    com:\n      alibaba:\n        nacos: warn\nsecurity:\n  oauth2:\n    client:\n      ignore-urls:\n        - /actuator/**\n        - /v2/api-docs\n        - /token/query/**\n    resource:\n      loadBalanced: true\n      token-info-uri: http://platform-auth/oauth/check_token', '2a42a1caac992af7f8f20d4bc12a3606', '2020-12-15 02:37:10', '2020-12-15 02:37:10', NULL, '192.168.1.100', '', '', NULL, NULL, NULL, 'yaml', NULL);
INSERT INTO `config_info` VALUES (4, 'platform-gateway-dev.yml', 'DEFAULT_GROUP', 'management:\r\n  endpoint:\r\n    health:\r\n      show-details: always\r\nignore:\r\n  clients:\r\n    - test\r\n  swagger-providers:\r\n    - platform-auth\r\nribbon:\r\n  eager-load:\r\n    enabled: true\r\n    clients:\r\n      - platform-gateway\r\nlogging:\r\n  level:\r\n    com:\r\n      alibaba:\r\n        nacos: warn', '2f600ed55b586bb77dfac28d9298f24b', '2020-12-15 02:37:10', '2020-12-15 02:37:10', NULL, '192.168.1.100', '', '', NULL, NULL, NULL, 'yaml', NULL);
INSERT INTO `config_info` VALUES (19, 'platform-record-dev.yml', 'DEFAULT_GROUP', '\nspring:\n  jackson:\n    date-format: yyyy-MM-dd HH:mm:ss\n  resources:\n    static-locations:\n      - classpath:/META-INF/resources/\n      - classpath:/resources/\n      - classpath:/static/\n      - classpath:/public/\n      - file:${user.dir}/\nsecurity:\n  oauth2:\n    client:\n      client-id: admin\n      client-secret: admin # 这里必须是加密前的密钥\n      scope: all\n      ignore-urls:\n        - /actuator/**\n        - /v2/api-docs\n        - /v3/api-docs\n        - /file/**\nribbon:\n  eager-load: #饥饿加载,防止第一次调用服务出现超时的问题\n    enabled: true\n    clients:\n      - platform-record\nlogging:\n  level:\n    com:\n      alibaba:\n        nacos: warn\noss:\n  endpoint: http://106.53.81.243:9153\n  enable: true\n  info: false\n  access-key: admin\n  secret-key: jiujia@mn991\n  bucket-name: cloud\nswagger:\n  enabled: true\n  title: 指挥调度平台API\n  license: 久嘉智能\n  licenseUrl: https://baidu.com/\n  terms-of-service-url: https://baidu.com/\n  prefix: ${spring.application.name}\n  version: 1.0\n  contact:\n    email: yyh@longtell.cn\n    url: https://baidu.com\n  authorization:\n    name: jj OAuth\n    auth-regex: ^.*$\n    authorization-scope-list:\n      - scope: all\n        description: server all\n    token-url-list:\n      - http://106.53.81.243:9152/platform-auth/oauth/token', 'de3cee8373b1d9a6d8a98d7125eeb6f0', '2021-03-30 19:35:38', '2021-12-16 16:57:50', NULL, '119.123.69.117', '', '', '', '', '', 'yaml', '');

-- ----------------------------
-- Table structure for config_info_aggr
-- ----------------------------
DROP TABLE IF EXISTS `config_info_aggr`;
CREATE TABLE `config_info_aggr`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `datum_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'datum_id',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '内容',
  `gmt_modified` datetime(0) NOT NULL COMMENT '修改时间',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfoaggr_datagrouptenantdatum`(`data_id`, `group_id`, `tenant_id`, `datum_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '增加租户字段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_info_aggr
-- ----------------------------

-- ----------------------------
-- Table structure for config_info_beta
-- ----------------------------
DROP TABLE IF EXISTS `config_info_beta`;
CREATE TABLE `config_info_beta`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `beta_ips` varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'betaIps',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `gmt_modified` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT 'source user',
  `src_ip` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'source ip',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfobeta_datagrouptenant`(`data_id`, `group_id`, `tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'config_info_beta' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_info_beta
-- ----------------------------

-- ----------------------------
-- Table structure for config_info_tag
-- ----------------------------
DROP TABLE IF EXISTS `config_info_tag`;
CREATE TABLE `config_info_tag`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT 'tenant_id',
  `tag_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'tag_id',
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `gmt_modified` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT 'source user',
  `src_ip` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'source ip',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfotag_datagrouptenanttag`(`data_id`, `group_id`, `tenant_id`, `tag_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'config_info_tag' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_info_tag
-- ----------------------------

-- ----------------------------
-- Table structure for config_tags_relation
-- ----------------------------
DROP TABLE IF EXISTS `config_tags_relation`;
CREATE TABLE `config_tags_relation`  (
  `id` bigint(0) NOT NULL COMMENT 'id',
  `tag_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'tag_name',
  `tag_type` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'tag_type',
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT 'tenant_id',
  `nid` bigint(0) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`nid`) USING BTREE,
  UNIQUE INDEX `uk_configtagrelation_configidtag`(`id`, `tag_name`, `tag_type`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'config_tag_relation' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_tags_relation
-- ----------------------------

-- ----------------------------
-- Table structure for group_capacity
-- ----------------------------
DROP TABLE IF EXISTS `group_capacity`;
CREATE TABLE `group_capacity`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
  `quota` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '配额，0表示使用默认值',
  `usage` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用量',
  `max_size` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '聚合子配置最大个数，，0表示使用默认值',
  `max_aggr_size` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '最大变更历史数量',
  `gmt_create` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `gmt_modified` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_id`(`group_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '集群、各Group容量信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of group_capacity
-- ----------------------------

-- ----------------------------
-- Table structure for his_config_info
-- ----------------------------
DROP TABLE IF EXISTS `his_config_info`;
CREATE TABLE `his_config_info`  (
  `id` bigint(0) UNSIGNED NOT NULL,
  `nid` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT,
  `data_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `group_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `app_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `gmt_create` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `gmt_modified` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `src_user` text CHARACTER SET utf8 COLLATE utf8_bin NULL,
  `src_ip` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `op_type` char(10) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`nid`) USING BTREE,
  INDEX `idx_gmt_create`(`gmt_create`) USING BTREE,
  INDEX `idx_gmt_modified`(`gmt_modified`) USING BTREE,
  INDEX `idx_did`(`data_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '多租户改造' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of his_config_info
-- ----------------------------

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions`  (
  `role` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `resource` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `action` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  UNIQUE INDEX `uk_role_permission`(`role`, `resource`, `action`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permissions
-- ----------------------------

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `role` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  UNIQUE INDEX `idx_user_role`(`username`, `role`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES ('nacos', 'ROLE_ADMIN');

-- ----------------------------
-- Table structure for tenant_capacity
-- ----------------------------
DROP TABLE IF EXISTS `tenant_capacity`;
CREATE TABLE `tenant_capacity`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'Tenant ID',
  `quota` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '配额，0表示使用默认值',
  `usage` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用量',
  `max_size` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '聚合子配置最大个数',
  `max_aggr_size` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '最大变更历史数量',
  `gmt_create` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `gmt_modified` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_id`(`tenant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '租户容量信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tenant_capacity
-- ----------------------------

-- ----------------------------
-- Table structure for tenant_info
-- ----------------------------
DROP TABLE IF EXISTS `tenant_info`;
CREATE TABLE `tenant_info`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `kp` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'kp',
  `tenant_id` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT 'tenant_id',
  `tenant_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT 'tenant_name',
  `tenant_desc` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'tenant_desc',
  `create_source` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'create_source',
  `gmt_create` bigint(0) NOT NULL COMMENT '创建时间',
  `gmt_modified` bigint(0) NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_info_kptenantid`(`kp`, `tenant_id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = 'tenant_info' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tenant_info
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `password` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES ('nacos', '$2a$10$ovg0thLRAGNLPYufrwdZ8.iPE3iFkwQM2ffAZhS.GXPeuHnu5Y3TW', 1);

SET FOREIGN_KEY_CHECKS = 1;
