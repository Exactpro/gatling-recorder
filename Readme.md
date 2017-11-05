
```
cat > Readme.md <<EoR
git clone https://github.com/gatling/gatling.git recorder
cd recorder
git filter-branch --prune-empty --subdirectory-filter gatling-recorder/ master
git checkout -b standalone
git remote set-url --push origin git@github.com:mz0/gatling-recorder.git
git push -u origin standalone
EoR
```

With current build.sbt I can run _sbt compile_ but not _sbt test_
