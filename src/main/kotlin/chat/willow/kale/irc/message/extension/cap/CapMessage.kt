package chat.willow.kale.irc.message.extension.cap

import chat.willow.kale.ICommand
import chat.willow.kale.ISubcommand
import chat.willow.kale.IrcMessageComponents
import chat.willow.kale.irc.CharacterCodes
import chat.willow.kale.irc.message.*

object CapMessage : ICommand {

    override val command = "CAP"

    object Parser : IMessageParser<CapMessage> {

        override fun parse(message: IrcMessage): CapMessage? {
            TODO("not implemented")
        }

    }

    object Ls : ISubcommand {

        override val subcommand = "LS"

        // CAP LS <version>

        data class Command(val version: String?) {

            object Parser : SubcommandParser<Command>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Command? {
                    val version = components.parameters.getOrNull(0)

                    return Command(version)
                }

            }

            object Serialiser : SubcommandSerialiser<Command>(command, subcommand) {

                override fun serialiseToComponents(message: Command): IrcMessageComponents {
                    val parameters: List<String> = if (message.version == null) {
                        listOf()
                    } else {
                        listOf(message.version)
                    }

                    return IrcMessageComponents(parameters = parameters)
                }

            }

        }

        data class Message(val target: String, val caps: Map<String, String?>, val isMultiline: Boolean = false) {

            // CAP * LS ...

            object Parser : SubcommandParser<Message>(subcommand, subcommandPosition = 1) {

                override fun parseFromComponents(components: IrcMessageComponents): Message? {
                    if (components.parameters.size < 2) {
                        return null
                    }

                    val target = components.parameters[0]
                    val asteriskOrCaps = components.parameters[1]

                    val rawCaps: String
                    val isMultiline: Boolean

                    if (asteriskOrCaps == "*") {
                        rawCaps = components.parameters.getOrNull(2) ?: ""
                        isMultiline = true
                    } else {
                        rawCaps = asteriskOrCaps
                        isMultiline = false
                    }

                    val caps = ParseHelper.parseToKeysAndOptionalValues(rawCaps, CharacterCodes.SPACE, CharacterCodes.EQUALS)

                    return Message(target, caps, isMultiline)
                }

            }

            object Serialiser : SubcommandSerialiser<Message>(command, subcommand, subcommandPosition = 1) {

                override fun serialiseToComponents(message: Message): IrcMessageComponents {
                    val caps = SerialiserHelper.serialiseKeysAndOptionalValues(message.caps, CharacterCodes.EQUALS, CharacterCodes.SPACE)

                    return IrcMessageComponents(parameters = listOf(message.target, caps))
                }

            }

        }

    }

    object Ack : ISubcommand {

        override val subcommand = "ACK"

        data class Command(val caps: List<String>) {

            // CAP ACK :caps

            object Parser : SubcommandParser<Command>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Command? {
                    if (components.parameters.isEmpty()) {
                        return null
                    }

                    val rawCaps = components.parameters[0]

                    val caps = rawCaps.split(delimiters = CharacterCodes.SPACE).filterNot(String::isEmpty)

                    return Command(caps)
                }

            }

            object Serialiser : SubcommandSerialiser<Command>(command, subcommand) {

                override fun serialiseToComponents(message: Command): IrcMessageComponents {
                    val caps = message.caps.joinToString(separator = " ")

                    val parameters = listOf(caps)

                    return IrcMessageComponents(parameters)
                }
            }

        }

        data class Message(val target: String, val caps: List<String>) {

            // CAP * ACK :

            object Parser : SubcommandParser<Message>(subcommand, subcommandPosition = 1) {

                override fun parseFromComponents(components: IrcMessageComponents): Message? {
                    if (components.parameters.size < 2) {
                        return null
                    }

                    val target = components.parameters[0]
                    val rawCaps = components.parameters[1]

                    val caps = rawCaps.split(delimiters = CharacterCodes.SPACE).filterNot(String::isEmpty)

                    return Message(target, caps)
                }

            }

            object Serialiser : SubcommandSerialiser<Message>(command, subcommand, subcommandPosition = 1) {

                override fun serialiseToComponents(message: Message): IrcMessageComponents {
                    val caps = message.caps.joinToString(separator = " ")

                    val parameters = listOf(message.target, caps)

                    return IrcMessageComponents(parameters)
                }

            }

        }

    }

    object Del : ISubcommand {

        override val subcommand = "DEL"

        data class Message(val target: String, val caps: List<String>) {

            // CAP * DEL :

            object Parser : SubcommandParser<Message>(subcommand, subcommandPosition = 1) {

                override fun parseFromComponents(components: IrcMessageComponents): Message? {
                    if (components.parameters.size < 2) {
                        return null
                    }

                    val target = components.parameters[0]
                    val rawCaps = components.parameters[1]

                    val caps = ParseHelper.parseToKeysAndOptionalValues(rawCaps, CharacterCodes.SPACE, CharacterCodes.EQUALS).keys.toList()

                    return Message(target, caps)
                }

            }

            object Serialiser : SubcommandSerialiser<Message>(command, subcommand, subcommandPosition = 1) {

                override fun serialiseToComponents(message: Message): IrcMessageComponents {
                    val capsToValues = message.caps.associate { (it to null) }
                    val caps = SerialiserHelper.serialiseKeysAndOptionalValues(capsToValues, CharacterCodes.EQUALS, CharacterCodes.SPACE)

                    return IrcMessageComponents(parameters = listOf(message.target, caps))
                }

            }

        }

    }

    object End : ISubcommand {

        override val subcommand = "END"

        object Command {

            // CAP END

            object Parser : SubcommandParser<Command>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Command? {
                    return Command
                }

            }

            object Serialiser : SubcommandSerialiser<Command>(command, subcommand) {

                override fun serialiseToComponents(message: Command): IrcMessageComponents {
                    return IrcMessageComponents()
                }

            }

        }

    }

    object Nak : ISubcommand {

        override val subcommand = "NAK"

        data class Message(val target: String, val caps: List<String>) {

            // CAP * NAK :
            // TODO: Same as DEL

            object Parser : SubcommandParser<Message>(subcommand, subcommandPosition = 1) {

                override fun parseFromComponents(components: IrcMessageComponents): Message? {
                    if (components.parameters.size < 2) {
                        return null
                    }

                    val target = components.parameters[0]
                    val rawCaps = components.parameters[1]

                    val caps = ParseHelper.parseToKeysAndOptionalValues(rawCaps, CharacterCodes.SPACE, CharacterCodes.EQUALS).keys.toList()

                    return Message(target, caps)
                }

            }

            object Serialiser : SubcommandSerialiser<Message>(command, subcommand, subcommandPosition = 1) {

                override fun serialiseToComponents(message: Message): IrcMessageComponents {
                    val capsToValues = message.caps.associate { (it to null) }
                    val caps = SerialiserHelper.serialiseKeysAndOptionalValues(capsToValues, CharacterCodes.EQUALS, CharacterCodes.SPACE)

                    return IrcMessageComponents(parameters = listOf(message.target, caps))
                }

            }

        }

    }

    object New : ISubcommand {

        override val subcommand = "NEW"

        data class Message(val target: String, val caps: Map<String, String?>) {

            // CAP * NEW :
            // TODO: Same as DEL

            object Parser : SubcommandParser<Message>(subcommand, subcommandPosition = 1) {

                override fun parseFromComponents(components: IrcMessageComponents): Message? {
                    if (components.parameters.size < 2) {
                        return null
                    }

                    val target = components.parameters[0]
                    val rawCaps = components.parameters[1]

                    val caps = ParseHelper.parseToKeysAndOptionalValues(rawCaps, CharacterCodes.SPACE, CharacterCodes.EQUALS)

                    return Message(target, caps)
                }

            }

            object Serialiser : SubcommandSerialiser<Message>(command, subcommand, subcommandPosition = 1) {

                override fun serialiseToComponents(message: Message): IrcMessageComponents {
                    val caps = SerialiserHelper.serialiseKeysAndOptionalValues(message.caps, CharacterCodes.EQUALS, CharacterCodes.SPACE)

                    return IrcMessageComponents(parameters = listOf(message.target, caps))
                }

            }

        }

    }

    object Req : ISubcommand {

        override val subcommand = "REQ"

        data class Command(val caps: List<String>) {

            // CAP REQ :
            // TODO: Same as ACK

            object Parser : SubcommandParser<Command>(subcommand) {

                override fun parseFromComponents(components: IrcMessageComponents): Command? {
                    if (components.parameters.isEmpty()) {
                        return null
                    }

                    val rawCaps = components.parameters[0]

                    val caps = rawCaps.split(delimiters = CharacterCodes.SPACE).filterNot(String::isEmpty)

                    return Command(caps)
                }

            }

            object Serialiser : SubcommandSerialiser<Command>(command, subcommand) {

                override fun serialiseToComponents(message: Command): IrcMessageComponents {
                    val caps = message.caps.joinToString(separator = " ")

                    val parameters = listOf(caps)

                    return IrcMessageComponents(parameters)
                }
            }


        }

    }

}