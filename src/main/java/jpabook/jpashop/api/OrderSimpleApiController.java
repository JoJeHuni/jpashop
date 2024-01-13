package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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
        // Order에 가보면 json이 Member를 뿌려야 한다. 그래서 Member로 가면 orders가 있다.
        // 이것 때문에 끝나지 않고 반복하게 돼 장애가 생긴다.
        // 양방향 관계 문제때문에 -> @JsonIgnore 가 필요하다. Member든 Order든. (Delivery, OrderItem도 Order와 관련이 있으니 필요하다.)

        // 위의 @JsonIgnore 을 추가해도 오류가 해결되지 않는다. -> 왜?
        // Order를 가지고 왔어도 fetch가 LAZY로 되어 있다. (지연 로딩이라는 뜻)
        // 지연 로딩이면 member든 Delivery든 객체를 바로 가져오지 않는다.
        // 로그를 읽어보면 bytebuddy 라는 것을 볼 수 있는데, 객체를 바로 가져오는 것이 아닌, Proxy 객체를 가져오기 때문이다.
        // Json은 이 Proxy 객체를 json으로 어떻게 생성해야하는지 몰라서 예외가 발생하는 것이다.

        // 해결 방법 : `Hibernate5Module` 을 스프링 빈으로 등록하면 해결

        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }

        return all;
    }

    /**
     * V2. 엔티티를 조회해서 DTO로 변환 (fetch join 사용X)
     * - 단점: 지연로딩으로 쿼리 N번 호출
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                // .map(SimpleOrderDto::new); 도 가능하다.
                .collect(Collectors.toList());
                // .collect(toList()); 도 가능하다.

        return result;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;
        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }

    /**
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - fetch join으로 쿼리 1번 호출
     * 참고: fetch join에 대한 자세한 내용은 JPA 기본편 참고(정말 중요함)
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());
        return result;
    }
}
