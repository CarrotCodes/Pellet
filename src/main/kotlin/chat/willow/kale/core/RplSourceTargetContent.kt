package chat.willow.kale.core

import chat.willow.kale.core.message.*

object RplSourceTargetContent {

    open class Message(val source: String, val target: String, val content: String) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Message) return false

            if (source != other.source) return false
            if (target != other.target) return false
            if (content != other.content) return false

            return true
        }

        override fun hashCode(): Int {
            var result = source.hashCode()
            result = 31 * result + target.hashCode()
            result = 31 * result + content.hashCode()
            return result
        }
    }

    abstract class Parser(val command: String) : MessageParser<Message>() {

        override fun parseFromComponents(components: IrcMessageComponents): Message? {
            if (components.parameters.size < 2) {
                return null
            }

            val source = components.prefix ?: ""
            val target = components.parameters[0]
            val content = components.parameters[1]

            return Message(source = source, target = target, content = content)
        }
    }

    abstract class Serialiser(val command: String) : MessageSerialiser<Message>(command) {

        override fun serialiseToComponents(message: Message): IrcMessageComponents {
            return IrcMessageComponents(prefix = message.source, parameters = listOf(message.target, message.content))
        }

    }

    abstract class Descriptor(command: String, parser: IMessageParser<Message>) : KaleDescriptor<Message>(matcher = commandMatcher(command), parser = parser)

}