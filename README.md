# spring-cloud-gateway-jwt使用教程
Spring Cloud Gateway接口鉴权。

开启jwt-client和jwt-gateway

开启spring-cloud-gateway-server项目下的两个微服务

开启nacos，当微服务启动后会注册到nacos中

开启redis:

启动redis服务端
```
$ cd /Users/guochao/software/redis-5.0.5
$ cd src
$ ./redis-server ../redis.conf
```

启动redis客户端
```
$ cd /Users/guochao/software/redis-5.0.5
$ cd src
$ ./redis-cli
```

redis管理系统
```
启动tomcat
cd /Users/guochao/software/tomcat/
./start.sh

http://127.0.0.1:8081/treenms/treesoft/login
用户名密码：admin  treesoft
1. 启动Tomcat
2. 访问地址
```

### nacos访问链接：

http://localhost:8848/nacos/index.html  用户名密码：nacos

### 用postman访问：

http://localhost:2222/auth?userName=admin&password=admin

获取token，使用token去访问微服务中的接口，gateway会检测进行token鉴权，正确才会跳转路由，访问微服务。

将token加入postman中Headers加入

Authorization:token(如eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJNSU5HIiwiZXhwIjoxNTY5NzUwNzg1LCJ1c2VyTmFtZSI6ImFkbWluIiwiaWF0IjoxNTY5NzUwNzI1fQ.ucCs3P8djRO9FtmKmlYhA5QVukwNj44dQFZwObCCLEo)

http://localhost:8888/route/exam/analyse/student/test?caseId=2001

如下链接不鉴权：

http://localhost:8888/api/exam/analyse/JWT-CLIENT/auth



