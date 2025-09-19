# SPRING PLUS
## 0. 프로젝트 요약

* JWT 기반 인증을 사용하는 Todo/코멘트/담당자 관리 API

    * `/auth/**`는 인증 없이 접근(permitAll), 나머지는 JWT 필요
    * 토큰은 응답 바디로 반환: `{"bearerToken":"Bearer ..."}`
    * Todo 생성 시 작성자가 자동으로 Manager 등록

* 스택: Java 17+, Spring Boot, Spring Security (JWT), Spring Data JPA, Validation, JUnit 5 / Mockito / MockMvc

---

## 1. API 명세서

### 1.0 Health Check API

* **GET** `/actuator/health`
* Success Response
  ```bash
  HTTP/1.1 200
  X-Content-Type-Options: nosniff
  X-XSS-Protection: 0
  Cache-Control: no-cache, no-store, max-age=0, must-revalidate
  Pragma: no-cache
  Expires: 0
  X-Frame-Options: DENY
  Content-Type: application/vnd.spring-boot.actuator.v3+json
  Transfer-Encoding: chunked
  Date: Thu, 18 Sep 2025 10:45:06 GMT
  
  {"status":"UP","groups":["liveness","readiness"]}
  ```

* Fail Response
  ```bash
  HTTP/1.1 403
  X-Content-Type-Options: nosniff
  X-XSS-Protection: 0
  Cache-Control: no-cache, no-store, max-age=0, must-revalidate
  Pragma: no-cache
  Expires: 0
  X-Frame-Options: DENY
  Content-Length: 0
  Date: Thu, 18 Sep 2025 10:16:42 GMT
  ```

### 1.1 인증 (Auth)

#### 회원가입

* **POST** `/auth/signup`
* **Request**

```json
{
  "email": "user@test.com",
  "password": "pw1234!",
  "nickname": "user",
  "userRole": "USER"
}
```

* **Response 200**

```json
{ "bearerToken": "Bearer xxx.yyy.zzz" }
```

#### 로그인

* **POST** `/auth/signin`
* **Request**

```json
{ "email": "user@test.com" }
```

* **Response 200**

```json
{ "bearerToken": "Bearer xxx.yyy.zzz" }
```

> 이후 요청에서 `Authorization: Bearer ...` 헤더로 사용

---

### 1.2 Todo

#### 단건 조회

* **GET** `/todos/{todoId}`
* **Response 200**

```json
{
  "id": 1,
  "title": "title",
  "contents": "contents",
  "weather": "Sunny",
  "user": { "id": 10, "email": "owner@test.com" },
  "createdAt": "2025-09-16T10:00:00",
  "modifiedAt": "2025-09-16T10:10:00"
}
```

#### 목록 조회

* **GET** `/todos?page=1&size=10&start=YYYY-MM-DD&end=YYYY-MM-DD&weather=Sunny`

* **Response 200**

```json
{
  "content": [ TodoResponse[] ],
  "page": { "size": 10, "number": 0, "totalElements": 12, "totalPages": 2 }
}
```

#### 생성

* **POST** `/todos`
* **Request**

```json
{ "title": "title", "contents": "contents" }
```

* **Response 200**

```json
{
  "id": 1,
  "title": "title",
  "contents": "contents",
  "weather": "Sunny",
  "user": { "id": 10, "email": "owner@test.com" }
}
```

#### 목록 검색

* **GET** `/todos/search?page=1&size=10&title=Title&start=YYYY-MM-DD&end=YYYY-MM-DD&nickname=Nick`

* **Response 200**

```json
{
  "content": [ TodoSearchResponse[] ],
  "page": { "size": 10, "number": 0, "totalElements": 12, "totalPages": 2 }
}
```

---

### 1.3 Comment

#### 생성

* **POST** `/todos/{todoId}/comments`
* **Request**

```json
{ "contents": "comment text" }
```

* **Response 200**

```json
{
  "id": 101,
  "contents": "comment text",
  "user": { "id": 10, "email": "owner@test.com" }
}
```

#### 조회

* **GET** `/todos/{todoId}/comments`
* **Response 200**

```json
[
  { "id": 101, "contents": "...", "user": { "id": 10, "email": "..." } },
  { "id": 102, "contents": "...", "user": { "id": 11, "email": "..." } }
]
```

---

### 1.4 Manager

> Todo 생성 시 작성자가 Manager로 자동 등록됨

#### 등록

* **POST** `/todos/{todoId}/managers`
* **Request**

```json
{ "managerUserId": 22 }
```

* **Response 200**

```json
{
  "id": 501,
  "user": { "id": 22, "email": "manager@test.com" }
}
```

#### 목록 조회

* **GET** `/todos/{todoId}/managers`
* **Response 200**

```json
[
  { "id": 500, "user": { "id": 10, "email": "owner@test.com" } },
  { "id": 501, "user": { "id": 22, "email": "manager@test.com" } }
]
```

#### 삭제

* **DELETE** `/todos/{todoId}/managers/{managerId}`
* **Response 200**

---

### 1.5 User

#### 단건 조회

* **GET** `/users/{userId}`
* **Response 200**

```json
{
  "id": 1,
  "email": "owner@test.com"
}
```

#### 비밀번호 변경

* **PUT** `/users`
* **Request**

```json
{ 
  "oldPassword": "oldPassword",
  "newPassword": "newPassword"
}
```
* **Response 200**


#### 닉네임 기준 목록 조회

* **GET** `/users/search?nickname=hello`
* **Response 200**
```json
[
  { "id": 10, "nickname": "hello" },
  { "id": 22, "nickname": "hello" }
]
```


#### 유저 권한 변경 (Admin)

* **PATCH** `/admin/users/{userId}`
* **Request**

```json
{ 
  "role": "ADMIN"
}
```
* **Response 200**

### 1.6 유저 프로필 이미지 관리

#### 프로필 이미지 등록

* **POST** `/users/{userId}/profile-image`
* **Request**
```json
"file=@/mnt/c/users/82109/desktop/sparta/profile_123.jpg;type=image/jpg"
```
* **Response**
```json
{
	"id":1,
	"key":"S3-upload-address-key"
}
```

#### 프로필 이미지 조회

* **GET** `/users/{userId}/profile-image`
* **Response**
```json
{
	"url":"download-presigned-url",
	"expiresIn":600
}
```

#### 프로필 이미지 삭제

* **DELETE** `/users/{userId}/profile-image`

---

## 2. AWS 배포

### 2.1 Settings

#### EC2
<img width="1937" height="1165" alt="ec2-instance" src="https://github.com/user-attachments/assets/47e52778-939f-41af-a5e4-8d70ada1536f" />
<img width="1937" height="1247" alt="ec2-security" src="https://github.com/user-attachments/assets/08ac6308-8adf-41de-9fd8-2fa4dd57b74f" />

#### RDS
<img width="1937" height="1053" alt="rds-database" src="https://github.com/user-attachments/assets/adbc6222-3c7a-490d-a59f-feecb71a0d8c" />
<img width="1914" height="1383" alt="rds-security" src="https://github.com/user-attachments/assets/1698c693-d273-4c5a-ab5f-2e08228ba627" />

#### IAM Role
<img width="1914" height="1161" alt="iam-role" src="https://github.com/user-attachments/assets/dcc44bd5-e13f-4eca-bc4c-a246b1c50f87" />
<img width="1914" height="1234" alt="iam-policy" src="https://github.com/user-attachments/assets/143b0121-ace6-4914-8181-5fcf63840092" />

#### S3
<img width="1914" height="1147" alt="s3-properties" src="https://github.com/user-attachments/assets/71ff56a6-73af-43fc-9aab-f0fe344e1276" />
<img width="1914" height="1223" alt="s3-permissions" src="https://github.com/user-attachments/assets/ee4c0a5c-3a3f-46e8-9a7d-47f2e37a8759" />


### 2.2 API Access

* Public IP: `http://43.200.65.248:8080/`

#### Health Check API

* `curl -i http://43.200.65.248:8080/actuator/health`
  
<img width="440" height="176" alt="health-check" src="https://github.com/user-attachments/assets/33d8d314-89e7-4040-a7b3-fa15fb63a686" />

#### Signup

<img width="428" height="175" alt="post_signup" src="https://github.com/user-attachments/assets/c562bc00-7789-4ec1-839e-24e5da456190" />

#### Signin

<img width="416" height="144" alt="post_login" src="https://github.com/user-attachments/assets/9d7d8de0-5079-4860-b89c-46c6bc6b0cf4" />

#### Post Todos (bearer token 필요)

<img width="405" height="290" alt="post_todo" src="https://github.com/user-attachments/assets/1488689b-8da4-4545-a4ae-109b2340d194" />

#### 프로필 이미지 업로드 (bearer token 필요)

<img width="563" height="59" alt="image" src="https://github.com/user-attachments/assets/6a8955ed-9b08-4c41-be33-b9d7770b3430" />

- S3에 업로드된 것을 확인할 수 있음

<img width="1061" height="354" alt="image" src="https://github.com/user-attachments/assets/f6570d7d-eeb7-41fa-9108-daf1c5e0ccbd" />

#### 프로필 이미지 조회 (bearer token 필요)

<img width="527" height="425" alt="image" src="https://github.com/user-attachments/assets/11177ef7-4ee1-4e09-a44d-feed29232145" />

- expiresIn(기본 600초)동안 일시적으로 조회 가능한 presignedUrl를 반환

<img width="559" height="402" alt="image" src="https://github.com/user-attachments/assets/c0ec5082-adf8-4451-867c-e55502ebfef7" />

- 600초 이후 접속 시 접근 실패

<img width="557" height="409" alt="image" src="https://github.com/user-attachments/assets/081e4589-741b-4734-a0a3-f5fffd067d2f" />

#### 프로필 이미지 삭제 (bearer token 필요)

<img width="551" height="58" alt="image" src="https://github.com/user-attachments/assets/273d749a-6e43-43da-af3e-0e98dadd2db4" />

- S3에서 삭제된 것을 확인할 수 있음

<img width="1072" height="334" alt="image" src="https://github.com/user-attachments/assets/1b8ef793-364a-4bfc-98c9-132fe8b69752" />

---

## 3. Local 실행 방법 (application.yml)

### 3.1 `src/main/resources/application.yml` (예시)

```yaml
app:
  s3:
    bucket: [S3-bucket-name]
    base-folder: [S3-folder-name]
    presign-ttl-seconds: 600

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/plus
    username: [your-username]
    password: [your-password]
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        show_sql: true
        format_sql: true
jwt:
  secret:
    key: [your-jwt-key]
```

### 3.2 `src/test/resources/application-test.yml` (예시)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop

jwt:
  secret:
    key: [your-jwt-key]
```

### 3.2 `src/test/resources/application-bulk.yml` (예시)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test?rewriteBatchedStatements=true&useServerPrepStmts=true&cachePrepStmts=true
    username: [your-username]
    password: [your-password]
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        show_sql: false
        format_sql: false
        jdbc:
          batch_size: 0
  sql:
    init:
      mode: never

bulk:
  seed:
    total: 5000000

jwt:
  secret:
    key: [your-jwt-key]
```

---

## 4. 트러블 슈팅

#### 🔵 Level 0.

```jsx
Could not resolve placeholder 'jwt.secret.key' in value "${jwt.secret.key}"
```

- application.yml 생성
- DB 연결 설정

#### 🔵 Level 1.

```jsx
Connection is read-only. Queries leading to data modification are not allowed
```

- 원인: Todo Service 전체에 `@Transactional(readOnly = true)` 작성되어 있음
- 해결: 각 메서드마다 분리하고, Save 메서드에는 `@Transactional`을 붙임

#### 🔵 Level 2.

User에 Nickname 필드 추가 필요

- Auth
    - AuthUser Entity에 Nickname 필드 추가
    - SignupRequest Dto에 Nickname 필드 추가
    - signup service 메서드에 User 생성부분 인자 추가
- User
    - User Entity에 Nickname 필드 추가
- Jwt
    - JwtFilter에 nickname 세팅 부분 추가
    - AuthUserArgumentResolver에 nickname 받아오는 부분 추가

#### 🔵 Level 3.

weather 검색 기능 추가

- Controller 파라미터 추가

```jsx
@RequestParam(required = false) @Valid @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
@RequestParam(required = false) @Valid @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
@RequestParam(required = false) String weather
```

- Service LocalDate → LocalDateTime 변경 부분 추가

```jsx
LocalDateTime startTime = start==null ? null : start.atStartOfDay();
LocalDateTime endTimeExclusive = end==null ? null : end.plusDays(1).atStartOfDay();        
```

- Repository 메소드 추가 (JPQL)

```jsx
@Query(value = "SELECT t FROM Todo t LEFT JOIN FETCH t.user u "
		+ "WHERE (:weather IS NULL OR t.weather = :weather) "
		+ "AND (:start IS NULL OR :start <= t.modifiedAt) "
		+ "AND (:end IS NULL OR t.modifiedAt < :end) "
		+ "ORDER BY t.modifiedAt DESC",
		countQuery = "SELECT COUNT(t) FROM Todo t "
			+ "WHERE (:weather IS NULL OR t.weather = :weather) "
			+ "AND (:start IS NULL OR :start <= t.modifiedAt) "
			+ "AND (:end IS NULL OR t.modifiedAt < :end) " )
	Page<Todo> findByConditionOrderByModifiedAtDesc(@Param("weather") String weather, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
```

#### 🔵 Level 4.

```jsx
mockMvc.perform(get("/todos/{todoId}", todoId))
	.andExpect(status().isBadRequest())
	.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
	.andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
	.andExpect(jsonPath("$.message").value("Todo not found"));
```

- 기존 테스트 코드 expected status : 200 OK
- 테스트 코드 의도 status: 400 Bad Request

#### 🔵 Level 5.

```jsx
//수정 전
@After("execution(* org.example.expert.domain.user.controller.UserController.getUser(..))")

// 수정 후
@Before("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
```

- changeUserRole 메서드 실행 전 동작으로 변경

#### 🟢 Level 6.

Cascade

- **Cascade (영속성 전이)란?**
    - 부모에 수행한 영속성 작업을 자식 entity로 전파하는 기능
- **사용하는 경우**
    - 부모 없이는 의미 없는 자식 entity일 때
    - 여러 부모가 공유하지 않는 전용 자식일 때
- **주요 옵션**
    - PERSIST
    - MERGE
    - REMOVE
    - REFRESH
    - DETACH
    - ALL
- **orphanRemoval과의 차이**
    - orphanRemoval: 부모, 자식 연관을 끊거나, 부모 컬렉션에서 제거하면 자식을 자동으로 삭제
    - cascade: 부모를 삭제하면 자식도 삭제됨
- **주의 사항**
    - 공유되는 엔티티에는 전이 사용 X
    - 전이 방향성 주의 (설정한 필드에서 반대편으로만 감)

```jsx
@OneToMany(mappedBy = "todo", cascade = CascadeType.PERSIST)
private List<Manager> managers = new ArrayList<>();
```

- 부모 저장 시 자식 같이 저장 → `CascadeType.PERSIST`

#### 🟢 Level 7.

N+1 문제 발생

```jsx
@Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.todo.id = :todoId")
```

- JOIN → JOIN FETCH 변경

#### 🟢 Level 8.

QueryDSL

- **QueryDSL이란?**
    - 타입 안전한 JPQL 빌더 (문자열 대신 Q-클래스 사용)
    - 동적 조건 적용 시 장점
- **세팅**

    ```jsx
    dependencies {
        implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
        annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
        annotationProcessor 'jakarta.annotation:jakarta.annotation-api:2.1.1'
        annotationProcessor 'jakarta.persistence:jakarta.persistence-api:3.1.0'
    }
    ```

- **사용 패턴**
    1. `JPAQueryFactory` 빈 등록

        ```jsx
        @Configuration
        @RequiredArgsConstructor
        public class QuerydslConfig {
            private final EntityManager em;
        
            @Bean
            public JPAQueryFactory jpaQueryFactory() {
                return new JPAQueryFactory(em);
            }
        }
        ```

    2. 커스텀 Repository 인터페이스, 구현체 생성

        ```jsx
        // TodoRepository
        public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryCustom {
        }
        
        // custom interface
        public interface TodoRepositoryCustom {
        	Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
        }
        
        // custom interface impl
        @RequiredArgsConstructor
        public class TodoRepositoryImpl implements TodoRepositoryCustom {
        	private final JPAQueryFactory jpaQueryFactory;
        
        	@Override
        	public Optional<Todo> findByIdWithUser(Long todoId) {
        		//구현
        	}
        }
        ```

        - 기존 Repository에 Custom Repository 상속 추가
    3. Q-타입 준비

        ```jsx
        import static com.example.domain.todo.QTodo.todo;
        import static com.example.domain.user.QUser.user;
        ```

    4. 기본 쿼리

        ```jsx
        Todo foundTodo = jpaQueryFactory
        			.selectFrom(todo)
        			.join(todo.user, user)
        			.fetchJoin()
        			.where(todo.id.eq(todoId))
        			.fetchOne();
        ```


#### 🟢 Level 9.

- Filter + ArgumentResolver 관련 파일 삭제
- Spring Security 적용 (JwtAuthenticationFilter, JwtAuthenticationToken, SecurityConfig)
- UserRole 수정 (ROLE_ 추가한 Authority 생성)
- Controller 적용 부분 수정 (@Auth → @AuthenticationPrincipal)

#### 🟡 Level 10.

- TodoSearchResponse Dto 추가

    ```jsx
    @Getter
    public class TodoSearchResponse {
    
        private final String title;
        private final long managerCount;
        private final long commentCount;
    
        public TodoSearchResponse(String title, long managerCount, long commentCount) {
            this.title = title;
            this.managerCount = managerCount;
            this.commentCount = commentCount;
        }
    }
    ```

- Search Todo QueryDsl 추가

    ```jsx
    @Override
    	public Page<TodoSearchResponse> searchTodosOrderByCreatedAtDesc(String title, LocalDateTime startTime,
    		LocalDateTime endTimeExclusive, String managerNickname, Pageable pageable) {
    		List<TodoSearchResponse> todos = jpaQueryFactory.select(Projections.constructor(
    				TodoSearchResponse.class,
    				todo.title,
    				manager.id.countDistinct(),
    				comment.id.countDistinct()
    			))
    			.from(todo)
    			.leftJoin(todo.managers, manager)
    			.leftJoin(manager.user, user)
    			.leftJoin(todo.comments, comment)
    			.where(
    				containTitle(title),
    				afterStartTime(startTime),
    				beforeEndTimeExclusive(endTimeExclusive),
    				containManagerNickname(managerNickname)
    			)
    			.groupBy(todo.id, todo.title)
    			.orderBy(todo.createdAt.desc())
    			.offset(pageable.getOffset())
    			.limit(pageable.getPageSize())
    			.fetch();
    
    		Long total = jpaQueryFactory.select(todo.id.countDistinct())
    			.from(todo)
    			.leftJoin(todo.managers, manager)
    			.leftJoin(manager.user, user)
    			.where(
    				containTitle(title),
    				afterStartTime(startTime),
    				beforeEndTimeExclusive(endTimeExclusive),
    				containManagerNickname(managerNickname)
    			)
    			.fetchOne();
    		return new PageImpl<>(todos, pageable, total == null ? 0: total);
    	}
    
    	private BooleanExpression containTitle(String title){
    		return (title == null || title.isBlank())? null : todo.title.containsIgnoreCase(title);
    	}
    
    	private BooleanExpression afterStartTime(LocalDateTime startTime){
    		return (startTime == null) ? null : todo.createdAt.goe(startTime);
    	}
    
    	private BooleanExpression beforeEndTimeExclusive(LocalDateTime endTimeExclusive){
    		return (endTimeExclusive == null) ? null : todo.createdAt.lt(endTimeExclusive);
    	}
    
    	private BooleanExpression containManagerNickname(String managerNickname){
    		return (managerNickname == null || managerNickname.isBlank())? null : user.nickname.containsIgnoreCase(managerNickname);
    	}
    ```

- 요구사항
    - 조건: 제목 부분검색, 생성일 범위([start, end) ), 담당자 닉네임 부분검색
    - 결과: 일정 제목, 담당자 수, 댓글 수 → 필요 필드만 Projection
    - 정렬: 생성일 최신순
    - 페이징
- countQuery 작성 시 필터에 영향을 주는 join만 계산 (comment는 제외함)
- Dto Projection을 적용하기 때문에 N+1 문제 발생하지 않음
- 동적 조건 따로 분리하여 가독성 & 재사용성 높임

#### 🟡 Level 11.

- Log 엔티티 및 레포지토리 생성
- saveManager 메서드 안에서 logSaveManager 메서드 실행
- propagation 속성 `REQUIRES_NEW` 적용하여 saveManager 오류 발생 시에도 로그 저장 롤백 제외

#### 🟡 Level 12.

**AWS 배포**
- EC2, RDS, S3의 역할
  	- **EC2**: 서버 역할. 스프링 앱이 실행되는 컴퓨팅
    	- 클라이언트 요청을 받고, 비즈니스 로직 처리
    	- RDS에 데이터 요청, 파일은 S3에 저장/조회
    	- IAM Role로 S3 접근 권한을 받음
    	- 퍼블릭 서브넷 + Elastic IP 붙이면 외부에서 접속 가능
	- **RDS**: 데이터베이스 역할
    	- 계정/비밀번호로 연결
    	- EC2에서만 접속되도록 보안그룹 제한(외부 직접 차단)
    	- 보통 프라이빗 서브넷에 둬서 인터넷에 노출 안 함
	- **S3**: 파일 저장소
    	- 이미지/첨부파일 같은 객체 보관
    	- 기본은 Public 차단, EC2가 IAM Role로 접근
- EC2, RDS
  1) EC2 ssh 접속 및 Java 17 설치
  2) 환경 변수 파일 생성 및 관리
  3) 외부 설정 파일 (application-prod.yml) 생성
  4) systemd 서비스 유닛 생성
  5) 로컬 프로젝트에서 JAR 배포 및 EC2로 복사
  6) 서비스 시작
- S3
  1) S3 버킷 생성
  2) EC2 IAM Role 권한 설정
  3) s3 의존성 추가 및 설정 추가
  4) ProfileImage 관련 기능 추가
- 문제: Jar 배포 시 아래와 같은 오류 발생
  ```bash
  $ ./gradlew bootJar
  Exception in thread "main" java.lang.RuntimeException: Wrapper properties file 'C:\Users\82109\Desktop\sparta\spring\spring-plus\gradle\wrapper\gradle-wrapper.properties' does not exist.
        at org.gradle.wrapper.GradleWrapperMain.main(SourceFile:74)
  ```
  해결: gradlew 재생성하여 해결
  ```bash
  	irm get.scoop.sh -outfile 'install.ps1'
	.\install.ps1 -RunAsAdmin
	scoop install gradle
	gradle -v
	#프로젝트 루트에서
	gradle wrapper --gradle-version 8.7
  ```

#### 🟡 Level 13.

- 대용량 데이터 처리 성능 개선
	- User 데이터 500만 건 Bulk insert 테스트 작성
	- 닉네임 생성은 생성 인덱스 기반 Base64 인코딩 활용
	- 닉네임 일치 유저 목록 조회 기능 추가 및 테스트

	**실험**
  
* 각 5회 반복
	* 기존 조회 (index 없음)
	* nickname index 적용
	* (nickname, id) index 적용 (커버링 인덱스)


	**실험 결과**

	<img width="679" height="480" alt="image" src="https://github.com/user-attachments/assets/40dd75fb-5788-4cc3-b9e3-81f3762efb16" />

	| Scenario              | Runs | Mean (ms) | Median (ms) | SD (ms) | Min | Max |
	|---|---:|---:|---:|---:|---:|---:|
	| No Index             | 5 | 5805.2 | 5900.0 | 1255.9 | 4233 | 7144 |
	| Nickname Index       | 5 | 3348.6 | 3471.0 | 750.7  | 2315 | 4244 |
	| **(Nickname, Id) Index** | 5 | **2594.0** | 2419.0 | 525.5  | 1912 | 3135 |


	* (Nickname, Id) 커버링 인덱스를 사용했을 때 조회 속도가 가장 빠른 것을 확인할 수 있음


### 🟣 그 외 문제 해결

* UserRole 관련 오류
  * authorities 설정에 userRole.name() 사용 (USER, ADMIN)
  	* userRole.getUserRole() 로 변경 (ROLE_USER, ROLE_ADMIN)
  * 스키마 컬럼 생성 시 ROLE_USER, ROLE_ADMIN으로 생성되어 data truncated 오류 발생
    * table drop 후 재생성
* Controller test 시 제대로 응답이 오지 않음
  * JwtAuthenticationFilter 대신 JwtUtil를 모킹하는 실수
  * filter 단에서 200 및 빈 응답이 전달되었다
* Auth Controller Test 시 401 오류가 발생함
  * @Import(SecurityConfig.class)로 해결

---

## 5. 테스트

* **단위 테스트 (Service)**: Mockito로 Repository/외부 의존 목킹, 성공/실패 분기 검증
* **컨트롤러 슬라이스 (@WebMvcTest)**

    * `@MockBean`으로 Service/JwtUtil 주입
    * `@WithMockAuthUser`로 인증 주입
    * `POST/DELETE`는 `.with(csrf())`

* **통합 테스트 (@SpringBootTest)**

    * `JwtUtil.createToken(...)`으로 실제 토큰 생성 → `Authorization` 헤더
    * `@Transactional`로 데이터 격리
      
<img width="650" height="300" alt="image" src="https://github.com/user-attachments/assets/3e8251cb-de28-4907-8f2b-2f5cd6d5eaed" />

---
