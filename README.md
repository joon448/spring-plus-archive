# SPRING PLUS
# 0. 프로젝트 요약

* JWT 기반 인증을 사용하는 Todo/코멘트/담당자 관리 API

    * `/auth/**`는 인증 없이 접근(permitAll), 나머지는 JWT 필요
    * 토큰은 응답 바디로 반환: `{"bearerToken":"Bearer ..."}`
    * Todo 생성 시 작성자가 자동으로 Manager 등록

* 스택: Java 17+, Spring Boot, Spring Security (JWT), Spring Data JPA, Validation, JUnit 5 / Mockito / MockMvc

---

# 1. API 명세서

## 1.1 인증 (Auth)

### 회원가입

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

### 로그인

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

## 1.2 Todo

### 단건 조회

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

### 목록 조회

* **GET** `/todos?page=1&size=10&start=YYYY-MM-DD&end=YYYY-MM-DD&weather=Sunny`

* **Response 200**

```json
{
  "content": [ TodoResponse[] ],
  "page": { "size": 10, "number": 0, "totalElements": 12, "totalPages": 2 }
}
```

### 생성

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

---

## 1.3 Comment

### 생성

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

### 조회

* **GET** `/todos/{todoId}/comments`
* **Response 200**

```json
[
  { "id": 101, "contents": "...", "user": { "id": 10, "email": "..." } },
  { "id": 102, "contents": "...", "user": { "id": 11, "email": "..." } }
]
```

---

## 1.4 Manager

> Todo 생성 시 작성자가 Manager로 자동 등록됨

### 등록

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

### 목록 조회

* **GET** `/todos/{todoId}/managers`
* **Response 200**

```json
[
  { "id": 500, "user": { "id": 10, "email": "owner@test.com" } },
  { "id": 501, "user": { "id": 22, "email": "manager@test.com" } }
]
```

### 삭제

* **DELETE** `/todos/{todoId}/managers/{managerId}`
* **Response 200**

---

## 1.5 User

### 단건 조회

* **GET** `/users/{userId}`
* **Response 200**

```json
{
  "id": 1,
  "email": "owner@test.com"
}
```

### 비밀번호 변경

* **PUT** `/users`
* **Request**

```json
{ 
  "oldPassword": "oldPassword",
  "newPassword": "newPassword"
}
```
* **Response 200**


### 유저 권한 변경 (Admin)

* **PATCH** `/admin/users/{userId}`
* **Request**

```json
{ 
  "role": "ADMIN"
}
```
* **Response 200**



---

# 2. 실행 방법 (application.yml)

## 2.1 `src/main/resources/application.yml` (예시)

```yaml
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

## 2.2 `src/test/resources/application-test.yml` (예시)

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

---

# 3. 트러블 슈팅

### Level 0.

```jsx
Could not resolve placeholder 'jwt.secret.key' in value "${jwt.secret.key}"
```

- application.yml 생성
- DB 연결 설정

### Level 1.

```jsx
Connection is read-only. Queries leading to data modification are not allowed
```

- 원인: Todo Service 전체에 `@Transactional(readOnly = true)` 작성되어 있음
- 해결: 각 메서드마다 분리하고, Save 메서드에는 `@Transactional`을 붙임

### Level 2.

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

### Level 3.

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

### Level 4.

```jsx
mockMvc.perform(get("/todos/{todoId}", todoId))
	.andExpect(status().isBadRequest())
	.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
	.andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
	.andExpect(jsonPath("$.message").value("Todo not found"));
```

- 기존 테스트 코드 expected status : 200 OK
- 테스트 코드 의도 status: 400 Bad Request

### Level 5.

```jsx
//수정 전
@After("execution(* org.example.expert.domain.user.controller.UserController.getUser(..))")

// 수정 후
@Before("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
```

- changeUserRole 메서드 실행 전 동작으로 변경

### Level 6.

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

### Level 7.

N+1 문제 발생

```jsx
@Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.todo.id = :todoId")
```

- JOIN → JOIN FETCH 변경

### Level 8.

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


### Level 9.

- Filter + ArgumentResolver 관련 파일 삭제
- Spring Security 적용 (JwtAuthenticationFilter, JwtAuthenticationToken, SecurityConfig)
- UserRole 수정 (ROLE_ 추가한 Authority 생성)
- Controller 적용 부분 수정 (@Auth → @AuthenticationPrincipal)

### Level 10.

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

### Level 11.

- Log 엔티티 및 레포지토리 생성
- saveManager 메서드 안에서 logSaveManager 메서드 실행
- propagation 속성 REQUIRES_NEW 적용하여 saveManager 오류 발생 시에도 로그 저장 롤백 제외


---

# 4. 테스트

* **단위 테스트 (Service)**: Mockito로 Repository/외부 의존 목킹, 성공/실패 분기 검증
* **컨트롤러 슬라이스 (@WebMvcTest)**

    * `@MockBean`으로 Service/JwtUtil 주입
    * `@WithMockAuthUser`로 인증 주입
    * `POST/DELETE`는 `.with(csrf())`

* **통합 테스트 (@SpringBootTest)**

    * `JwtUtil.createToken(...)`으로 실제 토큰 생성 → `Authorization` 헤더
    * `@Transactional`로 데이터 격리

---
