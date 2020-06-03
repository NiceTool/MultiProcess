#!/usr/bin/env bash

source_common() {
    pwd=$(
        cd $(dirname $0)
        pwd
    )
    source $pwd/common.sh md
}


buildAndRunUT(){
  # 编译apk包和ut测试包
  $gw :dev-sdk:assembleAndroidTest
  $gw :app:assembleRelease
  # 对已连接所有机器执行ut测试，返回结果至spoon目录
  java -jar tools/spoon/spoon.jar --test-apk ./dev-sdk/build/outputs/apk/androidTest/debug/dev-sdk-debug-androidTest.apk --apk ./app/build/outputs/apk/release/app-release.apk --output ./spoon/
}

mode_up()
{
    chmod -R 777 *
    git config core.filemode false
}

connectAdb(){
  # 连接自己机器
  $iadb connect 192.168.6.79:5555
  # todo 连接群控机器
}

main() {
    mode_up
    source_common
    connectAdb
    buildAndRunUT
}

main