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
