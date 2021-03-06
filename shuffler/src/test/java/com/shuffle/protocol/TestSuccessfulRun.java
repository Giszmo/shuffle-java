/**
 *
 * Copyright © 2016 Mycelium.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 *
 */

package com.shuffle.protocol;

import org.junit.Test;

/**
 * Tests for a successful run of the protocol.
 *
 * Created by Daniel Krawisz on 3/17/16.
 */
public class TestSuccessfulRun extends TestShuffleMachine {

    // Create a test case representing a successful run.
    private void SuccessfulRun(int numPlayer) {
        String description = "case " + caseNo + "; successful run with " + numPlayer + " players.";
        check(new MockTestCase(description).successfulTestCase(numPlayer));
    }

    @Test
    // Tests for successful runs of the protocol.
    public void testSuccess() {

        // Tests for successful runs.
        int minPlayers = 2;
        int maxPlayers = 13;
        for (int numPlayer = minPlayers; numPlayer <= maxPlayers; numPlayer++) {
            log.info("Protocol successful run with " + numPlayer + " players.");
            SuccessfulRun(numPlayer);
        }
    }
}
