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
 * stripped down tx pool content. no need to parse what we do not need
 */
public final class TxPoolGas extends Response<TxPoolGas.TxPoolContentGasResult> {
    public static class TxPoolContentGasResult {

        private Map<String, Map<BigInteger, GasTx>> pending;


        public TxPoolContentGasResult() {
        }

        public TxPoolContentGasResult(
                Map<String, Map<BigInteger, GasTx>> pending,
                Map<String, Map<BigInteger, GasTx>> queued) {
            this.pending = immutableCopy(pending, val -> immutableCopy(val, Function.identity()));
        }

        public Map<String, Map<BigInteger, GasTx>> getPending() {
            return pending;
        }

        public List<GasTx> getPendingTransactions() {
            return pending.values().stream().map(Map::values).flatMap(Collection::stream).toList();
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
