#!/usr/bin/env bash

source_common() {
  pwd=$(
    cd $(dirname $0)
    pwd
  )
  source $pwd/common.sh
}

build() {
  logw "[==========================================================]"
  logw "[======================   开始编译  =======================]"
  logw "[==========================================================]"
  #echo "You use gradle:  $gw "

  $gw :dexdemo:build

  if [ $? -ne 0 ]; then
    loge "[********************************]"
    loge "[**** graddew build 执行失败  ****]"
    loge "[********************************]"
  else
    logi "[********************************]"
    logi "[******* graddew build 成功 *****]"
    logi "[********************************]"
    # need delay .wait for build over
    $dx --dex --output=classes.dex ./dexdemo/build/intermediates/aar_main_jar/release/classes.jar
    if [ $? -ne 0 ]; then
      loge "[********************************]"
      loge "[*********** dx打包失败 **********]"
      loge "[********************************]"
    else
      logi "[********************************]"
      logi "[*********** dx打包成功 **********]"
      logi "[********************************]"
      jar cvf temp.jar classes.dex

      if [ $? -ne 0 ]; then
        loge "[********************************]"
        loge "[*********** jar打包失败 *********]"
        loge "[********************************]"
      else
        logi "[********************************]"
        logi "[*********** 打jar成功 ***********]"
        logi "[********************************]"
        mv -f temp.jar $HOME/Desktop/temp_$time.jar
        logw "[==========================================================]"
        logw "[======================   移动完毕  =======================]"
        logw "[==========================================================]"
      fi
    fi
  fi
}

main() {
  time=$(date "+%Y%m%d_%H%M%S")
  source_common
  build
}

main
