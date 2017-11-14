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

lines=""
while IFS='' read -r line || [ -n "$line" ]; do
    lines="$lines$line"$'\n'
done < "$fn"

OLDIFS=$IFS
IFS=$'\n'
for line in $lines; do
    line=$(echo $line | sed -e 's/[\x01-\x1f]//g')
    echo "$line"
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
IFS=$OLDIFS

rm *.'$$$'
