# user-service

用户与成员档案业务微服务，负责用户、学生/成员资料的查询和维护。

- HTTP：`18082`
- 数据库：`test.user`、`test.student`
- 统一 TraceId 和 API 错误日志来自 `platform-common`

## 表字段

`test.user`：

| 字段 | 含义 |
| --- | --- |
| `id` | 用户主键。 |
| `code` | 用户业务编码或外部账号编号。 |
| `name` | 用户名称。 |
| `age` | 用户年龄，现有表使用字符串类型保存。 |
| `address` | 用户地址。 |

`test.student`：

| 字段 | 含义 |
| --- | --- |
| `id` | 学生或成员档案主键。 |
| `name` | 学生或成员姓名。 |
| `age` | 学生或成员年龄。 |
| `beginDate` | 当前档案记录开始时间，默认使用创建时间。 |
| `endDate` | 当前档案记录结束时间，现有表会随记录更新自动刷新。 |

启动：

```bash
cd ~/IdeaProjects/springboot3-java21-demo
JAVA_HOME="$HOME/Library/Java/JavaVirtualMachines/graalvm-jdk-21.0.7/Contents/Home" \
PATH=$JAVA_HOME/bin:$PATH \
java -jar user-service/target/user-service-0.0.1-SNAPSHOT.jar
```

主要接口：

- `GET /api/users/findAll`
- `POST /api/users/insert`
- `POST /api/users/findById`
- `GET /api/students/selectOne`
- `GET /api/students/queryAllByLimit`
