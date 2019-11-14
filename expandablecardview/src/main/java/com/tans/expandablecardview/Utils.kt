package com.tans.expandablecardview

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 *
 * author: pengcheng.tan
 * date: 2019-11-14
 */

fun <T> callToObservable(completeCheck: (T) -> Boolean = { false }): Pair<Observable<T>, (T) -> Unit> {
    val obs = PublishSubject.create<T>().toSerialized()
    val call: (T) -> Unit = {
        obs.onNext(it)
        if (completeCheck(it)) {
            obs.onComplete()
        }
    }
    return obs to call
}