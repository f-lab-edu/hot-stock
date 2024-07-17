#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- 사용자 계정 생성
    CREATE USER "$CLIENT_POSTGRES_USERNAME" WITH ENCRYPTED PASSWORD '$CLIENT_POSTGRES_PASSWORD';

    -- 데이터베이스 생성 및 사용자에게 권한 부여
    GRANT ALL PRIVILEGES ON DATABASE "$POSTGRES_DB" TO "$CLIENT_POSTGRES_USERNAME";

    -- public 스키마에 대한 권한 부여
    GRANT CREATE ON SCHEMA public TO "$CLIENT_POSTGRES_USERNAME";
EOSQL
