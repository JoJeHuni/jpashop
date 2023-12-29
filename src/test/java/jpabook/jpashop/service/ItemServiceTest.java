package jpabook.jpashop.service;

import jakarta.transaction.Transactional;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.ItemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ItemServiceTest {

    @Autowired
    ItemService itemService;
    @Autowired
    ItemRepository itemRepository;

    @Test
    @Rollback(false)
    public void 상품등록() throws Exception {
        // Given
        Book book = new Book();
        book.setName("Test");
        book.setPrice(100);
        book.setStockQuantity(10);
        book.setAuthor("Jehun");
        book.setIsbn("123456789");

        // When
        itemService.saveItem(book);
        Book savedBook = (Book) itemRepository.findOne(book.getId());

        // Then
        assertEquals(savedBook, book);
    }

    @Test
    @Rollback(false)
    public void 재고추가() throws Exception {
        // Given
        Book book = new Book();
        book.setName("Test");
        book.setPrice(100);
        book.setStockQuantity(10);
        book.setAuthor("Jehun");
        book.setIsbn("123456789");

        // When
        itemService.saveItem(book);
        book.addStock(3);

        // Then
        assertEquals(book.getStockQuantity(), 13);
    }

    @Test(expected = NotEnoughStockException.class)
    @Rollback(false)
    public void 재고삭제() throws Exception {
        // Given
        Book book = new Book();
        book.setName("Test");
        book.setPrice(100);
        book.setStockQuantity(10);
        book.setAuthor("Jehun");
        book.setIsbn("123456789");

        // When
        itemService.saveItem(book);
        book.removeStock(11);

        // Then
        fail("예외가 발생해야 한다.");
    }
}