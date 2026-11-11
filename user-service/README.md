# user-service

用户与成员档案业务微服务，负责用户、学生/成员资料的查询和维护。

- HTTP：`18082`
- 数据库：`test.user`、`test.student`
- 统一 TraceId 和 API 错误日志来自 `platform-common`

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
