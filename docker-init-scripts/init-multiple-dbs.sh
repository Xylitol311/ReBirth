#!/bin/bash
set -e

# POSTGRES_USER 환경변수를 이용하여 기본 데이터베이스에 접속한 후, 두 개의 데이터베이스를 생성합니다.
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE rebirth;
    CREATE DATABASE cardissuer;
EOSQL
