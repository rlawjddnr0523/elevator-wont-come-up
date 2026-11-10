# 기능 목록

Elevator Scheduling이 제공하는 기능을 `기능 | 함수/위치 | 사용법` 기준으로 정리한 문서입니다. 전체 개요는 [README](README.md)를 참고하세요.

> 위치는 `src/main/java/` 기준 `파일#메서드`입니다. 콘솔 명령은 실행 중 프롬프트에 입력합니다.

## 실행 · 입력

| 기능 | 함수/위치 | 사용법 |
|---|---|---|
| 건물 규모 지정 | `Main#main`, `Building#Building` | `./gradlew run --args="[층수] [대수] [정원]"` (기본 10 2 8, 층수 ≥ 2, 대수 ≥ 1, 정원 ≥ 1) |
| 수동 호출 | `Main#main`, `Building#call` | `<층> <up\|down> [인원] [목적지]` 예) `3 up 3 8` |
| 목적지 랜덤 지정 | `Building#randomDestination` | 목적지를 생략하면 호출 방향 안에서 랜덤 |
| 호출 입력 검증 | `Building#call` | 범위 밖 층·목적지, 최상층 up, 최하층 down, 방향과 어긋난 목적지, 인원 < 1은 `IllegalArgumentException`으로 거부 |
| 시간 1틱 진행 | `Main#main`, `Building#tick` | (엔터) |
| N틱 진행 | `Main#main` | `run <N>` — 진행 후 화면과 통계 출력 |
| 종료 | `Main#main` | `exit` |

## 랜덤 호출 생성

| 기능 | 함수/위치 | 사용법 |
|---|---|---|
| 자동 생성 켜기/끄기 | `PersonGenerator#setEnabled` | `auto on` / `auto off` |
| 발생 확률 조절 | `PersonGenerator#setRate` | `auto rate <0~1>` (기본 0.3, 범위 밖이면 거부) |
| 틱당 랜덤 호출 생성 | `PersonGenerator#generate` | 켜져 있으면 `run`·엔터 때 확률적으로 1~3명 호출 |
| 시드 고정(재현) | `PersonGenerator#PersonGenerator(double, Random)` | 코드에서 `new PersonGenerator(rate, new Random(seed))` — `compare`가 사용 |

## 시뮬레이션 동작

| 기능 | 함수/위치 | 사용법 |
|---|---|---|
| 틱 처리(이동·정차·문 개폐) | `Building#tick` | 엔터 / `run <N>`. 정차 후 문 1틱 열림 → 다음 틱 닫힘 → 이동 |
| 호출 배정 | `Building#assign`, `DispatchPolicy#canServe`, `#cost` | 호출 시 자동. 비용이 가장 작은 가용 엘리베이터에 배정 |
| 배정 실패 호출 재시도 | `Building#pending`, `#addPending` | 자동. 가용 엘리베이터가 없으면 대기열에 두고 다음 틱에 재배정 |
| 정차 판단 | `DispatchPolicy#shouldStop` | 자동. 목적지·하차 대상이 있으면 만원이어도 정차, 외부 호출 층은 만원이면 통과 |
| 하차·탑승 | `Building#tick`, `Building#board` | 자동. 도착자는 내리고, 진행 방향이 같은 대기자가 정원까지 탑승 |
| 정원 초과 재호출 | `Building#refreshFloor` | 자동. 못 탄 사람이 있으면 호출을 유지해 다른 엘리베이터가 처리 |
| SCAN 경로 정렬 | `Elevator#addDestination`, `#routeWith` | 자동. 가는 방향 먼저 훑고 반대편은 돌아오며 처리 |
| 예상 도착 시간(ETA) | `Elevator#etaTo` | 자동(FAST 배정에 사용). 이동 + 중간 정차당 2틱 |
| 엘리베이터 상태 | `Elevator.Status`, `Elevator#moveStatus` | 화면의 상태 열 확인: `RUNNING` / `PARKING` / `WAITING` / `IDLE` |

## 배차 모드

| 기능 | 함수/위치 | 사용법 |
|---|---|---|
| 모드 변경 | `Building#setMode`, `Mode` | `mode <standard\|fast\|eco>` (수동 선택 시 자동 전환 해제) |
| STANDARD: 가장 가까운 엘리베이터 | `StandardPolicy` | `mode standard` — 한가할 때 제자리 대기 |
| FAST: ETA 최소 배정 + 분산 대기 | `FastPolicy#cost`, `#onIdle`, `#homeFloor` | `mode fast` — 한가하면 층 전체에 균등 분산(10층 2대 → 3층·8층) |
| 대기 위치 이동(PARKING) 취소 | `Building#assign` | 자동. 호출이 오면 PARKING 이동을 취소하고 배정 |
| ECO: 절반만 가동 | `EcoPolicy#canServe` | `mode eco` — 전체의 절반(최소 1대)만 호출 처리 |
| AUTO: 부하 기반 자동 전환 | `Building#setAutoMode`, `#updateMode` | `mode auto` — 평소 FAST, 10틱 연속 한가하면 ECO, 대기 3명 이상이면 FAST 복귀 |
| 새 모드 추가 | `DispatchPolicy`, `Mode` | `StandardPolicy`를 상속해 필요한 메서드만 재정의하고 `Mode`에 등록 |

## 시각화

| 기능 | 함수/위치 | 사용법 |
|---|---|---|
| 건물 단면 출력 | `Renderer#render` | 호출·틱·`run` 후 자동 출력. 엘리베이터별 열로 표시 |
| 엘리베이터 셀 | `Renderer#cell`, `#symbol` | `[▲ 3]` 닫힘 / `]▲ 3[` 열림 (▲▼■ = 진행 방향, 숫자 = 탑승 인원) |
| 층별 호출 버튼·대기 인원 | `Renderer#render` | 층 왼쪽 `▲▼` = 해당 방향 호출 중, `(n)` = 대기 인원 |
| 엘리베이터 상태 줄 | `Renderer#render` | 화면 하단 `EV1 3F 방향 상태 문 탑승/정원 목적지` |

## 통계 · 비교

| 기능 | 함수/위치 | 사용법 |
|---|---|---|
| 통계 수집 | `Stats#recordCall`, `#recordArrival` | 자동. 호출·도착 인원, 대기·탑승 시간 집계 |
| 통계 출력 | `Renderer#renderStats`, `Stats#avgWait`, `#avgRide`, `#getMaxWait` | `stats` (`run <N>` 후에도 자동 출력). 호출·도착·탑승 중·대기 중, 평균/최대 대기, 평균 탑승, 총 이동 층수 (대기·탑승 시간은 도착한 사람만 집계) |
| 이동량 집계 | `Elevator#step`, `Elevator#movedFloors` | 자동. `stats`의 "총 이동 N층" |
| 모드 성능 비교 | `Main#compare` | `compare <N>` — 같은 호출 패턴(시드 42)으로 STANDARD/FAST/ECO/AUTO를 N틱씩 실행 |

## 빌드 · 테스트

| 기능 | 함수/위치 | 사용법 |
|---|---|---|
| 실행 | `build.gradle` (application 플러그인) | `./gradlew run` |
| 단위 테스트 | `src/test/java/BuildingTest.java` | `./gradlew test` |
