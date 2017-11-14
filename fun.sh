#!/bin/sh
plmxdir="../plmx"
addenv ()
{
    cp $plmxdir/* .
}

delenv ()
{
    dir=$(basename $(pwd))
    if [ "$dir" = "plmx" ]
    then
        echo "Action cannot be done from plmx"
        return 1
    fi
    local file1='/tmp/.file$1'
    local file2='/tmp/.file$2'
    ls -1 | sort > $file1
    ls -1 $plmxdir | sort > $file2
    local files=$(comm -1 $file1 $file2)
    rm -f $files $file1 $file2
}

deltemp ()
{
    local file1='/tmp/.file$1'
    local file2='/tmp/.file$2'
    ls -1 | grep 'mac\|rel\|sym\|$$$' | sort > $file1
    ls -1 $plmxdir | sort > $file2
    local files=$(comm -23 $file1 $file2)
    rm -f $files $file1 $file2
}

# remove the '1A's at the end of cp/m text file
cleanfile()
{
    if [ -f "$1" ]
    then
        sed -i -e 's/\x1a//g' rect.hck
    fi
}
