# RxAndroid: Reactive Extensions for Android

Android specific bindings for [RxJava](http://github.com/ReactiveX/RxJava).

This module adds a number of classes to RxJava that make writing reactive components in 
Android applications easy and hassle free. More specifically, it

- provides a `Scheduler` that schedules an `Observable` on a given Android `Handler` thread, particularly the main UI thread
- provides base `Observer` implementations that make guarantees w.r.t. to reliable and thread-safe use throughout 
      `Fragment` and `Activity` life-cycle callbacks (coming soon)
- provides reusable, self-contained reactive components for common Android use cases and UI concerns (coming soon)

## Master Build Status

<a href='https://travis-ci.org/ReactiveX/RxAndroid/builds'><img src='https://travis-ci.org/ReactiveX/RxAndroid.svg?branch=0.x'></a>

## Communication

Since RxAndroid is part of the RxJava family the communication channels are similar:

- Google Group: [RxJava](http://groups.google.com/d/forum/rxjava)
- Twitter: [@RxJava](http://twitter.com/RxJava)
- [GitHub Issues](https://github.com/ReactiveX/RxAndroid/issues)

# Binaries

Binaries and dependency information for Maven, Ivy, Gradle and others can be found at [http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22rxjava-android%22).

Example for [Maven](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22rxjava-android%22):

```xml
<dependency>
    <groupId>com.netflix.rxjava</groupId>
    <artifactId>rxjava-android</artifactId>
    <version>0.10.1</version>
</dependency>
```

and for Ivy:

```xml
<dependency org="com.netflix.rxjava" name="rxjava-android" rev="0.10.1" />
```

## Build

To build:

```
$ git clone git@github.com:ReactiveX/RxAndroid.git
$ cd RxAndroid/
$ ./RxAndroid build
```

Futher details on building can be found on the RxJava [Getting Started](https://github.com/ReactiveX/RxJava/wiki/Getting-Started) page of the wiki.


# Sample usage

We are working on a samples project which provides runnable code samples that demonstrate common Rx patterns and
their use in Android applications.

## Observing on the UI thread

One of the most common operations when dealing with asynchronous tasks on Android is to observe the task's
result or outcome on the main UI thread. Using vanilla Android, this would
typically be accomplished with an `AsyncTask`. With RxJava instead you would declare your `Observable`
to be observed on the main thread:

    public class ReactiveFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Observable.from("one", "two", "three", "four", "five")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(/* an Observer */);
    }
 
This will execute the Observable on a new thread, and emit results through `onNext` on the main UI thread.
   
## Observing on arbitrary threads
The previous sample is merely a specialization of a more general concept, namely binding asynchronous
communication to an Android message loop using the `Handler` class. In order to observe an `Observable`
on an arbitrary thread, create a `Handler` bound to that thread and use the `AndroidSchedulers.handlerThread`
scheduler:

    new Thread(new Runnable() {
        @Override
        public void run() {
            final Handler handler = new Handler(); // bound to this thread
            Observable.from("one", "two", "three", "four", "five")
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.handlerThread(handler))
                    .subscribe(/* an Observer */)
                    
            // perform work, ...
        }
    }, "custom-thread-1").start();

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


