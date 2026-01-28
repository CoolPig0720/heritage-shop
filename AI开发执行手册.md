# AI开发执行手册

## 开发注意事项

### 1. 开发流程
- 根据我提出的问题先进行分析
- 给出分析结果及解决方案
- 制定开发任务清单
- 按照任务清单分阶段进行开发
- 开发完成进行该次总结
- 每个阶段完成后等待开发者测试确认
- 测试通过后再进行下一阶段开发

### 2. 代码规范
- 严格按照设计方案进行开发
- 遵循项目现有的代码风格和结构
- 所有实体类、DTO、Service、Controller等按照设计方案实现

### 3. Maven环境配置
每次打开Trae需要调用终端时，必须先执行以下命令：
```powershell
& "g:\heritage-shop\setup-maven.ps1"
```
打开后端服务命令：mvn -f g:\heritage-shop\backend\pom.xml spring-boot:run
打开前端服务命令：npm run dev
**原因**：Trae的环境变量和本地电脑不一样，无法正常使用Maven命令。
**关于 Trae 环境手动执行 mvn 的操作**
- `setup-maven.ps1` 做的事是“把 Maven 的 bin 目录追加到当前终端会话的 PATH”，所以它只对“当前这个 PowerShell 终端”生效；你新开一个终端就需要再执行一次（这是正常现象）。
- 在新终端中这样执行即可：
  - 方式 1（推荐）：  
    ```powershell
    & "g:\heritage-shop\setup-maven.ps1"
    mvn -v
    ```
  - 如果脚本被执行策略拦截：  
    ```powershell
    Set-ExecutionPolicy -Scope Process Bypass
    & "g:\heritage-shop\setup-maven.ps1"
    mvn -v
    ```
  - 方式 2（不跑脚本，直接用 Maven 绝对路径）：  
    ```powershell
    & "C:\Program Files\Java\apache-maven-3.9.12\bin\mvn.cmd" -v
    ```

**打开后端服务的命令**
- 你现在项目后端启动用这个（在任意终端里先确保能用 mvn）：
  ```powershell
  $env:JAVA_TOOL_OPTIONS='-Xms128m -Xmx512m'
  mvn -f g:\heritage-shop\backend\pom.xml spring-boot:run
  ```
  其中 `JAVA_TOOL_OPTIONS` 是为了避免你之前遇到的内存/页面文件问题（你也可以按机器情况调整）。


### 4. 数据库操作
所有数据库相关操作（建表、插入数据等）由开发者手动在Navicat中执行，AI只需要提供SQL语句文件。
**操作流程**：
1. AI生成SQL文件到 `backend/src/main/resources/sql/` 目录
2. 开发者手动在Navicat中执行SQL语句
3. 开发者告知AI数据库操作已完成

### 5.运行npn命令时
PowerShell doesn't support &&. I need to use ; instead or run the commands separately.

### 6.运行命令前
需要让我运行命令前，请告诉我该命令的作用。

### 7.前端端口
前端端口固定为3030



