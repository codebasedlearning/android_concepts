// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.supabase.model

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.broadcast
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/*
 * The data classes: kotlinx.serialization maps the JSON from Postgrest onto them. Property names
 * must match the column names (or use @SerialName). 'category' is not a column of shop_product -
 * it is the joined row from shop_category, see the select in loadProducts.
 */
@Serializable
data class Category(
    val id: Int,
    val name: String
)

@Serializable
data class Product(
    val id: Int,
    val name: String,
    val category: Category? = null,
    val unit: String,
    val price: Double,
    val vat: Double
)

@Serializable
data class Message(val msg: String, val no: Int)

/*
 * The only class that talks to Supabase; the ViewModels only know this one.
 * Before, the repository started collecting Auth and Realtime in its init block, in an
 *  application-wide scope - so the websocket stayed open as long as the process lived. Now it
 *  only describes what can be done (suspend functions) and what can be observed (cold flows);
 *  the callers decide when.
 */
class SupabaseRepository(private val supabase: SupabaseClient) {

    // ---- Auth

    // Initializing -> Authenticated / NotAuthenticated; it also reports a restored session after
    // an app restart (Auth stores the session on the device) and a failed token refresh.
    val sessionStatus: StateFlow<SessionStatus> get() = supabase.auth.sessionStatus

    suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() = supabase.auth.signOut()

    // ---- Postgrest (the tables as a REST API)

    /*
     * Reads shop_product with its category - a join by foreign key: 'category:shop_category(*)'
     * means 'all columns of the referenced shop_category row, as a field named category'.
     * The schema 'ami_zone' must be exposed in the Supabase API settings, and the signed-in user
     * needs the right to read - see supabase/01_shop_schema.sql and 02_shop_security.sql.
     * Without a policy that allows it, row level security returns no rows (not an error!).
     */
    suspend fun loadProducts(): List<Product> =
        supabase
            .from(schema = SCHEMA, table = "shop_product")
            .select(Columns.raw("id,name,category:shop_category(*),unit,price,vat"))
            .decodeList<Product>()

    // ---- Realtime (broadcast: messages between clients, nothing is stored)

    private val channel = supabase.realtime.channel(CHANNEL)

    /*
     * A cold flow (cf. the sensor flows in models/RawSensorLib): the channel is joined
     *  when somebody starts collecting and left when the last collector stops.
     *  onCompletion also runs on cancellation - and a suspend call in a cancelled coroutine
     *  would throw at once. withContext(NonCancellable) lets the clean-up finish. (Before:
     *  runCatching, which swallowed exactly that exception, so the channel was never left.)
     * supabase-kt 3.8: removeChannel instead of unsubscribe - it leaves the channel and
     *  closes the websocket when no channel is left (since 3.7). subscribe() registers the
     *  channel again, so coming back to the screen works.
     */
    val messages: Flow<Message> = channel.broadcastFlow<Message>(EVENT)
        .onStart { channel.subscribe(blockUntilSubscribed = true) }
        .onCompletion { withContext(NonCancellable) { supabase.realtime.removeChannel(channel) } }

    /*
     * Typed: Message is @Serializable, so supabase-kt encodes it - no hand-built JSON.
     *  Joined channel: sent via the websocket. Not joined: supabase-kt (3.7+) falls back to the
     *  REST endpoint (with a warning in Logcat; explicit: channel.httpSend).
     *  Note: by default a client does not receive its own broadcasts.
     */
    suspend fun send(message: Message) {
        channel.broadcast(EVENT, message)
    }

    companion object {
        const val SCHEMA = "ami_zone"
        const val CHANNEL = "ami_zone_channel"      // must be the same in all clients (see supabase/connect.py)
        const val EVENT = "my_event"
    }
}
