--------------------------------------------------
-- 1. 기존 테이블 삭제
--------------------------------------------------
DROP TABLE IF EXISTS cards CASCADE;
DROP TABLE IF EXISTS monthly_consumption_report CASCADE;
DROP TABLE IF EXISTS subcategory CASCADE;
DROP TABLE IF EXISTS card_templates CASCADE;
DROP TABLE IF EXISTS report_cards CASCADE;
DROP TABLE IF EXISTS consumption_patterns CASCADE;
DROP TABLE IF EXISTS report_card_categories CASCADE;
DROP TABLE IF EXISTS bank_transactions CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS monthly_transaction_summary CASCADE;
DROP TABLE IF EXISTS user_card_benefit CASCADE;
DROP TABLE IF EXISTS card_transactions CASCADE;
DROP TABLE IF EXISTS card_companies CASCADE;
DROP TABLE IF EXISTS recommended_cards CASCADE;
DROP TABLE IF EXISTS merchants CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS benefit_templates CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS pre_benefits CASCADE;

--------------------------------------------------
-- 2. 테이블 생성
--------------------------------------------------

/* 카드 정보 테이블 */
CREATE TABLE cards
(
    card_id            SERIAL    NOT NULL,
    user_id            INT       NOT NULL,
    card_template_id   INT       NOT NULL,
    card_unique_number VARCHAR(255),
    expiry_date        DATE,
    card_order         SMALLINT,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at         TIMESTAMP,
    is_expried         SMALLINT  NOT NULL DEFAULT 0,
    annual_fee         INT       NOT NULL DEFAULT 0,
    permanent_token    VARCHAR(255),
    payment_card_order SMALLINT,
    payment_created_at TIMESTAMP,
    spending_tier      SMALLINT,
    pay_count          SMALLINT  NOT NULL,
    CONSTRAINT PK_CARDS PRIMARY KEY (card_id)
);

/* 월별 소비내역 리포트 테이블 */
CREATE TABLE monthly_consumption_report
(
    report_id              INT        NOT NULL,
    consumption_pattern_id VARCHAR(3) NOT NULL,
    consumption_pattern    VARCHAR(100),
    report_descrpition     TEXT,
    over_consumption       SMALLINT,
    variation              SMALLINT,
    extrovert              SMALLINT,
    created_at             TIMESTAMP  NOT NULL,
    CONSTRAINT PK_MONTHLY_CONSUMPTION_REPORT PRIMARY KEY (report_id)
);

/* 업종 소분류 테이블 */
CREATE TABLE subcategory
(
    subcategory_id   SERIAL       NOT NULL,
    subcategory_name VARCHAR(100) NOT NULL,
    category_id      INT          NOT NULL,
    CONSTRAINT PK_SUBCATEGORY PRIMARY KEY (subcategory_id)
);

/* 카드 템플릿 테이블 */
CREATE TABLE card_templates
(
    card_template_id       SERIAL       NOT NULL,
    card_company_id        SMALLINT     NOT NULL,
    card_name              VARCHAR(100) NOT NULL,
    card_img_url           VARCHAR(255) NOT NULL,
    god_name               VARCHAR(10),
    god_img_url            VARCHAR(255),
    annual_fee             INT          NOT NULL DEFAULT 0,
    card_type              VARCHAR(10)  NOT NULL, -- ENUM('신용','체크') 대신
    spending_max_tier      SMALLINT     NOT NULL,
    max_performance_amount INT          NOT NULL,
    benefit_text           TEXT,
    CONSTRAINT PK_CARD_TEMPLATES PRIMARY KEY (card_template_id)
);

/* 카드별 소비내역 리포트 테이블 */
CREATE TABLE report_cards
(
    report_card_id        SERIAL    NOT NULL,
    card_id               INT       NOT NULL,
    report_id             INT       NOT NULL,
    month_spending_amount INT       NOT NULL,
    month_benefit_amount  INT       NOT NULL,
    created_at            TIMESTAMP NOT NULL,
    spending_tier         SMALLINT,
    CONSTRAINT PK_REPORT_CARDS PRIMARY KEY (report_card_id)
);

/* 소비 패턴 테이블 */
CREATE TABLE consumption_patterns
(
    consumption_pattern_id VARCHAR(3)   NOT NULL,
    name                   VARCHAR(20)  NOT NULL,
    description            VARCHAR(100) NOT NULL,
    CONSTRAINT PK_CONSUMPTION_PATTERNS PRIMARY KEY (consumption_pattern_id)
);

/* 카드별 소비내역 상세(카테고리) 테이블 */
CREATE TABLE report_card_categories
(
    report_category_id      SERIAL    NOT NULL,
    report_card_id          INT       NOT NULL,
    category_id             INT       NOT NULL,
    merchant_id             INT       NOT NULL,
    amount                  INT       NOT NULL,
    received_benefit_amount INT       NOT NULL,
    created_at              TIMESTAMP NOT NULL,
    count                   VARCHAR(255),
    CONSTRAINT PK_REPORT_CARD_CATEGORIES PRIMARY KEY (report_category_id)
);

/* 은행 거래 테이블 */
CREATE TABLE bank_transactions
(
    transaction_id   INT         NOT NULL,
    card_company_id  SMALLINT    NOT NULL,
    transaction_type VARCHAR(10) NOT NULL, -- ENUM('입금','출금') 대신
    account_number   VARCHAR(30) NOT NULL,
    transaction_memo VARCHAR(30),
    CONSTRAINT PK_BANK_TRANSACTIONS PRIMARY KEY (transaction_id)
);

/* 유저 테이블 */
CREATE TABLE users
(
    user_id                SERIAL      NOT NULL,
    consumption_pattern_id SMALLINT,
    user_name              VARCHAR(10) NOT NULL,
    user_email             VARCHAR(100),
    hashed_pin_number      VARCHAR(64) NOT NULL,
    phone_number           VARCHAR(15) NOT NULL,
    phone_serial_number    VARCHAR(100),
    user_api_key           VARCHAR(40),
    created_at             TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP,
    deleted_at             TIMESTAMP,
    latest_load_data_at    TIMESTAMP,
    average_monthly_income INT,
    CONSTRAINT PK_USERS PRIMARY KEY (user_id)
);

/* 월별 거래 요약 테이블 */
CREATE TABLE monthly_transaction_summary
(
    report_id               SERIAL    NOT NULL,
    user_id                 INT       NOT NULL,
    year                    INT       NOT NULL,
    month                   INT       NOT NULL,
    total_spending          INT       NOT NULL,
    received_benefit_amount INT       NOT NULL,
    created_at              TIMESTAMP NOT NULL,
    CONSTRAINT PK_MONTHLY_TRANSACTION_SUMMARY PRIMARY KEY (report_id)
);

/* 유저 카드 혜택 테이블 */
CREATE TABLE user_card_benefit
(
    user_id2             INT       NOT NULL,
    benefit_template_id2 INT       NOT NULL,
    spending_tier        SMALLINT  NOT NULL,
    benefit_count        SMALLINT  NOT NULL,
    benefit_amount       INT       NOT NULL,
    reset_date           TIMESTAMP NOT NULL,
    update_date          TIMESTAMP NOT NULL,
    CONSTRAINT PK_USER_CARD_BENEFIT PRIMARY KEY (user_id2, benefit_template_id2)
);

/* 카드 거래 테이블 */
CREATE TABLE card_transactions
(
    transaction_id     INT      NOT NULL,
    card_company_id    SMALLINT NOT NULL,
    merchant_id        INT      NOT NULL,
    card_unique_number VARCHAR(255),
    status             VARCHAR(10), -- ENUM('승인','거절','취소') 대신
    benefit_type       VARCHAR(10), -- ENUM('즉시 할인','청구 할인','캐시백') 대신
    benefit_amount     INT,
    CONSTRAINT PK_CARD_TRANSACTIONS PRIMARY KEY (transaction_id)
);

/* 카드사 테이블 */
CREATE TABLE card_companies
(
    card_company_id SERIAL      NOT NULL,
    company_name    VARCHAR(30) NOT NULL,
    CONSTRAINT PK_CARD_COMPANIES PRIMARY KEY (card_company_id)
);

/* 추천 카드 테이블 */
CREATE TABLE recommended_cards
(
    recomemdedcard    INT  NOT NULL,
    user_id           INT  NOT NULL,
    card_template_id  INT  NOT NULL,
    category_id       INT,
    if_benefit_type   VARCHAR(10), -- ENUM('할인','적립') 대신
    if_benefit_amount INT,
    reason            VARCHAR(100),
    created_at        DATE NOT NULL,
    CONSTRAINT PK_RECOMMENDED_CARDS PRIMARY KEY (recomemdedcard)
);

/* 가맹점 테이블 */
CREATE TABLE merchants
(
    merchant_id    SERIAL    NOT NULL,
    subcategory_id INT       NOT NULL,
    merchant_name  VARCHAR(100),
    brand_name     VARCHAR(100),
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at     TIMESTAMP,
    CONSTRAINT PK_MERCHANTS PRIMARY KEY (merchant_id)
);

/* 거래 테이블 */
CREATE TABLE transactions
(
    transaction_id SERIAL    NOT NULL,
    user_id        INT       NOT NULL,
    amount         INT       NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT PK_TRANSACTIONS PRIMARY KEY (transaction_id)
);

/* 혜택 템플릿 테이블 */
CREATE TABLE benefit_templates
(
    benefit_template_id           SERIAL      NOT NULL,
    card_template_id              INT         NOT NULL,
    merchant_id                   INT,
    category_id                   INT         NOT NULL,
    subcategory_id                INT         NOT NULL,
    max_benefit_limit             INT,
    max_benefit_count_limit_month SMALLINT,
    max_benefit_count_limit_year  SMALLINT,
    benefit_type                  VARCHAR(10) NOT NULL, -- ENUM('할인','적립','쿠폰') 대신
    benefit_amount                INT,
    spending_tier                 SMALLINT,
    spending_min_amount           INT,
    spending_max_amount           INT,
    coverage_type                 VARCHAR(10) NOT NULL, -- ENUM("퍼센트", "금액") 대신
    CONSTRAINT PK_BENEFIT_TEMPLATES PRIMARY KEY (benefit_template_id)
);

/* 업종 대분류 테이블 */
CREATE TABLE category
(
    category_id   SERIAL       NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    CONSTRAINT PK_CATEGORY PRIMARY KEY (category_id)
);

/* 사전 혜택 테이블 */
CREATE TABLE pre_benefits
(
    user_id             INT NOT NULL,
    card_id             INT NOT NULL,
    card_template_id    INT NOT NULL,
    amount              INT NOT NULL,
    if_benefit_type     VARCHAR(10), -- ENUM('할인','적립') 대신
    if_benefit_amount   INT,
    real_benefit_type   VARCHAR(10), -- ENUM('할인','적립') 대신
    real_benefit_amount INT,
    merchant_name       VARCHAR(100),
    CONSTRAINT PK_PRE_BENEFITS PRIMARY KEY (user_id)
);

--------------------------------------------------
-- 3. 외래키 설정
--------------------------------------------------
ALTER TABLE cards
    ADD CONSTRAINT FK_users_TO_cards_1 FOREIGN KEY (user_id) REFERENCES users (user_id);
ALTER TABLE cards
    ADD CONSTRAINT FK_card_templates_TO_cards_1 FOREIGN KEY (card_template_id) REFERENCES card_templates (card_template_id);

ALTER TABLE monthly_consumption_report
    ADD CONSTRAINT FK_monthly_transaction_summary_TO_monthly_consumption_report_1 FOREIGN KEY (report_id) REFERENCES monthly_transaction_summary (report_id);
ALTER TABLE monthly_consumption_report
    ADD CONSTRAINT FK_consumption_patterns_TO_monthly_consumption_report_1 FOREIGN KEY (consumption_pattern_id) REFERENCES consumption_patterns (consumption_pattern_id);

ALTER TABLE subcategory
    ADD CONSTRAINT FK_category_TO_subcategory_1 FOREIGN KEY (category_id) REFERENCES category (category_id);

ALTER TABLE card_templates
    ADD CONSTRAINT FK_card_companies_TO_card_templates_1 FOREIGN KEY (card_company_id) REFERENCES card_companies (card_company_id);

ALTER TABLE report_cards
    ADD CONSTRAINT FK_cards_TO_report_cards_1 FOREIGN KEY (card_id) REFERENCES cards (card_id);
ALTER TABLE report_cards
    ADD CONSTRAINT FK_monthly_transaction_summary_TO_report_cards_1 FOREIGN KEY (report_id) REFERENCES monthly_transaction_summary (report_id);

ALTER TABLE report_card_categories
    ADD CONSTRAINT FK_report_cards_TO_report_card_categories_1 FOREIGN KEY (report_card_id) REFERENCES report_cards (report_card_id);
ALTER TABLE report_card_categories
    ADD CONSTRAINT FK_subcategory_TO_report_card_categories_1 FOREIGN KEY (category_id) REFERENCES subcategory (subcategory_id);
ALTER TABLE report_card_categories
    ADD CONSTRAINT FK_merchants_TO_report_card_categories_1 FOREIGN KEY (merchant_id) REFERENCES merchants (merchant_id);

ALTER TABLE bank_transactions
    ADD CONSTRAINT FK_transactions_TO_bank_transactions_1 FOREIGN KEY (transaction_id) REFERENCES transactions (transaction_id);
ALTER TABLE bank_transactions
    ADD CONSTRAINT FK_card_companies_TO_bank_transactions_1 FOREIGN KEY (card_company_id) REFERENCES card_companies (card_company_id);

ALTER TABLE users
    ADD CONSTRAINT FK_consumption_patterns_TO_users_1 FOREIGN KEY (consumption_pattern_id) REFERENCES consumption_patterns (consumption_pattern_id);

ALTER TABLE monthly_transaction_summary
    ADD CONSTRAINT FK_users_TO_monthly_transaction_summary_1 FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE user_card_benefit
    ADD CONSTRAINT FK_users_TO_user_card_benefit_1 FOREIGN KEY (user_id2) REFERENCES users (user_id);
ALTER TABLE user_card_benefit
    ADD CONSTRAINT FK_benefit_templates_TO_user_card_benefit_1 FOREIGN KEY (benefit_template_id2) REFERENCES benefit_templates (benefit_template_id);

ALTER TABLE card_transactions
    ADD CONSTRAINT FK_transactions_TO_card_transactions_1 FOREIGN KEY (transaction_id) REFERENCES transactions (transaction_id);
ALTER TABLE card_transactions
    ADD CONSTRAINT FK_card_companies_TO_card_transactions_1 FOREIGN KEY (card_company_id) REFERENCES card_companies (card_company_id);
ALTER TABLE card_transactions
    ADD CONSTRAINT FK_merchants_TO_card_transactions_1 FOREIGN KEY (merchant_id) REFERENCES merchants (merchant_id);

ALTER TABLE recommended_cards
    ADD CONSTRAINT FK_users_TO_recommended_cards_1 FOREIGN KEY (user_id) REFERENCES users (user_id);
ALTER TABLE recommended_cards
    ADD CONSTRAINT FK_card_templates_TO_recommended_cards_1 FOREIGN KEY (card_template_id) REFERENCES card_templates (card_template_id);
ALTER TABLE recommended_cards
    ADD CONSTRAINT FK_category_TO_recommended_cards_1 FOREIGN KEY (category_id) REFERENCES category (category_id);

ALTER TABLE merchants
    ADD CONSTRAINT FK_subcategory_TO_merchants_1 FOREIGN KEY (subcategory_id) REFERENCES subcategory (subcategory_id);

ALTER TABLE transactions
    ADD CONSTRAINT FK_users_TO_transactions_1 FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE benefit_templates
    ADD CONSTRAINT FK_card_templates_TO_benefit_templates_1 FOREIGN KEY (card_template_id) REFERENCES card_templates (card_template_id);
ALTER TABLE benefit_templates
    ADD CONSTRAINT FK_merchants_TO_benefit_templates_1 FOREIGN KEY (merchant_id) REFERENCES merchants (merchant_id);
ALTER TABLE benefit_templates
    ADD CONSTRAINT FK_category_TO_benefit_templates_1 FOREIGN KEY (category_id) REFERENCES category (category_id);
ALTER TABLE benefit_templates
    ADD CONSTRAINT FK_subcategory_TO_benefit_templates_1 FOREIGN KEY (subcategory_id) REFERENCES subcategory (subcategory_id);

ALTER TABLE pre_benefits
    ADD CONSTRAINT FK_users_TO_pre_benefits_1 FOREIGN KEY (user_id) REFERENCES users (user_id);
ALTER TABLE pre_benefits
    ADD CONSTRAINT FK_cards_TO_pre_benefits_1 FOREIGN KEY (card_id) REFERENCES cards (card_id);
ALTER TABLE pre_benefits
    ADD CONSTRAINT FK_card_templates_TO_pre_benefits_1 FOREIGN KEY (card_template_id) REFERENCES card_templates (card_template_id);