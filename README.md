# RxAndroid: Reactive Extensions for Android

Android specific bindings for [RxJava](http://github.com/ReactiveX/RxJava).

This module adds the minimum classes to RxJava that make writing reactive components in Android
applications easy and hassle-free. More specifically, it provides a `Scheduler` that schedules on
the main thread or any given `Looper`.


## Communication

Since RxAndroid is part of the RxJava family the communication channels are similar:

- Google Group: [RxJava][list]
- Twitter: [@RxJava][twitter]
- StackOverflow: [rx-android][so]
- [GitHub Issues][issues]


# Binaries

```groovy
compile 'io.reactivex:rxandroid:1.2.0'
// Because RxAndroid releases are few and far between, it is recommended you also
// explicitly depend on RxJava's latest version for bug fixes and new features.
compile 'io.reactivex:rxjava:1.1.5'
```

* RxAndroid: <a href='http://search.maven.org/#search%7Cga%7C1%7Crxandroid'><img src='http://img.shields.io/maven-central/v/io.reactivex/rxandroid.svg'></a>
* RxJava: <a href='http://search.maven.org/#search%7Cga%7C1%7Crxjava'><img src='http://img.shields.io/maven-central/v/io.reactivex/rxjava.svg'></a>

Additional binaries and dependency information for can be found at [http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Crxandroid).


## Build

To build:

```bash
$ git clone git@github.com:ReactiveX/RxAndroid.git
$ cd RxAndroid/
$ ./gradlew build
```

Futher details on building can be found on the RxJava [Getting Started][start] page of the wiki.

<a href='https://travis-ci.org/ReactiveX/RxAndroid/builds'><img src='https://travis-ci.org/ReactiveX/RxAndroid.svg?branch=master'></a>


# Sample usage

A sample project which provides runnable code examples that demonstrate uses of the classes in this
project is available in the `sample-app/` folder.

## Observing on the main thread

One of the most common operations when dealing with asynchronous tasks on Android is to observe the task's
result or outcome on the main thread. Using vanilla Android, this would typically be accomplished with an
`AsyncTask`. With RxJava instead you would declare your `Observable` to be observed on the main thread:

```java
Observable.just("one", "two", "three", "four", "five")
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(/* an Observer */);
```

This will execute the `Observable` on a new thread, and emit results through `onNext` on the main thread.

## Observing on arbitrary loopers

The previous sample is merely a specialization of a more general concept: binding asynchronous
communication to an Android message loop, or `Looper`. In order to observe an `Observable` on an arbitrary
`Looper`, create an associated `Scheduler` by calling `AndroidSchedulers.from`:

```java
Looper backgroundLooper = // ...
Observable.just("one", "two", "three", "four", "five")
        .observeOn(AndroidSchedulers.from(backgroundLooper))
        .subscribe(/* an Observer */)
```

This will execute the Observable on a new thread and emit results through `onNext` on whatever thread is
running `backgroundLooper`.


## Bugs and Feedback

For bugs, feature requests, and discussion please use [GitHub Issues][issues].
For general usage questions please use the [mailing list][list] or [StackOverflow][so].


## LICENSE

    Copyright 2015 The RxAndroid authors

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



 [list]: http://groups.google.com/d/forum/rxjava
 [so]: http://stackoverflow.com/questions/tagged/rx-android
 [twitter]: http://twitter.com/RxJava
 [issues]: https://github.com/ReactiveX/RxAndroid/issues
 [start]: https://github.com/ReactiveX/RxJava/wiki/Getting-Started
