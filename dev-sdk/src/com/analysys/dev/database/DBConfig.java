package com.analysys.dev.database;

public class DBConfig {
  public static class OC {
    // 表名
    public static final String TABLE_NAME = "e_oci";
    /**
     * 列名
     */
    public static class Column {
      public static final String ID = "id";
      // 存储单条完整信息
      public static final String OCI = "oci_a";
      // 存储时间
      public static final String IT = "oci_b";
      // 存储标记，默认为0，读取成功设置1
      public static final String ST = "oci_c";
      // 备用字段 text 类型
      public static final String OCIRA = "oci_ra";
      public static final String OCIRB = "oci_rb";
      public static final String OCIRC = "oci_rc";
    }
    //建表
    public static final String CREATE_TABLE  = String.format("create table if not exists %s (%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s)",
            TABLE_NAME,Column.ID ,DBType.AUTOINCREMENT ,Column.OCI , DBType.TEXT ,Column.IT ,DBType.VARCHAR_TWENTY ,
            Column.ST,DBType.INT_NOT_NULL,Column.OCIRA , DBType.TEXT ,Column.OCIRB , DBType.TEXT ,Column.OCIRC ,DBType.TEXT );
  }

  public static class OCCount {
    // 表名
    public static final String TABLE_NAME = "e_occ";

    public static class Column {
      public static final String ID = "id";
      // 应用包名
      public static final String APN = "occ_a";
      // 应用名称
      public static final String AN = "occ_b";
      // 开始时间
      public static final String AOT = "occ_c";
      // 结束时间
      public static final String ACT = "occ_d";
      // 应用打开关闭次数
      public static final String CU = "occ_e";
      // 日期
      public static final String DY = "occ_f";
      // insert 的时间
      public static final String IT = "occ_g";
      // 应用版本信息
      public static final String AVC = "occ_h";
      // 网络类型
      public static final String NT = "occ_i";
      // 应用切换类型，1-正常使用，2-开关屏幕切换，3-服务重启
      public static final String AST = "occ_j";
      // 应用类型
      public static final String AT = "occ_k";
      // OC采集来源，1-getRunningTask，2-读取proc，3-辅助功能，4-系统统计
      public static final String CT = "occ_l";
      // 时间段标记
      public static final String TI = "occ_m";
      // 存储标记，默认为 0，上传读取后修改为 1
      public static final String ST = "occ_n";
      // 应用运行状态，默认值 0，正在运行为 1
      public static final String RS = "occ_o";

      // 备用字段 text 类型
      public static final String OCT_RA = "oct_ra";
      public static final String OCT_RB = "oct_rb";
      public static final String OCT_RC = "oct_rc";
    }
    //建表
    public static final String CREATE_TABLE = String.format("create table if not exists %s (%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s)",
            TABLE_NAME,Column.ID,DBType.AUTOINCREMENT ,Column.APN,DBType.VARCHAR_HUNDRED ,Column.AN,DBType.VARCHAR_TWENTY ,Column.AOT,DBType.VARCHAR_TWENTY ,Column.ACT,DBType.VARCHAR_TWENTY_NULL ,Column.CU,DBType.INT_NOT_NULL ,
            Column.DY,DBType.VARCHAR_TWENTY ,Column.IT,DBType.VARCHAR_TWENTY ,Column.AVC,DBType.VARCHAR_TWENTY ,Column.NT,DBType.VARCHAR_TEN ,Column.AST,DBType.VARCHAR_TEN_NULL ,Column.AT,DBType.VARCHAR_TEN ,
            Column.CT,DBType.VARCHAR_TEN,Column.TI,DBType.VARCHAR_TEN ,Column.ST,DBType.VARCHAR_TEN ,Column.RS,DBType.VARCHAR_TEN ,Column.OCT_RA,DBType.TEXT ,Column.OCT_RB,DBType.TEXT ,Column.OCT_RC,DBType.TEXT);
  }

  public static class AppSnapshot {
    // 表名
    public static final String TABLE_NAME = "e_asi";

    public static class Column {
      public static final String ID = "id";
      // 应用包名
      public static final String APN = "asi_a";
      // 应用名称
      public static final String AN = "asi_b";
      // 应用版本号
      public static final String AVC = "asi_c";
      // 应用状态
      public static final String AT = "asi_e";
      // 操作时间
      public static final String AHT = "asi_f";

      public static final String ASI_RA = "asi_ra";
      public static final String ASI_RB = "asi_rb";
      public static final String ASI_RC = "asi_rc";
    }
    //建表
    public static final String CREATE_TABLE  = String.format("create table if not exists %s (%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s)",
            TABLE_NAME,Column.ID,DBType.AUTOINCREMENT ,Column.APN,DBType.VARCHAR_HUNDRED ,Column.AN,DBType.VARCHAR_HUNDRED ,
            Column.AVC,DBType.VARCHAR_TWENTY ,Column.AT,DBType.VARCHAR_TWENTY  ,Column.AHT,DBType.VARCHAR_TWENTY  ,
            Column.ASI_RA,DBType.TEXT ,Column.ASI_RB,DBType.TEXT ,Column.ASI_RC,DBType.TEXT );
  }

  public static class Location {
    // 表名
    public static final String TABLE_NAME = "e_l";

    public static class Column {
      public static final String ID = "id";
      // 存储单条完整信息
      public static final String LI = "l_a";
      // 存储时间
      public static final String IT = "l_b";
      // 存储标记，默认为0，读取成功设置1
      public static final String ST = "l_c";

      // 备用字段 text 类型
      public static final String L_RA = "l_ra";
      public static final String L_RB = "l_rb";
      public static final String L_RC = "l_rc";
    }
    //建表
    public static final String CREATE_TABLE  = String.format("create table if not exists %s (%s%s,%s%s,%s%s,%s%s,%s%s,%s%s,%s%s)",
            TABLE_NAME,Column.ID,DBType.AUTOINCREMENT ,Column.LI,DBType.TEXT,Column.IT,DBType.VARCHAR_TWENTY ,
            Column.ST,DBType.VARCHAR_TEN , Column.L_RA,DBType.TEXT ,Column.L_RB,DBType.TEXT ,Column.L_RC,DBType.TEXT);
  }
}