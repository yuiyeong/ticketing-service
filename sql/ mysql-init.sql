-- 기존 사용자가 있다면 삭제
DROP USER IF EXISTS 'exporter'@'%';

-- exporter 사용자 생성 및 권한 부여
CREATE USER 'exporter'@'%' IDENTIFIED BY '${DB_PASSWORD}';
GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'exporter'@'%';
FLUSH PRIVILEGES;
