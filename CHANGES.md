# RxAndroid Releases #

### Version 1.1.0 - December 9th 2015 ###

 * New: `MainThreadSubscription` utility class runs its `onUnsubscribe` action on the Android main
   thread. This aids in adding tear-down actions which must be executed on the main thread without
   having to deal with posting to the main thread yourself.
 * Fix: Lazily initialize `mainThread()` scheduler so that no Android code is run when overridden.
   This allows unit tests overriding the implementation to work correctly.
 * RxJava dependency now points at v1.1.0.


### Version 1.0.1 - August 9th 2015 ###

 * Fix: Correctly check `isUnsubscribed()` state in `HandlerScheduler`'s worker before scheduling
   more work.
 * Fix: Eliminate a potential race condition in `HandlerScheduler` to ensure any posted work will
   be canceled on unsubscription.


### Version 1.0.0 - August 5th 2015 ###

Initial stable release!

In order to provide a library that no project using RxJava would hesitate to depend on, the decision
was made to remove the APIs which were not absolutely fundamental to all apps. That is what's
contained in this release.

Functionality which was previously part of this library is being explored in separate, modular
libraries:

 * `LifecycleObservable`: https://github.com/trello/RxLifecycle
 * `ViewObservable` and `WidgetObservable`: https://github.com/JakeWharton/RxBinding

This allows for a simpler process of design, development, and experimentation for the
best ways to provide features like hooks into the lifecycle, binding to UI components, and
simplifying interaction with all of Android's API. Not only can these projects now have their own
release schedule, but it allows developers to pick and choose which ones are appropriate for your
application.

Applications using the various APIs which were previously in this library do not need to update
immediately. Due to the number of APIs removed, switching to 1.0 and the use of these third-party
libraries should be done gradually.

Breaking changes:

 * `AndroidSchedulers.handlerThread()` is now `HandlerScheduler.from()`.
 * **All other APIs have been removed** aside from `AndroidSchedulers.mainThread()`,
   `RxAndroidPlugins`, and `RxAndroidSchedulersHook`.


### Version 0.25 - June 27th 2015 ###

* New: `RxAndroidPlugins` and its `RxAndroidSchedulersHook` provides a mechanism similar to `RxJavaPlugins` (and its `RxJavaSchedulersHook`) for
  changing the scheduler returned from `AndroidSchedulers.mainThread()` as well as a callback for each subscription on any `Handler`-based scheduler.
* Fix: Ensure errors are properly propagated from `ContentObservable.fromCursor`.
* Fix: `LifecycleObservable` now correctly unsubscribes from its sources.

Breaking changes:

* Users of `AppObservable.bindFragment` with a support-v4 `Fragment` should now use `bindSupportFragment`.


### Version 0.24 – January 3rd 2015 ###

This release has some breaking changes:

* `rx.android.observables.AndroidObservable` has changed to `rx.android.app.AppObservable`;
* `ViewObservable` has moved from `rx.android.observables` to `rx.android.view`
* (as part of RxJava's breaking changes) [`collect` has changed](https://github.com/ReactiveX/RxJava/blob/1a94d55fa8896931175896d09b86dca8d8d44f72/CHANGES.md#collect)


### Version 0.22 – October 15th 2014 ###

This release adds a number of new operators:

* [Pull 25](https://github.com/ReactiveX/RxAndroid/pull/25) Add operator to monitor SharedPreference changes
* [Pull 22](https://github.com/ReactiveX/RxAndroid/pull/22) Add view state event types to streamline ViewObservable
* [Pull 20](https://github.com/ReactiveX/RxAndroid/pull/20) Add OperatorAdapterViewOnItemClick to observe OnItemClick events in AdapterViews


### Version 0.21 – October 1st 2014 ###

Initial release outside the RxJava core project, no changes.
