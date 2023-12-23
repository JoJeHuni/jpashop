# 실전 스프링 부트, JPA 활용 1 - 웹 애플리케이션 개발

## 프로젝트 환경설정

### 프로젝트 생성
- 스프링 부트 스타터(https://start.spring.io/)
- Project: Gradle - Groovy Project
- 사용 기능: web, thymeleaf, jpa, h2, lombok, validation
  - groupId: jpabook
  - artifactId: jpashop

### 스프링 부트 3.x 버전 선택 시
1. Java 17 이상 사용
2. javax 패키지 이름 -> jakarta로 변경
   - 오라클 자바 라이센스 문제로 모든 `javax` 패키지를 `jakarta` 로 변경했다. 
3. H2 데이터베이스를 2.1.214 버전 이상으로 사용

`build.gradle` Gradle 전체 설정
```
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'jpabook'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}
configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-web'

    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'com.h2database:h2'

    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    //JUnit4 추가
    testImplementation("org.junit.vintage:junit-vintage-engine") {
        exclude group: "org.hamcrest", module: "hamcrest-core"
    }
}

tasks.named('test') {
    useJUnitPlatform()
}
```

강의가 JUnit4 기준으로 하기에 JUnit4 부분을 `build.gradle`에 추가했다.

---
### View 환경 설정

thymeleaf 템플릿 엔진을 이용해서 View를 설정했다.

- 스프링 부트 thymeleaf viewName 매핑
  - `resources:templates/` +{ViewName}+`.html`

**jpabook.jpashop.HelloController**
```java
@Controller
public class HelloController {
    
    @GetMapping("hello")
    public String hello(Model model) {
        model.addAttribute("data", "hello!!");
        return "hello";
    }
}
```

**thymeleaf 템플릿엔진 동작 확인(hello.html)**
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Hello</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
<p th:text="'안녕하세요. ' + ${data}" >안녕하세요. 손님</p>
</body>
</html>
```
위치: `resources/templates/hello.html`  

- index.html 하나 만들기
  - `static/index.html`
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <title>Hello</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
Hello
<a href="/hello">hello</a>
</body>
</html>
```
>참고: spring-boot-devtools 라이브러리를 추가하면, html 파일을 컴파일만 해주면 서버 재시작 없이 View 파일 변경이 가능하다.  
> 인텔리J 컴파일 방법: 메뉴 build Recompile

---
### H2 데이터베이스 설치
- 다운로드
  - 스프링 부트 3.x 사용시 **2.1.214 버전 이상** 사용  
- 데이터베이스 파일 생성 방법
  - `jdbc:h2:~/jpashop` (최소 한번)
  - `~/jpashop.mv.db` 파일 생성 확인 
  - 이후 부터는 `jdbc:h2:tcp://localhost/~/jpashop` 이렇게 접속

### JPA와 DB 설정, 동작 확인
`main/resources/application.yml`
```yaml
spring:
  datasource:
    url: jdbc:h2:tcp://localhost/~/jpashop
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
        # show_sql: true
        format_sql: true

logging.level:
  org.hibernate.SQL: debug
# org.hibernate.type: trace #스프링 부트 2.x, hibernate5
  org.hibernate.orm.jdbc.bind: trace #스프링 부트 3.x, hibernate6
```

- `spring.jpa.hibernate.ddl-auto: create`
- 이 옵션은 애플리케이션 실행 시점에 테이블을 drop 하고, 다시 생성한다.

> `show_sql` : 옵션은 System.out 에 하이버네이트 실행 SQL을 남긴다.  
> `org.hibernate.SQL` : 옵션은 logger를 통해 하이버네이트 실행 SQL을 남긴다.
---

### 실제 동작하는지 확인
**회원(Member) 엔티티**
```java
@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    private Long id;
    private String username;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
```

**회원 리포지토리 MemberRepository**
```java
@Repository
public class MemberRepository {

    @PersistenceContext
    EntityManager em;

    public Long save(Member member) {
        em.persist(member);
        return member.getId();
    }
    public Member find(Long id) {
        return em.find(Member.class, id);
    }
}
```

**테스트 MemberRepositoryTest**
```java
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;

    @Test
    public void testMember() throws Exception {
        //given
        Member member = new Member();
        member.setUsername("memberA");

        //when
        Long savedId = memberRepository.save(member);
        Member findMember = memberRepository.find(savedId);

        //then
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());

        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성 보장
    }
}
```

---
### 쿼리 파라미터 로그 남기기
로그에 추가하는 방법
- 스프링 부트 3.x, hibernate6
  - `org.hibernate.orm.jdbc.bind: trace` 부분의 주석을 해제한다.

2번째 방법은 외부 라이브러리를 사용하는 방법이다.  
스프링 부트 3.0 이상을 사용하면 라이브러리 버전을 1.9.0 이상을 사용해야 한다.   
`implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0'`  
위의 부분을 `build.gradle`에 추가한다.