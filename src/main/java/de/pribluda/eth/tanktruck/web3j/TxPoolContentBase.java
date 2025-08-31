/*
 * Copyright 2020 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package de.pribluda.eth.tanktruck.web3j;

import org.web3j.protocol.core.Response;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

/**
 * txpool_content including base fee
 */
public final class TxPoolContentBase extends Response<TxPoolContentBase.TxPoolContentBaseResult> {
    public static class TxPoolContentBaseResult {

        private Map<String, Map<BigInteger, GasTx>> pending;
        private Map<String, Map<BigInteger, GasTx>> queued;
        private Map<String, Map<BigInteger, GasTx>> baseFee;

        public TxPoolContentBaseResult() {
        }

        public TxPoolContentBaseResult(
                Map<String, Map<BigInteger, GasTx>> pending,
                Map<String, Map<BigInteger, GasTx>> queued,
                Map<String, Map<BigInteger, GasTx>> baseFee) {
            this.pending = immutableCopy(pending, val -> immutableCopy(val, Function.identity()));
            this.queued = immutableCopy(queued, val -> immutableCopy(val, Function.identity()));
            this.baseFee = immutableCopy(baseFee, val -> immutableCopy(val, Function.identity()));
        }

        public Map<String, Map<BigInteger, GasTx>> getPending() {
            return pending;
        }

        public Map<String, Map<BigInteger, GasTx>> getQueued() {
            return queued;
        }

        public Map<String, Map<BigInteger, GasTx>> getBaseFee() {
            return baseFee;
        }

        public List<GasTx> getPendingGasTransactions() {
            return pending.values().stream().map(Map::values).flatMap(Collection::stream).toList();
        }

        public List<GasTx> getQueuedGasTransactions() {
            return queued.values().stream().map(Map::values).flatMap(Collection::stream).toList();
        }

        public List<GasTx> getBaseFeeGasTransactions() {
            return baseFee.values().stream().map(Map::values).flatMap(Collection::stream).toList();
        }

        private static <K, V> Map<K, V> immutableCopy(Map<K, V> map, Function<V, V> valueMapper) {
            Map<K, V> result = new HashMap<>();
            for (Map.Entry<K, V> entry : map.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();
                result.put(key, valueMapper.apply(value));
            }
            return Collections.unmodifiableMap(result);
        }
    }
}
