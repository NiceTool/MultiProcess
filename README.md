# MultiProcess

多进程测试框架

### 依赖

```java
maven { url 'https://raw.githubusercontent.com/miqt/maven/master' }
maven { url 'https://gitee.com/miqt/maven/raw/master' }
```

```java
debugImplementation/implementation 'com.nicetool:MultiProcessTest:1.0.0'
```

### 使用

集成测试任务接口：

```java
public class ChinaPrint implements ImpTask {
    @Override
    public String getName() {
        return "[" + ProcessUtils.getCurrentProcessName() + "] I'm Chinese!";
    }

    @Override
    public void work() {
        MpLog.d(
                "ChinaPrint. work ------------["
                        + ProcessUtils.getCurrentProcessName()
                        + "]------print msg");
    }
}
```

调用开始测试：

```java
MultiprocessManager.getInstance(MainActivity.this)
        .postMultiMessages(4, new ChinaPrint());
```

参数1：测试进程数，最多支持50个进程

参数2：测试任务
