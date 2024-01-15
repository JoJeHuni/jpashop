# API 개발 고급 - 컬렉션 조회 최적화

- /주문 조회 V1: 엔티티 직접 노출
- /주문 조회 V2: 엔티티를 DTO로 변환
- /주문 조회 V3: 엔티티를 DTO로 변환 - 페치 조인 최적화
- /주문 조회 V3.1: 엔티티를 DTO로 변환 - 페이징과 한계 돌파
- /페이징과 한계 돌파
- /주문 조회 V4: JPA에서 DTO 직접 조회
- /주문 조회 V5: JPA에서 DTO 직접 조회 - 컬렉션 조회 최적화
- /주문 조회 V6: JPA에서 DTO로 직접 조회, 플랫 데이터 최적화
- /API 개발 고급 정리

위 순서대로 진행한다.

주문내역에서 추가로 주문한 상품 정보를 추가로 조회하자.  
Order 기준으로 컬렉션인 `OrderItem` 와 `Item` 이 필요하다.  

앞의 예제에서는 컬렉션 없이, toOne(OneToOne, ManyToOne) 관계만 있었다.  
이번에는 **컬렉션인 일대다 관계(OneToMany)를 조회하고, 최적화하는 방법을 알아보자.**

## 주문 조회 V1: 엔티티 직접 노출
**OrderApiController**
```java
package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    /**
     * 주문 조회 V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        
        // 이전에 했던 Lazy 강제 초기화를 해준 것이다.
        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기화
            
            // 아이템 이름을 가져와야하기 때문에 아이템도 초기화하기 위해 orderItems 를 만든다.
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName()); //Lazy 강제초기화
        }
        return all;
    }
}
```

```markdown
[
    {
        "id": 1,
        "member": {
            "id": 1,
            "name": "userA",
            "address": {
                "city": "서울",
                "street": "1",
                "zipcode": "1111"
            }
        },
        "orderItems": [
            {
                "id": 1,
                "item": {
                    "id": 1,
                    "name": "JPA1 BOOK",
                    "price": 10000,
                    "stockQuantity": 99,
                    "categories": null,
                    "author": null,
                    "isbn": null
                },
                "orderPrice": 10000,
                "count": 1,
                "totalPrice": 10000
            },
            {
                "id": 2,
                "item": {
                    "id": 2,
                    "name": "JPA2 BOOK",
                    "price": 20000,
                    "stockQuantity": 98,
                    "categories": null,
                    "author": null,
                    "isbn": null
                },
                "orderPrice": 20000,
                "count": 2,
                "totalPrice": 40000
            }
        ],
        "delivery": {
            "id": 1,
            "address": {
                "city": "서울",
                "street": "1",
                "zipcode": "1111"
            },
            "status": null
        },
        "orderDate": "2024-01-15T23:25:13.68457",
        "status": "ORDER",
        "totalPrice": 50000
    },
    {
        "id": 2,
        "member": {
            "id": 2,
            "name": "userB",
            "address": {
                "city": "진주",
                "street": "2",
                "zipcode": "2222"
            }
        },
        "orderItems": [
            {
                "id": 3,
                "item": {
                    "id": 3,
                    "name": "SPRING1 BOOK",
                    "price": 20000,
                    "stockQuantity": 197,
                    "categories": null,
                    "author": null,
                    "isbn": null
                },
                "orderPrice": 20000,
                "count": 3,
                "totalPrice": 60000
            },
            {
                "id": 4,
                "item": {
                    "id": 4,
                    "name": "SPRING2 BOOK",
                    "price": 40000,
                    "stockQuantity": 296,
                    "categories": null,
                    "author": null,
                    "isbn": null
                },
                "orderPrice": 40000,
                "count": 4,
                "totalPrice": 160000
            }
        ],
        "delivery": {
            "id": 2,
            "address": {
                "city": "진주",
                "street": "2",
                "zipcode": "2222"
            },
            "status": null
        },
        "orderDate": "2024-01-15T23:25:14.066217",
        "status": "ORDER",
        "totalPrice": 220000
    }
]
```

postman 으로 api를 보내면 위와 같이 나온다. 주문 안에 orderItem 에 대해 조회할 수 있다.

**V1. 엔티티 직접 노출**
- 엔티티가 변하면 API 스펙이 변한다.
- 트랜잭션 안에서 지연 로딩 필요
- 양방향 연관관계 문제


- `orderItem` , `item` 관계를 직접 초기화하면 `Hibernate5Module` 설정에 의해 엔티티를 JSON으로 생성한다.
- 양방향 연관관계면 무한 루프에 걸리지 않게 한곳에 `@JsonIgnore` 를 추가해야 한다.
- 엔티티를 직접 노출하므로 좋은 방법은 아니다.
- `JpashopApplication` 에 `Hibernate5Module` 에 대해 적혀 있어서 실행할 수 있다.

---