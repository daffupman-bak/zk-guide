# zk-guide

## 一、分布式系统

### 1、什么是分布式系统

- 有很多台计算机组成一个整体，一个整体一致对外并且处理同一请求；
- 内部的每台计算机都可以相互通信（rest、rpc、webservice）；
- 客户端到服务到服务端的一次请求到响应结束会历经多台计算机。

### 2、分布式系统的瓶颈

高并发场景

## 二、Zookeeper

### 1、zk特性

- 一致性：数据一致性，数据按照顺序分批入库；
- 原子性：事务要么成功要么失败，不会局部化；
- 单一视图：客户端连接集群中的任意zk节点，数据都是一致的；
- 可靠性：每次都zk的操作状态都会保存到服务端；
- 实时性：客户端可以读取到zk服务端的最新数据。

### 2、zk安装

- 依赖jdk
- zk单机

### 3、zk操作

- 客户端连接：

  ```shell
  ./zkServer.sh start		# 在bin目录下，启动服务端
  ./zkCli.sh				# 连接到客户端，按ctrl + c 退出客户端
  ```

- 基本操作：

  ```bash
  help					# 查看zk基本操作
  ls [path]				# 查看指定path下的子节点
  stat [path]				# 查看指定path节点的状态
  ls2 [path]				# 查看指定path下的子节点和path节点的状态（ls+stat）
  get [path]				# 查看指定path节点的数据和状态
  
  create [-s] [-e] path data acl
  set path data [version]
  delete path [version]
  ```

### 4、zoo.cfg文件

复制 `zoo_sample.cfg` 文件为 `zoo.cfg` 文件。

- `tickTime`：用于计算的时间单元。比如session超时：N * tickTime；
- `initLimit`：用于集群，允许 `从节点` 连接并同步到 `master节点` 的初始化连接时间，以tickTime的倍数来表示；
- `syncLimit`：用于集群，`master主节点` 与 `从节点` 之间发送消息，请求和应答时间长度。（心跳机制）；
- `dataDir`：建议配置到指定的位置，而不是临时位置；
- `dataLogDir`：日志目录，如果不配置会和dataDir公用；
- `clientPort`：连接服务器的端口，默认2181；

### 5、zk基本数据模型

- 类似于Unix的文件系统的树形结构；
- 每个节点称为ZNode，它可以有数据，也可以有子节点；
- 节点分为临时节点和永久节点，临时节点在客户端断开后会消失；
- 每个节点都有各自的版本号，可以通过命令行来显示节点信息；
- 每当节点数据发生变化，那么该节点的版本号会累加（乐观锁）；
- 删除或修改过时节点，版本号不匹配则会报错；
- 每个zk节点存储的数据不宜过大，几k即可；
- 节点可以设置权限ACL来限制用户的访问；

### 6、zk作用

- master节点选举

  主节点挂了以后，从节点就会接手工作，并且保证这个节点是唯一的，这也是所谓的首脑模式，从而保证集群的高可用；

- 统一配置文件管理

  只需要部署一台服务器，则可以把相同的配置同步更新到其他所有服务器，此操作常用于云计算；

- 发布与订阅

  类似消息队列MQ，发布者把数据存在ZNode上，订阅者会读取这个数据；

- 提供分布式锁

  解决分布式环境中不同进程之间的资源争夺问题，类似于多线程中的锁；

- 集群管理

  保证集群中数据的强一致性。

### 7、zk特性

- session的基本原理

  客户端与服务端之间的连接称为会话；每个会话都可以设置一个超时时间；心跳结束，session过期；Session过期，临时节点则会丢失；心跳机制：客户端向服务端发送ping包请求；

- watcher机制

  - 针对每一个节点都会有一个监听者（watcher）；

  - 当监控的每个对象（ZNode）发生了变化，则触发watcher事件；

  - zk的watcher是一次性，也就是触发后立即销毁；

  - 父子节点的增删改都能触发其watcher，事件包括：

    - 创建父节点触发的事件：NodeCreated

      ```shell
      stat /one watch		# 使用stat设置watch，/one节点还不存在
      create /one 123		# 创建one节点，此时触发NodeCreated事件
      ```

    - 节点数据变化触发的事件：NodeDeleted

      ```shell
      get /one watch		# 使用get设置watch
      set /one 456		# 修改节点数据，触发NodeDeleted事件
      ```

    - 节点删除触发的事件：NodeDataChanged

      ```shell
      get /one watch
      delete /one			# 删除节点，触发NodeDataChanged事件
      ```

    - 在父节点下创建或删除子节点触发的事件，但是修改子节点的数据不会触发此事件：NodeChildrenChanged

      ```shell
      ls /one watch		# 使用ls设置watch
      create /one/two 2	# 在one路径下创建two节点，触发NodeChildrenChanged事件
      ```

      以下命令都可以给指定的path节点设置watch事件，path可以不存在

      ```shell
      stat path watch
      get path watch
      ls path watch
      ls2 path watch
      ```
  
  - 使用场景
  
- acl权限控制

  针对节点可以设置相关读写等权限，目的为了保障数据安全性；权限permissions可以指定不同的权限范围以及角色；

  ACL命令行：

  - getAcl：获取某个节点的acl权限信息；
- setAcl：设置某个节点的acl权限信息；
  - addAuth：输入认证授权信息，注册时输入明文密码（登录）但在zk系统里，密码是以加密的形式存在的。

  ACL的构成：

  ` [ scheme:id:permissions ] `
  
  - scheme：采用的权限机制
  
    - world：world下只有一个id，即只有一个用户，也就是anyone，那么组合的写法就是 `world:anyone:[permissions]`；这是默认的权限，所有用户都可以访问；
  
    - auth：认证登录，需要注册用户有权限才可以操作，`auth:user:password:[permissions]` ，其中密码为明文；
  
        - digest：需要对密码加密才能访问，`disgest:username:BASE64(SHA1(password)):[permissions]`；
  
        - auth与disgest基本上是一样的，前者需要输入明文，后者需要输入密文；

          ```shell
          setAcl /path auth:wang:wang:cdrwa
          setAcl /path disgest:wang:BASE64(SHA1(wang)):cdrwa
          # 等价于
          addauth disgest lee:lee
          ```
  
  - ip：当设置为ip指定的ip地址，此时限制ip进行访问，比如`ip:192.168.1.1:[permissions]`；
  
  - super：超级管理员，拥有所有权限。
  
  - id：允许访问的用户
  
  - permissions：权限组合字符串
  
    - c：create，可创建当前节点的子节点
  
    - r：read，获取节点或子节点列表
  
    - d：delete，删除子节点
  
    - w：write，设置节点数据
  
    - a：admin，可分配权限
  
      ```shell
      setAcl /one/two world:anyone:crwd # 拥有crwd权限
      
      # 添加用户
      addauth digest wang:123:crwda
      # 设置权限
      setAcl /path auth:wang:123:crwda
      # 或者
      setAcl /path auth::crwda
      
      setAcl /names/ip ip:192.168.1.10:crwda # 限定ip
      
      # 增加super权限
      vim zkServer.sh
      # 添加如图配置
      ./zkServer.sh restart	# 重启zk
      ```
  
      ![](https://raw.githubusercontent.com/daffupman/markdown-img/master/20200205004635.png)
  
  acl的使用场景：
  
  - 开发/测试环境分离，开发者无权操作测试库的节点，只能读取；
  - 生产环境上可以控制指定的ip访问相关节点，防止混乱；
  
### 8、四字命令

zk可以通过它自身提供的简写命令来和服务器进行交互，需要使用nc命令，`yum install nc` 。

- stat：查看zk的状态信息，以及mode；

  ```shell
  echo stat | nc localhost 2181
  ```

- ruok：查看当前zkserver是否启动，返回imok；

- dump：列出未经处理的会话和临时节点；

- conf：查看服务器相关的配置；

- cons：展示连接到服务器的客户端信息；

- envi：环境变量；

- mntr：监控zk健康信息

- wchs：展示watch信息

- wchc、wchp 

##   三、zk集群

主从节点，心跳机制（选举模式）。zk集群至少需要3个节点，一个master，两个slave。

- 配置数据文件myid1/2/3对应server.1/2/3；
- 通过./zkCli.sh -server [ip]:[port]检测集群是否配置成功。

## 四、原生api

1、会话连接与恢复

2、节点的crud

3、watch与acl相关操作

4、CountDownLatch的使用
- 计数器，用于多线程，可以暂停也可以继续；
- await()
- countDown()

## 五、Apache Curator客户端

### zk原生api的不足

- 不能自动超时重连，需要手动操作；
- Watch注册一次后会失效；
- 不支持递归操作节点；