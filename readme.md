# track SDK

> 新版本的设备SDK. 更新工作机制,增加定制成本。

## 编译方法

* 确定版本信息: 现阶段需要手动保持一致.
    * `./dev-sdk/src/com/analysys/track/internal/Content/EGContext.java` 中`SDK_VERSION`调整SDK的版本号
    * `./dev-sdk/build.gradle`中的`version`和`date`调整打包版本号和日期
* 编译: 根目录下执行`sh build.sh`即可


## 更新日志

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
    
