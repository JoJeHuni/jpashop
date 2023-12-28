#  회원 도메인 개발

- **구현 기능**
  - 회원 등록
  - 회원 목록 조회
- **순서**
  - 회원 엔티티 코드 다시 보기
  - 회원 리포지토리 개발
  - 회원 서비스 개발
  - 회원 기능 테스트

## 회원 리포지토리 개발

### 회원 리포티토리 코드
```java
package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MemberRepository {

    @PersistenceContext
    EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }
    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    // Member List에 있는 전체를 조회한다.
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    // Member List 에 있는 member 중 이름으로 조회한다.
    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
```

**기술 설명**  
- `@Repository` : 스프링 빈으로 등록, JPA 예외를 스프링 기반 예외로 예외 변환
- `@PersistenceContext` : 엔티티 메니저( EntityManager ) 주입
- `@PersistenceUnit` : 엔티티 메니터 팩토리( EntityManagerFactory ) 주입

**기능 설명**
- save() : persist 로 member 를 저장한다.
- findOne() : 단 건 조회이다. (타입, 변수 순)
- findAll() : 
- findByName()

---
## 회원 서비스 개발
### 회원 서비스 코드
```java
package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    //회원 가입
    public Long join(Member member) {

        validateDuplicateMember(member); // 중복 이름인 사람은 가입 못 하게 하는 검증을 넣어봤다.
        memberRepository.save(member); // id 값이 key 값이 된다.
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        // 이름이 같을 경우의 Exception
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) throw new IllegalStateException("이미 존재하는 회원입니다.");
    }

    //회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
```

> 서비스 코드에서 `@Transactional` 어노테이션을 전체가 아닌 메소드에 해당해서 사용하는 경우도 있다.  
> save 같은 쓰기 기능의 경우에는 그대로 사용하되, find 같은 조회(읽기) 기능에서는 `readonly = true` 를 추가해주면 좋다.
> ```java
> @Transactional
> public Long join(Member member) {
>     validateDuplicateMember(member);
>     memberRepository.save(member);
>     return member.getId();
> }
> 
> @Transactional(readOnly = true)
> public List<Member> findMembers() {
>     return memberRepository.findAll();
> }
> 
> @Transactional(readOnly = true)
> public Member findOne(Long memberId) {
>     return memberRepository.findOne(memberId);
> }
> ```

**기술 설명**
- `@Service`
- `@Transactional` : 트랜잭션, 영속성 컨텍스트
  - `readOnly=true` : 데이터의 변경이 없는 읽기 전용 메서드에 사용, 영속성 컨텍스트를 플러시 하지 않으므로 약간의 성능 향상(읽기 전용에는 다 적용)
  - 데이터베이스 드라이버가 지원하면 DB에서 성능 향상
- `@Autowired`
  - 생성자 Injection 많이 사용, 생성자가 하나면 생략 가능

**기능 설명**
- join()
- findMembers()
- findOne()

> 참고: 실무에서는 검증 로직이 있어도 멀티 쓰레드 상황을 고려해서 회원 테이블의 회원명 컬럼에 유니크 제약 조건을 추가하는 것이 안전하다.

> 참고: 스프링 필드 주입 대신에 생성자 주입을 사용하자.

**필드 주입**
```java
public class MemberService {
    @Autowired
    MemberRepository memberRepository;
    ...
}
```

**생성자 주입**
```java
public class MemberService {
    private final MemberRepository memberRepository;
    
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    ...
}
```

위 2가지 방법이 있다.
- **생성자 주입 방식을 권장**
- 변경 불가능한 안전한 객체 생성 가능
- 생성자가 하나면, `@Autowired` 를 생략할 수 있다.
- ★ `final` 키워드를 추가하면 컴파일 시점에 `memberRepository` 를 설정하지 않는 오류를 체크할 수 있다.(보통 기본 생성자를 추가할 때 발견)

생성자 주입 방식을 사용하고, 롬복을 추가해보자.

**lombok**
```java
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberRepository memberRepository;
    ...
}
```

★ 위와 같이 `@RequiredArgsConstructor` 를 이용하면 `final` 이 있는 필드만 사용해 생성자를 만들어준다.
나도 롬복을 사용한 코드로 수정을 했다.

> 참고: 스프링 데이터 JPA를 사용하면 `EntityManager` 도 주입 가능

```java
@Repository
@RequiredArgsConstructor
public class MemberRepository {
    private final EntityManager em;
    ...
}
```

`MemberService` 최종 코드
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    //회원 가입
    public Long join(Member member) {

        validateDuplicateMember(member); // 중복 이름인 사람은 가입 못 하게 하는 검증을 넣어봤다.
        memberRepository.save(member); // id 값이 key 값이 된다.
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        // 이름이 같을 경우의 Exception
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) throw new IllegalStateException("이미 존재하는 회원입니다.");
    }

    //회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}

```
---
### 회원 기능 테스트

**테스트 요구 사항**
- 회원가입을 성공해야 한다.
- 회원가입을 할 때 같은 이름이 있으면 예외가 발생해야 한다.

**회원가입 테스트 코드**
```java
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    @Test
    @Rollback(false)
    public void 회원가입() throws Exception {
        //Given
        Member member = new Member();
        member.setName("kim");

        //when
        Long saveId = memberService.join(member);

        //Then
        assertEquals(member, memberRepository.findOne(saveId));
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);
        memberService.join(member2); // 예외가 발생해야 한다.
        //then
        fail("예외가 발생해야 한다.");
    }
}
```

- `@Transactional` : 반복 가능한 테스트 지원, 각각의 테스트를 실행할 때마다 트랜잭션을 시작하고 **테스트가 끝나면 트랜잭션을 강제로 롤백** (이 어노테이션이 테스트 케이스에서 사용될 때만 롤백)

**기능 설명**
- 회원가입 테스트
- 중복 회원 예외 처리 테스트

### 테스트 케이스를 위한 설정
테스트는 케이스 격리된 환경에서 실행하고, 끝나면 데이터를 초기화하는 것이 좋다. 그런 면에서 메모리 DB를 사용하는 것이 가장 이상적이다.  
추가로 테스트 케이스를 위한 스프링 환경과, 일반적으로 애플리케이션을 실행하는 환경은 보통 다르므로 설정 파일을 다르게 사용하자.  
다음과 같이 간단하게 테스트용 설정 파일을 추가하면 된다.  

- `test/resources/application.yml`
```yaml
spring:
#   datasource:
#   url: jdbc:h2:mem:testdb
#   username: sa
#   password:
#   driver-class-name: org.h2.Driver

#   jpa:
#       hibernate:
#           ddl-auto: create
#       properties:
#           hibernate:
#               show_sql: true
#           format_sql: true
#       open-in-view: false

logging.level:
  org.hibernate.SQL: debug
#   org.hibernate.type: trace
```

이제 테스트에서 스프링을 실행하면 이 위치에 있는 설정 파일을 읽는다.  
(만약 이 위치에 없으면 src/resources/application.yml 을 읽는다.)  

스프링 부트는 datasource 설정이 없으면, 기본적을 메모리 DB를 사용하고, driver-class도 현재 등록된 라이브러리를 보고 찾아준다.  
추가로 ddl-auto 도 create-drop 모드로 동작한다.  
따라서 데이터소스나, JPA 관련된 별도의 추가 설정을 하지 않아도 된다.  

