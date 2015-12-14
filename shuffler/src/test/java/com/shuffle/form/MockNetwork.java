package com.shuffle.form;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Daniel Krawisz on 12/5/15.
 */
public class MockNetwork implements Network {
    public static class SentMessage implements Map.Entry<MockPacket, MockVerificationKey> {
        MockPacket packet;
        MockVerificationKey to;

        public SentMessage(MockPacket packet, MockVerificationKey to) {
            this.to = to;
            this.packet = packet;
        }

        @Override
        public MockPacket getKey() {
            return packet;
        }

        @Override
        public MockVerificationKey getValue() {
            return to;
        }

        @Override
        public MockVerificationKey setValue(MockVerificationKey to) {
            return this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SentMessage)) {
                return false;
            }

            SentMessage msg = (SentMessage)o;
            return to.equals(msg.to) && packet.equals(msg.packet);
        }

        @Override
        public int hashCode() {
            return to.hashCode() + packet.hashCode();
        }

        @Override public String toString(){
            return packet.toString() + " -> " + to.toString();
        }
    }

    Queue<Map.Entry<MockPacket, MockVerificationKey>> responses;
    Queue<MockPacket> sent;

    MockNetwork() {
        this.sent = new LinkedList<>();
        this.responses = new LinkedList<>();
    }

    MockNetwork(Queue<MockPacket> sent) {
        this.sent = sent;
        this.responses = new LinkedList<>();
    }

    Queue<Map.Entry<MockPacket, MockVerificationKey>> getResponses() {
        return responses;
    }

    @Override
    public void sendTo(VerificationKey to, Packet packet) throws InvalidImplementationException {
        if (!(packet instanceof MockPacket)) {
            throw new InvalidImplementationException();
        }

        if (!(to instanceof MockVerificationKey)) {
            throw new InvalidImplementationException();
        }

        final MockVerificationKey mockTo = (MockVerificationKey)to;
        final MockPacket mockPacket = (MockPacket)packet;

        responses.add(new SentMessage(mockPacket, mockTo));
    }

    @Override
    public Packet receive() throws TimeoutException {
        if (sent.size() == 0) {
            throw new TimeoutException();
        }

        return sent.remove();
    }

}
