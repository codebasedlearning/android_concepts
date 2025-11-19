// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.sensors.service_locator

import android.content.Context
import de.fh_aachen.android.sensors.model.SensorRepository
import java.lang.ref.WeakReference

object ServiceLocator {
    // Assume for the sake of discussion that applicationContext is any Context object.

    // 1a) What is a weak reference?
    // 1b) When is the use of a weak reference a good idea?
    // 1c) Would it be better to use 'WeakReference<Context>?'?
    // 1d) Is a strong reference safe?
    var applicationContext: WeakReference<Context> = WeakReference(null)

    // 2a) Why do we need to check for zero, or in what constellation is there a problem?
    // 2b) How exactly does 'lazy' behave? And is it a good combination with a weak reference?
    val sensorRepository: SensorRepository by lazy {
        if (applicationContext.get() == null) throw IllegalStateException("ServiceLocator not initialized")
        SensorRepository(applicationContext)
    }

    // 3) And finally, what is so special about this particular context?
    fun initialize(applicationContext: Context) {
        this.applicationContext = WeakReference(applicationContext)
    }
}

/*
Assume for the sake of discussion that applicationContext is any Context object.

1a) What is a weak reference?
    A weak reference is a reference type that does not prevent the referenced object
    from being garbage-collected. If the only references to an object are weak references,
    the garbage collector is free to reclaim its memory when needed. This helps prevent
    memory leaks in cases where a long-lived reference might hold onto a shorter-lived
    object (like an Activity Context).

1b) When is the use of a weak reference a good idea?
    Weak references are useful when you want to hold onto an object temporarily, but don't
    want to keep it alive if it would otherwise be garbage-collected. For example, holding
    a `Context` reference in a singleton or global object benefits from a weak reference,
    because contexts are often tied to the lifecycle of activities or applications, and
    strong references to them can lead to memory leaks.

1c) Would it be better to use 'WeakReference<Context>?'
    Using `WeakReference<Context>?` (nullable weak reference) would allow you to
    set `applicationContext` to `null` if it’s no longer needed, adding another layer
    of safety. With your current non-nullable `WeakReference<Context>`, the `get()` method
    can still return `null` if the reference was cleared, but the `WeakReference` itself
    is always present. Making it nullable might be beneficial if you plan to check
    `applicationContext` for initialization status without relying on `.get()`.

1d) Is a strong reference safe?
    Using a strong reference to a `Context` in a singleton or global object is generally
    not safe because it can lead to memory leaks. For instance, if you hold a strong
    reference to an Activity context, it will not be garbage-collected until the singleton
    releases that reference. By using a weak reference, you ensure that the context can
    be garbage-collected when the app no longer needs it, helping to avoid potential leaks.

2a) Why do we need to check for zero, or in what constellation is there a problem?
    In this context, "checking for zero" means checking if `applicationContext.get()`
    returns `null`. This check is important because `WeakReference` may release its
    reference to the `Context` if the garbage collector has reclaimed it. Without this
    check, `sensorRepository` could attempt to initialize with a `null` context,
    which would cause a runtime error.

2b) How exactly does 'lazy' behave? And is it a good combination with a weak reference?
    `lazy` is a delegate that initializes the `sensorRepository` property only on the first
    access. After this initialization, it will continue to hold the initialized object
    without re-checking the `WeakReference`. This can be problematic with weak references,
    because if `applicationContext` is garbage-collected after `sensorRepository` is
    initialized, the `sensorRepository` might end up with a stale or invalid context.
    Therefore, `lazy` and weak references are generally not a good combination, as `lazy`
    assumes a stable reference, while weak references are meant to be flexible and
    garbage-collectable.

3)  This is the application context. It is available throughout the life of the application,
    so there is no fear of being garbage collected before the end of the application itself.
*/
