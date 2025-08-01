-- 복지 추천 데이터 초기화 스크립트
-- 외래키 제약으로 인해 순서대로 삭제해야 함

-- 1. welfare_recommendations 테이블 데이터 삭제 (외래키 참조 테이블)
DELETE FROM welfare_recommendations;

-- 2. welfare_programs 테이블 데이터 삭제 (참조되는 테이블)
DELETE FROM welfare_programs;

-- 3. AUTO_INCREMENT 초기화 (선택사항)
ALTER TABLE welfare_recommendations AUTO_INCREMENT = 1;
ALTER TABLE welfare_programs AUTO_INCREMENT = 1;

-- 확인 쿼리
SELECT COUNT(*) as welfare_programs_count FROM welfare_programs;
SELECT COUNT(*) as welfare_recommendations_count FROM welfare_recommendations;
