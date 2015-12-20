package com.shuffle.protocol;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Tests for the functions in NetworkOperations
 *
 * Created by Daniel Krawisz on 12/7/15.
 */
public class TestNetworkOperations  {
    static class opponentSetTestCase {
        int i; // Minimum number to take.
        int n; // Maximum number to take.
        int N; // Number of players.
        int player; // Which player is us.
        int[] expected; // Which keys should have been returned.

        opponentSetTestCase(int i, int n, int N, int player, int[] expected) {
            this.i = i;
            this.n = n;
            this.N = N;
            this.player = player;
            this.expected = expected;
        }
    }

    @Test
    public void testOpponentSet() {
        opponentSetTestCase tests[] =
                new opponentSetTestCase[]{
                        new opponentSetTestCase(
                                1,5,0,1,
                                new int[]{}
                        ),
                        new opponentSetTestCase(
                                1,5,1,1,
                                new int[]{}
                        ),
                        new opponentSetTestCase(
                                1,5,1,2,
                                new int[]{1}
                        ),
                        new opponentSetTestCase(
                                1,5,5,1,
                                new int[]{2,3,4,5}
                        ),
                        new opponentSetTestCase(
                                1,3,5,1,
                                new int[]{2,3}
                        ),
                        new opponentSetTestCase(
                                1,5,5,-1,
                                new int[]{1,2,3,4,5}
                        ),
                        new opponentSetTestCase(
                                2,4,5,3,
                                new int[]{2,4}
                        ),
                        new opponentSetTestCase(
                                -1,7,5,3,
                                new int[]{1,2,4,5}
                        ),
                        new opponentSetTestCase(
                                4,7,5,1,
                                new int[]{4,5}
                        )
                };

        for(opponentSetTestCase test : tests) {
            // make the set of players.
            VerificationKey players[] = new VerificationKey[test.N];
            for (int i = 0; i < test.N; i ++) {
                players[i] = new MockVerificationKey(i + 1);
            }

            // Set up the network operations object.
            NetworkOperations netop = new NetworkOperations(new MockSessionIdentifier(), new MockSigningKey(test.player), players, null);

            Set<VerificationKey> result = null;
            try {
                result = netop.opponentSet(test.i, test.n);
            } catch (CryptographyError e) {
                Assert.fail("Unexpected CryptographyException.");
            } catch (InvalidImplementationError e) {
                Assert.fail("Unexpected InvalidImplementationException.");
            }

            for (int expect : test.expected) {
                Assert.assertTrue(result.remove(new MockVerificationKey(expect)));
            }

            Assert.assertTrue(result.isEmpty());
        }
    }

    static class broadcastTestCase {
        int recipients;
        int sender;

        public broadcastTestCase(int recipients, int sender) {
            this.recipients = recipients;
            this.sender = sender;
        }
    }

    @Test
    public void testBroadcast() {
        broadcastTestCase tests[] =
                new broadcastTestCase[]{
                        new broadcastTestCase(0, 2),
                        new broadcastTestCase(1, 2),
                        new broadcastTestCase(2, 2),
                        new broadcastTestCase(3, 2),
                        new broadcastTestCase(4, 2),
                };

        for (broadcastTestCase test : tests) {
            // The player sending and inbox.
            MockSigningKey sk = new MockSigningKey(0);

            // Create mock network object.
            MockNetwork network = new MockNetwork();

            // make the set of players.
            VerificationKey players[] = new VerificationKey[test.recipients];
            for (int i = 0; i < test.recipients; i ++) {
                players[i] = new MockVerificationKey(i);
            }


            MockMessageFactory messages = new MockMessageFactory();
            SessionIdentifier τ = new MockSessionIdentifier();

            // Set up the shuffle machine (only used to query for the current phase).
            ShuffleMachine machine = new ShuffleMachine(τ, messages, new MockCrypto(577), new MockCoin(), network);
            machine.phase = ShufflePhase.Shuffling;

            // Set up the network operations object.
            NetworkOperations netop = new NetworkOperations(τ, sk, players, network);

            try {
                netop.broadcast(new Packet(messages.make(), new MockSessionIdentifier(), ShufflePhase.Shuffling, sk.VerificationKey()));
            } catch (TimeoutError e) {
                Assert.fail("Unexpected exception.");
            } catch (CryptographyError e) {
                e.printStackTrace();
                Assert.fail("Unexpected CryptograhyException.");
            } catch (InvalidImplementationError e) {
                Assert.fail("Unexpected InvalidImplementationException.");
            }
        }
    }

    static class sendToTestCase {
        int sender;
        int recipient;
        int players;
        boolean success;

        public sendToTestCase(int sender, int recipient, int players, boolean success) {
            this.sender = sender;
            this.recipient = recipient;
            this.players = players;
            this.success = success;
        }
    }

    @Test
    public void testSendTo() {
        sendToTestCase tests[] = new sendToTestCase[]{
                // Case where recipient does not exist.
                new sendToTestCase(1, 2, 0, false),
                new sendToTestCase(1, 2, 1, false),
                new sendToTestCase(1, 2, 2, true),
                // Cannot send to myself.
                new sendToTestCase(1, 1, 2, false)
        };

        for (sendToTestCase test : tests) {
            try {
                // The player sending and inbox.
                MockSigningKey sk = new MockSigningKey(0);

                // Create mock network object.
                MockNetwork network = new MockNetwork();

                // make the set of players.
                VerificationKey players[] = new VerificationKey[test.players];
                for (int i = 0; i < test.players; i++) {
                    players[i] = new MockVerificationKey(i);
                }

                // Set up the shuffle machine (only used to query for the current phase).
                MockMessageFactory message = new MockMessageFactory();
                SessionIdentifier τ = new MockSessionIdentifier();
                ShuffleMachine machine = new ShuffleMachine(τ, message, new MockCrypto(8989), new MockCoin(), network);
                machine.phase = ShufflePhase.Shuffling;

                // Set up the network operations object.
                NetworkOperations netop = new NetworkOperations(τ, sk, players, network);

                netop.sendTo(new MockVerificationKey(test.recipient), new Packet(new MockMessage(), new MockSessionIdentifier(), ShufflePhase.Shuffling, sk.VerificationKey()));

                Queue<Map.Entry<Packet, MockVerificationKey>> responses = network.getResponses();
                Assert.assertEquals(1, responses.size());

                for (Map.Entry msg : responses) {
                    Assert.assertTrue(new MockVerificationKey(test.recipient).equals(msg.getValue()));
                }

            } catch (InvalidImplementationError e) {
                Assert.fail("Unexpected InvalidImplementationException");
            } catch (CryptographyError e) {
                Assert.fail("Unexpected CryptographyException");
            } catch (TimeoutError e) {
                Assert.fail("Unexpected TimeoutException");
            }
        }
    }

    static class receiveFromTestCase {
        int[] players;
        int requested; // The player that the message was expected from.
        ShufflePhase phase; // The expected phase.
        Packet packet;
        Exception e; // If an exception is expected.

        public receiveFromTestCase(int[] players, int requested, ShufflePhase phase,Packet packet, Exception e) {
            this.players = players;
            this.requested = requested;
            this.packet = packet;
            this.e = e;
            this.phase = phase;
        }
    }

    @Test
    public void testReceiveFrom() {
        receiveFromTestCase tests[] = new receiveFromTestCase[]{
                // time out exception test case.
                new receiveFromTestCase(new int []{1,2,3}, 2, ShufflePhase.Shuffling, null, new TimeoutError()),
                // Various malformed inbox.
                // TODO
        };

        for (receiveFromTestCase test : tests) {
            try {
                // The player sending and inbox.
                MockSigningKey sk = new MockSigningKey(0);

                // Create mock network object.
                MockNetwork network = new MockNetwork();

                // make the set of players.
                VerificationKey players[] = new VerificationKey[test.players.length];
                for (int i = 0; i < test.players.length; i++) {
                    players[i] = new MockVerificationKey(test.players[i]);
                }

                // Set up the shuffle machine (only used to query for the current phase).
                SessionIdentifier τ = new MockSessionIdentifier();

                // Set up the network operations object.
                NetworkOperations netop = new NetworkOperations(τ, sk, players, network);

                netop.receiveFrom(new MockVerificationKey(test.requested), test.phase);
            } catch (TimeoutError | CryptographyError | FormatException | BlameReceivedException
                    | ValueException | InterruptedException | InvalidImplementationError e) {
                if (test.e != null) {
                    if (!test.e.getClass().equals(e.getClass())) {
                        Assert.fail("Wrong exception encountered.");
                    }
                } else {
                    Assert.fail("Unexpected exception encountered.");
                }
            }
        }
    }

    static class receiveFromMultipleTestCase {
        int[] players;
    }

    @Test
    public void testReceiveFromMultiple() {
        // TODO
        receiveFromMultipleTestCase tests[] = new receiveFromMultipleTestCase[]{};

        for (receiveFromMultipleTestCase test : tests) {
            // The player sending and inbox.
            MockSigningKey sk = new MockSigningKey(0);

            // Create mock network object.
            MockNetwork network = new MockNetwork();

            // make the set of players.
            VerificationKey players[] = new VerificationKey[test.players.length];
            for (int i = 0; i < test.players.length; i++) {
                players[i] = new MockVerificationKey(test.players[i]);
            }

            // Set up the shuffle machine (only used to query for the current phase).
            SessionIdentifier τ = new MockSessionIdentifier();
            ShuffleMachine machine = new ShuffleMachine(τ, new MockMessageFactory(), new MockCrypto(475), new MockCoin(), network);
            machine.phase = ShufflePhase.Shuffling;

            // Set up the network operations object.
            NetworkOperations netop = new NetworkOperations(τ, sk, players, network);

        }
    }
}