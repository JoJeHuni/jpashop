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
