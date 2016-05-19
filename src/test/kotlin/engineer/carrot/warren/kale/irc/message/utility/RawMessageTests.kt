package engineer.carrot.warren.kale.irc.message.utility

import engineer.carrot.warren.kale.irc.message.IMessageFactory
import engineer.carrot.warren.kale.irc.message.IrcMessage
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class RawMessageTests {
    lateinit var factory: IMessageFactory<RawMessage>

    @Before fun setUp() {
        factory = RawMessage.Factory
    }

    @Test fun test_parse() {
        val message = factory.parse(IrcMessage(command = "123", prefix = "prefix", parameters = listOf("1", "2 3")))

        assertEquals(":prefix 123 1 :2 3", message?.line)
    }

    @Test fun test_serialise_WellFormedLine() {
        val message = factory.serialise(RawMessage(line = ":prefix 123 1 :2 3"))

        assertEquals(IrcMessage(command = "123", prefix = "prefix", parameters = listOf("1", "2 3")), message)
    }

    @Test fun test_serialise_BadlyFormedLine_Empty() {
        val message = factory.serialise(RawMessage(line = ""))

        assertNull(message)
    }

    @Test fun test_serialise_BadlyFormedLine_Garbage() {
        val message = factory.serialise(RawMessage(line = ": :1 :2 :3"))

        assertNull(message)
    }

}