#!/bin/sh

set -e

mkdir -p git/snapshots/com/agifac/lib/app-framework/$1
mkdir -p git/snapshots/com/agifac/maf/maf-desktop-datamodel/$2

cp ~/.m2/repository/com/agifac/lib/app-framework/$1/*.jar git/snapshots/com/agifac/lib/app-framework/$1
cp ~/.m2/repository/com/agifac/lib/app-framework/$1/*.pom git/snapshots/com/agifac/lib/app-framework/$1
cp ~/.m2/repository/com/agifac/maf/maf-desktop-datamodel/$1/*.jar git/snapshots/com/agifac/maf/maf-desktop-datamodel/$1
cp ~/.m2/repository/com/agifac/maf/maf-desktop-datamodel/$1/*.pom git/snapshots/com/agifac/maf/maf-desktop-datamodel/$1

cd git
git init

git config user.name "Travis CI"
git config user.email "marc.schar@the-agile-factory.com"

git add snapshots
git commit -m "Deploy to GitHub Pages"

git push --force "https://${GH_TOKEN}@${GH_REF}" master:gh-pages
