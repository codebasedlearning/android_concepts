// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// The message board: read the view 'messages', post via RPC, live changes via Realtime.
class ChatRepository(private val supabase: SupabaseClient) {

    // newest first
    suspend fun latest(count: Long = 50): List<ChatMessage> =
        supabase.from("messages")
            .select {
                order("id", Order.DESCENDING)
                limit(count)
            }
            .decodeList<ChatMessage>()

    suspend fun post(body: String) {
        supabase.postgrest.rpc("post_message", buildJsonObject { put("p_body", body) })
    }

    // Emits when the list should be (re)loaded: once after subscribing, then on every insert,
    // update or delete in mobappdata.messages. Cold: the channel only lives while collected.
    val reloads: Flow<Unit> = channelFlow {
        val channel = supabase.realtime.channel(CHANNEL)
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "mobappdata") { table = "messages" }
        launch(start = CoroutineStart.UNDISPATCHED) { changes.collect { send(Unit) } }    // listen first ...
        try {
            channel.subscribe(blockUntilSubscribed = true)
            send(Unit)                                                                      // ... then load
            awaitCancellation()
        } finally {
            withContext(NonCancellable) { supabase.realtime.removeChannel(channel) }
        }
    }

    private companion object {
        const val CHANNEL = "messages"
    }
}
