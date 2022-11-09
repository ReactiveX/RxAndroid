# RxAndroid Releases #

### Version 3.0.2 - November, 9 2022 ###

Fixed:
- Ensure the main scheduler can be replaced in unit tests without needing Robolectric.


### Version 3.0.1 - November, 8 2022 ###

Fixed:
- `AndroidSchedulers.mainThread()` now correctly checks whether async messages are supported by the
  current Android version. Previously it always assumed they were available (true on API 16+).

Changed:
- Update to RxJava 3.1.5. This includes a transitive dependency bump to Reactive-Streams 1.0.4 which
  re-licenses that dependency from CC-0 to MIT-0.


### Version 3.0.0 - February, 14 2020 ###

General availability of RxAndroid 3.0 for use with RxJava 3.0!

The Maven groupId has changed to `io.reactivex.rxjava3` and the package is now `io.reactivex.rxjava3.android`.

The APIs and behavior of RxAndroid 3.0.0 is otherwise exactly the same as RxAndroid 2.1.1 with one notable exception:

Schedulers created via `AndroidSchedulers.from` now deliver [async messages](https://developer.android.com/reference/android/os/Handler.html#createAsync(android.os.Looper)) by default.
This is also true for `AndroidSchedulers.mainThread()`.

For more information about RxJava 3.0 see [its release notes](https://github.com/ReactiveX/RxJava/releases/tag/v3.0.0).

---

Version 2.x can be found at https://github.com/ReactiveX/RxAndroid/blob/2.x/CHANGES.md

Version 1.x can be found at https://github.com/ReactiveX/RxAndroid/blob/1.x/CHANGES.md
