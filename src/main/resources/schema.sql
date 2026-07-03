-- ============================================================
-- 因子管理子系统 - 缺失表 DDL
-- 已存在的表（由 Python 同步模块创建，此处不重复定义）：
--   ak_fund_profile, ak_fund_nav, ak_fund_portfolio,
--   ak_fund_dividend, ak_fund_manager, ak_fund_company,
--   ak_market_quote, ak_index_quote, ak_macro_indicator,
--   ak_factor_series, ak_source_task, ak_source_raw_payload,
--   ak_sync_audit
-- ============================================================

SET search_path TO biz_factor;

-- ============================================================
-- Python 同步模块需要的表（原由 akshare-factor-sync 创建）
-- ============================================================

-- 基金基本信息
CREATE TABLE IF NOT EXISTS ak_fund_profile (
    fund_code VARCHAR(32) NOT NULL,
    fund_name VARCHAR(255) NOT NULL,
    fund_type VARCHAR(128),
    company_name VARCHAR(255),
    manager_name VARCHAR(255),
    issue_date DATE,
    setup_date DATE,
    fund_size VARCHAR(64),
    fee_rate VARCHAR(64),
    source_url TEXT,
    source_system VARCHAR(64) NOT NULL DEFAULT 'akshare',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ak_fund_profile PRIMARY KEY (fund_code)
);

-- 基金净值
CREATE TABLE IF NOT EXISTS ak_fund_nav (
    fund_code VARCHAR(32) NOT NULL,
    fund_name VARCHAR(255),
    trade_date DATE NOT NULL,
    nav_value NUMERIC(24, 8),
    accum_nav_value NUMERIC(24, 8),
    daily_growth_rate NUMERIC(24, 8),
    source_system VARCHAR(64) NOT NULL DEFAULT 'akshare',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ak_fund_nav PRIMARY KEY (fund_code, trade_date)
);

-- ============================================================
-- 基础业务表
-- ============================================================

-- 基础因子定义
CREATE TABLE IF NOT EXISTS base_factor (
    id VARCHAR(64) PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category_id VARCHAR(64),
    data_type VARCHAR(32),
    unit VARCHAR(32),
    update_frequency VARCHAR(32),
    data_source VARCHAR(64),
    fetch_logic TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    derivable BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 基础因子值
CREATE TABLE IF NOT EXISTS base_factor_value (
    id VARCHAR(64) PRIMARY KEY,
    fund_code VARCHAR(32) NOT NULL,
    base_factor_id VARCHAR(64) NOT NULL,
    data_date DATE NOT NULL,
    value NUMERIC(24, 8),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 因子分类
CREATE TABLE IF NOT EXISTS factor_category (
    id VARCHAR(64) PRIMARY KEY,
    parent_id VARCHAR(64),
    name VARCHAR(255) NOT NULL,
    cat_level INT NOT NULL DEFAULT 1,
    sort_no INT NOT NULL DEFAULT 0,
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- 衍生因子
CREATE TABLE IF NOT EXISTS derivative_factor (
    id VARCHAR(64) PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_by VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    formula TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);
ALTER TABLE derivative_factor ADD COLUMN IF NOT EXISTS formula TEXT;

-- 衍生因子组成项
CREATE TABLE IF NOT EXISTS derivative_factor_item (
    id VARCHAR(64) PRIMARY KEY,
    derivative_factor_id VARCHAR(64) NOT NULL REFERENCES derivative_factor(id),
    base_factor_id VARCHAR(64) NOT NULL REFERENCES base_factor(id),
    weight NUMERIC(24, 8) NOT NULL
);

-- 衍生因子值
CREATE TABLE IF NOT EXISTS derivative_factor_value (
    id VARCHAR(64) PRIMARY KEY,
    fund_code VARCHAR(32) NOT NULL,
    derivative_factor_id VARCHAR(64) NOT NULL,
    data_date DATE NOT NULL,
    value NUMERIC(24, 8),
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 风格因子定义
CREATE TABLE IF NOT EXISTS style_factor (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_by VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- 风格因子组成项
CREATE TABLE IF NOT EXISTS style_factor_item (
    id VARCHAR(64) PRIMARY KEY,
    style_factor_id VARCHAR(64) NOT NULL REFERENCES style_factor(id),
    derivative_factor_id VARCHAR(64) NOT NULL REFERENCES derivative_factor(id),
    weight NUMERIC(24, 8) NOT NULL
);

-- 风格因子值
CREATE TABLE IF NOT EXISTS style_factor_value (
    id VARCHAR(64) PRIMARY KEY,
    fund_code VARCHAR(32) NOT NULL,
    style_factor_id VARCHAR(64) NOT NULL,
    data_date DATE NOT NULL,
    value NUMERIC(24, 8),
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 账号
CREATE TABLE IF NOT EXISTS account (
    id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    display_name VARCHAR(255),
    user_type VARCHAR(32) NOT NULL,
    role_id VARCHAR(64),
    tenant_id VARCHAR(64),
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- 角色
CREATE TABLE IF NOT EXISTS role (
    id VARCHAR(64) PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    user_type VARCHAR(32) NOT NULL,
    scope VARCHAR(32) NOT NULL,
    permissions TEXT,
    built_in BOOLEAN NOT NULL DEFAULT FALSE
);

-- ============================================================
-- 初始数据（与原有 InMemory 数据一致）
-- ============================================================

-- 因子分类（完整因子树）
INSERT INTO factor_category (id, parent_id, name, cat_level, sort_no, description, enabled) VALUES
    ('cat-1', NULL, '费率水平', 1, 1, '费率相关因子分类', TRUE),
    ('cat-1-1', 'cat-1', '管理费率', 2, 1, '基金管理费率', TRUE),
    ('cat-1-2', 'cat-1', '运作费率', 2, 2, '基金运作费率（管理+托管+销售服务费）', TRUE),
    ('cat-1-3', 'cat-1', '托管费率', 2, 3, '基金托管费率', TRUE),
    ('cat-2', NULL, '规模与仓位', 1, 2, '规模和仓位因子分类', TRUE),
    ('cat-2-1', 'cat-2', '最新规模', 2, 1, '基金最新资产规模', TRUE),
    ('cat-2-2', 'cat-2', '最新份额', 2, 2, '基金最新份额', TRUE),
    ('cat-2-3', 'cat-2', '最新仓位', 2, 3, '基金最新仓位（股票仓位）', TRUE),
    ('cat-2-4', 'cat-2', '近期平均规模', 2, 4, '近3个月平均资产规模', TRUE),
    ('cat-2-5', 'cat-2', '历史最大规模', 2, 5, '基金历史最大资产规模', TRUE),
    ('cat-3', NULL, '收益表现', 1, 3, '收益表现因子分类', TRUE),
    ('cat-3-1', 'cat-3', '单位净值', 2, 1, '基金单位净值', TRUE),
    ('cat-3-2', 'cat-3', '累计净值', 2, 2, '基金累计净值', TRUE),
    ('cat-3-3', 'cat-3', '日增长率', 2, 3, '基金日增长率', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 基础因子
INSERT INTO base_factor (id, code, name, category_id, data_type, unit, update_frequency, data_source, fetch_logic, enabled, derivable, description) VALUES
    ('bf-1', 'management_fee', '管理费率', 'cat-1-1', '数值型', '%', '月度', 'Wind', '基金公告/费率表', TRUE, TRUE, '基金管理费率，按基金类型估算'),
    ('bf-2', 'operation_fee', '运作费率', 'cat-1-2', '数值型', '%', '月度', 'Wind', '基金公告/费率表', TRUE, TRUE, '基金运作费率（管理+托管+销售服务费合计）'),
    ('bf-3', 'custodian_fee', '托管费率', 'cat-1-3', '数值型', '%', '月度', 'Wind', '基金公告/费率表', TRUE, TRUE, '基金托管费率'),
    ('bf-4', 'latest_scale', '最新规模', 'cat-2-1', '数值型', '亿元', '日频', 'Wind', '基金规模数据', TRUE, TRUE, '基金最新资产规模'),
    ('bf-5', 'latest_share', '最新份额', 'cat-2-2', '数值型', '亿份', '日频', 'Wind', '基金份额数据', TRUE, TRUE, '基金最新份额'),
    ('bf-6', 'latest_position', '最新仓位', 'cat-2-3', '数值型', '%', '季度', 'Wind', '基金季报', TRUE, TRUE, '基金最新股票仓位'),
    ('bf-7', 'recent_avg_scale', '近期平均规模', 'cat-2-4', '数值型', '亿元', '月度', 'Wind', '基金规模数据', TRUE, TRUE, '近3个月平均资产规模'),
    ('bf-8', 'historical_max_scale', '历史最大规模', 'cat-2-5', '数值型', '亿元', '一次性', 'Wind', '基金规模数据', TRUE, TRUE, '基金历史最大资产规模'),
    ('bf-9', 'nav', '单位净值', 'cat-3-1', '数值型', '元', '日频', 'Wind', '每日净值', TRUE, TRUE, '基金单位净值'),
    ('bf-10', 'accum_nav', '累计净值', 'cat-3-2', '数值型', '元', '日频', 'Wind', '每日净值', TRUE, TRUE, '基金累计净值'),
    ('bf-11', 'daily_growth', '日增长率', 'cat-3-3', '数值型', '%', '日频', 'Wind', '每日净值', TRUE, TRUE, '基金日增长率')
ON CONFLICT (id) DO NOTHING;

-- 账号（登录用）
INSERT INTO account (id, username, password_hash, display_name, user_type, role_id, tenant_id, enabled) VALUES
    ('a1', 'admin', 'admin123', '系统超级管理员', 'SYSTEM_ADMIN', 'r1', NULL, TRUE),
    ('a2', 'trader', 'trader123', '交易员', 'TRADER', 'r2', 't1', TRUE),
    ('a3', 'customer', 'customer123', '客户', 'CUSTOMER', 'r3', 't1', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 角色
INSERT INTO role (id, code, name, user_type, scope, permissions, built_in) VALUES
    ('r1', 'SYSTEM_ADMIN', '系统超级管理员', 'SYSTEM_ADMIN', 'SYSTEM', 'TENANT_MANAGE,ORGANIZATION_MANAGE,ACCOUNT_MANAGE,ROLE_TEMPLATE_CONFIG,PERMISSION_MENU_CONFIG,SYSTEM_PARAMETER_CONFIG,RISK_THRESHOLD_CONFIG,LOG_AUDIT,DATA_BACKUP,API_KEY_MANAGE,FACTOR_READ,FACTOR_MANAGE,FACTOR_TREE_MANAGE,DERIVED_FACTOR_MANAGE,STYLE_FACTOR_MANAGE,TRADE_ORDER_VIEW,TRADE_ORDER_EXECUTE,TRADE_ORDER_REVIEW,PORTFOLIO_VIEW,AGREEMENT_VIEW,AGREEMENT_SIGN,REDEMPTION_REQUEST,DISCLOSURE_VIEW,EXTENDABLE', TRUE),
    ('r2', 'TRADER', '交易员', 'TRADER', 'BUSINESS', 'TRADE_ORDER_VIEW,TRADE_ORDER_EXECUTE,TRADE_ORDER_REVIEW,PORTFOLIO_VIEW,DISCLOSURE_VIEW', TRUE),
    ('r3', 'CUSTOMER', '客户', 'CUSTOMER', 'BUSINESS', 'PORTFOLIO_VIEW,AGREEMENT_VIEW,AGREEMENT_SIGN,REDEMPTION_REQUEST,DISCLOSURE_VIEW', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 衍生因子（示例）
INSERT INTO derivative_factor (id, code, name, created_by, created_at, description, enabled) VALUES
    ('df-1', 'fee_bundle', '费率组合因子', 'system', CURRENT_TIMESTAMP, '管理费率、运作费率、托管费率组合', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 风格因子（示例）
INSERT INTO style_factor (id, name, created_by, created_at, description, enabled) VALUES
    ('sf-1', '稳健风格因子', 'system', CURRENT_TIMESTAMP, '稳健收益风格', TRUE)
ON CONFLICT (id) DO NOTHING;
