-- ============================================
-- eattea 数据库初始化脚本
-- 在 MySQL 8 中执行：
--   1. CREATE DATABASE eattea CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
--   2. USE eattea;
--   3. source init.sql;
-- ============================================

-- 监管文档表
CREATE TABLE IF NOT EXISTS eattea_document (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(500)   NOT NULL COMMENT '文档标题',
    file_name    VARCHAR(500)   NOT NULL COMMENT '原始文件名',
    file_type    VARCHAR(20)    NOT NULL COMMENT '文件类型: pdf/doc/docx/xls/xlsx',
    file_path    VARCHAR(1000)  NOT NULL COMMENT '存储路径',
    content      LONGTEXT       COMMENT '提取的文本内容',
    department   VARCHAR(200)   COMMENT '所属部门',
    doc_category VARCHAR(200)   COMMENT '文档分类/报送类型',
    publish_date DATE           COMMENT '发布日期',
    tags         VARCHAR(1000)  COMMENT '标签，逗号分隔',
    create_time  DATETIME       DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FULLTEXT INDEX ft_content (content) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监管文档';

-- 金融知识词条表
CREATE TABLE IF NOT EXISTS eattea_knowledge (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    term          VARCHAR(200)  NOT NULL COMMENT '术语',
    definition    TEXT          NOT NULL COMMENT '定义/解释',
    category      VARCHAR(200)  COMMENT '分类：票据、同业、债券、衍生品等',
    related_terms VARCHAR(1000) COMMENT '关联术语',
    source        VARCHAR(500)  COMMENT '出处',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FULLTEXT INDEX ft_knowledge (term, definition) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='金融知识词条';

-- 分类表
CREATE TABLE IF NOT EXISTS eattea_category (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100) NOT NULL COMMENT '分类名称',
    type      VARCHAR(20)  NOT NULL COMMENT '类型: document/knowledge',
    parent_id BIGINT       DEFAULT 0 COMMENT '父分类ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类标签';

-- ============================================
-- 预置金融业务知识词条（票据、同业、债券为主）
-- ============================================
INSERT INTO eattea_knowledge (term, definition, category) VALUES
('票据', '由出票人签发的，约定自己或委托他人无条件支付一定金额的有价证券。在我国金融监管语境下，通常指商业汇票（银行承兑汇票和商业承兑汇票）。', '票据'),
('银行承兑汇票', '由银行作为承兑人，承诺在汇票到期日支付票面金额的票据。信用等级高，是票据市场的主要品种。', '票据'),
('商业承兑汇票', '由企业作为承兑人，承诺在汇票到期日支付票面金额的票据。信用取决于出票企业的资信状况。', '票据'),
('票据贴现', '持票人在汇票到期日前，将票据权利转让给银行，银行扣除贴现利息后将余额支付给持票人的融资行为。', '票据'),
('转贴现', '商业银行将其已贴现但尚未到期的票据，向另一家商业银行进行贴现的融资行为。', '票据'),
('再贴现', '商业银行将其已贴现但尚未到期的票据，向中央银行进行贴现的融资行为，是央行的货币政策工具之一。', '票据'),
('票据背书', '持票人在票据背面记载有关事项并签名，将票据权利转让给他人的行为。背书应连续，否则可能影响票据权利的行使。', '票据'),
('同业业务', '金融机构之间开展的各项业务的总称，包括同业拆借、同业存款、同业借款、同业代付、买入返售和卖出回购等。', '同业'),
('同业拆借', '金融机构之间为了调剂临时性资金余缺而进行的短期资金融通行为，期限一般较短（隔夜至数月）。', '同业'),
('同业存单', '存款类金融机构在全国银行间市场发行的记账式定期存款凭证，是一种货币市场工具。', '同业'),
('买入返售', '交易一方（逆回购方）按照协议约定先买入金融资产，再按约定价格于到期日返售给交易对手的融资行为。实质上是以金融资产为质押的资金融出。', '同业'),
('卖出回购', '交易一方（正回购方）按照协议约定先卖出金融资产，再按约定价格于到期日从交易对手买回的融资行为。实质上是以金融资产为质押的资金融入。', '同业'),
('债券', '政府、金融机构、企业等为筹集资金而向投资者发行的、约定在一定期限内还本付息的债权债务凭证。', '债券'),
('国债', '中央政府为筹集财政资金而发行的债券，以国家信用为担保，被视为无风险资产。', '债券'),
('地方政府债券', '地方政府为筹集资金而发行的债券，分为一般债券和专项债券。', '债券'),
('金融债券', '银行和非银行金融机构发行的债券，用于补充资本金或筹集长期资金。', '债券'),
('企业债券', '非金融企业依照法定程序发行的债券。在我国分为公司债券、企业债券、银行间债务融资工具等。', '债券'),
('债券收益率', '投资债券所获得的回报率，通常用到期收益率（YTM）衡量，反映债券投资的综合收益水平。', '债券'),
('久期', '衡量债券价格对利率变动敏感度的指标。久期越长，利率变动对债券价格的影响越大。', '债券'),
('净价交易', '债券买卖时不包含应计利息的交易价格。实际结算金额 = 净价 + 应计利息。', '债券'),
('应计利息', '债券自上一个付息日以来累积的利息。在债券交易中，买方需向卖方支付应计利息。', '债券'),
('衍生品', '其价值取决于一种或多种基础资产（标的物）的金融合约，主要包括远期、期货、期权、互换四大类。', '衍生品'),
('利率互换', '交易双方约定在未来一定期限内，根据约定数量的名义本金交换利息现金流的合约。常见形式为固定利率与浮动利率互换。', '衍生品'),
('信用违约互换', 'CDS，信用衍生品的一种。买方定期向卖方支付费用，卖方承诺在参考实体发生信用事件时向买方支付补偿。', '衍生品'),
('资本充足率', '银行资本与风险加权资产的比率，是衡量银行资本充足性和抵御风险能力的核心监管指标。最低要求通常由巴塞尔协议和各国监管机构确定。', '监管指标'),
('不良贷款率', '不良贷款余额占总贷款余额的比例。不良贷款通常指五级分类中的次级、可疑和损失类贷款。', '监管指标'),
('拨备覆盖率', '贷款损失准备余额与不良贷款余额的比率，反映银行对不良贷款的损失弥补能力。', '监管指标'),
('流动性覆盖率', 'LCR，优质流动性资产与未来30天净现金流出量的比率，衡量银行短期流动性风险承受能力。', '监管指标'),
('杠杆率', '一级资本与表内外总资产风险暴露的比率，作为资本充足率指标的补充，不依赖风险加权计算。', '监管指标'),
('表外业务', '商业银行从事的、按会计准则不计入资产负债表，但可能形成或有资产或或有负债的业务，如担保、承诺、信用证等。', '综合'),
('信用风险', '因借款人或交易对手未能履行合同义务而导致损失的风险，是银行面临的最主要风险类型。', '综合'),
('市场风险', '因市场价格（利率、汇率、股票价格、商品价格等）的不利变动而导致损失的风险。', '综合'),
('操作风险', '由不完善或有问题的内部程序、员工、信息科技系统或外部事件导致损失的风险。', '综合'),
('流动性风险', '银行无法以合理成本及时获得充足资金，用于偿付到期债务、履行其他支付义务和满足正常业务开展所需的风险。', '综合');
