# 팀프로젝트로 진행한 S1JIN DELIVERY를 개인적으로 리팩토링 합니다.

## 개발 환경 소개

|분류|상세|
|:--|:--|
|IDE|IntelliJ|
|Language|Java 17|
|Framework|Spring Boot 3.4.2|
|Repository|PostgreSQL 16.3|
|Build Tool|Gradle 8.12.1|
|DevOps - dev | EC2, RDS(PostgreSQL), S3, Docker, Gihub Actions|

## 개발 일지
<details>
<summary>Order 서비스 레이어 설계 개선</summary>

### 개요(프로젝트 요구사항)
- `주문 CRUD`시, `권한 별 검증 로직`이 다르고 `응답 또한 다르게 return` 필요한 상황
  ![스크린샷 2025-02-28 23 37 50](https://github.com/user-attachments/assets/5f5291a9-0212-4a0d-87ba-4c05cea9c396)

### 기존 구현 방식(리팩토링 전)

- 권한 별(`Customer, Owner, Manager`)로 3개의 서비스로 분기처리 후, `전략 패턴` 적용
- `응답을 다르게 return` 하기 위해, 서비스별 `Response` 클래스 작성(e.g `CustomerSummaryResponseDto`)
  ![](https://github.com/user-attachments/assets/6e6505cd-d5f7-4b53-ab1e-73ac3b786823)

### 개선을 고려한 이유
- 구현한 사람은 한눈에 이해할 수 있었지만, `동료가 보기에 이해하기 어려운 코드`라고 판단(`DTO 클래스도 복잡했기 때문`)

### 파트1: 하나의 서비스로 구현하기 [🔗 branch link](https://github.com/cchoijjinyoung/topy-delivery/tree/refactor/order-service-structure-part1)
- 기존에 권한 별로 나눴던 서비스를 하나의 서비스로 병합
</details>
