-- ==========================================
-- SplitPay DB Schema — V1 Initial Migration
-- ==========================================

CREATE TABLE IF NOT EXISTS users (
                                     id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    role          ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)   DEFAULT CURRENT_TIMESTAMP(6)
    );

CREATE TABLE IF NOT EXISTS split_groups (
                                            id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            name        VARCHAR(100)  NOT NULL,
    description TEXT,
    created_by  BIGINT        NOT NULL,
    currency    VARCHAR(5)    NOT NULL DEFAULT 'INR',
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  DATETIME(6)   DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_group_creator FOREIGN KEY (created_by) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS group_members (
                                             id        BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             group_id  BIGINT NOT NULL,
                                             user_id   BIGINT NOT NULL,
                                             role      ENUM('ADMIN','MEMBER') NOT NULL DEFAULT 'MEMBER',
    joined_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_gm_group FOREIGN KEY (group_id) REFERENCES split_groups(id),
    CONSTRAINT fk_gm_user  FOREIGN KEY (user_id)  REFERENCES users(id),
    CONSTRAINT uq_group_user UNIQUE (group_id, user_id)
    );

CREATE TABLE IF NOT EXISTS expenses (
                                        id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        group_id     BIGINT          NOT NULL,
                                        paid_by      BIGINT          NOT NULL,
                                        amount       DECIMAL(10,2)   NOT NULL,
    description  VARCHAR(255)    NOT NULL,
    split_type   ENUM('EQUAL','PERCENT','EXACT') NOT NULL,
    expense_date DATE,
    created_at   DATETIME(6)     DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_exp_group  FOREIGN KEY (group_id) REFERENCES split_groups(id),
    CONSTRAINT fk_exp_paidby FOREIGN KEY (paid_by)  REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS expense_splits (
                                              id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              expense_id  BIGINT        NOT NULL,
                                              user_id     BIGINT        NOT NULL,
                                              amount_owed DECIMAL(10,2) NOT NULL,
    is_settled  BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_split_expense FOREIGN KEY (expense_id) REFERENCES expenses(id),
    CONSTRAINT fk_split_user    FOREIGN KEY (user_id)    REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS settlements (
                                           id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           group_id   BIGINT        NOT NULL,
                                           paid_by    BIGINT        NOT NULL,
                                           paid_to    BIGINT        NOT NULL,
                                           amount     DECIMAL(10,2) NOT NULL,
    settled_at DATETIME(6)   DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_set_group   FOREIGN KEY (group_id) REFERENCES split_groups(id),
    CONSTRAINT fk_set_paidby  FOREIGN KEY (paid_by)  REFERENCES users(id),
    CONSTRAINT fk_set_paidto  FOREIGN KEY (paid_to)  REFERENCES users(id)
    );