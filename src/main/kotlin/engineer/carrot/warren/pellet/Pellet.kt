package engineer.carrot.warren.pellet

import engineer.carrot.warren.pellet.irc.message.IMessage
import engineer.carrot.warren.pellet.irc.message.IMessageFactory
import engineer.carrot.warren.pellet.irc.message.IrcMessageParser
import engineer.carrot.warren.pellet.irc.message.rfc1459.PingMessage
import engineer.carrot.warren.pellet.irc.message.rfc1459.PongMessage

class Pellet: IPellet {
    private var messageFactories: MutableMap<String, IMessageFactory<*>> = hashMapOf()
    private var messageToFactory: MutableMap<Class<*>, IMessageFactory<*>> = hashMapOf()

    var handlers: MutableMap<String, IPelletHandler<*>> = hashMapOf()

    fun addDefaultMessages(): Pellet {
        addMessageFromFactory(PingMessage.Factory)
        addMessageFromFactory(PongMessage.Factory)

        return this
    }

    fun <T: IMessage> addMessageFromFactory(factory: IMessageFactory<T>) {
        messageFactories.put(factory.command, factory)
        messageToFactory.put(factory.messageType, factory)
    }

    private fun factoryFromMessage(message: Class<*>): IMessageFactory<*>? {
        return messageToFactory[message]
    }

    override fun <T: IMessage> register(handler: IPelletHandler<T>) {
        val command = factoryFromMessage(handler.messageType)?.command ?: throw RuntimeException("couldn't look up factory for handler: $handler")

        if (handlers.containsKey(command)) {
            throw RuntimeException("tried to register a handler when one already exists for $command: $handler")
        }

        handlers.put(command, handler)
    }

    override fun process(line: String) {
        val ircMessage = IrcMessageParser.parse(line)
        if (ircMessage == null) {
            println("failed to parse line to IrcMessage: $line")
            return
        }

        val factory = messageFactories[ircMessage.command]
        if (factory == null) {
            println("failed to find factory for message: $ircMessage")
            return
        }

        val message = factory.parse(ircMessage)
        if (message == null) {
            println("factory failed to parse message: $factory $ircMessage")
            return
        }

        val handler = handlers[ircMessage.command]
        if (handler == null) {
            println("failed to find handler for message: $message")
            return
        }

        @Suppress("UNCHECKED_CAST")
        val typedHandler = handler as? IPelletHandler<IMessage> ?: return
        if (!typedHandler.messageType.isInstance(message)) {
            println("tried to pass wrong type to handler: ${message.javaClass} to ${handler.messageType}")
            return
        }

        typedHandler.handle(message)
    }

}