# 웹 계층 개발



## 홈 화면과 레이아웃
### 홍 컨트롤러 등록
```java
package jpabook.jpashop.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller@Slf4j
public class HomeController {

    @RequestMapping("/")
    public String home() {
        log.info("home controller");
        return "home";
    }
}
```
스프링 부트 타임리프 기본 설정
```yaml
spring:
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
```

- 스프링 부트 타임리프 viewName 매핑
  - `resources:templates/` +{ViewName}+ `.html`
  - `resources:templates/home.html`

반환한 문자( `home` )과 스프링부트 설정 `prefix` , `suffix` 정보를 사용해서 렌더링할 뷰( `html` )를 찾는다.

### 타임리프 템플릿 등록
`home.html`
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="fragments/header :: header">
    <title>Hello</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
<div class="container">
    <div th:replace="fragments/bodyHeader :: bodyHeader" />
    <div class="jumbotron">
        <h1>HELLO SHOP</h1>
        <p class="lead">회원 기능</p>
        <p>
            <a class="btn btn-lg btn-secondary" href="/members/new">회원 가입</a>
            <a class="btn btn-lg btn-secondary" href="/members">회원 목록</a>
        </p>
        <p class="lead">상품 기능</p>
        <p>
            <a class="btn btn-lg btn-dark" href="/items/new">상품 등록</a>
            <a class="btn btn-lg btn-dark" href="/items">상품 목록</a>
        </p>
        <p class="lead">주문 기능</p>
        <p>
            <a class="btn btn-lg btn-info" href="/order">상품 주문</a>
            <a class="btn btn-lg btn-info" href="/orders">주문 내역</a>
        </p>
    </div>
    <div th:replace="fragments/footer :: footer" />
</div> <!-- /container -->
</body>
</html>
```

**fragments/header.html**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:fragment="header">
  <!-- Required meta tags -->
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrinkto-fit=no">
  <!-- Bootstrap CSS -->
  <link rel="stylesheet" href="/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
  <!-- Custom styles for this template -->
  <link href="/css/jumbotron-narrow.css" rel="stylesheet">
  <title>Hello, world!</title>
</head>
```

**fragments/bodyHeader.html**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<div class="header" th:fragment="bodyHeader">
  <ul class="nav nav-pills pull-right">
    <li><a href="/">Home</a></li>
  </ul>
  <a href="/"><h3 class="text-muted">HELLO SHOP</h3></a>
</div>
```

**fragments/footer.html**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<div class="footer" th:fragment="footer">
  <p>&copy; Hello Shop V2</p>
</div>
```

![img.png](img.png)  
H2를 키고, JpashopApplication 실행 후 localhost:8080 에 들어가면 이제 이런 화면이 나온다.
순수한 html만 적용돼 있다.

> 참고: 뷰 템플릿 변경사항을 서버 재시작 없이 즉시 반영하기
> 1. spring-boot-devtools 추가 ( `implementation 'org.springframework.boot:spring-boot-devtools'` )
> 2. html 파일 build -> Recompile

### view 리소스 등록
이쁜 디자인을 위해 부트스트랩을 사용하겠다. (https://getbootstrap.com/)  

- `resources/static` 하위에 css , js 추가
- `resources/static/css/jumbotron-narrow.css` 추가

jumbotron-narrow.css 파일
```css
/* Space out content a bit */
body {
    padding-top: 20px;
    padding-bottom: 20px;
}
/* Everything but the jumbotron gets side spacing for mobile first views */
.header,
.marketing,
.footer {
    padding-left: 15px;
    padding-right: 15px;
}
/* Custom page header */
.header {
    border-bottom: 1px solid #e5e5e5;
}
/* Make the masthead heading the same height as the navigation */
.header h3 {
    margin-top: 0;
    margin-bottom: 0;
    line-height: 40px;
    padding-bottom: 19px;
}
/* Custom page footer */
.footer {
    padding-top: 19px;
    color: #777;
    border-top: 1px solid #e5e5e5;
}
/* Customize container */
@media (min-width: 768px) {
    .container {
        max-width: 730px;
    }
}
.container-narrow > hr {
    margin: 30px 0;
}
/* Main marketing message and sign up button */
.jumbotron {
    text-align: center;
    border-bottom: 1px solid #e5e5e5;
}
.jumbotron .btn {
    font-size: 21px;
    padding: 14px 24px;
}
/* Supporting marketing content */
.marketing {
    margin: 40px 0;
}
.marketing p + h4 {
    margin-top: 28px;
}
/* Responsive: Portrait tablets and up */
@media screen and (min-width: 768px) {
    /* Remove the padding we set earlier */
    .header,
    .marketing,
    .footer {
        padding-left: 0;
        padding-right: 0;
    }
    /* Space out the masthead */
    .header {
        margin-bottom: 30px;
    }
    /* Remove the bottom border on the jumbotron for visual effect */
    .jumbotron {
        border-bottom: 0;
    }
}
```

bootstrap 버전이 달라서 강의와는 다르지만 변하긴 변했다.  
![img_1.png](img_1.png)

이제 기능을 추가해보자.  

---
## 회원 추가
- 폼 객체를 사용해 화면 계층과 서비스 계층을 명확하게 분리한다.

**회원 등록 폼 객체**
```java
package jpabook.jpashop.web;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberForm {

    @NotEmpty(message = "회원 이름은 필수입니다.")
    private String name;

    private String city;
    private String street;
    private String zipcode;
}
```

**회원 등록 컨트롤러**
```java
package jpabook.jpashop.web;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping(value = "/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    @PostMapping(value = "/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {
        
        if (result.hasErrors()) {
            return "members/createMemberForm";
        }
        
        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);
        
        memberService.join(member);
        
        return "redirect:/";
    }
}
```

회원 등록 폼 화면( `templates/members/createMemberForm.html` )
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="fragments/header :: header" />
<style>
  .fieldError {
    border-color: #bd2130;
  }
</style>
<body>
<div class="container">
  <div th:replace="fragments/bodyHeader :: bodyHeader"/>
  <form role="form" action="/members/new" th:object="${memberForm}"
        method="post">
    <div class="form-group">
      <label th:for="name">이름</label>
      <input type="text" th:field="*{name}" class="form-control"
             placeholder="이름을 입력하세요"
             th:class="${#fields.hasErrors('name')}? 'form-control
fieldError' : 'form-control'">
      <p th:if="${#fields.hasErrors('name')}"
         th:errors="*{name}">Incorrect date</p>
    </div>
    <div class="form-group">
      <label th:for="city">도시</label>
      <input type="text" th:field="*{city}" class="form-control"
             placeholder="도시를 입력하세요">
    </div>
    <div class="form-group">
      <label th:for="street">거리</label>
      <input type="text" th:field="*{street}" class="form-control"
             placeholder="거리를 입력하세요">
    </div>
    <div class="form-group">
      <label th:for="zipcode">우편번호</label>
      <input type="text" th:field="*{zipcode}" class="form-control"
             placeholder="우편번호를 입력하세요">
    </div>
    <button type="submit" class="btn btn-primary">Submit</button>
  </form>
  <br/>
  <div th:replace="fragments/footer :: footer" />
</div> <!-- /container -->
</body>
</html><!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="fragments/header :: header" />
<style>
  .fieldError {
    border-color: #bd2130;
  }
</style>
<body>
<div class="container">
  <div th:replace="fragments/bodyHeader :: bodyHeader"/>
  <form role="form" action="/members/new" th:object="${memberForm}"
        method="post">
    <div class="form-group">
      <label th:for="name">이름</label>
      <input type="text" th:field="*{name}" class="form-control"
             placeholder="이름을 입력하세요"
             th:class="${#fields.hasErrors('name')}? 'form-control
fieldError' : 'form-control'">
      <p th:if="${#fields.hasErrors('name')}"
         th:errors="*{name}">Incorrect date</p>
    </div>
    <div class="form-group">
      <label th:for="city">도시</label>
      <input type="text" th:field="*{city}" class="form-control"
             placeholder="도시를 입력하세요">
    </div>
    <div class="form-group">
      <label th:for="street">거리</label>
      <input type="text" th:field="*{street}" class="form-control"
             placeholder="거리를 입력하세요">
    </div>
    <div class="form-group">
      <label th:for="zipcode">우편번호</label>
      <input type="text" th:field="*{zipcode}" class="form-control"
             placeholder="우편번호를 입력하세요">
    </div>
    <button type="submit" class="btn btn-primary">Submit</button>
  </form>
  <br/>
  <div th:replace="fragments/footer :: footer" />
</div> <!-- /container -->
</body>
</html>
```

---
## 회원 목록 조회
**회원 목록 컨트롤러에 내용 추가**
```java
package jpabook.jpashop.web;

@Controller
@RequiredArgsConstructor

public class MemberController { 
    
    // 내용 추가
    @GetMapping(value = "/members") 
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
  }
  
}
```

- 조회한 상품을 뷰에 전달하기 위해 스프링 MVC가 제공하는 모델( `Model` ) 객체에 보관
- 실행할 뷰 이름을 반환

회원 목록 뷰( `templates/members/memberList.html` )
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="fragments/header :: header" />
<body>
<div class="container">
  <div th:replace="fragments/bodyHeader :: bodyHeader" />
  <div>
    <table class="table table-striped">
      <thead>
      <tr>
        <th>#</th>
        <th>이름</th>
        <th>도시</th>
        <th>주소</th>
        <th>우편번호</th>
      </tr>
      </thead>
      <tbody>
      <tr th:each="member : ${members}">
        <td th:text="${member.id}"></td>
        <td th:text="${member.name}"></td>
        <td th:text="${member.address?.city}"></td>
        <td th:text="${member.address?.street}"></td>
        <td th:text="${member.address?.zipcode}"></td>
      </tr>
      </tbody>
    </table>
  </div>
  <div th:replace="fragments/footer :: footer" />
</div> <!-- /container -->
</body>
</html>
```

> 참고: 타임리프에서 ?를 사용하면 null 을 무시한다.

> 참고: 폼 객체 vs 엔티티 직접 사용  
> 참고: 요구사항이 정말 단순할 때는 폼 객체( `MemberForm` ) 없이 엔티티( `Member` )를 직접 등록과 수정 화면에서 사용해도 된다.  
> 하지만 화면 요구사항이 복잡해지기 시작하면, 엔티티에 화면을 처리하기 위한 기능이 점점 증가한다. 결과적으로 엔티티는 점점 화면에 종속적으로 변하고, 이렇게 화면 기능 때문에 지저분해진 엔티티는 결국 유지보수하기 어려워진다.  
실무에서 **엔티티는 핵심 비즈니스 로직만 가지고 있고, 화면을 위한 로직은 없어야 한다.** 화면이나 API에 맞는 폼 객체나 DTO를 사용하자. 그래서 화면이나 API 요구사항을 이것들로 처리하고, 엔티티는 최대한 순수하게 유지하자.

---
## 상품 등록
**상품 등록 폼 (Book)**
```java
package jpabook.jpashop.web;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BookForm {
    private Long id;
    
    private String name;
    private int price;
    private int stockQuantity;
    
    private String author;
    private String isbn;
}
```

**상품 등록 컨트롤러**
```java
package jpabook.jpashop.web;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {
    
    private final ItemService itemService;
    
    @GetMapping(value = "/items/new")
    public String createForm(Model model) {
        
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }
    @PostMapping(value = "/items/new")
    public String create(BookForm form) {
        
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        
        itemService.saveItem(book);
        
        return "redirect:/items";
    }
}
```

**상품 등록 뷰**(`items/createItemForm.html`)
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="fragments/header :: header" />
<body>
<div class="container">
  <div th:replace="fragments/bodyHeader :: bodyHeader"/>
  <form th:action="@{/items/new}" th:object="${form}" method="post">
    <div class="form-group">
      <label th:for="name">상품명</label>
      <input type="text" th:field="*{name}" class="form-control"
             placeholder="이름을 입력하세요">
    </div>
    <div class="form-group">
      <label th:for="price">가격</label>
      <input type="number" th:field="*{price}" class="form-control"
             placeholder="가격을 입력하세요">
    </div>
    <div class="form-group">
      <label th:for="stockQuantity">수량</label>
      <input type="number" th:field="*{stockQuantity}" class="formcontrol" placeholder="수량을 입력하세요">
    </div>
    <div class="form-group">
      <label th:for="author">저자</label>
      <input type="text" th:field="*{author}" class="form-control"
             placeholder="저자를 입력하세요">
    </div>
    <div class="form-group">
      <label th:for="isbn">ISBN</label>
      <input type="text" th:field="*{isbn}" class="form-control"
             placeholder="ISBN을 입력하세요">
    </div>
    <button type="submit" class="btn btn-primary">Submit</button>
  </form>
  <br/>
  <div th:replace="fragments/footer :: footer" />
</div> <!-- /container -->
</body>
</html>
```

**상품 등록**
- 상품 등록 폼에서 데이터를 입력하고 Submit 버튼을 입력하면 `/items/new` 를 POST 방식으로 요청한다.
- 상품 저장이 끝나면 상품 목록 화면 ( `redirect:/items` ) 로 리다이렉트한다.

---
## 상품 목록
**상품 목록 컨트롤러**
```java
package jpabook.jpashop.web;

@Controller
@RequiredArgsConstructor
public class ItemController { 
    private final ItemService itemService;
    
    // 상품 목록 list -> 모델에 담아둔 상품 목록인 items를 꺼내서 상품 정보 출력
    @GetMapping(value = "/items") 
    public String list(Model model) {
        
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        
        return "items/itemList";
    }
}
```

상품 목록 뷰( `items/itemList.html` )
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="fragments/header :: header" />
<body>
<div class="container">
  <div th:replace="fragments/bodyHeader :: bodyHeader"/>
  <div>
    <table class="table table-striped">
      <thead>
      <tr>
        <th>#</th>
        <th>상품명</th>
        <th>가격</th>
        <th>재고수량</th>
        <th></th>
      </tr>
      </thead>
      <tbody>
      <tr th:each="item : ${items}">
        <td th:text="${item.id}"></td>
        <td th:text="${item.name}"></td>
        <td th:text="${item.price}"></td>
        <td th:text="${item.stockQuantity}"></td>
        <td>
          <a href="#" th:href="@{/items/{id}/edit (id=${item.id})}"
             class="btn btn-primary" role="button">수정</a>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
  <div th:replace="fragments/footer :: footer"/>
</div> <!-- /container -->
</body>
</html>
```

`model` 에 담아둔 상품 목록인 `items` 를 꺼내서 상품 정보를 출력

---
## 상품 수정
**상품 수정과 관련된 컨트롤러 코드 (ItemController list 메서드 아래에 추가)**
```java
    // 상품(Book) 수정 폼 updateItemForm 메서드 실행하여 조회 후 뷰로 전달
    @GetMapping(value = "/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {

        Book item = (Book) itemService.findOne(itemId);

        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form", form);
        return "items/updateItemForm";
        }

    // 상품 수정 실행 폼 HTML 에는 id, 상품명, 가격, 수량 정보가 있음
    // 정보 수정 후 submit -> updateItem() 실행 -> item 엔티티 인스턴스는 준영속 상태
    // 영속성 컨텍스트의 지원 X, 데이터를 수정해도 변경 감지 기능 동작 X
    @PostMapping(value = "/items/{itemId}/edit")
    public String updateItem(@ModelAttribute("form") BookForm form) {

        Book book = new Book();
        book.setId(form.getId());
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);
        return "redirect:/items";
        }
```

상품 수정 폼 화면( `items/updateItemForm` )
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="fragments/header :: header" />
<body>
<div class="container">
  <div th:replace="fragments/bodyHeader :: bodyHeader"/>
  <form th:object="${form}" method="post">
    <!-- id -->
    <input type="hidden" th:field="*{id}" />
    <div class="form-group">
      <label th:for="name">상품명</label>
      <input type="text" th:field="*{name}" class="form-control"
             placeholder="이름을 입력하세요" />
    </div>
    <div class="form-group">
      <label th:for="price">가격</label>
      <input type="number" th:field="*{price}" class="form-control"
             placeholder="가격을 입력하세요" />
    </div>
    <div class="form-group">
      <label th:for="stockQuantity">수량</label>
      <input type="number" th:field="*{stockQuantity}" class="formcontrol" placeholder="수량을 입력하세요" />
    </div>
    <div class="form-group">
      <label th:for="author">저자</label>
      <input type="text" th:field="*{author}" class="form-control"
             placeholder="저자를 입력하세요" />
    </div>
    <div class="form-group">
      <label th:for="isbn">ISBN</label>
      <input type="text" th:field="*{isbn}" class="form-control"
             placeholder="ISBN을 입력하세요" />
    </div>
    <button type="submit" class="btn btn-primary">Submit</button>
  </form>
  <div th:replace="fragments/footer :: footer" />
</div> <!-- /container -->
</body>
</html>
```

### 상품 수정 폼 이동
1. 수정 버튼을 선택하면 `/items/{itemId}/edit` URL을 GET 방식으로 요청
2. 그 결과로 `updateItemForm()` 메서드를 실행하는데 이 메서드는 `itemService.findOne(itemId)` 를 호출해서 수정할 상품을 조회
3. 조회 결과를 모델 객체에 담아서 뷰( `items/updateItemForm` )에 전달

### 상품 수정 실행
상품 수정 폼 HTML에는 상품의 id(hidden), 상품명, 가격, 수량 정보 있음

1. 상품 수정 폼에서 정보를 수정하고 Submit 버튼을 선택
2. `/items/{itemId}/edit` URL을 POST 방식으로 요청하고 `updateItem()` 메서드를 실행
3. 이때 컨트롤러에 파라미터로 넘어온 `item` 엔티티 인스턴스는 현재 준영속 상태다. 따라서 영속성 컨텍스트의 지원을 받을 수 없고 데이터를 수정해도 변경 감지 기능은 동작X

---