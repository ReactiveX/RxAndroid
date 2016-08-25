# RxAndroid Releases #

Version 1.x can be found at https://github.com/ReactiveX/RxAndroid/blob/1.x/CHANGES.md

### Version 2.0.0-RC1 - August 25, 2016 ###

RxAndroid 2.0 has been rewritten from scratch to support RxJava 2.0.

The library still offers the same APIs: a scheduler and stream cancelation callback that know about
the main thread, a means of creating a scheduler from any `Looper`, and plugin support for the
main thread sheduler. They just reside in a new package, `io.reactivex.android`, and may have
slightly different names.

For more information about RxJava 2.0 see
[its RC1 release notes](https://github.com/ReactiveX/RxJava/releases/tag/v2.0.0-RC1)
