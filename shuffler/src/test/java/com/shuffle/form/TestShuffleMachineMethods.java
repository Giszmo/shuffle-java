package com.shuffle.form;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests the methods in the shuffle machine other than the main ones.
 *
 * Created by Daniel Krawisz on 12/5/15.
 */
public class TestShuffleMachineMethods {
    private class shuffleTestCase {
        int[] randomSequence;
        int[] input;
        int[] expected;

        shuffleTestCase(int[] randomSequence, int[] input, int[] expected) {
            this.randomSequence = randomSequence;
            this.input = input;
            this.expected = expected;
        }
    }

    private ShuffleMachine shuffleTestInitialization(int rand[]) {
        return new ShuffleMachine(
                new MockPacketFactory(),
                new MockCrypto(
                        new MockRandomSequence(rand)),
                new MockCoin(),
                new MockNetwork());
    }

    @Test
    // This tests some cases for the function that randomly shuffles a message.
    public void shuffleTest() {
        shuffleTestCase tests[] = new shuffleTestCase[]{
                // The empty case, of course!
                new shuffleTestCase(
                        new int[]{},
                        new int[]{},
                        new int[]{}
                ),
                // One element.
                new shuffleTestCase(
                        new int[]{0},
                        new int[]{1},
                        new int[]{1}
                ),
                // All possibilities for two elements.
                new shuffleTestCase(
                        new int[]{1, 0},
                        new int[]{1, 2},
                        new int[]{2, 1}
                ),
                new shuffleTestCase(
                        new int[]{0, 0},
                        new int[]{1, 2},
                        new int[]{1, 2}
                ),
                // Most possibilities for three elements.
                new shuffleTestCase(
                        new int[]{2, 0, 0},
                        new int[]{1, 2, 3},
                        new int[]{3, 1, 2}
                ),
                new shuffleTestCase(
                        new int[]{2, 1, 0},
                        new int[]{1, 2, 3},
                        new int[]{3, 2, 1}
                ),
                new shuffleTestCase(
                        new int[]{1, 1, 0},
                        new int[]{1, 2, 3},
                        new int[]{2, 1, 3}
                ),
                new shuffleTestCase(
                        new int[]{0, 1, 0},
                        new int[]{1, 2, 3},
                        new int[]{1, 3, 2}
                ),
                // Some randomly generated larger cases.
                new shuffleTestCase(
                        new int[]{1, 0, 1, 0},
                        new int[]{1, 2, 3, 4},
                        new int[]{2, 3, 1, 4}
                ),
                new shuffleTestCase(
                        new int[]{2, 3, 1, 0, 0},
                        new int[]{1, 2, 3, 4, 5},
                        new int[]{3, 2, 5, 1, 4}
                ),
                new shuffleTestCase(
                        new int[]{3, 2, 0, 0, 1, 0},
                        new int[]{1, 2, 3, 4, 5, 6},
                        new int[]{4, 1, 2, 3, 6, 5}
                ),
        };

        for(shuffleTestCase test : tests) {
            ShuffleMachine machine = shuffleTestInitialization(test.randomSequence);

            MockPacket input = new MockPacket(test.input);
            MockPacket expected = new MockPacket(test.expected);
            try {
                Packet result = machine.shuffle(input);
                System.out.println("got " + result.toString() + "; expected " + expected.toString());
                Assert.assertTrue(result.equal(expected));
            } catch (CryptographyException e) {
                Assert.fail("Unexpected CryptographyException");
            } catch (InvalidImplementationException e) {
                Assert.fail("Unexpected InvalidImplementationException");
            } catch (FormatException e) {
                Assert.fail("Unexpected FormatException");
            }
        }
    }

    private class areEqualTestCase {
        int[] input;
        boolean expected;

        areEqualTestCase(int[] input, boolean expected) {
            this.input = input;
            this.expected = expected;
        }
    }

    Map<VerificationKey, Packet> mockPacketMap(int[] input) {
        Map<VerificationKey, Packet> map = new HashMap<>();

        int key = 0;
        for(int i : input) {
            map.put(new MockVerificationKey(key), new MockPacket(i));
            key++;
        }

        return map;
    }

    @Test
    public void testAreEqual() {
        areEqualTestCase tests[] = new areEqualTestCase[]{
                // The empty case, of course!
                new areEqualTestCase(
                        new int[]{},
                        true
                ),
                new areEqualTestCase(
                        new int[]{1},
                        true
                ),
                new areEqualTestCase(
                        new int[]{1, 1},
                        true
                ),
                new areEqualTestCase(
                        new int[]{1, 2},
                        false
                ),
                new areEqualTestCase(
                        new int[]{2, 2, 2},
                        true
                ),
                new areEqualTestCase(
                        new int[]{2, 3, 2},
                        false
                ),
                new areEqualTestCase(
                        new int[]{3, 2, 2},
                        false
                ),
                new areEqualTestCase(
                        new int[]{2, 2, 3},
                        false
                )
        };

        for(areEqualTestCase testCase : tests) {
            try {
                Assert.assertEquals(testCase.expected, ShuffleMachine.areEqual(mockPacketMap(testCase.input)));
            } catch (InvalidImplementationException e) {
                Assert.fail("Tests have failed due to error in test class.");
            }
        }
    }

    @Test
    public void testDecryptAll() {
        // TODO
    }

    @Test
    public void testReadNewAddresses() {
        // TODO
    }
}