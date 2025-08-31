/*
 * Copyright 2019 Web3 Labs Ltd.
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

import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

/**
 * Transaction object used by both {@link EthTransaction} and {@link EthBlock}.
 */
public class GasTx {
    private String hash;
    private String nonce;
    private String blockNumber;
    private String from;
    private String to;
    private String value;
    private String gasPrice;
    private String gas;
    private String type;
    private String maxFeePerGas;
    private String maxPriorityFeePerGas;
    private String maxFeePerBlobGas;

    public GasTx() {
    }

    /**
     * Use constructor with ChainId
     */
    @Deprecated
    public GasTx(
            String hash,
            String nonce,
            String blockHash,
            String blockNumber,
            String transactionIndex,
            String from,
            String to,
            String value,
            String gas,
            String gasPrice,
            String input,
            String creates,
            String publicKey,
            String raw,
            String r,
            String s,
            long v,
            String type,
            String maxFeePerGas,
            String maxPriorityFeePerGas,
            List accessList) {
        this.hash = hash;
        this.nonce = nonce;
        this.blockNumber = blockNumber;
        this.from = from;
        this.to = to;
        this.value = value;
        this.gasPrice = gasPrice;
        this.gas = gas;

        this.type = type;
        this.maxFeePerGas = maxFeePerGas;
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
    }

    public GasTx(
            String hash,
            String nonce,
            String blockNumber,
            String from,
            String to,
            String value,
            String gas,
            String gasPrice,
            String type,
            String maxFeePerGas,
            String maxPriorityFeePerGas) {
        this.hash = hash;
        this.nonce = nonce;
        this.blockNumber = blockNumber;

        this.from = from;
        this.to = to;
        this.value = value;
        this.gasPrice = gasPrice;
        this.gas = gas;
        this.type = type;
        this.maxFeePerGas = maxFeePerGas;
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
    }

    public GasTx(
            String hash,
            String nonce,
            String blockNumber,
            String from,
            String to,
            String value,
            String gas,
            String gasPrice,

            String type,
            String maxFeePerGas,
            String maxPriorityFeePerGas,
            String maxFeePerBlobGas) {
        this.hash = hash;
        this.nonce = nonce;
        this.blockNumber = blockNumber;

        this.from = from;
        this.to = to;
        this.value = value;
        this.gasPrice = gasPrice;
        this.gas = gas;

        this.type = type;
        this.maxFeePerGas = maxFeePerGas;
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
        this.maxFeePerBlobGas = maxFeePerBlobGas;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public BigInteger getNonce() {
        return Numeric.decodeQuantity(nonce);
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getNonceRaw() {
        return nonce;
    }


    public BigInteger getBlockNumber() {
        return Numeric.decodeQuantity(blockNumber);
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getBlockNumberRaw() {
        return blockNumber;
    }


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigInteger getValue() {
        return Numeric.decodeQuantity(value);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueRaw() {
        return value;
    }

    public BigInteger getGasPrice() {
        return Numeric.decodeQuantity(gasPrice);
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }

    public String getGasPriceRaw() {
        return gasPrice;
    }

    public BigInteger getGas() {
        return Numeric.decodeQuantity(gas);
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getGasRaw() {
        return gas;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigInteger getMaxFeePerGas() {
        if (maxFeePerGas == null) return null;
        return Numeric.decodeQuantity(maxFeePerGas);
    }

    public String getMaxFeePerGasRaw() {
        return maxFeePerGas;
    }

    public void setMaxFeePerGas(String maxFeePerGas) {
        this.maxFeePerGas = maxFeePerGas;
    }

    public String getMaxPriorityFeePerGasRaw() {
        return maxPriorityFeePerGas;
    }

    public BigInteger getMaxPriorityFeePerGas() {
        return Numeric.decodeQuantity(maxPriorityFeePerGas);
    }

    public void setMaxPriorityFeePerGas(String maxPriorityFeePerGas) {
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
    }

    public String getMaxFeePerBlobGasRaw() {
        return maxFeePerBlobGas;
    }

    public BigInteger getMaxFeePerBlobGas() {
        return Numeric.decodeQuantity(maxFeePerBlobGas);
    }

    public void setMaxFeePerBlobGas(String maxFeePerBlobGas) {
        this.maxFeePerBlobGas = maxFeePerBlobGas;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GasTx)) {
            return false;
        }

        GasTx that = (GasTx) o;

        if (getHash() != null ? !getHash().equals(that.getHash()) : that.getHash() != null) {
            return false;
        }
        if (getNonceRaw() != null
                ? !getNonceRaw().equals(that.getNonceRaw())
                : that.getNonceRaw() != null) {
            return false;
        }

        if (getBlockNumberRaw() != null
                ? !getBlockNumberRaw().equals(that.getBlockNumberRaw())
                : that.getBlockNumberRaw() != null) {
            return false;
        }

        if (getFrom() != null ? !getFrom().equals(that.getFrom()) : that.getFrom() != null) {
            return false;
        }
        if (getTo() != null ? !getTo().equals(that.getTo()) : that.getTo() != null) {
            return false;
        }
        if (getValueRaw() != null
                ? !getValueRaw().equals(that.getValueRaw())
                : that.getValueRaw() != null) {
            return false;
        }
        if (getGasPriceRaw() != null
                ? !getGasPriceRaw().equals(that.getGasPriceRaw())
                : that.getGasPriceRaw() != null) {
            return false;
        }
        if (getGasRaw() != null
                ? !getGasRaw().equals(that.getGasRaw())
                : that.getGasRaw() != null) {
            return false;
        }

        if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) {
            return false;
        }
        if (getMaxFeePerGasRaw() != null
                ? !getMaxFeePerGasRaw().equals(that.getMaxFeePerGasRaw())
                : that.getMaxFeePerGasRaw() != null) {
            return false;
        }
        if (getMaxPriorityFeePerGasRaw() != null
                ? !getMaxPriorityFeePerGasRaw().equals(that.getMaxPriorityFeePerGasRaw())
                : that.getMaxPriorityFeePerGasRaw() != null) {
            return false;
        }

        if (getMaxFeePerBlobGasRaw() != null
                ? !getMaxFeePerBlobGasRaw().equals(that.getMaxFeePerBlobGasRaw())
                : that.getMaxFeePerBlobGasRaw() != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getHash() != null ? getHash().hashCode() : 0;
        result = 31 * result + (getNonceRaw() != null ? getNonceRaw().hashCode() : 0);
        result = 31 * result + (getBlockNumberRaw() != null ? getBlockNumberRaw().hashCode() : 0);

        result = 31 * result + (getFrom() != null ? getFrom().hashCode() : 0);
        result = 31 * result + (getTo() != null ? getTo().hashCode() : 0);
        result = 31 * result + (getValueRaw() != null ? getValueRaw().hashCode() : 0);
        result = 31 * result + (getGasPriceRaw() != null ? getGasPriceRaw().hashCode() : 0);
        result = 31 * result + (getGasRaw() != null ? getGasRaw().hashCode() : 0);

        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result = 31 * result + (getMaxFeePerGasRaw() != null ? getMaxFeePerGasRaw().hashCode() : 0);
        result =
                31 * result
                        + (getMaxPriorityFeePerGasRaw() != null
                        ? getMaxPriorityFeePerGasRaw().hashCode()
                        : 0);
        result =
                31 * result
                        + (getMaxFeePerBlobGasRaw() != null
                        ? getMaxFeePerBlobGasRaw().hashCode()
                        : 0);
        return result;
    }
}
