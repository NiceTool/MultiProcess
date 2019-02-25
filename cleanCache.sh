#!/bin/bash

: '
 清除程序入口
'
echo "clean android studio cache! "
echo ">>>>you must close android studio<<<<"
dir=("app" "dev-sdk" )

for element in ${dir[@]}
do
    #clean task
    rm -rf $element/build/
    rm -rf $element/bin/
    rm -rf $element/gen/
    rm -rf $element/.settings/
    rm -rf $element/.externalNativeBuild
    rm -rf *.iml
done

rm -rf build/
rm -rf release/
rm -rf releasebak/
rm -rf *.iml
rm -rf .gradle/
rm -rf .idea/

if  [ $# == 0 ]; then
    echo " clean project success. "
else
    echo ">>clean project Failed!<<"
fi
