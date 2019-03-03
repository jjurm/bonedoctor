package uk.ac.cam.cl.bravo.util

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.functions.Function4
import io.reactivex.functions.Function5

fun <A, B> combineObservables(a: Observable<A>, b: Observable<B>): Observable<Tuple2<A, B>> =
    Observable.combineLatest(a, b, BiFunction(::Tuple2))

fun <A, B, C> combineObservables(
    a: Observable<A>,
    b: Observable<B>,
    c: Observable<C>
): Observable<Tuple3<A, B, C>> =
    Observable.combineLatest(a, b, c, Function3(::Tuple3))

fun <A, B, C, D> combineObservables(
    a: Observable<A>,
    b: Observable<B>,
    c: Observable<C>,
    d: Observable<D>
): Observable<Tuple4<A, B, C, D>> =
    Observable.combineLatest(a, b, c, d, Function4(::Tuple4))

fun <A, B, C, D, E> combineObservables(
    a: Observable<A>,
    b: Observable<B>,
    c: Observable<C>,
    d: Observable<D>,
    e: Observable<E>
): Observable<Tuple5<A, B, C, D, E>> =
    Observable.combineLatest(a, b, c, d, e, Function5(::Tuple5))

fun <A, B, R> Observable<Tuple2<A, B>>.mapDestructing(t: (A, B) -> R): Observable<R> =
    this.map { (a, b) -> t(a, b) }

fun <A, B, C, R> Observable<Tuple3<A, B, C>>.mapDestructing(t: (A, B, C) -> R): Observable<R> =
    this.map { (a, b, c) -> t(a, b, c) }

fun <A, B, C, D, R> Observable<Tuple4<A, B, C, D>>.mapDestructing(t: (A, B, C, D) -> R): Observable<R> =
    this.map { (a, b, c, d) -> t(a, b, c, d) }

fun <A, B, C, D, E, R> Observable<Tuple5<A, B, C, D, E>>.mapDestructing(t: (A, B, C, D, E) -> R): Observable<R> =
    this.map { (a, b, c, d, e) -> t(a, b, c, d, e) }
