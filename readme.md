# track SDK

> 新版本的设备SDK. 更新工作机制,增加定制成本。

## 编译方法

* 确认隐藏字符串: `dev-sdk/proguard.json`
* 确定是否`release`版本: `dev-sdk/build.gradle` 中，`isRelease`，`true`会关闭日志上传正式地址，`false`可自己配置
* 确定打包类型: `dev-sdk/build.gradle `中`isPackageJar`，`true`打jar包，`false`则打dex包
* 确定版本号: `dev-sdk/build.gradle` 中，`ver`、`date`、`subVersion`
    * 规则: jar包的`subVersion`必须为`00`，`dex`的包必须从`01`开始
* 编译: 根目录下执行`sh build.sh`即可
* 编译完成后，需要测试混淆字符串是否正确，需要手动调用对应测试case脚本`dev-sdk/src/androidTest/java/com/miqt/costtime/StrMixPsGenerate.java`， 跑**case**: `checkProguardText`

## release 包 验证步骤
1. 最后打了release 包之后,需要验证的事项, 防止操作人打包失误.
2. 验证jar包混淆了 ,  (开发自己来)
3. 验证 hide api ,三个维度 ,
    a. 使用谷歌扫描工具扫描不出来(开发自己来)
    b. logcat 无打印相关 hide api 信息 (开发测试都进行)过滤 hidden ,
    c. 9,10 的设备, 不弹窗警告灰色api(开发测试都进行验证) 验证的时候新建一个app,里面什么都不集成 只有我们SDK
4. 验证logcat 没有忘关(开发测试都验证)
5. 验证上传地址是线上地址(开发测试都验证)
6. 验证版本没有填错(开发测试都验证)
7. 验证bugly上报,方法耗时上报关掉了(开发自己来)



## 更新日志


### 4.3.0.7|20200214
---------

* 更新日期：`2020年3月5日`
* 打包类型：线下版本
* 分支: `dev`
* 体积：
* 更新内容：

  1. USM优化
  2. 调试设备部分优化


### 4.3.0.7|20200201
---------

* 更新日期：`2020年2月15日`
* 打包类型：线上版本
* 分支: `dev`
* 体积：
* 更新内容：

  1. 隐藏热修复，classloader
  2. 提高判断debug设备的可靠性。
  3. 详细内容，参考：[v4.3.0.7变更项](http://con.analysys.cn/pages/viewpage.action?pageId=27002228)

### 4.3.0.6|20191213
---------

* 更新日期：`2019年12月23日`
* 打包类型：线上版本
* 分支: `dev`
* 体积：
* 更新内容：

  1. 修复热修复的可靠性优化
  2. 混淆敏感字符串
  3. 隐藏必要的灰色 api 调用
  4. 不工作策略

### 4.3.0.5|20191121
---------

* 更新日期：`2019年12月3日`
* 打包类型：线上版本
* 分支: `dev`
* 体积：
* 更新内容：

  1. 新增热修复功能,支持打包不包含热修功能的包
  2. 新增USMInfo,获取用量信息
  3. 新增NetInfo网络信息节点
  4. 修复上个版本的一个DB错误
  5. 新增低内存检测,低内存下NetInfo,上传停止

### 4.3.0.4|20191113_v2
---------

* 更新日期：`2019年11月22日`
* 打包类型：线下版本
* 分支: `compatible`
* 体积：
* 更新内容：

  1. 服务启动逻辑对于调试设备不主动初始化,非调试设备不初始化时,计数工作由10次调整为20次
  2. 文档里面服务名字改为:AnalysysService
  3. 服务调整为可选声明,保证稳定

### 4.3.0.4|20191113
---------

* 更新日期：`2019年11月13日`
* 打包类型：线上版本
* 分支: `compatible`
* 体积：
* 更新内容：

    1. 新增启动兼容逻辑
    2. 新增字符串混
    3. 用户文档进程名字修改为:as


### 4.3.0.4|20191015
---------

* 更新日期：`2019年10月22日`
* 打包类型：线上版本
* 分支: `dev`
* 体积：
* 更新内容：

    1. 修复策略同步不生效的问题
    2. 修复首次上传,概率机型无安装列表信息的问题
    3. 代码优化

    
### 4.3.0.3|20190806
---------

* 更新日期：`2019.08.12`
* 打包类型：线上版本
* 分支: `dev`
* 体积：
* 更新内容：

    1. 消息工作机制调整
    2. 调整目录结构,去除部分无用代码
    


### 4.3.0.1|20190524
---------

* 更新日期：`2019.05.24`
* 打包类型：线上版本
* 分支: `sprint`
* 体积：152k
* 更新内容：

    1. 修复遗留bug
        双卡获取异常
        service异常
    2. 定位部分增加指标
    


## 4.3.0.0|20190509
---------

* 更新日期：`2019.05.09`
* 打包类型：线上版本
* 分支: `dev`
* 体积：147k
* 更新内容[新版本首次发版]：

    1. 收集应用快照、位置信息、wifi信息、基站信息、OC信息等
    2. 其中所有大模块均受策略控制，是否收集；各字段均受策略控制，是否收集；默认均为收集状态
    3. 快照获取方式为通过API方式获取，若在部分手机拒绝了是否允许弹窗，获取应用个数小于5个，则改为shell命令方式获取，对应用快照数据进行补充
    4. 基站信息会优先获取周围的基站，最多获取不重复的5组值；然后根据网络不同，依次会获取各网络制式下的基站信息，每种亦最多记录不重复的5组值
    5. OC信息根据版本号不同，4.x版本和5/6版本获取方式不同，前者依赖Android提供的API接口，后者通过proc相关信息获取，与此同时，后者也会在计算OC信息的同时获取proc相关的数据，作为XXXInfo,用于验证后者获取的OC数据的准确性
    6. 广播情况下，会对OC数据和应用快照进行数据补充操作
    7. 支持可以注册服务、广播工作，不集成时依附所在进程工作
    

