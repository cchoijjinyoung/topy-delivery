# S1JIN DILIVERY

## 프로젝트 소개
> 배달 시스템이 달성해야 하는 기술적 목표와 우리팀이 집중한 구현 목표를 포함한 간략한 개요

<br>

## 개발 환경 소개
> 프로젝트 개발을 위해 채택한 기술과 용도 및 선택 근거를 포함한 간략한 설명

|분류|상세|
|:--|:--|
|IDE|IntelliJ|
|Language|Java 17|
|Framework|Spring Boot 3.4.2|
|Repository||
|Build Tool|Gradle 8.12.1|
|DevOps - dev | |

<br>

## 프로젝트 실행 방법
> 로컬 환경에서 필요한 .env 양식 혹은 Docker Compose 등과 같이 실행에 필요한 요소 포함

### 1. git clone 하기
```shell
git clone https://github.com/4S1JIN/s1jin-delivery.git
```

### 2. 로컬 DB 세팅을 위해 docker-compose up하기
- docker를 미리 설치해야한다.
- 프로젝트의 루트 폴더에 존재하는 `docker-compose.yml`를 들어가서 service 를 up 해준다.
- (참고) 현재는 postgresql만 존재하고 포트는 `5432:5432` 이다.

### 3. 애플리케이션 정상 실행 확인
- 스프링 애플리케이션 (`DeliveryApplication`)이 정상 실행되는 지 확인한다.

<br>

## 설계 산출물
- ![테이블 설계서]()
- ![ERD]()
- ![API 명세서]()
- ![인프라 설계서]()
- ![Convention]()
- ![GitHub Project URL]()

<br>

## 개발 산출물

- ![트러블 슈팅]()
- ![공통 관심 사항]()
- ![테스트 코드]()
- ![설계 대비 API 구현률]()

<br>

## 시스템을 발전 시키기 위해 더 해본다면?
> 현재 시스템 문제점 인지 -> 개선을 위한 기술적 계획 접근

<br>

## 협업 시 우리조가 잘한 것들
> 예를 들어 
> - Rumtime Exception 기반의 공통 예외 처리 정책
> - 일관된 응답 반환을 위한 공통 응답 객체 정의
> - soft-delete 처리를 위한 삭제 정책
> - data 추적/감사를 위한 auditing 정책
> - 로깅 정책 등

![image](https://github.com/user-attachments/assets/3a9cb677-aadc-464d-80ba-8291ca462674)
![image](https://github.com/user-attachments/assets/fcc23f08-ab16-4abe-b174-7ad7568b484c)

<br>

## 협업 시 아쉽거나 부족했던 부분

<br>

## 팀원 소개

| <img src="https://img.shields.io/badge/Leader-%2310069F%20" /> | <img src="https://img.shields.io/badge/Sub_Leader-%2300264B" /> |   |   |   |
| :--------------------------------------------------------------: | :--------------------------------------------------------------: | :--------------------------------------------------------------------------: | :-----------------------------------------------------------: |:-----------------------------------------------------------: |
|      <img src="https://avatars.githubusercontent.com/u/137313172?v=4" width="120px;" alt=""/>      |      <img src="https://avatars.githubusercontent.com/u/68311264?v=4" width="120px;" alt=""/>      |            <img src="https://avatars.githubusercontent.com/u/104823900?v=4" width="120px;" alt=""/>            |    <img src="https://avatars.githubusercontent.com/u/109949465?v=4" width="120px;" alt=""/>     |    <img src="https://avatars.githubusercontent.com/u/85011923?v=4" width="120px;" alt=""/>     |
|           [박소해](https://github.com/S2gamzaS2)           |           [최진영](https://github.com/cchoijjinyoung)           |                 [김소민](https://github.com/ss0ming)                 |         [임승택](https://github.com/lime1st)          |         [김승수](https://github.com/kss123456789)          |
|                            BE / 리뷰 / 회원                           |                            BE / 주문                          |                                  BE / 상점 / 메뉴                                 |                         BE / 인증·인가                          |                          BE / 결제                          |


