#!/bin/sh

if [[ $# -lt 1 ]]; then
echo "Usage: $0 folderPath"
echo "Example: ./fix.sh /home/maf/Development/workspaces/java_workspace/github-maf-desktop-app/app"
exit 1
fi

#remove existing java license
for file in $(find $1 -name "*.java")
do
  sed -i -e '/^\/\*\! LICENSE/,/ \*\//{s/\/\*\! LICENSE//p;d}' $file
done

#add java license
for file in $(find $1 -name "*.java")
do
  cat java.txt $file > tmp
  mv tmp $file
done

#remove existing html license
for file in $(find $1 -name "*.html")
do
  sed -i -e '/^@\* LICENSE/,/ \*@/{s/@\* LICENSE//p;d}' $file
done

#if exists, move the current top comment block to the end of the file
for file in $(find $1 -name "*.html")
do
  sed -n -e '/^@\*/,/\*@/{s/@\* //p;p}' $file > tmp1
  if [[ -s tmp1 ]] ; then
    sed -e '/^@\*/,/\*@/{s/@\* //p;d}' $file > tmp2
    (cat tmp2; echo; cat tmp1) > tmp3
    mv tmp3 $file
  fi ;
done

#add html license
for file in $(find $1 -name "*.html")
do
  cat html.txt $file > tmp
  mv tmp $file
done

