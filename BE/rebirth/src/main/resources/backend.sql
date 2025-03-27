-- 기존 테이블 삭제 (종속관계가 있는 경우 CASCADE 옵션 사용)
DROP TABLE IF EXISTS public.pre_benefits CASCADE;
DROP TABLE IF EXISTS public.user_card_benefit CASCADE;
DROP TABLE IF EXISTS public.report_card_categories CASCADE;
DROP TABLE IF EXISTS public.card_transactions CASCADE;
DROP TABLE IF EXISTS public.recommended_cards CASCADE;
DROP TABLE IF EXISTS public.merchants CASCADE;
DROP TABLE IF EXISTS public.subcategory CASCADE;
DROP TABLE IF EXISTS public.category CASCADE;
DROP TABLE IF EXISTS public.bank_transactions CASCADE;
DROP TABLE IF EXISTS public.transactions CASCADE;
DROP TABLE IF EXISTS public.report_cards CASCADE;
DROP TABLE IF EXISTS public.cards CASCADE;
DROP TABLE IF EXISTS public.card_templates CASCADE;
DROP TABLE IF EXISTS public.card_companies CASCADE;
DROP TABLE IF EXISTS public.monthly_consumption_report CASCADE;
DROP TABLE IF EXISTS public.monthly_transaction_summary CASCADE;
DROP TABLE IF EXISTS public.users CASCADE;
DROP TABLE IF EXISTS public.consumption_patterns CASCADE;
DROP TABLE IF EXISTS public.benefit_templates CASCADE;

------------------------------------------------------------
-- 테이블 생성 시작
------------------------------------------------------------

/* 1. 소비패턴 테이블
   - 소비 패턴의 식별자, 이름, 설명 정보를 저장 */
CREATE TABLE IF NOT EXISTS public.consumption_patterns (
                                                           consumption_pattern_id VARCHAR(3) NOT NULL CONSTRAINT pk_consumption_patterns PRIMARY KEY,
    name                   VARCHAR(20) NOT NULL,
    description            VARCHAR(100) NOT NULL
    );

/* 2. 사용자 테이블
   - 사용자의 기본 정보 및 소비 패턴과의 관계를 저장 */
CREATE TABLE IF NOT EXISTS public.users (
                                            user_id                BIGINT DEFAULT nextval('users_user_id_seq'::regclass) NOT NULL CONSTRAINT pk_users PRIMARY KEY,
    consumption_pattern_id VARCHAR(255) CONSTRAINT fk_consumption_patterns_to_users_1 REFERENCES public.consumption_patterns,
    user_name              VARCHAR(10) NOT NULL,
    user_email             VARCHAR(100),
    hashed_pin_number      VARCHAR(64) NOT NULL,
    phone_number           VARCHAR(15) NOT NULL,
    phone_serial_number    VARCHAR(100),
    user_api_key           VARCHAR(40),
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at             TIMESTAMP,
    deleted_at             TIMESTAMP,
    latest_load_data_at    TIMESTAMP,
    average_monthly_income INTEGER
    );

/* 3. 월별 거래 요약 테이블
   - 사용자별, 월별 거래 및 혜택 금액 등의 요약 정보를 저장 */
CREATE TABLE IF NOT EXISTS public.monthly_transaction_summary (
                                                                  report_id               SERIAL CONSTRAINT pk_monthly_transaction_summary PRIMARY KEY,
                                                                  user_id                 INTEGER NOT NULL CONSTRAINT fk_users_to_monthly_transaction_summary_1 REFERENCES public.users,
                                                                  year                    INTEGER NOT NULL,
                                                                  month                   INTEGER NOT NULL,
                                                                  total_spending          INTEGER NOT NULL,
                                                                  received_benefit_amount INTEGER NOT NULL,
                                                                  created_at              TIMESTAMP NOT NULL
);

/* 4. 월별 소비 보고서 테이블
   - 월별 거래 요약과 소비 패턴을 기반으로 상세 보고서를 저장 */
CREATE TABLE IF NOT EXISTS public.monthly_consumption_report (
                                                                 report_id              INTEGER NOT NULL CONSTRAINT pk_monthly_consumption_report PRIMARY KEY
                                                                 CONSTRAINT fk_monthly_transaction_summary_to_monthly_consumption_report_1 REFERENCES public.monthly_transaction_summary,
                                                                 consumption_pattern_id VARCHAR(3) NOT NULL CONSTRAINT fk_consumption_patterns_to_monthly_consumption_report_1 REFERENCES public.consumption_patterns,
    report_descrpition     TEXT,
    over_consumption       SMALLINT,
    variation              SMALLINT,
    extrovert              SMALLINT,
    created_at             TIMESTAMP NOT NULL
    );

/* 5. 카드사 테이블
   - 카드사의 식별자와 이름 정보를 저장 */
CREATE TABLE IF NOT EXISTS public.card_companies (
                                                     card_company_id SERIAL CONSTRAINT pk_card_companies PRIMARY KEY,
                                                     company_name    VARCHAR(30) NOT NULL
    );

/* 6. 카드 템플릿 테이블
   - 카드사와 연계된 카드 템플릿 정보를 저장하며, 카드 종류(신용/체크) 제약 조건 포함 */
CREATE TABLE IF NOT EXISTS public.card_templates (
                                                     card_template_id        SERIAL CONSTRAINT pk_card_templates PRIMARY KEY,
                                                     card_company_id         SMALLINT NOT NULL CONSTRAINT fk_card_companies_to_card_templates_1 REFERENCES public.card_companies,
                                                     card_name               VARCHAR(100) NOT NULL,
    card_img_url            VARCHAR(255) NOT NULL,
    card_deity_name         VARCHAR(50),
    card_deity_img_url      VARCHAR(255),
    annual_fee              INTEGER DEFAULT 0 NOT NULL,
    card_type               VARCHAR(10) NOT NULL
    CONSTRAINT card_templates_card_type_check CHECK ((card_type)::text = ANY ((ARRAY ['신용', '체크'])::text[])),
    card_detail_info        TEXT,
    card_constellation_info JSONB,
    benefit_conditions      JSONB,
    last_month_usage_ranges JSONB,
    god_img_url             VARCHAR(255),
    god_name                VARCHAR(10)
    );

/* 7. 카드 테이블
   - 사용자가 보유한 카드 정보를 저장 (사용자, 카드 템플릿과 연계) */
CREATE TABLE IF NOT EXISTS public.cards (
                                            card_id            SERIAL CONSTRAINT pk_cards PRIMARY KEY,
                                            user_id            INTEGER NOT NULL CONSTRAINT fk_users_to_cards_1 REFERENCES public.users,
                                            card_template_id   INTEGER NOT NULL CONSTRAINT fk_card_templates_to_cards_1 REFERENCES public.card_templates,
                                            card_unique_number VARCHAR(255),
    expiry_date        DATE,
    card_order         SMALLINT,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at         TIMESTAMP,
    is_expried         SMALLINT DEFAULT 0 NOT NULL,
    annual_fee         INTEGER DEFAULT 0 NOT NULL,
    permanent_token    VARCHAR(255),
    payment_card_order SMALLINT,
    payment_created_at TIMESTAMP,
    spending_tier      SMALLINT,
    pay_count          SMALLINT NOT NULL,
    card_number        VARCHAR(255) NOT NULL,
    is_expired         SMALLINT DEFAULT 0 NOT NULL
    );

/* 8. 리포트 카드 테이블
   - 카드와 월별 거래 요약 정보를 연결하여 리포트 정보를 저장 */
CREATE TABLE IF NOT EXISTS public.report_cards (
                                                   report_card_id        SERIAL CONSTRAINT pk_report_cards PRIMARY KEY,
                                                   card_id               INTEGER NOT NULL CONSTRAINT fk_cards_to_report_cards_1 REFERENCES public.cards,
                                                   report_id             INTEGER NOT NULL CONSTRAINT fk_monthly_transaction_summary_to_report_cards_1 REFERENCES public.monthly_transaction_summary,
                                                   month_spending_amount INTEGER NOT NULL,
                                                   month_benefit_amount  INTEGER NOT NULL,
                                                   created_at            TIMESTAMP NOT NULL,
                                                   spending_tier         SMALLINT
);

/* 9. 거래 테이블
   - 사용자의 개별 거래 내역을 저장 */
CREATE TABLE IF NOT EXISTS public.transactions (
                                                   transaction_id SERIAL CONSTRAINT pk_transactions PRIMARY KEY,
                                                   user_id        INTEGER NOT NULL CONSTRAINT fk_users_to_transactions_1 REFERENCES public.users,
                                                   amount         INTEGER NOT NULL,
                                                   created_at     TIMESTAMP DEFAULT now() NOT NULL
    );

/* 10. 은행 거래 테이블
    - 거래 내역에 추가하여, 카드사와 거래 유형(입금/출금) 등 세부 정보를 저장 */
CREATE TABLE IF NOT EXISTS public.bank_transactions (
                                                        transaction_id   INTEGER NOT NULL CONSTRAINT pk_bank_transactions PRIMARY KEY
                                                        CONSTRAINT fk_transactions_to_bank_transactions_1 REFERENCES public.transactions,
                                                        card_company_id  SMALLINT NOT NULL CONSTRAINT fk_card_companies_to_bank_transactions_1 REFERENCES public.card_companies,
                                                        transaction_type VARCHAR(10) NOT NULL
    CONSTRAINT bank_transactions_transaction_type_check CHECK ((transaction_type)::text = ANY ((ARRAY ['입금', '출금'])::text[])),
    account_number   VARCHAR(30) NOT NULL,
    transaction_memo VARCHAR(30)
    );

/* 11. 카테고리 테이블
    - 거래 혹은 혜택 관련 카테고리 정보를 저장 */
CREATE TABLE IF NOT EXISTS public.category (
                                               category_id   SERIAL CONSTRAINT pk_category PRIMARY KEY,
                                               category_name VARCHAR(100) NOT NULL
    );

/* 12. 서브 카테고리 테이블
    - 카테고리에 속하는 서브 카테고리 정보를 저장 */
CREATE TABLE IF NOT EXISTS public.subcategory (
                                                  subcategory_id   SERIAL CONSTRAINT pk_subcategory PRIMARY KEY,
                                                  subcategory_name VARCHAR(100) NOT NULL,
    category_id      INTEGER NOT NULL CONSTRAINT fk_category_to_subcategory_1 REFERENCES public.category
    );

/* 13. 추천 카드 테이블
    - 사용자에게 추천할 카드 정보와 관련 조건들을 저장 */
CREATE TABLE IF NOT EXISTS public.recommended_cards (
                                                        recomemdedcard    INTEGER NOT NULL CONSTRAINT pk_recommended_cards PRIMARY KEY,
                                                        user_id           INTEGER NOT NULL CONSTRAINT fk_users_to_recommended_cards_1 REFERENCES public.users,
                                                        card_template_id  INTEGER NOT NULL CONSTRAINT fk_card_templates_to_recommended_cards_1 REFERENCES public.card_templates,
                                                        category_id       INTEGER CONSTRAINT fk_category_to_recommended_cards_1 REFERENCES public.category,
                                                        if_benefit_type   VARCHAR(10) CONSTRAINT recommended_cards_if_benefit_type_check CHECK ((if_benefit_type)::text = ANY ((ARRAY ['할인', '적립'])::text[])),
    if_benefit_amount INTEGER,
    reason            VARCHAR(100),
    created_at        DATE NOT NULL
    );

/* 14. 상점(가맹점) 테이블
    - 서브 카테고리와 연계하여 가맹점 정보를 저장 */
CREATE TABLE IF NOT EXISTS public.merchants (
                                                merchant_id    SERIAL CONSTRAINT pk_merchants PRIMARY KEY,
                                                subcategory_id INTEGER NOT NULL CONSTRAINT fk_subcategory_to_merchants_1 REFERENCES public.subcategory,
                                                merchant_name  VARCHAR(100),
    brand_name     VARCHAR(100),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at     TIMESTAMP
    );

/* 15. 리포트 카드 카테고리 테이블
    - 리포트 카드와 가맹점, 서브 카테고리 정보를 연결하여 거래 세부 내역을 저장 */
CREATE TABLE IF NOT EXISTS public.report_card_categories (
                                                             report_category_id      SERIAL CONSTRAINT pk_report_card_categories PRIMARY KEY,
                                                             report_card_id          INTEGER NOT NULL CONSTRAINT fk_report_cards_to_report_card_categories_1 REFERENCES public.report_cards,
                                                             category_id             INTEGER NOT NULL CONSTRAINT fk_subcategory_to_report_card_categories_1 REFERENCES public.subcategory,
                                                             merchant_id             INTEGER NOT NULL CONSTRAINT fk_merchants_to_report_card_categories_1 REFERENCES public.merchants,
                                                             amount                  INTEGER NOT NULL,
                                                             received_benefit_amount INTEGER NOT NULL,
                                                             created_at              TIMESTAMP NOT NULL,
                                                             count                   VARCHAR(255)
    );

/* 16. 카드 거래 테이블
    - 카드 거래 내역과 관련 세부 정보를 저장 */
CREATE TABLE IF NOT EXISTS public.card_transactions (
                                                        transaction_id     INTEGER NOT NULL CONSTRAINT pk_card_transactions PRIMARY KEY
                                                        CONSTRAINT fk_transactions_to_card_transactions_1 REFERENCES public.transactions,
                                                        card_company_id    SMALLINT NOT NULL CONSTRAINT fk_card_companies_to_card_transactions_1 REFERENCES public.card_companies,
                                                        merchant_id        INTEGER NOT NULL CONSTRAINT fk_merchants_to_card_transactions_1 REFERENCES public.merchants,
                                                        card_unique_number VARCHAR(255),
    status             VARCHAR(10) CONSTRAINT card_transactions_status_check CHECK ((status)::text = ANY ((ARRAY ['승인', '거절', '취소'])::text[])),
    benefit_type       VARCHAR(10) CONSTRAINT card_transactions_benefit_type_check CHECK ((benefit_type)::text = ANY ((ARRAY ['즉시 할인', '청구 할인', '캐시백'])::text[])),
    benefit_amount     INTEGER
    );

/* 17. 혜택 템플릿 테이블
    - 카드 템플릿과 연계된 혜택 템플릿 정보를 저장 */
CREATE TABLE IF NOT EXISTS public.benefit_templates (
                                                        benefit_template_id SERIAL CONSTRAINT pk_benefit_templates PRIMARY KEY,
                                                        card_template_id    INTEGER NOT NULL CONSTRAINT fk_card_templates_to_benefit_templates_1 REFERENCES public.card_templates,
                                                        category_id         INTEGER CONSTRAINT fk_category_to_benefit_templates_1 REFERENCES public.category,
                                                        subcategory_id      INTEGER CONSTRAINT fk_subcategory_to_benefit_templates_1 REFERENCES public.subcategory,
                                                        benefit_type        VARCHAR(10) NOT NULL
    CONSTRAINT benefit_templates_benefit_type_check CHECK ((benefit_type)::text = ANY ((ARRAY ['적립', '할인', '그 외'])::text[])),
    benefit_title       VARCHAR(255) NOT NULL,
    merchant_info       JSONB,
    benefit_conditions  JSONB,
    benefit_details     JSONB,
    restrictions        JSONB,
    additional_info     JSONB
    );

/* 18. 사용자 카드 혜택 테이블
    - 사용자와 혜택 템플릿 간의 혜택 사용 정보를 저장 (복합 기본키) */
CREATE TABLE IF NOT EXISTS public.user_card_benefit (
                                                        user_id2             INTEGER NOT NULL CONSTRAINT fk_users_to_user_card_benefit_1 REFERENCES public.users,
                                                        benefit_template_id2 INTEGER NOT NULL CONSTRAINT fk_benefit_templates_to_user_card_benefit_1 REFERENCES public.benefit_templates,
                                                        spending_tier        SMALLINT NOT NULL,
                                                        benefit_count        SMALLINT NOT NULL,
                                                        benefit_amount       INTEGER NOT NULL,
                                                        reset_date           TIMESTAMP NOT NULL,
                                                        update_date          TIMESTAMP NOT NULL,
                                                        CONSTRAINT pk_user_card_benefit PRIMARY KEY (user_id2, benefit_template_id2)
    );

/* 19. 프리 혜택 테이블
    - 카드와 관련된 프리 혜택 정보를 저장 */
CREATE TABLE IF NOT EXISTS public.pre_benefits (
                                                   user_id             INTEGER NOT NULL CONSTRAINT pk_pre_benefits PRIMARY KEY CONSTRAINT fk_users_to_pre_benefits_1 REFERENCES public.users,
                                                   card_id             INTEGER NOT NULL CONSTRAINT fk_cards_to_pre_benefits_1 REFERENCES public.cards,
                                                   card_template_id    SERIAL CONSTRAINT fk_card_templates_to_pre_benefits_1 REFERENCES public.card_templates,
                                                   amount              INTEGER NOT NULL,
                                                   if_benefit_type     VARCHAR(10) CONSTRAINT pre_benefits_if_benefit_type_check CHECK ((if_benefit_type)::text = ANY ((ARRAY ['할인', '적립'])::text[])),
    if_benefit_amount   INTEGER,
    real_benefit_type   VARCHAR(10) CONSTRAINT pre_benefits_real_benefit_type_check CHECK ((real_benefit_type)::text = ANY ((ARRAY ['할인', '적립'])::text[])),
    real_benefit_amount INTEGER,
    merchant_name       VARCHAR(100)
    );
