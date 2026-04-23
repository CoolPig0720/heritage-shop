# Spring Boot Windows 终端中文日志乱码修复指南

## 问题现象

Spring Boot 应用在 Windows 终端（包括 Qoder 内置终端、IDEA Terminal、PowerShell 等）运行时，日志中的中文显示为乱码，例如：

```
c.h.security.JwtAuthenticationFilter : 鐢ㄦ埛鏉冮檺: [ROLE_ADMIN]
c.h.security.JwtAuthenticationFilter : Token 楠岃瘉鎴愬姛
```

或

```
c.h.security.JwtAuthenticationFilter : �û�Ȩ��: [ROLE_ADMIN]
```

## 根因分析

### 编码链路

```
Java String (Unicode)
    → Logback encoder (charset=?)
        → stdout 字节流
            → Windows ConPTY / 终端 (按编码解读)
                → 屏幕显示
```

乱码的本质是 **编码链路中某一环的编码不匹配**。

### 两种典型乱码模式

| 乱码示例 | 解读 | 含义 |
|---------|------|------|
| `鐢ㄦ埛鏉冮檺` | UTF-8 字节 → 被 GBK 解读 | JVM 输出了 UTF-8，终端按 GBK 解读 |
| `�û�Ȩ��` | GBK 字节 → 被 UTF-8 解读 | JVM 输出了 GBK，终端按 UTF-8 解读 |

### 为什么 Windows 上特别容易出现此问题

1. **Windows 中文系统**的默认代码页是 936（GBK），Java 的 `file.encoding` 默认跟随系统为 GBK
2. **Qoder 内置终端 / VSCode Terminal** 基于 xterm.js，内部使用 UTF-8，不走 Windows 代码页体系
3. **IDEA Terminal** 同样基于 xterm.js，也期望 UTF-8
4. 如果 JVM 输出 GBK 字节，但这些终端按 UTF-8 解读 → 乱码

## 正确修复方案

### 两步缺一不可

#### 第一步：创建 logback-spring.xml

在 `backend/src/main/resources/logback-spring.xml` 中配置 ConsoleAppender 的 encoder charset 为 UTF-8：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <!-- 控制台输出：使用 UTF-8 编码 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder charset="UTF-8">
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

**作用**：控制 Logback encoder 输出的字节编码为 UTF-8。

#### 第二步：在 pom.xml 中设置 JVM 参数

在 `spring-boot-maven-plugin` 配置中添加 `jvmArguments`：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
                <jvmArguments>-Dfile.encoding=UTF-8</jvmArguments>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**作用**：让 JVM 的 `System.out` 编码也为 UTF-8，与 Logback encoder 保持一致。

### 为什么两步缺一不可

| 只做第一步 | 只做第二步 |
|-----------|-----------|
| Logback encoder 输出 UTF-8 字节 | `file.encoding=UTF-8` 让 System.out 用 UTF-8 |
| 但 `file.encoding` 仍为 GBK | 但没有 logback-spring.xml，encoder 可能用默认编码 |
| System.out 编码与 encoder 不一致，仍可能乱码 | 编码链路不完全一致，仍可能乱码 |

## 常见错误方案及原因

### ❌ 方案一：只加 JVM 参数 `-Dfile.encoding=UTF-8`

```xml
<jvmArguments>-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8</jvmArguments>
```

**问题**：没有 logback 配置文件时，Logback 的 ConsoleAppender 可能使用默认编码（跟随 `file.encoding`），但如果 Spring Boot 的默认 logback 配置被覆盖或行为不一致，仍可能出现编码不匹配。

### ❌ 方案二：`chcp 65001` + PowerShell Profile 永久配置

```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 > $null
```

**问题**：
- Qoder / VSCode / IDEA 的内置终端基于 xterm.js，不走 Windows 代码页体系，`chcp` 对它们无效
- 即使在原生 PowerShell 中生效，也只改了终端侧，没解决 Java 输出侧的编码

### ❌ 方案三：logback charset 设为 GBK

```xml
<encoder charset="GBK">
```

**问题**：方向反了。Qoder / IDE 终端期望 UTF-8，输出 GBK 字节反而会导致 `�` 替换字符。

### ❌ 方案四：同时设置 `sun.stdout.encoding` 和 `sun.stderr.encoding`

这些是私有 API 参数，不同 JVM 版本行为不一致，且 Logback 不一定通过 `System.out` 直接输出（它有自己的 encoder 机制），所以不可靠。

## 不同运行方式的注意事项

### Maven 启动（`mvn spring-boot:run`）

pom.xml 中的 `jvmArguments` 会生效，两步配置即可。

### IDEA 直接运行

pom.xml 的 `jvmArguments` 对 IDEA 直接运行不生效，需要在 IDEA 的 Run Configuration 中手动添加 VM options：

```
-Dfile.encoding=UTF-8
```

同时确保 `logback-spring.xml` 存在。

### JAR 包直接运行

```bash
java -Dfile.encoding=UTF-8 -jar heritage-shop-1.0.0.jar
```

启动时手动指定 `-Dfile.encoding=UTF-8`，同时确保 `logback-spring.xml` 已打包进 JAR。

## 验证方法

启动后端后，观察包含中文的日志行是否正常显示：

```log
c.h.security.JwtAuthenticationFilter : 请求路径: /api/customize/unread-count     ✅ 正确
c.h.security.JwtAuthenticationFilter : 提取的用户名: admin                         ✅ 正确
c.h.security.JwtAuthenticationFilter : 用户权限: [ROLE_ADMIN]                      ✅ 正确
c.h.security.JwtAuthenticationFilter : Token 验证成功，已设置认证信息                ✅ 正确
```

如果看到以下内容则为乱码，需要检查配置：

```log
c.h.security.JwtAuthenticationFilter : 鐢ㄦ埛鏉冮檺: [ROLE_ADMIN]     ❌ UTF-8被当GBK解读
c.h.security.JwtAuthenticationFilter : �û�Ȩ��: [ROLE_ADMIN]            ❌ GBK被当UTF-8解读
```

## 快速排查清单

遇到乱码时，按以下顺序检查：

1. ✅ `logback-spring.xml` 是否存在于 `src/main/resources/` 目录
2. ✅ `logback-spring.xml` 中 encoder 的 charset 是否为 `UTF-8`
3. ✅ pom.xml 中 `spring-boot-maven-plugin` 是否配置了 `-Dfile.encoding=UTF-8`
4. ✅ 如果用 IDEA 运行，Run Configuration 的 VM options 是否有 `-Dfile.encoding=UTF-8`
5. ✅ 如果用 JAR 运行，启动命令是否包含 `-Dfile.encoding=UTF-8`
6. ❌ 确认没有多余的 `chcp 65001` 或 PowerShell Profile 中的编码设置干扰
7. ❌ 确认没有 `sun.stdout.encoding` / `sun.stderr.encoding` 等私有 API 参数
