
```
cat > Readme.md <<EoR
git clone https://github.com/gatling/gatling.git recorder
cd recorder
// put gatling* 3.0.0-SNAPSHOT to ~/.ivy2/local/io.gatling
sbt compile && sbt test && sbt publishLocal
git filter-branch --prune-empty --subdirectory-filter gatling-recorder/ master
git checkout -b standalone
git remote set-url --push origin git@github.com:mz0/gatling-recorder.git
git push -u origin standalone
EoR
```

I had to compose build.sbt and copy BaseSpec (commit e259eb8). 
Now I can run _sbt compile_, _sbt test_, and start Recorder from IDE.
