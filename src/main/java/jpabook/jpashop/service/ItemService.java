package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    // 변경 감기 기능 사용을 위한 메서드 updateItem
    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity) { //param: 파리미터로 넘어온 준영속 상태의 엔티티
        Item findItem = itemRepository.findOne(itemId);//같은 엔티티를 조회한다.
        findItem.setPrice(price); //데이터를 수정한다.
        findItem.setName(name);
        findItem.setStockQuantity(stockQuantity);

        // itemRepository.sava(findItem); < 얘를 넣을 필요가 없다. 이미 트랜잭션이 커밋되면서 JPA가 영속성 컨텍스트 중 변경된 것만 DB에 업데이트를 날려준다.
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
