#!/bin/sh

fn="$1.sub"
if [ -z "$fn" ]
then
    arguments: filename of the sub to execute
fi

if [ -n "$2" -a "$2" = "-i" ]
then
    interactive=true
else
    interactive=false
fi

IFS='
'
for line in $(cat $fn)
do
    if [ -z "$line" ]
    then
        continue
    fi
    line=$(echo $line | sed -e 's/[\x01-\x1f]//g')
    echo line="#$line#"
    if [ "$interactive" = "true" ]
    then
        echo "(Y/n)"
        read answer
        if [ "$answer" = "y" -o "$answer" = "" ]
        then
            cpm "$line"
        else
            break
        fi
    else
        cpm "$line"
    fi
done

rm -f *.'$$$'
