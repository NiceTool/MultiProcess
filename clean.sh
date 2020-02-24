#!/usr/bin/env bash


: '
 清除程序入口
'

source_common()
{
    pwd=$(cd `dirname $0`; pwd)
    source $pwd/common.sh
}

clean_task()
{
    logw "[$filename]>>>>clean project<<<<"
    dir=("app" "dev-sdk" "buildSrc" "dex")
    for element in ${dir[@]}
    do
        #clean task
        rm -rf $element/build/
        rm -rf $element/bin/
        rm -rf $element/gen/
        rm -rf $element/.externalNativeBuild
        logd "[$filename]clean $element over."
    done

    rm -rf build/
    rm -rf release/
    rm -rf releasebak/
    rm -rf sh.exe.stackdump
    rm -rf classes.dex

    if  [ $# == 0 ]; then
        logi "[$filename]clean project success. "
    else
        loge "[$filename]clean project Failed!"
    fi

}

main()
{
    source_common
    clean_task
}

main

