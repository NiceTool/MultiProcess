package com.eguan.db;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 18/2/3 16:04
 * @Author: sanbo
 */
public class EGDBEncryptException extends Exception {
    private static final long serialVersionUID = 8994678041814102833L;

    public EGDBEncryptException() {
        super();
    }

    public EGDBEncryptException(String msg) {
        super(msg);
    }

    public EGDBEncryptException(Exception e) {
        super(e);
    }
}
