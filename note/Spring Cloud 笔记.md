# Spring Cloud 笔记

# 一、基本项目搭建

## 1.1 父工程

这里首先搭建一个普通的`maven`工程

![1](.\assert\1.png)

然后将相关的`src`目录删除，下面给出`pom`文件：

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>win.iot4yj</groupId>
	<artifactId>springcloud-demo01</artifactId>
	<version>1.0</version>
	<packaging>pom</packaging>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.0.3.RELEASE</version>
		<relativePath/>
	</parent>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>11</java.version>
		<spring-cloud.version>Finchley.RELEASE</spring-cloud.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<scope>provided</scope>
		</dependency>
	</dependencies>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
</project>
```

## 1.2 Eureka Server 搭建

`springcloud`是一种利用`springboot`框架搭建的解决方案，而在这种项目中一般会存在多个微服务，而`Eureka Server`就是用来管理这些为服务的。

![2](.\assert\2.png)

下面给出其`pom`文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>win.iot4yj</groupId>
	<artifactId>eureka-server</artifactId>
	<version>1.0</version>
	<name>eureka-server</name>
	<description>Demo project for Spring Boot</description>

	<parent>
		<groupId>win.iot4yj</groupId>
		<artifactId>springcloud-demo01</artifactId>
		<version>1.0</version>
	</parent>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
		</dependency>
	</dependencies>
</project>

```

这里继承了父工程，当然还需要在父工程的`pom`文件中添加：

```xml
<modules>
	<module>eureka-server</module>
</modules>
```

给启动类`EurekaServerApplication`加上`@EnableEurekaServer`注解：

```java
package win.iot4yj;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaServerApplication.class, args);
	}
}
```

然后对应用进行配置（`application.yml`）

```yaml
spring:
  application:
    name: eureka-server
server:
  port: 8880
eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka
```

注意：上面的`defaultZone`地址最后一定要是`eureka`，不然其他服务注册不了。通过`register-with-eureka：false`和`fetch-registry：false`来表明自己是一个eureka server。

此时如果启动，则会报

```
java.lang.TypeNotPresentException: Type javax.xml.bind.JAXBContext not present
```

因为`JAXB-API`是`java ee`的一部分，在`jdk9`中没有在默认的类路径中。`java ee api`在`jdk`中还是存在的，默认没有加载而已，`jdk9`中引入了模块的概念，可以使用模块命令`--add-modules java.xml.bind`引入`jaxb-api`。这里我们在父工程中加入：

```xml
<dependency>
	<groupId>javax.xml.bind</groupId>
	<artifactId>jaxb-api</artifactId>
	<version>2.3.0</version>
</dependency>
<dependency>
	<groupId>com.sun.xml.bind</groupId>
	<artifactId>jaxb-impl</artifactId>
	<version>2.3.0</version>
</dependency>
<dependency>
	<groupId>org.glassfish.jaxb</groupId>
	<artifactId>jaxb-runtime</artifactId>
	<version>2.3.0</version>
</dependency>
<dependency>
	<groupId>javax.activation</groupId>
	<artifactId>activation</artifactId>
	<version>1.1.1</version>
</dependency>
```

此时启动，使用地址`localhost:8880`即可访问。

![3](.\assert\3.png)

## 1.3 user-client用户管理微服务搭建

这里我们创建一个简单的用户管理微服务，创建方式和上面一样，这里直接给出`pom`文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>win.iot4yj</groupId>
	<artifactId>user-client</artifactId>
	<version>1.0</version>
	<name>user-client</name>
	<description>Demo project for Spring Boot</description>

	<parent>
		<groupId>win.iot4yj</groupId>
		<artifactId>springcloud-demo01</artifactId>
		<version>1.0</version>
	</parent>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
		</dependency>
	</dependencies>
</project>
```

这里主要添加`spring-cloud-starter-netflix-eureka-client`，其他的配置后面再详述。然后进行配置，直接给出配置文件：

```yaml
server:
  port: 8881
spring:
  application:
    name: user-client
eureka:
  instance:
    hostname: localhost
  client:
    service-url:
      defaultZone: http://${eureka.instance.hostname}:8880/eureka
```

注意：这里的`defaultZone`要和`eureka-server`一致。

下面给启动类加上`@EnableEurekaClient`注解，然后启动`eureka-server`(先启动)和本应用。

![4](.\assert\4.png)

这里可以看到我们的用户微服务就注册上去了。



































