/**
 *
 * Copyright © 2016 Mycelium.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 *
 */

package com.shuffle.player;

import com.shuffle.bitcoin.Address;
import com.shuffle.bitcoin.Coin;
import com.shuffle.bitcoin.Crypto;
import com.shuffle.bitcoin.SigningKey;
import com.shuffle.bitcoin.Transaction;
import com.shuffle.bitcoin.VerificationKey;
import com.shuffle.chan.Chan;
import com.shuffle.chan.packet.SessionIdentifier;
import com.shuffle.p2p.Bytestring;
import com.shuffle.p2p.Channel;
import com.shuffle.protocol.CoinShuffle;
import com.shuffle.protocol.Mailbox;
import com.shuffle.protocol.message.MessageFactory;
import com.shuffle.protocol.message.Phase;
import com.shuffle.protocol.blame.Matrix;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 *
 * Created by Daniel Krawisz on 2/1/16.
 */
class Player<Identity> {
    private static final Logger log = LogManager.getLogger(Player.class);

    private final SigningKey sk;

    private final Coin coin;

    private final MessageFactory messages;

    public class Settings {
        final SessionIdentifier session;
        final long amount;
        final Address addrNew;
        final Address change;
        final int minPlayers;
        final int maxRetries;
        final int timeout;

        public Settings(
                SessionIdentifier session,
                long amount,
                Address addrNew,
                Address change,
                int minPlayers,
                int maxRetries,
                int timeout
        ) {
            this.session = session;
            this.amount = amount;
            this.addrNew = addrNew;
            this.change = change;
            this.minPlayers = minPlayers;
            this.maxRetries = maxRetries;
            this.timeout = timeout;
        }
    }

    Player(
            SigningKey sk,
            MessageFactory messages, // Object that knows how to create and copy messages.
            Coin coin // Connects us to the Bitcoin or other cryptocurrency netork.
    ) {
        if (sk == null || coin == null || messages == null) {
            throw new NullPointerException();
        }
        this.sk = sk;
        this.coin = coin;
        this.messages = messages;
    }

    static void main(String[] args) {

    }

    public Transaction coinShuffle(
            Set<Identity> identities,
            Channel<Identity, Bytestring> channel,
            Map<Identity, VerificationKey> keys, // Can be null.
            Settings settings,
            Crypto crypto,
            Chan<Phase> chan
    ) {

        // Start by making connections to all the identies.
        for (Identity identity : identities) {
            channel.getPeer(identity);
        }

        SortedSet<VerificationKey> players = new TreeSet<>();
        players.add(sk.VerificationKey());
        // if the keys have not been given, send out those.
        if (keys == null) {
            // TODO
            keys = new HashMap<>();
        }

        for (VerificationKey key : keys.values()) {
            players.add(key);
        }

        // Try the protocol.
        int attempt = 0;

        // The eliminated players. A player is eliminated when there is a subset of players
        // which all blame him and none of whom blame one another.
        SortedSet<VerificationKey> eliminated = new TreeSet<>();

        CoinShuffle shuffle = new CoinShuffle(messages, crypto, coin);

        while (true) {

            if (players.size() - eliminated.size() < settings.minPlayers) {
                return null;
            }

            // Get the initial ordering of the players.
            int i = 1;
            SortedSet<VerificationKey> validPlayers = new TreeSet<>();
            for (VerificationKey player : players) {
                if (!eliminated.contains(player)) {
                    validPlayers.add(player);
                    i++;
                }
            }

            // Make an inbox for the next round.
            Mailbox mailbox = new Mailbox(sk.VerificationKey(), validPlayers, messages);

            // Send an introductory message and make sure all players agree on who is in
            // this round of the protocol.
            // TODO

            Matrix blame = null;
            try {
                return shuffle.runProtocol(
                        settings.amount, sk, validPlayers, settings.addrNew, settings.change, chan);
            } catch (Matrix m) {
                blame = m;
            } catch (Exception e) {
                e.printStackTrace();
            }

            attempt++;

            if (attempt > settings.maxRetries) {
                return null;
            }

            // TODO
            // Now we try to start a new round.
            // If this player has not been eliminated, make a message to broadcast.
            // receive messages from other players.
            // Resend everything to show that we all agree.
            // Receive rebroadcasts and check.
            // Begin protocol.
        }
    }
}
