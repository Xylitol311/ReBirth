-- 기존 테이블이 존재하면 삭제 (역순으로 삭제하여 외래키 충돌 방지)
DROP TABLE IF EXISTS pre_benefits CASCADE;
DROP TABLE IF EXISTS recommended_cards CASCADE;
DROP TABLE IF EXISTS report_card_categories CASCADE;
DROP TABLE IF EXISTS report_cards CASCADE;
DROP TABLE IF EXISTS monthly_consumption_report CASCADE;
DROP TABLE IF EXISTS monthly_transaction_summary CASCADE;
DROP TABLE IF EXISTS bank_transactions CASCADE;
DROP TABLE IF EXISTS card_transactions CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS benefit_templates CASCADE;
DROP TABLE IF EXISTS merchants CASCADE;
DROP TABLE IF EXISTS cards CASCADE;
DROP TABLE IF EXISTS card_templates CASCADE;
DROP TABLE IF EXISTS card_companies CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS subcategory CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS consumption_patterns CASCADE;

-- 생성 순서에 따른 테이블 생성

-- 1. 카테고리 테이블
CREATE TABLE category (
                          category_id SERIAL PRIMARY KEY,
                          category_name VARCHAR(100) NOT NULL
);

-- 2. 서브카테고리 테이블
CREATE TABLE subcategory (
                             subcategory_id SERIAL PRIMARY KEY,
                             subcategory_name VARCHAR(100) NOT NULL,
                             category_id INT NOT NULL
);

-- 3. 소비 패턴 테이블
CREATE TABLE consumption_patterns (
                                      consumption_pattern_id VARCHAR(3) PRIMARY KEY,
                                      name VARCHAR(20) NOT NULL,
                                      description VARCHAR(100) NOT NULL
);

-- 4. 사용자 테이블 (consumption_pattern_id를 VARCHAR(3)로 수정)
CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       consumption_pattern_id VARCHAR(3),
                       user_name VARCHAR(10) NOT NULL,
                       hashed_pin_number VARCHAR(64) NOT NULL,
                       phone_number VARCHAR(15) NOT NULL,
                       phone_serial_number VARCHAR(100),
                       user_api_key VARCHAR(40),
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP,
                       deleted_at TIMESTAMP,
                       latest_load_data_at TIMESTAMP
);

-- 5. 카드사 테이블
CREATE TABLE card_companies (
                                card_company_id SMALLINT PRIMARY KEY,
                                company_name VARCHAR(30) NOT NULL
);

-- 6. 카드 템플릿 테이블
CREATE TABLE card_templates (
                                card_template_id SERIAL PRIMARY KEY,
                                card_company_id SMALLINT NOT NULL,
                                card_name VARCHAR(100) NOT NULL,
                                card_img_url VARCHAR(255) NOT NULL,
                                god_name VARCHAR(10),
                                god_img_url VARCHAR(255),
                                annual_fee INT NOT NULL DEFAULT 0,
                                card_type VARCHAR(10) NOT NULL CHECK (card_type IN ('신용', '체크')),
                                spending_max_tier SMALLINT NOT NULL,
                                max_performance_amount INT NOT NULL,
                                benefit_text TEXT
);

-- 7. 카드 테이블
CREATE TABLE cards (
                       card_id SERIAL PRIMARY KEY,
                       user_id INT NOT NULL,
                       card_template_id INT NOT NULL,
                       card_number VARCHAR(255) NOT NULL,
                       card_unique_number VARCHAR(255) NOT NULL,
                       expiry_date DATE NOT NULL,
                       card_order SMALLINT NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       deleted_at TIMESTAMP,
                       is_expried SMALLINT NOT NULL DEFAULT 0,
                       annual_fee INT NOT NULL DEFAULT 0,
                       permanent_token VARCHAR(255),
                       payment_card_order SMALLINT,
                       payment_created_at TIMESTAMP
);

-- 8. 가맹점 테이블
CREATE TABLE merchants (
                           merchant_id SERIAL PRIMARY KEY,
                           subcategory_id INT NOT NULL,
                           merchant_name VARCHAR(100),
                           brand_name VARCHAR(100),
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           deleted_at TIMESTAMP
);

-- 9. 혜택 템플릿 테이블
CREATE TABLE benefit_templates (
                                   benefit_template_id SERIAL PRIMARY KEY,
                                   card_template_id INT NOT NULL,
                                   merchant_id INT NOT NULL,
                                   category_id INT NOT NULL,
                                   subcategory_id INT NOT NULL,
                                   max_benefit_limit INT,
                                   max_benefit_count_limit_month SMALLINT NOT NULL,
                                   max_benefit_count_limit_year SMALLINT,
                                   benefit_type VARCHAR(10) NOT NULL CHECK (benefit_type IN ('할인', '적립', '쿠폰')),
                                   benefit_rate INT,
                                   spending_tier SMALLINT,
                                   spending_min_amount INT,
                                   spending_max_amount INT,
                                   coverage_type VARCHAR(10) NOT NULL CHECK (coverage_type IN ('퍼센트', '금액'))
);

-- 10. 트랜잭션 테이블
CREATE TABLE transactions (
                              transaction_id SERIAL PRIMARY KEY,
                              user_id INT NOT NULL,
                              amount INT NOT NULL,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 11. 카드 트랜잭션 테이블
CREATE TABLE card_transactions (
                                   transaction_id INT PRIMARY KEY,
                                   card_company_id SMALLINT NOT NULL,
                                   merchant_id INT NOT NULL,
                                   card_unique_number VARCHAR(255),
                                   status VARCHAR(10) CHECK (status IN ('승인', '거절', '취소')),
                                   benefit_type VARCHAR(20) CHECK (benefit_type IN ('즉시 할인', '청구 할인', '캐시백')),
                                   benefit_amount INT
);

-- 12. 뱅크 트랜잭션 테이블
CREATE TABLE bank_transactions (
                                   transaction_id INT PRIMARY KEY,
                                   card_company_id SMALLINT NOT NULL,
                                   transaction_type VARCHAR(10) CHECK (transaction_type IN ('입금', '출금')),
                                   account_number VARCHAR(30) NOT NULL,
                                   transaction_memo VARCHAR(30)
);

-- 13. 월별 거래 요약 테이블
CREATE TABLE monthly_transaction_summary (
                                             report_id SERIAL PRIMARY KEY,
                                             user_id INT NOT NULL,
                                             year INT NOT NULL,
                                             month INT NOT NULL,
                                             total_spending INT NOT NULL,
                                             received_benefit_amount INT NOT NULL,
                                             created_at TIMESTAMP NOT NULL
);

-- 14. 월별 소비 리포트 테이블
CREATE TABLE monthly_consumption_report (
                                            report_id INT PRIMARY KEY,
                                            consumption_pattern_id VARCHAR(3) NOT NULL,
                                            consumption_pattern VARCHAR(100),
                                            report_descrpition TEXT,
                                            over_consumption SMALLINT,
                                            variation SMALLINT,
                                            extrovert SMALLINT,
                                            created_at TIMESTAMP NOT NULL
);

-- 15. 리포트 카드 테이블
CREATE TABLE report_cards (
                              report_card_id SERIAL PRIMARY KEY,
                              card_id INT NOT NULL,
                              report_id INT NOT NULL,
                              month_spending_amount INT NOT NULL,
                              month_benefit_amount INT NOT NULL,
                              created_at TIMESTAMP NOT NULL,
                              spending_tier SMALLINT,
                              field VARCHAR(255)
);

-- 16. 리포트 카드 카테고리 테이블
CREATE TABLE report_card_categories (
                                        report_category_id VARCHAR(255) PRIMARY KEY,
                                        report_card_id INT NOT NULL,
                                        subcategory_id INT NOT NULL,
                                        merchant_id INT NOT NULL,
                                        amount INT NOT NULL,
                                        received_benefit_amount INT NOT NULL,
                                        created_at TIMESTAMP NOT NULL,
                                        count VARCHAR(255)
);

-- 17. 추천 카드 테이블
CREATE TABLE recommended_cards (
                                   recomemdedcard INT PRIMARY KEY,
                                   user_id INT NOT NULL,
                                   card_template_id INT NOT NULL,
                                   category_id INT,
                                   if_benefit_type VARCHAR(10) CHECK (if_benefit_type IN ('할인', '적립')),
                                   if_benefit_amount INT,
                                   reason VARCHAR(100),
                                   created_at DATE NOT NULL
);

-- 18. 예상 혜택 테이블
CREATE TABLE pre_benefits (
                              user_id INT PRIMARY KEY,
                              card_id INT NOT NULL,
                              card_template_id INT NOT NULL,
                              amount INT NOT NULL,
                              if_benefit_type VARCHAR(10) CHECK (if_benefit_type IN ('할인', '적립')),
                              if_benefit_amount INT,
                              real_benefit_type VARCHAR(10) CHECK (real_benefit_type IN ('할인', '적립')),
                              real_benefit_amount INT,
                              merchant_name VARCHAR(100)
);

-- 외래 키 제약 조건 추가
ALTER TABLE subcategory ADD CONSTRAINT fk_subcategory_category
    FOREIGN KEY (category_id) REFERENCES category (category_id);

ALTER TABLE users ADD CONSTRAINT fk_users_consumption_patterns
    FOREIGN KEY (consumption_pattern_id) REFERENCES consumption_patterns (consumption_pattern_id);

ALTER TABLE card_templates ADD CONSTRAINT fk_card_templates_company
    FOREIGN KEY (card_company_id) REFERENCES card_companies (card_company_id);

ALTER TABLE cards ADD CONSTRAINT fk_cards_user
    FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE cards ADD CONSTRAINT fk_cards_template
    FOREIGN KEY (card_template_id) REFERENCES card_templates (card_template_id);

ALTER TABLE merchants ADD CONSTRAINT fk_merchants_subcategory
    FOREIGN KEY (subcategory_id) REFERENCES subcategory (subcategory_id);

ALTER TABLE benefit_templates ADD CONSTRAINT fk_benefit_templates_card_template
    FOREIGN KEY (card_template_id) REFERENCES card_templates (card_template_id);

ALTER TABLE benefit_templates ADD CONSTRAINT fk_benefit_templates_merchant
    FOREIGN KEY (merchant_id) REFERENCES merchants (merchant_id);

ALTER TABLE benefit_templates ADD CONSTRAINT fk_benefit_templates_category
    FOREIGN KEY (category_id) REFERENCES category (category_id);

ALTER TABLE benefit_templates ADD CONSTRAINT fk_benefit_templates_subcategory
    FOREIGN KEY (subcategory_id) REFERENCES subcategory (subcategory_id);

ALTER TABLE transactions ADD CONSTRAINT fk_transactions_user
    FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE card_transactions ADD CONSTRAINT fk_card_transactions_transaction
    FOREIGN KEY (transaction_id) REFERENCES transactions (transaction_id);

ALTER TABLE card_transactions ADD CONSTRAINT fk_card_transactions_company
    FOREIGN KEY (card_company_id) REFERENCES card_companies (card_company_id);

ALTER TABLE card_transactions ADD CONSTRAINT fk_card_transactions_merchant
    FOREIGN KEY (merchant_id) REFERENCES merchants (merchant_id);

ALTER TABLE bank_transactions ADD CONSTRAINT fk_bank_transactions_transaction
    FOREIGN KEY (transaction_id) REFERENCES transactions (transaction_id);

ALTER TABLE bank_transactions ADD CONSTRAINT fk_bank_transactions_company
    FOREIGN KEY (card_company_id) REFERENCES card_companies (card_company_id);

ALTER TABLE monthly_transaction_summary ADD CONSTRAINT fk_monthly_transaction_summary_user
    FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE monthly_consumption_report ADD CONSTRAINT fk_monthly_consumption_report_summary
    FOREIGN KEY (report_id) REFERENCES monthly_transaction_summary (report_id);

ALTER TABLE monthly_consumption_report ADD CONSTRAINT fk_monthly_consumption_report_pattern
    FOREIGN KEY (consumption_pattern_id) REFERENCES consumption_patterns (consumption_pattern_id);

ALTER TABLE report_cards ADD CONSTRAINT fk_report_cards_card
    FOREIGN KEY (card_id) REFERENCES cards (card_id);

ALTER TABLE report_cards ADD CONSTRAINT fk_report_cards_report
    FOREIGN KEY (report_id) REFERENCES monthly_transaction_summary (report_id);

ALTER TABLE report_card_categories ADD CONSTRAINT fk_report_card_categories_report_card
    FOREIGN KEY (report_card_id) REFERENCES report_cards (report_card_id);

ALTER TABLE report_card_categories ADD CONSTRAINT fk_report_card_categories_subcategory
    FOREIGN KEY (subcategory_id) REFERENCES subcategory (subcategory_id);

ALTER TABLE report_card_categories ADD CONSTRAINT fk_report_card_categories_merchant
    FOREIGN KEY (merchant_id) REFERENCES merchants (merchant_id);

ALTER TABLE recommended_cards ADD CONSTRAINT fk_recommended_cards_user
    FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE recommended_cards ADD CONSTRAINT fk_recommended_cards_card_template
    FOREIGN KEY (card_template_id) REFERENCES card_templates (card_template_id);

ALTER TABLE recommended_cards ADD CONSTRAINT fk_recommended_cards_category
    FOREIGN KEY (category_id) REFERENCES category (category_id);

ALTER TABLE pre_benefits ADD CONSTRAINT fk_pre_benefits_user
    FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE pre_benefits ADD CONSTRAINT fk_pre_benefits_card
    FOREIGN KEY (card_id) REFERENCES cards (card_id);

ALTER TABLE pre_benefits ADD CONSTRAINT fk_pre_benefits_card_template
    FOREIGN KEY (card_template_id) REFERENCES card_templates (card_template_id);