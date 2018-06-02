#!/bin/sh
plmx="plmx/plmx.zip"
addenv ()
{
    unzip -oq $plmx
}

plmxfiles()
{
    zipinfo -1 $plmx
}

delenv ()
{
    dir=$(basename $(pwd))
    if [ "$dir" = "plmx" ]
    then
        echo "Action cannot be done from plmx"
        return 1
    fi
    file1='/tmp/.file$1'
    file2='/tmp/.file$2'
    ls -1 | sort > $file1
    plmxfiles | sort > $file2
    rm -f $(comm -1 "$file1" "$file2") $file1 $file2
}

deltemp ()
{
    file1='/tmp/.file$1'
    file2='/tmp/.file$2'
    ls -1 | grep 'mac\|rel\|sym\|rom\|ram\|$$$' | sort > $file1
    plmxfiles | sort > $file2
    rm -f $(comm -23 "$file1" "$file2") $file1 $file2
}

# remove the '1A's at the end of cp/m text file
cleanfile()
{
    if [ -f "$1" ]
    then
        sed -i -e 's/\x1a//g' "$1"
    fi
}

