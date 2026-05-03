# 酒店管理系统

一个基于 Java JDBC 和 Oracle 的控制台版酒店管理系统，覆盖课程设计文档中的核心功能：

- 用户登录和角色区分
- 房间增删改查
- 房间条件查询和空闲房查询
- 普通用户预订和取消订单
- 管理员查看、确认、完成、取消、删除订单
- 房间状态、订单状态、收入统计
- 房间号重复校验、订单日期冲突校验、状态合法性校验

## 项目结构

```text
src/com/hotel
├─ Main.java
├─ config
├─ dao
├─ entity
├─ service
└─ util

sql
├─ schema.sql
├─ init_data.sql
└─ query_demo.sql
```

## 运行前准备

1. 安装 Oracle 数据库并创建一个可用账号。
2. 执行 `sql/schema.sql` 创建表、序列、索引、视图和触发器。
3. 执行 `sql/init_data.sql` 导入测试数据。
4. 可选执行 `sql/query_demo.sql` 验证主要查询。
5. 将 Oracle JDBC 驱动 `ojdbc8.jar` 放到 `lib/` 目录。
6. 修改 `src/com/hotel/config/DatabaseConfig.java` 中的数据库连接信息。

## 编译

```powershell
javac -encoding UTF-8 -cp "lib/*" -d out (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
```

## 运行

```powershell
java -cp "out;lib/*" com.hotel.Main
```

## 单元测试

本项目内置了一套不依赖外部测试框架的轻量级单元测试，默认不连接 Oracle，使用内存假数据执行。

全部执行：

```powershell
cmd /c scripts\run-tests.cmd
```

按测试类执行：

```powershell
cmd /c scripts\run-tests.cmd RoomServiceTest
```

按测试方法执行：

```powershell
cmd /c scripts\run-tests.cmd OrderServiceTest#bookRoomShouldCreatePendingOrderAndCalculateTotalPrice
```

## 默认测试账号

- 管理员：`admin / 123456`
- 用户：`user01 / 123456`
- 用户：`user02 / 123456`

## 主要规则说明

- 房间状态只允许：`空闲`、`已预订`、`维修中`
- 订单状态只允许：`待确认`、`已确认`、`已完成`、`已取消`
- 用户角色只允许：`admin`、`user`
- 新增和修改房间时会检查房间号是否重复
- 预订房间时会检查入住日期、离店日期和订单时间冲突
- 管理员不能直接删除已有订单历史的房间

## 建议答辩演示顺序

1. 使用 `admin` 登录，展示房间查询和新增房间
2. 使用 `user01` 登录，查询空闲房并提交预订
3. 切回管理员，查看全部订单并确认订单
4. 再演示完成订单和统计信息
