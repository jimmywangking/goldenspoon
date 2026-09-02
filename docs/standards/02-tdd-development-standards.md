# TDD 测试驱动开发规范

> 适用场景：本项目后端开发全流程
> 核心理念：先写测试，再写实现，测试是需求的第一版文档

---

## 一、TDD 核心循环：RED → GREEN → REFACTOR

    RED          GREEN         REFACTOR
   +------+    +------+      +----------+
   |测试  | -> |测试  |  ->  |代码重构   |
   |失败  |    |通过  |      |优化结构   |
   +------+    +------+      +----------+

   写失败测试    写最少代码    消除重复/改进设计

### 1.1 RED 阶段（写失败测试）
- 先写测试用例，描述期望行为
- 运行测试，确认**失败**（绿色条之前必须是红色）
- 如果测试编译不通过或无法运行，不算真正的 RED

### 1.2 GREEN 阶段（写最少代码）
- 写**刚好能让测试通过**的代码
- 不做任何多余的功能
- 不优化、不重构，只要能过测试就行

### 1.3 REFACTOR 阶段（重构）
- 测试全部通过的前提下，优化代码结构
- 消除重复、提取方法、改进命名
- **重构后必须重新跑测试**，确保没有回归

---

## 二、测试分层金字塔

       /\
      /  \     E2E 测试（少量）
     /----\
    /      \   集成测试（中等数量）
   /--------\
  /          \ 单元测试（大量）
 /------------\

| 层级 | 占比 | 速度 | 覆盖范围 | 工具 |
|------|------|------|----------|------|
| 单元测试 | 70% | 毫秒级 | 单个方法/类 | JUnit5 + Mockito |
| 集成测试 | 25% | 秒级 | 模块间协作 | SpringBootTest |
| E2E 测试 | 5% | 分钟级 | 完整业务流程 | Cypress/Playwright |

---

## 三、单元测试规范

### 3.1 命名规范
```
方法命名：test_被测方法_输入条件_期望结果
```

**JUnit5 风格：**
```java
class CustomerServiceTest {

    @Test
    @DisplayName("创建客户 - 有效输入 - 返回客户ID")
    void createCustomer_validInput_returnsCustomerId() {
        // arrange
        CreateCustomerRequest request = new CreateCustomerRequest("张三", "zhangsan@example.com");
        when(customerRepository.save(any())).thenReturn(new Customer(1L, "张三"));

        // act
        Customer result = customerService.createCustomer(request);

        // assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("张三");
    }

    @Test
    @DisplayName("创建客户 - 名称为空 - 抛出BusinessException")
    void createCustomer_emptyName_throwsBusinessException() {
        assertThrows(BusinessException.class, () -> {
            customerService.createCustomer(new CreateCustomerRequest("", null));
        });
    }
}
```

### 3.2 AAA 模式（必遵守）
```java
@Test
void methodTest() {
    // Arrange（准备）- 设置数据和mock
    // Act（执行）- 调用被测方法
    // Assert（断言）- 验证结果
}
```

### 3.3 断言规范
```java
// 推荐：使用 AssertJ  fluent API
assertThat(result).isNotNull();
assertThat(result.getId()).isEqualTo(1L);
assertThat(result.getName()).isEqualTo("张三");
assertThat(list).hasSize(3);
assertThat(list).containsExactly("a", "b", "c");
assertThat(exception).isInstanceOf(BusinessException.class)
                     .hasMessageContaining("名称不能为空");

// 禁止：模糊断言
// assertTrue(result != null);  ← 应该用 assertThat(result).isNotNull()
```

### 3.4 Mock 规范
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_shouldNotifyWhenCreated() {
        // 只 mock 依赖，不 mock 被测对象本身
        when(orderRepository.save(any())).thenReturn(mockOrder);
        
        orderService.createOrder(request);
        
        // 验证交互
        verify(notificationService).sendOrderCreatedNotification(any());
        verify(orderRepository, times(1)).save(any());
    }
}
```

---

## 四、测试覆盖率要求

| 层级 | 最低覆盖率 | 目标覆盖率 |
|------|-----------|-----------|
| 核心业务逻辑（Service层） | 80% | 90%+ |
| 工具类（Utils/Helpers） | 70% | 80%+ |
| Controller层 | 50% | 70%+ |
| 整体项目 | 70% | 80%+ |

> **注意**：覆盖率高不代表质量好，重点覆盖边界条件和异常路径。

---

## 五、测试数据规范

### 5.1 内联数据（小量）
```java
@Test
void test() {
    List<String> testData = List.of("Alice", "Bob", "Charlie");
    // ...
}
```

### 5.2 参数化测试（多组数据）
```java
@ParameterizedTest
@ValueSource(strings = {"valid@example.com", "user@domain.org"})
@NullSource
@EmptySource
void validateEmail_invalidEmail_throwsException(String email) {
    if (email == null || email.isEmpty()) {
        assertThrows(IllegalArgumentException.class, 
            () -> validator.validateEmail(email));
    } else {
        assertThatCode(() -> validator.validateEmail(email))
            .doesNotThrowAnyException();
    }
}
```

### 5.3 工厂方法（复杂对象）
```java
class CustomerFactory {
    static Customer buildValidCustomer() {
        return new Customer(1L, "测试客户", "test@example.com", 
            CustomerType.ENTERPRISE, true);
    }
    
    static Customer buildWithInvalidEmail() {
        return new Customer(2L, "坏数据客户", "not-an-email", 
            CustomerType.PERSONAL, true);
    }
}
```

---

## 六、集成测试规范

### 6.1 使用 H2 内存数据库
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional  // 每个测试方法结束后回滚
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void testCreateCustomer() throws Exception {
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"张三\",\"email\":\"z@x.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }
}
```

### 6.2 测试数据库隔离
- 每个测试类独立 Schema
- 使用 `@Transactional` 保证回滚
- 禁止在测试中修改共享测试数据

---

## 七、本项目 TDD 工作流

```
1. 写测试（RED）
   -> tests/customer/CustomerServiceTest.java
   -> 确认测试失败

2. 写实现（GREEN）
   -> src/modules/customer/service/CustomerServiceImpl.java
   -> 确认测试通过

3. 重构（REFACTOR）
   -> 优化代码结构
   -> 重新跑测试确保无回归

4. 覆盖率检查
   -> mvn test-coverage
   -> 未达标的补充测试
```

---

## 八、常见反模式（禁止）

| 反模式 | 问题 | 正确做法 |
|--------|------|----------|
| 先写全部代码再写测试 | 失去TDD意义 | 先写失败测试 |
| 测试之间共享状态 | 测试顺序敏感 | 每个测试独立准备数据 |
| 测试断言过多 | 耦合实现细节 | 只断言公开行为 |
| 测试名为 "test1" "test2" | 无法理解意图 | 描述性行为名称 |
| 忽略边界条件 | 遗漏重要路径 | 正常 + 边界 + 异常三路 |
| 测试里 sleep/wait | 不稳定 | 用 CountDownLatch 或轮询 |

---

## 九、测试工具链

```xml
<!-- pom.xml 依赖 -->
<dependencies>
    <!-- 测试框架 -->
    <dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId><scope>test</scope></dependency>
    <!-- Mock框架 -->
    <dependency><groupId>org.mockito</groupId><artifactId>mockito-core</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.mockito</groupId><artifactId>mockito-junit-jupiter</artifactId><scope>test</scope></dependency>
    <!-- 断言库 -->
    <dependency><groupId>org.assertj</groupId><artifactId>assertj-core</artifactId><scope>test</scope></dependency>
    <!-- 测试工具 -->
    <dependency><groupId>org.testcontainers</groupId><artifactId>junit-jupiter</artifactId><scope>test</scope></dependency>
    <!-- 覆盖率 -->
    <dependency><groupId>org.jacoco</groupId><artifactId>jacoco-maven-plugin</artifactId><version>0.8.12</version></dependency>
</dependencies>
```

---

*文档创建日期：2026-08-20*
