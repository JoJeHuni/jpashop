# API 개발 고급 - 지연 로딩과 조회 성능 최적화

**순서**
- /간단한 주문 조회 V1: 엔티티를 직접 노출
- /간단한 주문 조회 V2: 엔티티를 DTO로 변환
- /간단한 주문 조회 V3: 엔티티를 DTO로 변환 - 페치 조인 최적화
- /간단한 주문 조회 V4: JPA에서 DTO로 바로 조회

주문 + 배송정보 + 회원을 조회하는 API를 만들자.  
지연 로딩 때문에 발생하는 성능 문제를 단계적으로 해결해보자.  
(아주 중요한 내용이니 꼭 곱씹어볼 것)  

## 간단한 주문 조회 V1: 엔티티를 직접 노출
OrderSimpleApiController
```java
package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * xToOne(ManyToOne, OneToOne) 관계 최적화
 * (Order - Member는 ManyToOne 관계다.)
 * (Order - Delivery 는 OneToOne 관계다.)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        // return all; 이것을 바로 하면 문제가 생긴다. -> 왜?

        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }

        return all;
    }
}
```

> 주석 처리해둔 곳에서 return all을 하면 오류가 생기는 이유   
> Order에 가보면 json이 Member를 뿌려야 한다. 그래서 Member로 가면 orders가 있다.  
> ```java 
>   @OneToMany(mappedBy = "member")
>   private List<Order> orders = new ArrayList<>();
> ```
> 이것 때문에 끝나지 않고 반복하게 돼 장애가 생긴다.  
> 양방향 관계 문제때문에 -> `@JsonIgnore` 가 필요하다. Member든 Order든. (Delivery, OrderItem도 Order와 관련이 있으니 필요하다.)  
> ```java
>   @JsonIgnore
>   @OneToMany(mappedBy = "member")
>   private List<Order> orders = new ArrayList<>();
> ```
> 위의 `@JsonIgnore` 을 추가해도 오류가 해결되지 않는다. -> 왜?  
> Order를 가지고 왔어도 fetch가 `LAZY`로 되어 있다. (지연 로딩이라는 뜻)  
> ```java
>   @ManyToOne(fetch = FetchType.LAZY)
>   @JoinColumn(name = "member_id")
>   private Member member; //주문 회원
> ```
> 지연 로딩이면 member든 Delivery든 객체를 바로 가져오지 않는다.  
> 로그를 읽어보면 bytebuddy 라는 것을 볼 수 있는데, 객체를 바로 가져오는 것이 아닌, **Proxy 객체를 가져오기 때문이다.**  
> **Json은 이 Proxy 객체를 json으로 어떻게 생성해야 하는지 몰라서 예외가 발생**하는 것이다.  
> (Member만 코드로 해놨지만, OrderItem, Delivery에서도 Order와 관련된 것이라면 `@JsonIgnore`가 필요하다.)

> **해결 방법** : `Hibernate5Module` 을 스프링 빈으로 등록하면 해결


### 하이버네이트 모듈 등록
**스프링 부트 3.0 이상: Hibernate5JakartaModule 등록**
`build.gradle` 에 다음 라이브러리를 추가하자
`implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5-jakarta'`

`JpashopApplication` 에 다음 코드를 추가하자
```java
    @Bean
    Hibernate5JakartaModule hibernate5Module() {
        return new Hibernate5JakartaModule();
    }
```
- 기본적으로 초기화 된 프록시 객체만 노출, 초기화 되지 않은 프록시 객체는 노출 안함

강제로 지연 로딩을 하는 방법은 이렇게 수정하면 된다.
```java
    @Bean
    Hibernate5JakartaModule hibernate5Module() {
        Hibernate5JakartaModule hibernate5JakartaModule = new Hibernate5JakartaModule();
        // 강제 지연 로딩 설정
        hibernate5JakartaModule.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING, true);
        return new Hibernate5JakartaModule();
    }
```

> V1 엔티티 직접 노출하는 것에 대한 이야기이기 때문에 열심히 볼 필요는 없다.

> 참고: 앞에서 계속 강조했듯이 정말 간단한 애플리케이션이 아니면 엔티티를 API 응답으로 외부로 노출하는 것은 좋지 않다.  
> 따라서 `Hibernate5Module` 를 사용하기 보다는 DTO로 변환해서 반환하는 것이 더 좋은 방법이다.

> 주의: 지연 로딩(LAZY)을 피하기 위해 즉시 로딩(EARGR)으로 설정하면 안된다!  
> 즉시 로딩 때문에 연관관계가 필요 없는 경우에도 데이터를 항상 조회해서 성능 문제가 발생할 수 있다.  
> 즉시 로딩으로 설정하면 성능 튜닝이 매우 어려워 진다.  
> 항상 지연 로딩을 기본으로 하고, 성능 최적화가 필요한 경우에는 페치 조인(fetch join)을 사용해라!(V3에서 설명)

---