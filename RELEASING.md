Release Process
===============

 1.  Ensure `VERSION_NAME` in `gradle.properties` is set to the version you want to release.
 2.  Add an entry in `CHANGELOG.md` with the changes for the release.
 3.  Update `README.md` with the version about to be released. Also update the RxJava version in
     this file to its latest.
 4.  Update the RxJava version in `rxandroid/build.gradle` to its latest. (We tell people that we
     won't be tracking RxJava releases, and we don't, but we do it anyway when we are releasing for
     those who ignore the advice.)
 5.  Commit: `git commit -am "Prepare version X.Y.X"`
 6.  Tag: `git tag -a X.Y.Z -m "Version X.Y.Z"`
 7.  Update `VERSION_NAME` in `gradle.properties` to the next development version. For example, if
     you just tagged version 1.0.4 you would set this value to 1.0.5. Do NOT append "-SNAPSHOT" to
     this value, it will be added automatically.
 8.  Commit: `git commit -am "Prepare next development version."`
 9.  Push: `git push && git push --tags`
 10. Paste the `CHANGELOG.md` contents for this version into a Release on GitHub along with the
     Groovy for depending on the new version (https://github.com/ReactiveX/RxAndroid/releases).
