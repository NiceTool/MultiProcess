package com.analysys.dev.internal.Content;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 变量持有类
 * @Version: 1.0
 * @Create: 2018年9月3日 下午2:40:40
 * @Author: sanbo
 */
public class EGContext {

    /**
     * EGuan 内部调试控制. 主要用于控制堆栈打印、错误打印、内部提示信息打印
     */
    public static final boolean FLAG_DEBUG_INNER = true;
    /**
     * 用户debug控制
     */
    public static boolean FLAG_DEBUG_USER = true;
    /**
     * 是否展示广告通知。授权后，服务高版本可以切换成前台服务
     */
    public static boolean FLAG_SHOW_NOTIFY = true;

    /**
     * SDK版本
     */
    public static final String SDK_VERSION = "4.0.1";

    public static final String LOGTAG_DEBUG = "xxx";

    /**
     * xml 中声明的 appid、channel
     */
    public static final String XML_METADATA_APPKEY = "ANALYSYS_APPKEY";
    public static final String XML_METADATA_CHANNEL = "ANALYSYS_CHANNEL";

    public static final String SERVICE_NAME = "com.analysys.dev.service.AnalysysService";

    // 应用列表获取周期时间
    public static final int SNAPSHOT_CYCLE = 30 * 60 * 1000;
    // 位置获取周期时间
    public static final int LOCATION_CYCLE = 5 * 60 * 1000;
    // 应用打开关闭获取周期时间
    public static final int OC_CYCLE = 5 * 1000;
    // 应用打开关闭获取周期时间
    public static final int UPLOAD_CYCLE = 6 * 60 * 60 * 1000;
    // 心跳检查
    public static final int CHECK_HEARTBEAT_CYCLE = 15 * 1000;

    public static final String SP_APP_KEY = "appKey";
    public static final String SP_APP_CHANNEL = "appKey";

    public static final String SP_SNAPSHOT_TIME = "getSnapshotTime";

    public static final String SP_WIFI = "wifi";
    public static final String SP_WIFI_DETAIL = "wifiDetail";

//    public static final String SP_BASE_STATION = "baseStation";
    public static final String SP_LOCATION = "location";

    public static final String SP_LOCATION_TIME = "getLocationTime";
    public static final String SP_MAC_ADDRESS = "MACAddress";

    public static final String SP_DAEMON_TIME = "getDaemonTime";

    public class LOGINFO {
        public static final String LOG_NOT_APPKEY = "please check you appkey!";
    }
    public static final String APPSNAPSHOT_PROC_SYNC_NAME = "install.txt";
    public static final String SP_APP_IDFA = "appIDFA";
    public static final String SWITCH_TYPE_DEFAULT = "1";

    //防作弊相关信息开关，默认不上传，可控制上传
    public static boolean SWITCH_OF_PREVENT_CHEATING = false;
    //蓝牙信息，默认不上传，需要根据服务器控制
    public static boolean SWITCH_OF_BLUETOOTH = false;
    //电量信息，默认不上传，需要根据服务器控制
    public static boolean SWITCH_OF_BATTERY = false;
    //更加详细的设备详情信息，默认可不上传，可用于确定设备信息
    public static boolean SWITCH_OF_DEV_FURTHER_DETAIL= false;
    //系统阶段保持信息，默认可不上传，根据服务器控制来上传
    public static boolean SWITCH_OF_SYSTEM_INFO= false;


    public static int OC_COLLECTION_TYPE_RUNNING_TASK = 1;//getRunningTask
    public static int OC_COLLECTION_TYPE_PROC = 2;//读取proc
    public static int OC_COLLECTION_TYPE_AUX = 3;//辅助功能
    public static int OC_COLLECTION_TYPE_SYSTEM = 4;//系统统计

    public static String SNAP_SHOT_DEFAULT = "-1";
    public static String SNAP_SHOT_INSTALL = "0";
    public static String SNAP_SHOT_UNINSTALL = "1";
    public static String SNAP_SHOT_UPDATE = "2";

    public static String NETWORK_TYPE_2G ="2G";
    public static String NETWORK_TYPE_3G ="3G";
    public static String NETWORK_TYPE_4G ="4G";
    public static String NETWORK_TYPE_WIFI ="WIFI";
    public static String NETWORK_TYPE_NO_NET ="无网络";

    public static final String SP_NAME = "eg_policy";
    public static final String POLICY_VER_DEFALUT = "";
    public static final long SERVER_DELAY_DEFAULT = 0L;
    public static final int FAIL_COUNT_DEFALUT = 5;
    public static final long FAIL_TRY_DELAY_DEFALUT = 60 * 60 * 1000;
    public static final int TIMER_INTERVAL_DEFALUT = 5 * 1000;
    public static final int TIMER_INTERVAL_DEFALUT_60 = 60 * 1000;
    public static final int EVENT_COUNT_DEFALUT = 10;
    public static final int USER_RTP_DEFALUT = 1;
    public static final int USER_RTL_DEFAULT = 1;
    public static final int UPLOAD_SD_DEFALUT = 1;
    public static final int REMOTE_IP = 1;
    public static final int MERGE_INTERVAL = 30 * 60 * 60 * 1000;//TODO 需确认
    public static final int MIN_DURATION = 60 * 1000;//TODO 需确认
    public static final int MAX_DURATION = 5 * 60 * 1000;//TODO 需确认
    public static final int DOMAIN_UPDATE_TIMES = 0;//TODO 需确认
    public static final long PERMIT_FOR_FAIL_TIME_DEFALUT = 0;
    private static final int PERMIT_FOR_SERVER_TIME_DEFALUT = 0;

    public static String APP_URL = null;
    public static String DEVIER_URL = null;




//    /**
//     * 运营统计分析与用户画像内部版本与发版日期 及整体版本号
//     */
//    public static final String SDK_VERSION = "3.7.9.3|20181228";
    public final static String URL_SCHEME = "http://";
    /**
     * 实时上传端口
     */
    public final static String RT_PORT = ":8099";
    /**
     * 测试回传接口.Debug模式
     */
    public final static String TEST_CALLBACK_PORT = ":10031";
    /**
     * 非实时上传端口
     */
    public final static String ORI_PORT = ":8089";
    /**
     * 实时域名
     */
    public static final String RT_DOMAIN_NAME = "rt101.analysys.cn";
    public static final String USERTP_URL = URL_SCHEME + RT_DOMAIN_NAME + RT_PORT;
    /**
     * 测试域名
     */
    public static final String TEST_CALLBACK_DOMAIN_NAME = "apptest.analysys.cn";
    /**
     * 非实时上传是,使用的域名池,以ait开始的为应用上传接口;以urd开始的为设备上传接口
     */
    public final static String[] NORMAL_UPLOAD_URL = {"ait103.analysys.cn",
            "urd103.analysys.cn",// 0
            "ait240.analysys.cn", "urd240.analysys.cn",// 1
            "ait183.analysys.cn", "urd183.analysys.cn",// 2
            "ait409.analysys.cn", "urd409.analysys.cn",// 3
            "ait203.analysys.cn", "urd203.analysys.cn",// 4
            "ait490.analysys.cn", "urd490.analysys.cn",// 5
            "ait609.analysys.cn", "urd609.analysys.cn",// 6
            "ait301.analysys.cn", "urd301.analysys.cn",// 7
            "ait405.analysys.cn", "urd405.analysys.cn",// 8
            "ait025.analysys.cn", "urd025.analysys.cn",// 9
            "ait339.analysys.cn", "urd339.analysys.cn"// 头部应用 用作测试
    };
    /**
     * 实时计算接口
     */
    public static final String RT_URL = URL_SCHEME + RT_DOMAIN_NAME + RT_PORT;
    /**
     * 测试回传接口
     */
    public static final String TEST_CALLBACK_URL = URL_SCHEME + TEST_CALLBACK_DOMAIN_NAME + TEST_CALLBACK_PORT;
    public static final String TEST_URL = URL_SCHEME + TEST_CALLBACK_DOMAIN_NAME + TEST_CALLBACK_PORT;
    public static String RT_APP_URL = "";
    public static String RT_DEVIER_URL = "";
    public static String NORMAL_APP_URL = "";
    public static String NORMAL_DEVIER_URL = "";

    public static String PERMIT_FOR_FAIL_TIME = "policy_fail_time";


    public static String POLICY_SERVICE_PULL_VER = "servicePullVer";

}
