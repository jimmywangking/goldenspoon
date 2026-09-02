# 阿里巴巴 Java 开发手册（核心要点）

> 适用场景：本项目 CRM 后端可能采用 Java/Spring Boot 技术栈时参考
> 版本：基于《阿里巴巴Java开发手册》1.x/2.x 核心规范

---

## 一、命名规范

### 1.1 类名
- **风格**：UpperCamelCase（大驼峰）
- **要求**：名词，表示类职责
- **示例**：`CustomerManager`、`OrderService`、`ProductController`
- **禁止**：`Manager1`、`Test`、`A`

### 1.2 方法名
- **风格**：lowerCamelCase（小驼峰）
- **动词开头**：`get` / `find` / `query` / `count` / `delete` / `update` / `save` / `create` / `remove`
- **布尔返回**：`is` / `has` / `can` 开头
- **示例**：`getCustomerById()`、`isValidOrder()`、`hasPermission()`

### 1.3 变量名
- **风格**：lowerCamelCase
- **禁止**：单字母变量（除循环 `i/j/k`）、匈牙利命名法
- **示例**：`customerList`、`orderId`、`totalCount`

### 1.4 常量名
- **风格**：UPPER_SNAKE_CASE
- **示例**：`MAX_RETRY_COUNT`、`DEFAULT_PAGE_SIZE`、`API_TIMEOUT_MS`

### 1.5 包名
- **风格**：全小写，点分隔
- **规范**：`com.company.project.module`
- **示例**：`com.example.crm.customer`

---

## 二、代码格式

### 2.1 缩进
- 使用 **4 个空格**，禁止 Tab
- 大括号不换行（K&R 风格）

### 2.2 行宽
- 单行不超过 **120 字符**
- 超长时换行对齐到括号或操作符

### 2.3 空行
- 方法之间空一行
- 逻辑块之间空一行
- 类头部加 Javadoc 后空一行再写字段

### 2.4 大括号
```java
// 正确
if (condition) {
    doSomething();
} else {
    doAnother();
}

// 错误
if (condition)
{
    doSomething();
}
```

---

## 三、OOP 规约

### 3.1 访问控制
- 禁止通过 `对象.内部变量` 直接访问，使用 getter/setter
- 静态方法用 `类名.方法()` 调用

### 3.2 equals 与 hashCode
- 重写 `equals` 必须重写 `hashCode`
- 用 `Objects.equals()` 比较，禁止 `==` 比较对象内容

### 3.3 构造器
- 多个构造参数时，使用 **Builder 模式**
- 禁止在构造器中做耗时操作

### 3.4 集合处理
```java
// 初始化时指定容量
List<String> list = new ArrayList<>(16);
Map<String, Object> map = new HashMap<>(16);

// 判空用 CollectionUtils
if (CollectionUtils.isEmpty(list)) { ... }

// 遍历删除用 Iterator
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (shouldRemove(it.next())) it.remove();
}
```

### 3.5 泛型
- 禁止 `Raw Type`（如 `List` 不带泛型）
- 泛型参数用单个大写字母：`T`, `E`, `K`, `V`, `R`

---

## 四、异常处理

### 4.1 原则
- **不要**捕获 `Exception` 作为万能catch，指定具体异常类型
- **不要**吞掉异常（空 catch 块）
- **不要**用异常做流程控制
- 抛出异常要携带有意义的消息

### 4.2 统一异常处理
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result handleBusiness(BusinessException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }
}
```

### 4.3 自定义异常
```java
public class BusinessException extends RuntimeException {
    private final String code;
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}
```

---

## 五、日志规范

### 5.1 使用 SLF4J + Logback
```java
@Slf4j  // Lombok 注解
public class CustomerService {
    public void createCustomer(CustomerDTO dto) {
        log.info("开始创建客户, customerId={}", dto.getCustomerId());
        try {
            // 业务逻辑
            log.info("客户创建成功, id={}", customerId);
        } catch (Exception e) {
            log.error("客户创建失败, dto={}", dto, e);  // 最后一个参数是 Throwable
            throw new BusinessException("CREATE_FAILED", e.getMessage());
        }
    }
}
```

### 5.2 禁止
- 禁止用 `System.out.println`
- 禁止打印敏感信息（密码、身份证、手机号部分脱敏）
- ERROR 级别必须带异常堆栈

### 5.3 日志级别
| 级别 | 用途 |
|------|------|
| DEBUG | 开发调试，生产关闭 |
| INFO | 业务流程关键节点 |
| WARN | 可恢复的异常，需关注 |
| ERROR | 系统错误，需立即处理 |

---

## 六、SQL 规约

### 6.1 禁止项
- 禁止 `SELECT *`，明确列出字段
- 禁止在 SQL 中使用函数（如 `WHERE YEAR(create_time) = 2026`）
- 禁止大表全表扫描，必须加索引
- 禁止事务中包含 RPC/HTTP 调用

### 6.2 索引规范
```sql
-- 单列索引：区分度 > 10% 的字段
-- 联合索引：遵循最左前缀原则
-- 示例
CREATE INDEX idx_customer_phone ON customer(phone);
CREATE INDEX idx_order_status_time ON orders(status, create_time);
```

### 6.3 分页
```sql
-- 深分页优化：延迟关联
SELECT t.* FROM customers t
INNER JOIN (SELECT id FROM customers WHERE status = 1 LIMIT 10000, 20) tmp
ON t.id = tmp.id;
```

---

## 七、Spring Boot 规约

### 7.1 分层架构
```
controller  → 接收请求，参数校验，调用 service
service     → 业务逻辑，事务管理
mapper      → 数据访问，只做 CRUD + 简单查询
dto         → 请求/响应数据传输对象
entity      → 数据库实体映射
vo          → 视图展示对象（返回给前端）
```

### 7.2 事务注解
```java
@Service
public class OrderServiceImpl implements OrderService {

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public OrderResult createOrder(CreateOrderRequest request) {
        // 事务边界内只做 DB 操作
    }
}
```

### 7.3 参数校验
```java
public class CreateCustomerRequest {
    @NotBlank(message = "客户名称不能为空")
    @Size(max = 100, message = "客户名称不能超过100字")
    private String customerName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "客户类型不能为空")
    private Integer customerType;
}
```

---

## 八、安全规约

- 密码必须加密存储（BCrypt）
- 敏感操作需要二次确认
- 接口防重放：时间戳 + 签名
- SQL 注入防护：用参数化查询，禁止字符串拼接
- XSS 防护：输入过滤 + 输出转义

---

## 九、注释规范

```java
/**
 * 创建客户
 *
 * @param request 客户创建请求
 * @return 创建结果，包含新生成的客户ID
 * @throws BusinessException 当客户名称已存在时抛出
 */
@Transactional(rollbackFor = Exception.class)
public CustomerVO createCustomer(CreateCustomerRequest request) {
    // 业务实现
}
```

**禁止**：无意义注释（`// 设置名称`）、过时注释、中文 mixed 英文混合无标点。

---

*文档创建日期：2026-08-20*
*参考来源：阿里巴巴Java开发手册（泰山版）*
