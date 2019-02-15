# RxAndroid Releases #

### Version 2.1.1 - February, 15 2019 ###

**Bugfixes**

 * [Pull 442](https://github.com/ReactiveX/RxAndroid/pull/442) - Use async messages for Scheduler.scheduleDirect


### Version 2.1.0 - August 16, 2018 ###

**API Enhancements**

 * [Pull 416](https://github.com/ReactiveX/RxAndroid/pull/416) - Add an option to post async messages


### Version 2.0.2 - January 12, 2018 ###

**API Enhancements**

 * [Pull 358](https://github.com/ReactiveX/RxAndroid/pull/358) - Add handler getters to RxAndroidPlugins

**Bugfixes**

 * [Pull 391](https://github.com/ReactiveX/RxAndroid/pull/391) - Update scheduler error handling to match RxJava
 * [Pull 408](https://github.com/ReactiveX/RxAndroid/pull/408) - Remove superfluous negative checks
 * [Pull 415](https://github.com/ReactiveX/RxAndroid/pull/415) - Disable useless `BuildConfig` class generation


### Version 2.0.1 - November 12, 2016 ###

**Bugfixes**

 * [Pull 347](https://github.com/ReactiveX/RxAndroid/pull/347) - Schedule tasks with negative delays immediately


### Version 2.0.0 - October 29, 2016 ###

General availability of RxAndroid 2.0 for use with RxJava 2.0!

The sections below contain the changes since 2.0.0-RC1.

**API Enhancements**

 * [Pull 338](https://github.com/ReactiveX/RxAndroid/pull/338) - Evaluate `Schedulers` initialization via `Callable`


### Version 2.0.0-RC1 - August 25, 2016 ###

RxAndroid 2.0 has been rewritten from scratch to support RxJava 2.0.

The library still offers the same APIs: a scheduler and stream cancelation callback that know about
the main thread, a means of creating a scheduler from any `Looper`, and plugin support for the
main thread scheduler. They just reside in a new package, `io.reactivex.android`, and may have
slightly different names.

For more information about RxJava 2.0 see
[its RC1 release notes](https://github.com/ReactiveX/RxJava/releases/tag/v2.0.0-RC1)


---

Version 1.x can be found at https://github.com/ReactiveX/RxAndroid/blob/1.x/CHANGES.md
