# RxAndroid: Reactive Extensions for Android

Android specific bindings for [RxJava](http://github.com/ReactiveX/RxJava).

This module adds a number of classes to RxJava that make writing reactive components in 
Android applications easy and hassle free. More specifically, it

- provides a `Scheduler` that schedules an `Observable` on a given Android `Handler` thread, particularly the main UI thread

## Communication

Since RxAndroid is part of the RxJava family the communication channels are similar:

- Google Group: [RxJava](http://groups.google.com/d/forum/rxjava)
- Twitter: [@RxJava](http://twitter.com/RxJava)
- [GitHub Issues](https://github.com/ReactiveX/RxAndroid/issues)

# Versioning

RxAndroid 0.21 and beyond are published under the `io.reactivex` GroupID and depend on RxJava 1.0.x. Versions 0.20 and earlier were `rxjava-android` and published along with `rxjava-core` under the `com.netflix.rxjava` GroupID.

RxAndroid is staying on the 0.x versioning for now despite RxJava hitting 1.0 as it is not yet felt that the RxAndroid APIs are stabilized.

All usage of 0.20.x and earlier under `com.netflix.rxjava` should eventually be migrated to RxJava 1.x and `io.reactivex`. This was done as part of the migration of the project from `Netflix/RxJava` to `ReactiveX/RxJava` and `ReactiveX/RxAndroid`.

During the transition it will be possible for an application to resolve both the `com.netflix.rxjava` and `io.reactivex` artifacts. This is unfortunate but was accepted as a reasonable cost for adopting the new name as we hit version 1.0.

The RxJava 0.20.x branch is being maintained with bug fixes on the `com.netflix.rxjava` GroupId until version 1.0 Final is released to allow time to migrate between the artifacts.

# Binaries

Binaries and dependency information for Maven, Ivy, Gradle and others can be found at [http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Crxandroid).

<a href='http://search.maven.org/#search%7Cga%7C1%7Crxandroid'><img src='http://img.shields.io/maven-central/v/io.reactivex/rxandroid.svg'></a>

Example for Maven:

```xml
<dependency>
    <groupId>io.reactivex</groupId>
    <artifactId>rxandroid</artifactId>
    <version>0.25.0</version>
</dependency>
```

and for Ivy:

```xml
<dependency org="io.reactivex" name="rxandroid" rev="0.25.0" />
```

and for Gradle:
```groovy
compile 'io.reactivex:rxandroid:0.25.0'
```

## Build

To build:

```bash
$ git clone git@github.com:ReactiveX/RxAndroid.git
$ cd RxAndroid/
$ ./gradlew build
```

Futher details on building can be found on the RxJava [Getting Started](https://github.com/ReactiveX/RxJava/wiki/Getting-Started) page of the wiki.

<a href='https://travis-ci.org/ReactiveX/RxAndroid/builds'><img src='https://travis-ci.org/ReactiveX/RxAndroid.svg?branch=0.x'></a>


# Sample usage

We are working on a samples project which provides runnable code samples that demonstrate common Rx patterns and
their use in Android applications.

## Observing on the UI thread

One of the most common operations when dealing with asynchronous tasks on Android is to observe the task's
result or outcome on the main UI thread. Using vanilla Android, this would
typically be accomplished with an `AsyncTask`. With RxJava instead you would declare your `Observable`
to be observed on the main thread:

```java
public class ReactiveFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Observable.just("one", "two", "three", "four", "five")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(/* an Observer */);
    }
```

This will execute the Observable on a new thread, and emit results through `onNext` on the main UI thread.

## Observing on arbitrary threads
The previous sample is merely a specialization of a more general concept, namely binding asynchronous
communication to an Android message loop using the `Handler` class. In order to observe an `Observable`
on an arbitrary thread, create a `Handler` bound to that thread and use the `AndroidSchedulers.handlerThread`
scheduler:

```java
new Thread(new Runnable() {
    @Override
    public void run() {
        final Handler handler = new Handler(); // bound to this thread
        Observable.just("one", "two", "three", "four", "five")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.handlerThread(handler))
                .subscribe(/* an Observer */)

        // perform work, ...
    }
}, "custom-thread-1").start();
```

This will execute the Observable on a new thread and emit results through `onNext` on "custom-thread-1".
(This example is contrived since you could as well call `observeOn(Schedulers.currentThread())` but it
shall suffice to illustrate the idea.)


## Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/ReactiveX/RxAndroid/issues).


## LICENSE

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


