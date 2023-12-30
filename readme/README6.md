# 주문 도메인 개발

**구현 기능**
- 상품 주문
- 주문 내역 조회
- 주문 취소

**순서**
- 주문 엔티티, 주문상품 엔티티 개발
- 주문 리포지토리 개발
- 주문 서비스 개발
- 주문 검색 기능 개발
- 주문 기능 테스트

## 주문, 주문상품 엔티티 개발
### 주문 엔티티 개발

**주문 엔티티 코드 (Order 뒷 내용에 추가)**
```java
    // 생성 메서드 createOrder : 회원정보, 배송정보, 주문상품정보로 주문 엔티티 생성
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);

        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());

        return order;
    }
    
    // 비즈니스 로직
    /*
     주문 취소 로직 메서드 cancel : 주문 취소 시 사용
     주문 상태를 취소로 변경 -> 주문 상품에 주문 취소 알리기
     이미 배송 완료 시 -> 주문 취소 못하도록 예외처리
     */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송 완료된 상품은 취소가 불가능합니다.");
        }
        
        this.setStatus(OrderStatus.CANCEL);
        
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }
    
    // 조회로직
    // 전체 주문 가격 조회 : getTotalPrice()
    public int getTotalPrice() {
        int totalPrice = 0;
        
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        
        return totalPrice;
    }
```

**기능 설명**
- **생성 메서드**( `createOrder()` ): 주문 엔티티를 생성할 때 사용한다. 주문 회원, 배송정보, 주문상품의 정보를 받아서 실제 주문 엔티티를 생성한다.
- **주문 취소**( `cancel()` ): 주문 취소시 사용한다. 주문 상태를 취소로 변경하고 주문상품에 주문 취소를 알린다. 만약 이미 배송을 완료한 상품이면 주문을 취소하지 못하도록 예외를 발생시킨다. 
- **전체 주문 가격 조회**: 주문 시 사용한 전체 주문 가격을 조회한다. 전체 주문 가격을 알려면 각각의 주문상품 가격을 알아야 한다. 로직을 보면 연관된 주문상품들의 가격을 조회해서 더한 값을 반환한다. (실무에서는 주로 주문에 전체 주문 가격 필드를 두고 역정규화 한다.)

### 주문 상품 엔티티 개발

**주문상품 엔티티 코드 (OrderItem 뒷 내용에 추가)**
```java
    // 생성 메서드
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);
        
        item.removeStock(count);
        
        return orderItem;
    }
    
    // 비즈니스 로직
    // 주문 취소
    public void cancel() {
        getItem().addStock(count);
    }
    
    // 조회 로직
    // 주문 상품 전체 가격 조회
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
```

**기능 설명**
- **생성 메서드**( `createOrderItem()` ): 주문 상품, 가격, 수량 정보를 사용해서 주문상품 엔티티를 생성한다. 그
리고 `item.removeStock(count)` 를 호출해서 주문한 수량만큼 상품의 재고를 줄인다.
- **주문 취소**( `cancel()` ): `getItem().addStock(count)` 를 호출해서 취소한 주문 수량만큼 상품의 재고를
증가시킨다.
- **주문 가격 조회**( `getTotalPrice()` ): 주문 가격에 수량을 곱한 값을 반환한다.

### 주문 리포지토리 개발
**주문 리포지토리 코드**
```java
package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    // 주문 엔티티 저장 기능
    public void save(Order order) {
        em.persist(order);
    }

    // 주문 엔티티 검색 기능
    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }
    
    // findAll 메서드 또한 추후에 만든다.
}
```

### 주문 서비스 개발
**주문 서비스 코드**
```java
package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    
    // 주문 메서드
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        
        // 엔티티 조회
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);
        
        // 배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());
        delivery.setStatus(DeliveryStatus.READY);
        
        // 주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
        
        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);
        
        // 주문 저장
        orderRepository.save(order);
        
        return order.getId();
    }
    
    // 주문 취소
    @Transactional
    public void cancelOrder(Long orderId) {
        
        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        
        // 주문 취소
        order.cancel();
    }
    
    // 주문 검색
    /*
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAll(orderSearch);
     */
}
```

주문 서비스는 주문 엔티티와 주문 상품 엔티티의 비즈니스 로직을 활용해서  
주문, 주문 취소, 주문 내역 검색 기능을 제공한다.

> 참고 : 예제를 단순화하려고 한 번에 하나의 상품만 주문 가능하다.

- **주문**( `order()` ): 주문하는 회원 식별자, 상품 식별자, 주문 수량 정보를 받아서 실제 주문 엔티티를 생성한 후 저장한다.
- **주문 취소**( `cancelOrder()` ): 주문 식별자를 받아서 주문 엔티티를 조회한 후 주문 엔티티에 주문 취소를 요청한다.
- **주문 검색**( `findOrders()` ): `OrderSearch` 라는 검색 조건을 가진 객체로 주문 엔티티를 검색한다. **자세한 내용은 다음에 나오는 주문 검색 기능에서 알아보자.**

---
