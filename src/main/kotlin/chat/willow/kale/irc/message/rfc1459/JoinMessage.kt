package chat.willow.kale.irc.message.rfc1459

import chat.willow.kale.irc.CharacterCodes
import chat.willow.kale.irc.message.IMessage
import chat.willow.kale.irc.message.IMessageParser
import chat.willow.kale.irc.message.IMessageSerialiser
import chat.willow.kale.irc.message.IrcMessage
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.prefix.PrefixParser
import chat.willow.kale.irc.prefix.PrefixSerialiser

data class JoinMessage(val source: Prefix? = null, val channels: List<String>, val keys: List<String>? = null): IMessage {
    override val command: String = "JOIN"

    companion object Factory: IMessageParser<JoinMessage>, IMessageSerialiser<JoinMessage> {

        override fun serialise(message: JoinMessage): IrcMessage? {
            val prefix = if (message.source != null) { PrefixSerialiser.serialise(message.source) } else { null }
            val channels = message.channels.joinToString(separator = CharacterCodes.COMMA.toString())

            if (message.keys == null || message.keys.isEmpty()) {
                return IrcMessage(command = message.command, prefix = prefix, parameters = listOf(channels))
            } else {
                val keys = message.keys.joinToString(separator = CharacterCodes.COMMA.toString())

                return IrcMessage(command = message.command, prefix = prefix, parameters = listOf(channels, keys))
            }
        }

        override fun parse(message: IrcMessage): JoinMessage? {
            if (message.parameters.isEmpty()) {
                return null
            }

            val source = PrefixParser.parse(message.prefix ?: "")
            val unsplitChannels = message.parameters[0]
            val channels = unsplitChannels.split(delimiters = CharacterCodes.COMMA).filterNot(String::isEmpty)

            if (message.parameters.size < 2) {
                return JoinMessage(source = source, channels = channels)
            } else {
                val unsplitKeys = message.parameters[1]
                val keys = unsplitKeys.split(delimiters = CharacterCodes.COMMA).filterNot(String::isEmpty)

                return JoinMessage(source = source, channels = channels, keys = keys)
            }
        }
    }

}